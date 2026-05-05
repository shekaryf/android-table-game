package ir.baran.bookPack;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.ComponentActivity;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import ir.baran.baranBook.R;
import ir.baran.book.db.SettingsManager;
import ir.baran.framework.forms.Form;
import ir.baran.framework.utilities.ConfigurationUtils;
import ir.baran.framework.utilities.MyConfig;
import ir.cafebazaar.poolakey.Connection;
import ir.cafebazaar.poolakey.Payment;
import ir.cafebazaar.poolakey.config.PaymentConfiguration;
import ir.cafebazaar.poolakey.config.SecurityCheck;
import ir.cafebazaar.poolakey.request.PurchaseRequest;

import java.lang.reflect.Method;
import java.util.List;

import kotlin.Unit;

public class BazaarPay {
	private static BazaarPay instance;
	private static final int _ACTIVEVALUE = 1;
	private static final String STR_ACTIVED = "ACTIVED";
	private static final String STR_EXTRA_OPENED = "EXTRA_OPENED";
	private static final String PAYLOAD = "baranpack-premium";

	private static final String[] _ARR_MESSAGES = {"",
			"", "کاربر عملیات را متوقف کرده است", "API برای درخواست ارسال شده پشتیبانی نمی شود",
			"این محصول برای فروش موجود نیست", "پارامترهای ارسالی به API معتبر نیستند",
			"خطا در هنگام عملیات پرداخت", "این محصول قبلا برای این حساب خریداری شده است",
			"این خرید متعلق به کاربر فعلی نیست"};

	private static String SKU_PREMIUM;

	private ComponentActivity _activity;
	private Dialog _OpenedDialog;
	private Payment payment;
	private Connection paymentConnection;
	private boolean mIsPremium = false;
	private boolean isBillingReady = false;
	private boolean isConnecting = false;

	public BazaarPay(ComponentActivity activity) {
		attachActivity(activity);
	}

	public static BazaarPay getInstance(ComponentActivity activity) {
		if (instance == null) {
			instance = new BazaarPay(activity);
		} else {
			instance.attachActivity(activity);
		}
		return instance;
	}

	public void init() {
		if (_activity == null) {
			return;
		}
		SKU_PREMIUM = _activity.getResources().getString(R.string.SKU_PREMIUM);
		mIsPremium = isStoredActive();
		createPay();
	}

	private void attachActivity(ComponentActivity activity) {
		if (_activity == activity) {
			return;
		}
		_activity = activity;
		resetBillingState();
	}

	private void resetBillingState() {
		isBillingReady = false;
		isConnecting = false;
		try {
			if (paymentConnection != null) {
				paymentConnection.disconnect();
			}
		} catch (Exception ignored) {
		}
		paymentConnection = null;
		payment = null;
	}

	private void createPay() {
		if (_activity == null || isBillingReady || isConnecting) {
			return;
		}

		String base64EncodedPublicKey = _activity.getResources().getString(R.string.base64Encode);
		SecurityCheck securityCheck = new SecurityCheck.Enable(base64EncodedPublicKey);
		PaymentConfiguration paymentConfiguration = new PaymentConfiguration(securityCheck, true);
		payment = new Payment(_activity, paymentConfiguration);
		isConnecting = true;
		paymentConnection = payment.connect(connectionCallback -> {
			connectionCallback.connectionSucceed(() -> {
				isConnecting = false;
				isBillingReady = true;
				queryPurchasedProducts();
				return Unit.INSTANCE;
			});
			connectionCallback.connectionFailed(throwable -> {
				isConnecting = false;
				isBillingReady = false;
				return Unit.INSTANCE;
			});
			connectionCallback.disconnected(() -> {
				isBillingReady = false;
				isConnecting = false;
				return Unit.INSTANCE;
			});
			return Unit.INSTANCE;
		});
	}

	private void queryPurchasedProducts() {
		if (!isBillingReady || payment == null) {
			return;
		}
		payment.getPurchasedProducts(queryCallback -> {
			queryCallback.querySucceed(purchasedProducts -> {
				if (hasPremiumPurchase(purchasedProducts)) {
					activatePremium(false);
				} else {
					mIsPremium = isStoredActive();
				}
				return Unit.INSTANCE;
			});
			queryCallback.queryFailed(throwable -> {
				mIsPremium = isStoredActive();
				return Unit.INSTANCE;
			});
			return Unit.INSTANCE;
		});
	}

	private boolean hasPremiumPurchase(List<?> purchasedProducts) {
		if (purchasedProducts == null) {
			return false;
		}
		for (Object purchasedProduct : purchasedProducts) {
			String productId = invokeStringMethod(purchasedProduct, "getProductId");
			if (SKU_PREMIUM.equals(productId)) {
				return true;
			}
		}
		return false;
	}

	private String invokeStringMethod(Object target, String methodName) {
		if (target == null) {
			return null;
		}
		try {
			Method method = target.getClass().getMethod(methodName);
			Object value = method.invoke(target);
			return value == null ? null : value.toString();
		} catch (Exception ignored) {
			return null;
		}
	}

