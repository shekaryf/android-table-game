package ir.baran.bookPack;

import android.content.res.AssetFileDescriptor;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import androidx.appcompat.app.AlertDialog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ir.baran.bookPack.game.data.repository.GameRepository;
import ir.baran.bookPack.game.domain.model.CellState;
import ir.baran.bookPack.game.domain.model.GameBoard;
import ir.baran.bookPack.game.domain.model.GameCell;
import ir.baran.bookPack.game.presentation.GameViewModel;
import ir.baran.framework.forms.Form;
import ir.baran.framework.utilities.ConfigurationUtils;
import ir.baran.framework.utilities.Functions;
import ir.baran.framework.utilities.MyConfig;

/**
 * صفحه اصلی بازی: نمایش جدول، مدیریت کلیک‌ها، نمایش راهنماها و زوم/اسکرول.
 */
public class GamePage extends Form {

    public static final String EXTRA_LEVEL_ID = "level_id";
    private static final String SCORE_PREFS_NAME = "game_prefs";
    private static final String KEY_SCORE_ZERO_AT = "score_zero_at_ms";
    private static final long FREE_SCORE_INTERVAL_MS = 60L * 60L * 1000L;
    private static final int FREE_SCORE_AMOUNT = 5;

    private int colorBgPage;
    private int colorCellMovable;
    private int colorCellSelected;
    private int colorCellLocked;
    private int colorCellBlocked;
    private int colorText;
    private int colorSubtext;

    private int colorBtnEnabledBg;
    private int colorBtnDisabledBg;
    private int colorBtnEnabledText;
    private int colorBtnDisabledText;
    private int colorBtnEnabledStroke;
    private int colorBtnDisabledStroke;
    private int colorBtnRipple;

    private GameViewModel viewModel;
    private GameRepository repository;

    private GridLayout gridLayout;
    private TextView tvTitle;
    private TextView tvHint;
    private TextView tvStageInfo;
    private TextView tvScore;
    private LinearLayout boardWrapper;
    private HorizontalScrollView horizontalScrollView;
    private ScrollView verticalScrollView;

    private MaterialButton btnPrev;
    private MaterialButton btnNext;

    private float zoomFactor = 1f;
    private ScaleGestureDetector scaleDetector;
    private final Map<String, Drawable> arrowCache = new HashMap<>();

    private GameBoard currentBoard;
    private int currentLevelId = -1;
    private int levelsCount = 0;
    private int winHandledLevelId = -1;
    private boolean allowNextFromCurrentWin = false;
    private boolean isScoreLocked = false;
    private boolean isScorePurchaseDialogShowing = false;
    private boolean isScorePurchaseInProgress = false;
    private AlertDialog scorePurchaseDialog;

    private BazaarPay bazaarPay;
    private final List<BazaarPay.ScorePackPlan> scorePackPlans = new ArrayList<>();

    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(GameViewModel.class);
        repository = new GameRepository(getApplicationContext());
        bazaarPay = BazaarPay.getInstance(this);
        bazaarPay.init();
        initScorePlans();
        subscribeToViewModel();
        viewModel.validateAllLevels();
        viewModel.loadScore();
        if (tvScore != null) {
            tvScore.setText("10");
        }
        repository.getLevelsCountAsync(count -> uiHandler.post(() -> {
            levelsCount = count;
            updateHeaderTexts();
        }));

