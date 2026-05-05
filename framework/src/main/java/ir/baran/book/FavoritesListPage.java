package ir.baran.book;

import ir.baran.book.base.ListFormBase;
import ir.baran.book.db.ContentManager;
import ir.baran.book.db.FavoritesItem;
import ir.baran.book.db.FavoritesManager;
import ir.baran.framework.R;
import ir.baran.framework.components.ListFormAdapter;
import ir.baran.framework.components.ListFormItem;
import ir.baran.framework.utilities.ConfigurationUtils;
import ir.baran.framework.utilities.Functions;
import ir.baran.framework.utilities.MyConfig;

import java.util.Vector;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

public class FavoritesListPage extends ListFormBase {
    private static final int SWIPE_ACTION_WIDTH_DP = 92;

    @Override
    protected void initHeader(LinearLayout llHeader) {
        super.initHeader(llHeader);
        int lp = Functions.dp2px(15);
        llHeader.setPadding(lp, lp, lp, lp);
        lblHead.setText("علاقه مندي ها");
        this.lblHead.setTextColor(getColor(R.color.headerFavorite));
        llHeader.setBackgroundResource(R.drawable.header_content);
        ConfigurationUtils.setDefaultTextSize(lblHead);
    }

    @Override
    protected void onItemSelected(ListFormItem itm, int position) {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected ListFormAdapter initAdapter() {
        Vector<FavoritesItem> items = FavoritesManager.getInstance().getItems();
        if (items.isEmpty()) {
            showMessage("موردي ثبت نشده است");
            finish();
        }

        return new ListFormAdapter(items, FavoritesListPage.this) {

            @Override
            protected View getView(ListFormItem item, final int position) {
                final FavoritesItem itm = (FavoritesItem) item;
                final View lv = getLayoutInflater().inflate(R.layout.list_item, null);
                final View card = lv.findViewById(R.id.favoriteCard);
                final View deleteAction = lv.findViewById(R.id.favoriteDelete);
                TextView titleView = (TextView) lv.findViewById(R.id.favoriteTitle);
                TextView dateView = (TextView) lv.findViewById(R.id.favoriteDate);

                titleView.setTypeface(MyConfig.getDefaultTypeface());
                dateView.setTypeface(MyConfig.getDefaultTypeface());
                titleView.setText(itm._Text.trim());
                dateView.setText(itm.date.trim());
                ConfigurationUtils.setDefaultTextSize(titleView);
                dateView.setTextSize(13);

                card.setTag(itm);
                card.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onItemSelected(itm, position);
                    }
                });
                card.setOnLongClickListener(new OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        confirmDelete(itm);
                        return true;
                    }
                });
                deleteAction.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        confirmDelete(itm);
                    }
                });
                bindSwipe(card, deleteAction);
                return lv;
            }
        };
    }

    private void bindSwipe(final View card, final View deleteAction) {
        final int touchSlop = ViewConfiguration.get(this).getScaledTouchSlop();
        final float actionWidth = Functions.dp2px(SWIPE_ACTION_WIDTH_DP);
        card.setTranslationX(0f);
        deleteAction.setAlpha(0.45f);
        card.setOnTouchListener(new OnTouchListener() {
            private float downX;
            private float startX;
            private boolean swiping;
            private boolean longPressed;
            private Runnable longPressRunnable;

            @Override
            public boolean onTouch(final View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        downX = event.getRawX();
                        startX = v.getTranslationX();
                        swiping = false;
                        longPressed = false;
                        longPressRunnable = new Runnable() {
                            @Override
                            public void run() {
                                longPressed = true;
                                v.performLongClick();
                            }
                        };
                        v.postDelayed(longPressRunnable, ViewConfiguration.getLongPressTimeout());
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        float delta = event.getRawX() - downX;
                        if (!swiping && Math.abs(delta) > touchSlop) {
                            swiping = true;
                            v.removeCallbacks(longPressRunnable);
                        }
                        if (!swiping)
                            return true;

                        float target = Math.max(-actionWidth,
                                Math.min(actionWidth, startX + delta));
                        v.setTranslationX(target);
                        deleteAction.setAlpha(Math.min(1f, 0.35f + (Math.abs(target) / actionWidth)));
                        return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        v.removeCallbacks(longPressRunnable);
                        if (longPressed)
                            return true;
                        if (!swiping) {
                            v.performClick();
                            return true;
                        }

                        float currentX = v.getTranslationX();
                        if (Math.abs(currentX) > actionWidth / 2f) {
                            float openX = currentX > 0 ? actionWidth : -actionWidth;
                            v.animate().translationX(openX).setDuration(180).start();
                            deleteAction.animate().alpha(1f).setDuration(180).start();
                        } else {
                            v.animate().translationX(0f).setDuration(180).start();
                            deleteAction.animate().alpha(0.45f).setDuration(180).start();
                        }
                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    protected void initFooter(final LinearLayout llFooter) {
        TextView msg = new TextView(this);
        llFooter.setBackgroundResource(R.drawable.footer);

        msg.setText(R.string.favorite_footer);
        ConfigurationUtils.setDefaultTextSizeTF(msg, this);
        msg.setTextColor(getColor(R.color.headerFavorite));
        int pd = Functions.dp2px(10);
        msg.setPadding(pd * 2, pd, pd * 2, pd);
        llFooter.addView(msg);
    }

    private void confirmDelete(final FavoritesItem item) {
        FavoritesManager.getInstance().deleteRow(item._Id);
        ListAdapter adapter = initAdapter();
        listView.setAdapter(adapter);
        listView.postInvalidate();

//		confirm("براي حذف اطمينان داريد ؟", "حذف", new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//			}
//		});
    }
}