	private boolean isStoredActive() {
		return SettingsManager.getInstance().getIntegetValue(STR_ACTIVED) == _ACTIVEVALUE;
	}

	private void activatePremium(boolean showSuccessMessage) {
		mIsPremium = true;
		SettingsManager.getInstance().saveInteger(STR_ACTIVED, _ACTIVEVALUE);
		if (_activity != null && showSuccessMessage) {
			ConfigurationUtils.showMessage("با تشکر از خرید شما - پرداخت موفق", _activity);
		}
		try {
			if (_OpenedDialog != null && _OpenedDialog.isShowing()) {
				_OpenedDialog.dismiss();
			}
		} catch (Exception ignored) {
		}
	}

	private void launchPurchaseFlow() {
		if (_activity == null) {
			return;
		}
		if (!isBillingReady || payment == null) {
			createPay();
			ConfigurationUtils.showMessage("در حال اتصال به بازار هستیم. لطفا چند لحظه بعد دوباره تلاش کنید", _activity);
			return;
		}

		PurchaseRequest purchaseRequest = new PurchaseRequest(SKU_PREMIUM, PAYLOAD, null);
		payment.purchaseProduct(_activity.getActivityResultRegistry(), purchaseRequest, purchaseCallback -> {
			purchaseCallback.purchaseFlowBegan(() -> Unit.INSTANCE);
			purchaseCallback.failedToBeginFlow(throwable -> {
				ConfigurationUtils.showMessage("شروع فرآیند پرداخت ممکن نشد", _activity);
				return Unit.INSTANCE;
			});
			purchaseCallback.purchaseSucceed(purchaseEntity -> {
				activatePremium(true);
				return Unit.INSTANCE;
			});
			purchaseCallback.purchaseCanceled(() -> {
				ConfigurationUtils.showMessage(_ARR_MESSAGES[2], _activity);
				return Unit.INSTANCE;
			});
			purchaseCallback.purchaseFailed(throwable -> {
				ConfigurationUtils.showMessage("پرداخت ناموفق", _activity);
				return Unit.INSTANCE;
			});
			return Unit.INSTANCE;
		});
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	}

	public void aboutClick(final ComponentActivity activity) {
		attachActivity(activity);
		final int activeValue = SettingsManager.getInstance().getIntegetValue(STR_ACTIVED);
		if (activeValue != _ACTIVEVALUE) {
			showPurchaseBottomSheet(activity);
		} else {
			ConfigurationUtils.openUrl(activity);
		}
	}

	private void showPurchaseBottomSheet(final ComponentActivity activity) {
		final BottomSheetDialog dialog = new BottomSheetDialog(activity);
		dialog.setContentView(R.layout.bottom_sheet_purchase);
		dialog.setCanceledOnTouchOutside(true);

//		View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
//		if (bottomSheet != null) {
//			bottomSheet.setBackgroundResource(android.R.color.transparent);
//			ViewGroup.LayoutParams params = bottomSheet.getLayoutParams();
//			if (params != null) {
//				params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
//				bottomSheet.setLayoutParams(params);
//			}
//			BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
//			behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
//			behavior.setSkipCollapsed(true);
//		}

		TextView title = (TextView) dialog.findViewById(R.id.purchaseTitle);
		TextView subtitle = (TextView) dialog.findViewById(R.id.purchaseSubtitle);
		TextView description = (TextView) dialog.findViewById(R.id.purchaseDescription);
		TextView benefitOne = (TextView) dialog.findViewById(R.id.purchaseBenefitOne);
		TextView benefitTwo = (TextView) dialog.findViewById(R.id.purchaseBenefitTwo);
		TextView benefitThree = (TextView) dialog.findViewById(R.id.purchaseBenefitThree);
		TextView action = (TextView) dialog.findViewById(R.id.purchaseAction);
		TextView later = (TextView) dialog.findViewById(R.id.purchaseLater);
		ImageView close = (ImageView) dialog.findViewById(R.id.purchaseClose);

		applyTypeface(title, subtitle, description, benefitOne, benefitTwo, benefitThree, action, later);

		if (close != null) {
			close.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
		}

		if (later != null) {
			later.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
		}

		if (action != null) {
			action.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					launchPurchaseFlow();
				}
			});
		}

		dialog.show();
		_OpenedDialog = dialog;
	}

	private void applyTypeface(TextView... views) {
		if (views == null) {
			return;
		}
		for (TextView view : views) {
			if (view != null) {
				view.setTypeface(MyConfig.getDefaultTypeface());
			}
		}
	}

	public void onDistroy() {
		resetBillingState();
	}

	public boolean isActive(int position) {
		if (isActive())
			return true;
		if (extraOpened())
			return false;
		return position <= 2;
	}

	private boolean extraOpened() {
		int countExtra = SettingsManager.getInstance().getIntegetValue(STR_EXTRA_OPENED);
		if (countExtra < 0)
			countExtra = 0;
		if (countExtra > 5)
			return true;
		countExtra++;
		SettingsManager.getInstance().saveInteger(STR_EXTRA_OPENED, countExtra);
		return false;
	}

	public boolean isActive() {
		return mIsPremium || isStoredActive();
	}
}