        int requestedLevel = getIntent() != null ? getIntent().getIntExtra(EXTRA_LEVEL_ID, -1) : -1;
        if (requestedLevel > 0) {
            viewModel.loadLevel(requestedLevel);
            repository.setCurrentLevelIdAsync(requestedLevel);
        } else {
            repository.getCurrentLevelIdAsync(levelId -> uiHandler.post(() -> viewModel.loadLevel(levelId)));
        }
    }

    @Override
    protected void initFooter(LinearLayout llFooter) {
        llFooter.setOrientation(LinearLayout.HORIZONTAL);
        llFooter.setPadding(dp(12), dp(6), dp(12), dp(8));
        llFooter.setGravity(Gravity.CENTER_HORIZONTAL);

        btnPrev = new MaterialButton(this);
        btnPrev.setText("مرحله قبل");
        btnPrev.setIconResource(ir.baran.baranBook.R.drawable.ic_prev_level);
        btnPrev.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START);
        btnPrev.setIconPadding(dp(6));
        btnPrev.setCornerRadius(dp(12));
        btnPrev.setRippleColor(ColorStateList.valueOf(colorBtnRipple));
        btnPrev.setOnClickListener(v -> {
            if (currentLevelId > 1) {
                int prev = currentLevelId - 1;
                viewModel.loadLevel(prev);
                repository.setCurrentLevelIdAsync(prev);
            }
        });

        btnNext = new MaterialButton(this);
        btnNext.setText("مرحله بعد");
        btnNext.setIconResource(ir.baran.baranBook.R.drawable.ic_next_level);
        btnNext.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_END);
        btnNext.setIconPadding(dp(6));
        btnNext.setCornerRadius(dp(12));
        btnNext.setRippleColor(ColorStateList.valueOf(colorBtnRipple));
        btnNext.setEnabled(false);
        btnNext.setOnClickListener(v -> {
            int next = currentLevelId + 1;
            if (allowNextFromCurrentWin) {
                repository.hasLevelAsync(next, exists -> uiHandler.post(() -> {
                    if (exists) {
                        allowNextFromCurrentWin = false;
                        viewModel.loadLevel(next);
                        repository.setCurrentLevelIdAsync(next);
                    } else {
                        showMessage("مرحله بعدی موجود نیست.");
                    }
                }));
                return;
            }

            repository.isLevelCompletedAsync(next, completed -> uiHandler.post(() -> {
                if (completed) {
                    viewModel.loadLevel(next);
                    repository.setCurrentLevelIdAsync(next);
                } else {
                    showMessage("مرحله بعد هنوز آزاد نشده است.");
                }
            }));
        });

        LinearLayout.LayoutParams lpPrev = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        lpPrev.rightMargin = dp(6);
        llFooter.addView(btnPrev, lpPrev);

        LinearLayout.LayoutParams lpNext = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        lpNext.leftMargin = dp(6);
        llFooter.addView(btnNext, lpNext);

        styleFooterButton(btnPrev, false);
        styleFooterButton(btnNext, false);
    }

    @Override
    public void initContent(LinearLayout llContent) {
        MyConfig._FirstForm = this;
        initPalette();

        llContent.setBackgroundResource(ir.baran.baranBook.R.drawable.game_bg_pattern);
        llContent.setOrientation(LinearLayout.VERTICAL);
        llContent.setPadding(dp(12), dp(12), dp(12), dp(12));
        llContent.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);

        LinearLayout headerRow = new LinearLayout(this);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setGravity(Gravity.CENTER_VERTICAL);
        llContent.addView(headerRow, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        LinearLayout stageWrap = new LinearLayout(this);
        stageWrap.setOrientation(LinearLayout.HORIZONTAL);
        stageWrap.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams stageLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        headerRow.addView(stageWrap, stageLp);

        ImageView stageIcon = new ImageView(this);
        stageIcon.setImageResource(ir.baran.baranBook.R.drawable.ic_star_gold);
        LinearLayout.LayoutParams stageIconLp = new LinearLayout.LayoutParams(dp(28), dp(28));
        stageIconLp.rightMargin = dp(6);
        stageWrap.addView(stageIcon, stageIconLp);

        tvStageInfo = new TextView(this);
        tvStageInfo.setTextColor(colorText);
        tvStageInfo.setTypeface(ConfigurationUtils.getLabelFont(this));
        tvStageInfo.setTextSize(17f);
        stageWrap.addView(tvStageInfo);

        LinearLayout scoreWrap = new LinearLayout(this);
        scoreWrap.setOrientation(LinearLayout.HORIZONTAL);
        scoreWrap.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
        headerRow.addView(scoreWrap, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        ImageView scoreIcon = new ImageView(this);
        scoreIcon.setImageResource(ir.baran.baranBook.R.drawable.ic_heart_red);
        LinearLayout.LayoutParams scoreIconLp = new LinearLayout.LayoutParams(dp(28), dp(28));
        scoreIconLp.rightMargin = dp(6);
        scoreWrap.addView(scoreIcon, scoreIconLp);

        tvScore = new TextView(this);
        tvScore.setTextColor(colorText);
        tvScore.setTypeface(ConfigurationUtils.getLabelFont(this));
        tvScore.setTextSize(17f);
        tvScore.setText("10");
        scoreWrap.addView(tvScore);

        tvTitle = new TextView(this);
        tvTitle.setText("انتخاب و جا به جایی");
        tvTitle.setTextColor(colorSubtext);
        tvTitle.setTextSize(13f);
        tvTitle.setPadding(0, dp(4), 0, 0);
        tvTitle.setGravity(Gravity.CENTER_HORIZONTAL);
        llContent.addView(tvTitle, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        tvHint = new TextView(this);
        tvHint.setText("دو حرف را انتخاب کن تا جابه‌جا شوند. بزرگنمایی با دو انگشت فعال است.");
        tvHint.setTextColor(colorSubtext);
        tvHint.setTextSize(14f);
        tvHint.setGravity(Gravity.CENTER_HORIZONTAL);
        tvHint.setPadding(0, dp(4), 0, dp(10));
        llContent.addView(tvHint, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        horizontalScrollView = new HorizontalScrollView(this);
        horizontalScrollView.setHorizontalScrollBarEnabled(false);
        horizontalScrollView.setFillViewport(true);
        LinearLayout.LayoutParams hLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
        );
        llContent.addView(horizontalScrollView, hLp);

        verticalScrollView = new ScrollView(this);
        verticalScrollView.setVerticalScrollBarEnabled(false);
        verticalScrollView.setFillViewport(true);
        horizontalScrollView.addView(verticalScrollView, new HorizontalScrollView.LayoutParams(
                HorizontalScrollView.LayoutParams.MATCH_PARENT,
                HorizontalScrollView.LayoutParams.MATCH_PARENT
        ));

        boardWrapper = new LinearLayout(this);
        boardWrapper.setOrientation(LinearLayout.VERTICAL);
        boardWrapper.setGravity(Gravity.CENTER);
        boardWrapper.setPadding(dp(2), dp(2), dp(2), dp(2));
        LinearLayout.LayoutParams wrapperLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        wrapperLp.gravity = Gravity.CENTER_HORIZONTAL;
        verticalScrollView.addView(boardWrapper, wrapperLp);

        gridLayout = new GridLayout(this);
        gridLayout.setUseDefaultMargins(true);
        gridLayout.setClipChildren(false);
        gridLayout.setClipToPadding(false);
        boardWrapper.addView(gridLayout, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        boardWrapper.setClipChildren(false);
        boardWrapper.setClipToPadding(false);
        verticalScrollView.setClipChildren(false);
        horizontalScrollView.setClipChildren(false);

        scaleDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                zoomFactor *= detector.getScaleFactor();
                zoomFactor = Math.max(0.7f, Math.min(zoomFactor, 2.4f));
                if (currentBoard != null) {
                    renderBoardInternal(currentBoard);
                }
                return true;
            }
        });

        View.OnTouchListener zoomTouchListener = (v, event) -> {
            scaleDetector.onTouchEvent(event);
            return false;
        };
        boardWrapper.setOnTouchListener(zoomTouchListener);
        gridLayout.setOnTouchListener(zoomTouchListener);
        verticalScrollView.setOnTouchListener(zoomTouchListener);
        horizontalScrollView.setOnTouchListener(zoomTouchListener);

        float dif = ConfigurationUtils.getTextSizeDiferent(MyConfig._FirstForm);
        ConfigurationUtils.initTypefacesAndSize(
                llContent,
                ConfigurationUtils.getLabelFont(GamePage.this),
                ConfigurationUtils.START_SIZE * dif
        );
    }

    private void subscribeToViewModel() {
        viewModel.getBoardLiveData().observe(this, this::renderBoard);

        viewModel.getWinLiveData().observe(this, isWin -> {
            if (Boolean.TRUE.equals(isWin)) {
                int finishedLevel = currentLevelId;
                if (finishedLevel <= 0 || winHandledLevelId == finishedLevel) {
                    return;
                }
                winHandledLevelId = finishedLevel;
                allowNextFromCurrentWin = true;
                playSoundFromAssets("applause.mp3");
                showWinConfirmDialog(finishedLevel);
            }
        });

        viewModel.getErrorLiveData().observe(this, error -> {
            if (!TextUtils.isEmpty(error)) {
                showMessage(error);
            }
        });

        viewModel.getValidationErrorsLiveData().observe(this, errors -> {
            if (errors == null || errors.isEmpty()) {
                return;
            }
            showMessage("خطای داده مرحله: " + errors.get(0));
        });

        viewModel.getScoreLiveData().observe(this, score -> {
            if (tvScore != null && score != null) {
                tvScore.setText(String.valueOf(score));
            }
            int currentScore = score == null ? 10 : score;
            boolean granted = handleFreeScoreRecoveryIfNeeded(currentScore);
            if (!granted) {
                handleScoreLock(currentScore);
            }
        });

        viewModel.getMoveResultLiveData().observe(this, isOk -> {
            if (isOk == null) {
                return;
            }
            handleScoreLock(viewModel.getCurrentScore());
        });

        viewModel.getSoundEventLiveData().observe(this, event -> {
            if (TextUtils.isEmpty(event)) {
                return;
            }
            switch (event) {
                case "click":
                    playSoundFromAssets("click.mp3");
                    break;
                case "click-ok":
                    playSoundFromAssets("click-ok.mp3");
                    break;
                default:
                    playSoundFromAssets("click-no.mp3");
                    break;

            }
        });
    }

    private void renderBoard(GameBoard board) {
        if (board == null || board.getCells() == null) {
            return;
        }

        if (board.getLevelId() != currentLevelId) {
            currentLevelId = board.getLevelId();
            zoomFactor = 1f;
            winHandledLevelId = -1;
            allowNextFromCurrentWin = false;
            repository.setCurrentLevelIdAsync(currentLevelId);
            refreshFooterButtons();
            updateHeaderTexts();
        }

        currentBoard = board;
        renderBoardInternal(board);
    }

    private void renderBoardInternal(GameBoard board) {
        if (board == null || board.getCells() == null) {
            return;
        }

        List<List<GameCell>> rows = board.getCells();
        int rowCount = rows.size();
        int colCount = rowCount > 0 ? rows.get(0).size() : 0;
        tvTitle.setText("جدول واژه");

        Map<String, List<ClueItem>> clueMap = parseCluesByAnchor(board.getCluesDataJson());

        gridLayout.removeAllViews();
        gridLayout.setRowCount(rowCount);
        gridLayout.setColumnCount(colCount);

        int baseSize = calculateCellSize(colCount);
        int cellSize = Math.max(dp(34), Math.min(dp(140), (int) (baseSize * zoomFactor)));

        for (int r = 0; r < rowCount; r++) {
            List<GameCell> row = rows.get(r);
            for (int c = 0; c < colCount; c++) {
                GameCell cell = row.get(c);
                String key = key(r, c);
                View cellView = buildCellView(cell, cellSize, clueMap.get(key));
                gridLayout.addView(cellView);
            }
        }
    }

    private View buildCellView(GameCell cell, int cellSize, List<ClueItem> cluesAtCell) {
        if (cell.getState() == CellState.BLOCKED && cluesAtCell != null && !cluesAtCell.isEmpty()) {
            return buildClueCell(cellSize, cluesAtCell);
        }

        TextView tv = new TextView(this);
        tv.setGravity(Gravity.CENTER);
        tv.setTextColor(cell.getState() == CellState.BLOCKED ? colorSubtext : colorText);

        Typeface tf2 = ConfigurationUtils.getLabelFont2(GamePage.this);
        Typeface tf1 = ConfigurationUtils.getLabelFont(GamePage.this);
        tv.setTypeface(cell.getState() == CellState.BLOCKED ? tf1 : tf2);
        tv.setTextSize(cell.getState() == CellState.BLOCKED ? clueFontSizeForCell(cellSize) : letterFontSizeForCell(cellSize));
        tv.setText(safeLetter(cell.getLetter()));

        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = cellSize;
        lp.height = cellSize;
        tv.setLayoutParams(lp);
        tv.setBackground(createCellBackground(cell.getState()));

        if (cell.getState() == CellState.MOVABLE || cell.getState() == CellState.SELECTED) {
            tv.setOnClickListener(v -> {
                if (isScoreLocked) {
                    showScorePurchaseDialogIfNeeded();
                    return;
                }
                runTapAnimation(v);
                viewModel.onCellTapped(cell.getRow(), cell.getCol());
            });
        }

        return tv;
    }

    private View buildClueCell(int cellSize, List<ClueItem> cluesAtCell) {
        FrameLayout root = new FrameLayout(this);
        root.setClipChildren(false);
        root.setClipToPadding(false);

        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = cellSize;
        lp.height = cellSize;
        root.setLayoutParams(lp);

        LinearLayout rowsContainer = new LinearLayout(this);
        rowsContainer.setOrientation(LinearLayout.VERTICAL);
        rowsContainer.setPadding(0, 0, 0, 0);
        rowsContainer.setBackground(createCellBackground(CellState.BLOCKED));
        rowsContainer.setClipChildren(false);
        rowsContainer.setClipToPadding(false);
        root.addView(rowsContainer, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        int count = Math.min(2, cluesAtCell.size());
        int iconSize = Math.max(dp(20), Math.min(dp(38), cellSize / 3));
        for (int i = 0; i < count; i++) {
            ClueItem clue = cluesAtCell.get(i);
            String normalizedDir = normalizeDirection(clue.direction);

            FrameLayout clueBox = new FrameLayout(this);
            clueBox.setBackground(createMiniClueBoxBackground(i == 0, count));
            clueBox.setClipChildren(false);
            clueBox.setClipToPadding(false);
            clueBox.setClickable(true);
            clueBox.setOnClickListener(v -> showSingleClueDialog(clue));

            TextView tv = new TextView(this);
            tv.setTextColor(colorSubtext);
            tv.setTextSize(clueInlineFontSizeForCell(cellSize));
            tv.setTypeface(ConfigurationUtils.getLabelFont(GamePage.this));
            tv.setSingleLine(true);
            tv.setEllipsize(TextUtils.TruncateAt.END);
            tv.setText(clue.clue);
            tv.setGravity(Gravity.CENTER_VERTICAL);

            int basePadStart = dp(5);
            int basePadEnd = dp(5);
            int extraPad = Math.max(dp(6), iconSize / 2);
            if (isLeftFamily(normalizedDir)) {
                tv.setPadding(basePadStart + extraPad, dp(2), basePadEnd, dp(2));
            } else if (isRightFamily(normalizedDir)) {
                tv.setPadding(basePadStart, dp(2), basePadEnd + extraPad, dp(2));
            } else {
                tv.setPadding(basePadStart, dp(2), basePadEnd, dp(2));
            }

            clueBox.addView(tv, new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            ));

            Drawable arrow = getArrowDrawable(clue.direction);
            if (arrow != null) {
                ImageView iv = new ImageView(this);
                iv.setImageDrawable(arrow);
                iv.setClickable(false);
                clueBox.addView(iv, clueArrowParams(normalizedDir, iconSize));
            }

            LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    1f
            );
            rowsContainer.addView(clueBox, rowLp);
        }

        return root;
    }

    private FrameLayout.LayoutParams clueArrowParams(String dir, int iconSize) {
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(iconSize, iconSize);
        int over = -(iconSize / 2);
        int diagOver = -(iconSize / 3);
        switch (dir) {
            case "left":
                lp.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
                lp.leftMargin = over;
                break;
            case "right":
                lp.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
                lp.rightMargin = over;
                break;
            case "up":
                lp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
                lp.topMargin = over;
                break;
            case "down":
                lp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                lp.bottomMargin = over;
                break;
            case "up_left":
            case "left_up":
                lp.gravity = Gravity.TOP | Gravity.START;
                lp.topMargin = diagOver;
                lp.leftMargin = diagOver;
                break;
            case "up_right":
            case "right_up":
                lp.gravity = Gravity.TOP | Gravity.END;
                lp.topMargin = diagOver;
                lp.rightMargin = diagOver;
                break;
            case "down_left":
            case "left_down":
                lp.gravity = Gravity.BOTTOM | Gravity.START;
                lp.bottomMargin = diagOver;
                lp.leftMargin = diagOver;
                break;
            case "down_right":
            case "right_down":
                lp.gravity = Gravity.BOTTOM | Gravity.END;
                lp.bottomMargin = diagOver;
                lp.rightMargin = diagOver;
                break;
            default:
                lp.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
                lp.rightMargin = over;
                break;
        }
        return lp;
    }

    private boolean isLeftFamily(String dir) {
        return "left".equals(dir) || "left_up".equals(dir) || "up_left".equals(dir)
                || "left_down".equals(dir) || "down_left".equals(dir);
    }

    private boolean isRightFamily(String dir) {
        return "right".equals(dir) || "right_up".equals(dir) || "up_right".equals(dir)
                || "right_down".equals(dir) || "down_right".equals(dir);
    }

    private GradientDrawable createMiniClueBoxBackground(boolean first, int count) {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(0x00000000);
        bg.setStroke(dp(1), 0xFFCAD5E0);
        if (count == 1) {
            bg.setCornerRadius(dp(8));
            return bg;
        }
        if (first) {
            bg.setCornerRadii(new float[]{dp(8), dp(8), dp(8), dp(8), 0, 0, 0, 0});
        } else {
            bg.setCornerRadii(new float[]{0, 0, 0, 0, dp(8), dp(8), dp(8), dp(8)});
        }
        return bg;
    }

    private String normalizeDirection(String direction) {
        return direction == null ? "" : direction.trim().toLowerCase(Locale.US).replace('-', '_');
    }

    private void showSingleClueDialog(ClueItem clue) {
        if (clue == null) {
            return;
        }
        StringBuilder sb = new StringBuilder(clue.clue);
        //TODO:remove it is just for test
        if (!TextUtils.isEmpty(clue.answer)) {
//            sb.append(" ( ").append(clue.answer).append(" )");
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle("متن کامل راهنما")
                .setMessage(sb.toString())
                .setPositiveButton("بستن", null)
                .show();
    }

    private void showWinConfirmDialog(int finishedLevel) {

//        int nextLevel = finishedLevel + 1;
//        new MaterialAlertDialogBuilder(this)
//                .setTitle("تبریک")
//                .setMessage("مرحله " + finishedLevel + " کامل شد. ورود به مرحله " + nextLevel + "؟")
//                .setPositiveButton("بله", (dialog, which) -> {
//                    repository.hasLevelAsync(nextLevel, exists -> uiHandler.post(() -> {
//                        if (exists) {
//                            viewModel.loadLevel(nextLevel);
//                            repository.setCurrentLevelIdAsync(nextLevel);
//                            showMessage("مرحله " + nextLevel + " شروع شد.");
//                        } else {
//                            showMessage("مرحله بعدی موجود نیست.");
//                        }
//                    }));
//                })
//                .setNegativeButton("خیر", (dialog, which) -> {
//                    // کاربر روی همین مرحله بماند و فقط دکمه مرحله بعد فعال شود.
//                })
//                .show();
        showMessage("تبریک! " + "مرحله " + finishedLevel + " کامل شد. ورود به مرحله ");
        allowNextFromCurrentWin = true;
        if (btnNext != null) {
            btnNext.setEnabled(true);
            styleFooterButton(btnNext, true);
        }

    }

    private void refreshFooterButtons() {
        if (btnPrev != null) {
            boolean enabledPrev = currentLevelId > 1 && !isScoreLocked;
            btnPrev.setEnabled(enabledPrev);
            styleFooterButton(btnPrev, enabledPrev);
        }

        if (btnNext != null && currentLevelId > 0) {
            if (isScoreLocked) {
                btnNext.setEnabled(false);
                styleFooterButton(btnNext, false);
                return;
            }
            int next = currentLevelId + 1;
            repository.isLevelCompletedAsync(next, completed -> uiHandler.post(() -> {
                btnNext.setEnabled(completed);
                styleFooterButton(btnNext, completed);
            }));
        }
    }

    private void styleFooterButton(MaterialButton button, boolean enabled) {
        int bg = enabled ? colorBtnEnabledBg : colorBtnDisabledBg;
        int txt = enabled ? colorBtnEnabledText : colorBtnDisabledText;
        int stroke = enabled ? colorBtnEnabledStroke : colorBtnDisabledStroke;

        button.setBackgroundTintList(ColorStateList.valueOf(bg));
        button.setTextColor(txt);
        button.setStrokeColor(ColorStateList.valueOf(stroke));
        button.setStrokeWidth(dp(1));
        button.setIconTint(ColorStateList.valueOf(txt));
    }

    private void playSoundFromAssets(String assetName) {
        try {
            AssetFileDescriptor afd = getAssets().openFd(assetName);
            MediaPlayer player = new MediaPlayer();
            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
            player.setOnCompletionListener(MediaPlayer::release);
            player.prepare();
            player.start();
        } catch (Exception ignored) {
        }
    }

    private Map<String, List<ClueItem>> parseCluesByAnchor(String cluesJson) {
        Map<String, List<ClueItem>> map = new HashMap<>();
        if (TextUtils.isEmpty(cluesJson)) {
            return map;
        }

        try {
            JSONArray arr = new JSONArray(cluesJson);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.optJSONObject(i);
                if (obj == null) {
                    continue;
                }
                int row = obj.optInt("row", -1);
                int col = obj.optInt("col", -1);
                String clue = obj.optString("clue", "").trim();
                String direction = obj.optString("direction", "").trim();
                String answer = obj.optString("answer", "").trim();

                if (row < 0 || col < 0 || clue.isEmpty()) {
                    continue;
                }
                String key = key(row, col);
                List<ClueItem> list = map.computeIfAbsent(key, k -> new ArrayList<>());
                list.add(new ClueItem(clue, direction, answer));
            }
        } catch (Exception ignored) {
        }

        return map;
    }

    private Drawable getArrowDrawable(String direction) {
        if (TextUtils.isEmpty(direction)) {
            return null;
        }

        String normalized = direction.trim().toLowerCase(Locale.US).replace('-', '_');
        String fileName;
        switch (normalized) {
            case "left":
                fileName = "left.png";
                break;
            case "down":
                fileName = "down.png";
                break;
            case "up":
            case "up_left":
            case "left_up":
                fileName = "up_left.png";
                break;
            case "up_right":
            case "right_up":
                fileName = "up_right.png";
                break;
            case "down_left":
            case "left_down":
                fileName = "down_left.png";
                break;
            case "right":
            case "down_right":
            case "right_down":
                fileName = "right_down.png";
                break;
            default:
                return null;
        }

        if (arrowCache.containsKey(fileName)) {
            return arrowCache.get(fileName);
        }

        try {
            InputStream is = getAssets().open("dirs/" + fileName);
            Drawable drawable = Drawable.createFromStream(is, fileName);
            arrowCache.put(fileName, drawable);
            return drawable;
        } catch (Exception e) {
            return null;
        }
    }

    private void runTapAnimation(View v) {
        if (Looper.myLooper() == null) {
            return;
        }

        ViewPropertyAnimator animator = v.animate();
        animator.cancel();
        animator.scaleX(0.93f)
                .scaleY(0.93f)
                .setDuration(70)
                .withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(90).start())
                .start();
    }

    private GradientDrawable createCellBackground(CellState state) {
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(dp(10));

        int fillColor;
        int strokeColor;
        if (state == CellState.BLOCKED) {
            fillColor = colorCellBlocked;
            strokeColor = 0xFFD9E2EC;
        } else if (state == CellState.SELECTED) {
            fillColor = colorCellSelected;
            strokeColor = 0xFFE0A800;
        } else if (state == CellState.LOCKED) {
            fillColor = colorCellLocked;
            strokeColor = 0xFF57A773;
        } else {
            fillColor = colorCellMovable;
            strokeColor = 0xFFD9E2EC;
        }

        shape.setColor(fillColor);
        shape.setStroke(dp(2), strokeColor);
        return shape;
    }

    private int calculateCellSize(int colCount) {
        if (colCount <= 0) {
            return dp(42);
        }

        int totalHorizontalPadding = dp(36);
        int totalWidth = getScreenWidth() - totalHorizontalPadding;
        int maxSizeByWidth = totalWidth / colCount;
        int maxCap = dp(80);
        int minCap = dp(42);

        int result = Math.min(maxSizeByWidth, maxCap);
        if (result < minCap) {
            result = minCap;
        }
        return result;
    }

    private int dp(int value) {
        return Functions.dp2px(value);
    }

    private void updateHeaderTexts() {
        if (tvStageInfo != null) {
            if (levelsCount > 0 && currentLevelId > 0) {
                tvStageInfo.setText("مرحله " + currentLevelId + " از " + levelsCount);
            } else if (currentLevelId > 0) {
                tvStageInfo.setText("مرحله " + currentLevelId);
            } else {
                tvStageInfo.setText("مرحله -");
            }
        }
    }

    private String safeLetter(String letter) {
        return letter == null ? "" : letter.trim();
    }

    private String key(int row, int col) {
        return row + "_" + col;
    }

    private float letterFontSizeForCell(int cellSizePx) {
        return clamp(cellSizePx / 3.0f, 14f, 40f);
    }

    private float clueFontSizeForCell(int cellSizePx) {
        return clamp(cellSizePx / 5.2f, 9f, 19f);
    }

    private float clueInlineFontSizeForCell(int cellSizePx) {
        return clamp(cellSizePx / 15.2f, 3f, 16f);
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private void initPalette() {
        colorBgPage = getColorCompat(ir.baran.baranBook.R.color.game_bg_page);
        colorCellMovable = getColorCompat(ir.baran.baranBook.R.color.game_cell_movable);
        colorCellSelected = getColorCompat(ir.baran.baranBook.R.color.game_cell_selected);
        colorCellLocked = getColorCompat(ir.baran.baranBook.R.color.game_cell_locked);
        colorCellBlocked = getColorCompat(ir.baran.baranBook.R.color.game_cell_blocked);
        colorText = getColorCompat(ir.baran.baranBook.R.color.game_text_primary);
        colorSubtext = getColorCompat(ir.baran.baranBook.R.color.game_text_secondary);

        colorBtnEnabledBg = getColorCompat(ir.baran.baranBook.R.color.game_btn_enabled_bg);
        colorBtnDisabledBg = getColorCompat(ir.baran.baranBook.R.color.game_btn_disabled_bg);
        colorBtnEnabledText = getColorCompat(ir.baran.baranBook.R.color.game_btn_enabled_text);
        colorBtnDisabledText = getColorCompat(ir.baran.baranBook.R.color.game_btn_disabled_text);
        colorBtnEnabledStroke = getColorCompat(ir.baran.baranBook.R.color.game_btn_enabled_stroke);
        colorBtnDisabledStroke = getColorCompat(ir.baran.baranBook.R.color.game_btn_disabled_stroke);
        colorBtnRipple = getColorCompat(ir.baran.baranBook.R.color.game_btn_ripple);
    }

    private int getColorCompat(int colorRes) {
        return getColor(colorRes);
    }

    private void initScorePlans() {
        scorePackPlans.clear();
        scorePackPlans.add(new BazaarPay.ScorePackPlan("score_100", 100, 1000));
        scorePackPlans.add(new BazaarPay.ScorePackPlan("score_150", 150, 1200));
        scorePackPlans.add(new BazaarPay.ScorePackPlan("score_350", 350, 2000));
    }

    private void handleScoreLock(int score) {
        boolean shouldLock = score <= 0;
        if (isScoreLocked == shouldLock && !(shouldLock && !isScorePurchaseDialogShowing)) {
            return;
        }
        isScoreLocked = shouldLock;
        refreshFooterButtons();
        if (isScoreLocked) {
            showScorePurchaseDialogIfNeeded();
        }
    }

    private void showScorePurchaseDialogIfNeeded() {
        if (!isScoreLocked || isFinishing() || isScorePurchaseDialogShowing) {
            return;
        }
        if (scorePackPlans.isEmpty()) {
            initScorePlans();
        }
        if (scorePurchaseDialog != null && scorePurchaseDialog.isShowing()) {
            return;
        }
        isScorePurchaseDialogShowing = true;
        LinearLayout plansContainer = new LinearLayout(this);
        plansContainer.setOrientation(LinearLayout.VERTICAL);
        plansContainer.setPadding(dp(8), dp(8), dp(8), dp(8));

        for (BazaarPay.ScorePackPlan plan : scorePackPlans) {
            TextView planView = new TextView(this);
            planView.setText(plan.title);
            planView.setTextSize(16f);
            planView.setTextColor(colorText);
            planView.setPadding(dp(14), dp(12), dp(14), dp(12));
            planView.setBackground(createMiniClueBoxBackground(true, 1));
            LinearLayout.LayoutParams planLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            planLp.bottomMargin = dp(8);
            planView.setLayoutParams(planLp);
            planView.setOnClickListener(v -> startScorePurchase(plan));
            plansContainer.addView(planView);
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setTitle("امتیاز شما تمام شد")
                .setMessage("برای ادامه بازی یکی از بسته‌های امتیاز را خریداری کنید.\n\nطرح بازیابی امتیاز: اگر امتیاز شما ۰ باشد و بعد از ۶۰ دقیقه بازی را باز کنید، ۵ امتیاز رایگان دریافت می‌کنید.")
                .setCancelable(false)
                .setView(plansContainer);

        scorePurchaseDialog = builder.create();
        scorePurchaseDialog.setCanceledOnTouchOutside(false);
        scorePurchaseDialog.show();
    }

    private void startScorePurchase(BazaarPay.ScorePackPlan selectedPlan) {
        if (selectedPlan == null || bazaarPay == null) {
            return;
        }
        isScorePurchaseInProgress = true;
        bazaarPay.purchaseScorePlan(selectedPlan, new BazaarPay.ScorePurchaseListener() {
            @Override
            public void onScorePurchaseSuccess(BazaarPay.ScorePackPlan plan) {
                runOnUiThread(() -> {
                    isScorePurchaseInProgress = false;
                    clearScoreZeroTimestamp();
                    viewModel.addScore(plan.scoreAmount);
                    showMessage(plan.scoreAmount + " امتیاز به حساب شما اضافه شد.");
                    isScorePurchaseDialogShowing = false;
                    if (scorePurchaseDialog != null) {
                        scorePurchaseDialog.dismiss();
                    }
                });
            }

            @Override
            public void onScorePurchaseCancelled() {
                runOnUiThread(() -> {
                    isScorePurchaseInProgress = false;
                    showMessage("خرید لغو شد.");
                    isScorePurchaseDialogShowing = false;
                    showScorePurchaseDialogIfNeeded();
                });
            }

            @Override
            public void onScorePurchaseFailed(String message) {
                runOnUiThread(() -> {
                    isScorePurchaseInProgress = false;
                    showMessage(message);
                    isScorePurchaseDialogShowing = false;
                    showScorePurchaseDialogIfNeeded();
                });
            }
        });
    }

    private boolean handleFreeScoreRecoveryIfNeeded(int score) {
        if (score > 0) {
            clearScoreZeroTimestamp();
            return false;
        }

        long now = System.currentTimeMillis();
        long zeroAt = getSharedPreferences(SCORE_PREFS_NAME, MODE_PRIVATE).getLong(KEY_SCORE_ZERO_AT, 0L);
        if (zeroAt <= 0L) {
            getSharedPreferences(SCORE_PREFS_NAME, MODE_PRIVATE)
                    .edit()
                    .putLong(KEY_SCORE_ZERO_AT, now)
                    .apply();
            return false;
        }

        if ((now - zeroAt) >= FREE_SCORE_INTERVAL_MS) {
            clearScoreZeroTimestamp();
            viewModel.addScore(FREE_SCORE_AMOUNT);
            showMessage(FREE_SCORE_AMOUNT + " امتیاز رایگان به شما اضافه شد.");
            return true;
        }

        return false;
    }

    private void clearScoreZeroTimestamp() {
        getSharedPreferences(SCORE_PREFS_NAME, MODE_PRIVATE)
                .edit()
                .remove(KEY_SCORE_ZERO_AT)
                .apply();
    }

    private static class ClueItem {
        final String clue;
        final String direction;
        final String answer;

        ClueItem(String clue, String direction, String answer) {
            this.clue = clue;
            this.direction = direction;
            this.answer = answer;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (scorePurchaseDialog != null) {
            scorePurchaseDialog.dismiss();
            scorePurchaseDialog = null;
        }
        if (bazaarPay != null) {
            bazaarPay.onDistroy();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        handleScoreLock(viewModel.getCurrentScore());
    }
}
