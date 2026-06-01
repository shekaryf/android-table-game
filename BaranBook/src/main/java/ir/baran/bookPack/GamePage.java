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
import android.view.LayoutInflater;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ir.baran.baranBook.R;
import ir.baran.bookPack.api.GiftCodeApi;
import ir.baran.bookPack.api.LeaderboardApi;
import ir.baran.bookPack.api.ScorePlansApi;
import ir.baran.bookPack.api.model.LeaderboardEntry;
import ir.baran.bookPack.api.model.LeaderboardResult;
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
    private static final String TAG_CLUE_CELL = "tag_clue_cell";
    private static final String SCORE_PREFS_NAME = "game_prefs";
    public static final int INIT_DEFAULT_SCORE = 10;
    private static final String LEADERBOARD_PREFS = "leaderboard_prefs";
    private static final String KEY_PLAYER_NAME = "player_name";
    private static final String KEY_PLAYER_MOBILE = "player_mobile";
    private static final String KEY_PLAYER_SERVER_ID = "player_server_id";

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
    private TextView tvStageInfo;
    private TextView tvScore;
    private ImageView ivScoreIcon;
    private LinearLayout boardWrapper;
    private HorizontalScrollView horizontalScrollView;
    private ScrollView verticalScrollView;

    private MaterialButton btnPrev;
    private MaterialButton btnNext;
    private MaterialButton btnHelp;
    private MaterialButton btnLeaderboard;

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
    private View lastTappedCellView;
    private int lastTappedRow = -1;
    private int lastTappedCol = -1;

    private BazaarPay bazaarPay;
    private ScorePlansApi scorePlansApi;
    private LeaderboardApi leaderboardApi;
    private GiftCodeApi giftCodeApi;
    private final List<BazaarPay.ScorePackPlan> scorePackPlans = new ArrayList<>();

    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(GameViewModel.class);
        repository = new GameRepository(getApplicationContext());
        bazaarPay = BazaarPay.getInstance(this);
        scorePlansApi = new ScorePlansApi();
        leaderboardApi = new LeaderboardApi();
        giftCodeApi = new GiftCodeApi();
        bazaarPay.init();
        initScorePlans();
        subscribeToViewModel();
        viewModel.validateAllLevels();
        viewModel.loadScore();
        if (tvScore != null) {
            tvScore.setText(String.valueOf(INIT_DEFAULT_SCORE));
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
                        syncLeaderboardSilentlyIfRegistered(next);
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
                    syncLeaderboardSilentlyIfRegistered(next);
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

        llContent.setOrientation(LinearLayout.VERTICAL);
        llContent.setPadding(dp(12), dp(12), dp(12), dp(12));
        llContent.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);

        View headerView = LayoutInflater.from(this).inflate(ir.baran.baranBook.R.layout.game_page_header, llContent, false);
        llHeader.addView(headerView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        tvStageInfo = headerView.findViewById(ir.baran.baranBook.R.id.tvStageInfo);
        tvScore = headerView.findViewById(ir.baran.baranBook.R.id.tvScore);
        btnLeaderboard = headerView.findViewById(ir.baran.baranBook.R.id.btnLeaderboard);
        btnHelp = headerView.findViewById(ir.baran.baranBook.R.id.btnHelp);
        ImageView stageIcon = headerView.findViewById(ir.baran.baranBook.R.id.stageIcon);
        ivScoreIcon = headerView.findViewById(ir.baran.baranBook.R.id.scoreIcon);

        tvStageInfo.setTextColor(colorText);
        tvStageInfo.setTypeface(ConfigurationUtils.getLabelFont(this));
        tvScore.setTextColor(colorText);
        tvScore.setTypeface(ConfigurationUtils.getLabelFont(this));
        tvScore.setText(String.valueOf(INIT_DEFAULT_SCORE));
        stageIcon.setImageResource(ir.baran.baranBook.R.drawable.ic_star_gold);
        ivScoreIcon.setImageResource(R.drawable.ic_coin_gold);
        ivScoreIcon.setOnClickListener(v -> {
            int scoreNow = getSharedPreferences(SCORE_PREFS_NAME, MODE_PRIVATE).getInt("score", INIT_DEFAULT_SCORE);
            showScorePurchaseDialog(true, scoreNow);
        });
        btnLeaderboard.setOnClickListener(v -> onLeaderboardClick());

        btnHelp.setCornerRadius(dp(10));
        btnHelp.setIconPadding(dp(4));
        btnHelp.setInsetTop(0);
        btnHelp.setInsetBottom(0);
        btnHelp.setOnClickListener(v -> Help.showGameHelpDialog(
                GamePage.this,
                getString(ir.baran.baranBook.R.string.game_title_text),
                getString(ir.baran.baranBook.R.string.game_hint_text)
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
            if (tvScore==null)
                tvScore = findViewById(ir.baran.baranBook.R.id.tvScore);
            int currentScore = score == null ? INIT_DEFAULT_SCORE : score;
            if (tvScore != null) {
                tvScore.setText(String.valueOf(currentScore));
            }
            handleScoreLock(currentScore);
        });

        viewModel.getMoveResultLiveData().observe(this, isOk -> {
            if (isOk == null) {
                return;
            }
            if (isOk) {
                runCoinRewardAnimation();
            } else {
                uiHandler.post(() -> runWrongTapCrashAnimation(findCurrentCellView(lastTappedRow, lastTappedCol)));
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
                    int scoreNow = getSharedPreferences(SCORE_PREFS_NAME, MODE_PRIVATE).getInt("score", INIT_DEFAULT_SCORE);
                    showScorePurchaseDialogIfNeeded(scoreNow);
                    return;
                }
                lastTappedCellView = v;
                lastTappedRow = cell.getRow();
                lastTappedCol = cell.getCol();
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
        root.setTag(TAG_CLUE_CELL);
        root.setClickable(true);
        // Higher Z prevents right/down arrows from being covered by neighbor cells.
        root.setElevation(dp(10));
        root.setTranslationZ(dp(10));

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

        List<ClueItem> orderedClues = orderCluesForTwoRows(cluesAtCell);
        int count = Math.min(2, orderedClues.size());
        int iconSize = Math.max(dp(20), Math.min(dp(38), cellSize / 3));
        for (int i = 0; i < count; i++) {
            ClueItem clue = orderedClues.get(i);
            String normalizedDir = normalizeDirection(clue.direction);

            FrameLayout clueBox = new FrameLayout(this);
            clueBox.setBackground(createMiniClueBoxBackground(i == 0, count));
            clueBox.setClipChildren(false);
            clueBox.setClipToPadding(false);
            clueBox.setClickable(true);
            clueBox.setOnClickListener(v -> showSingleClueDialog(clue));

            TextView tv = new TextView(this);
            tv.setTextColor(colorSubtext);
            boolean isLongClue = clue.clue != null && clue.clue.trim().length() > 10;
            float baseInlineSize = clueInlineFontSizeForCell(cellSize);
            tv.setTextSize(isLongClue ? Math.max(8f, baseInlineSize - 4f) : baseInlineSize);
            tv.setTypeface(ConfigurationUtils.getLabelFont(GamePage.this));
            tv.setSingleLine(!isLongClue);
            tv.setMaxLines(isLongClue ? 2 : 1);
            tv.setEllipsize(TextUtils.TruncateAt.END);
            tv.setText(clue.clue);
            tv.setGravity(Gravity.CENTER);

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
            clueBox.bringToFront();
        }

        return root;
    }

    private List<ClueItem> orderCluesForTwoRows(List<ClueItem> source) {
        List<ClueItem> ordered = new ArrayList<>();
        if (source == null || source.isEmpty()) {
            return ordered;
        }
        if (source.size() == 1) {
            ordered.add(source.get(0));
            return ordered;
        }

        // باکس دو ردیفی: UP باید بالا باشد و DOWN باید پایین باشد.
        // این کار از افتادن فلش UP روی متن ردیف بالا جلوگیری می‌کند.
        ClueItem first = source.get(0);
        ClueItem second = source.get(1);
        String dirFirst = normalizeDirection(first.direction);
        String dirSecond = normalizeDirection(second.direction);

        boolean firstUp = isUpFamily(dirFirst);
        boolean secondUp = isUpFamily(dirSecond);
        boolean firstDown = isDownFamily(dirFirst);
        boolean secondDown = isDownFamily(dirSecond);

        if (secondUp && !firstUp) {
            ordered.add(second); // بالا
            ordered.add(first);  // پایین
            return ordered;
        }
        if (firstDown && !secondDown) {
            ordered.add(second); // بالا
            ordered.add(first);  // پایین (DOWN)
            return ordered;
        }

        ordered.add(first);
        ordered.add(second);
        return ordered;
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

    private boolean isUpFamily(String dir) {
        return "up".equals(dir) || "up_left".equals(dir) || "left_up".equals(dir)
                || "up_right".equals(dir) || "right_up".equals(dir);
    }

    private boolean isDownFamily(String dir) {
        return "down".equals(dir) || "down_left".equals(dir) || "left_down".equals(dir)
                || "down_right".equals(dir) || "right_down".equals(dir);
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
        String keyName;
        int drawableRes;
        switch (normalized) {
            case "left":
                keyName = "arrow_left";
                drawableRes = R.drawable.arrow_left;
                break;
            case "down":
                keyName = "arrow_down";
                drawableRes = R.drawable.arrow_down;
                break;
            case "up":
            case "up_left":
            case "left_up":
                keyName = "arrow_up_left";
                drawableRes = R.drawable.arrow_up_left;
                break;
            case "up_right":
            case "right_up":
                keyName = "arrow_up_right";
                drawableRes = R.drawable.arrow_up_right;
                break;
            case "down_left":
            case "left_down":
                keyName = "arrow_down_left";
                drawableRes = R.drawable.arrow_down_left;
                break;
            case "right":
            case "down_right":
            case "right_down":
                keyName = "arrow_right_down";
                drawableRes = R.drawable.arrow_right_down;
                break;
            default:
                return null;
        }

        if (arrowCache.containsKey(keyName)) {
            return arrowCache.get(keyName);
        }

        try {
            Drawable drawable = getResources().getDrawable(drawableRes);
            arrowCache.put(keyName, drawable);
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

    private void runCoinRewardAnimation() {
        if (ivScoreIcon == null) {
            return;
        }
        ivScoreIcon.clearAnimation();
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.coin_reward);
        ivScoreIcon.startAnimation(anim);
    }

    private void runWrongTapCrashAnimation(View v) {
        if (v == null) {
            return;
        }
        v.clearAnimation();
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.wrong_tap_crash);
        v.startAnimation(anim);
    }

    private View findCurrentCellView(int row, int col) {
        if (gridLayout == null || currentBoard == null || currentBoard.getCells() == null) {
            return null;
        }
        List<List<GameCell>> rows = currentBoard.getCells();
        if (row < 0 || row >= rows.size()) {
            return null;
        }
        int colCount = rows.get(0).size();
        if (col < 0 || col >= colCount) {
            return null;
        }
        int index = row * colCount + col;
        if (index < 0 || index >= gridLayout.getChildCount()) {
            return null;
        }
        return gridLayout.getChildAt(index);
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
        scorePackPlans.add(new BazaarPay.ScorePackPlan("score_1", 100, 3000));
        scorePackPlans.add(new BazaarPay.ScorePackPlan("score_2", 150, 4000));
        scorePackPlans.add(new BazaarPay.ScorePackPlan("score_3", 300, 7000));
    }

    private void handleScoreLock(int score) {
        // Immediately reflect latest score in header to avoid stale visual state.
        if (tvScore != null) {
            tvScore.setText(String.valueOf(score));
        }
        boolean shouldLock = score <= 0;
        if (isScoreLocked == shouldLock && !(shouldLock && !isScorePurchaseDialogShowing)) {
            return;
        }
        isScoreLocked = shouldLock;
        refreshFooterButtons();
        if (isScoreLocked) {
            showScorePurchaseDialogIfNeeded(score);
        }
    }

    private void showScorePurchaseDialogIfNeeded(int scoreSnapshot) {
        showScorePurchaseDialog(false, scoreSnapshot);
    }

    private void showScorePurchaseDialog(boolean forcedByUser, int scoreSnapshot) {
        boolean locked = scoreSnapshot <= 0;
        if ((!forcedByUser && !locked) || isFinishing() || isScorePurchaseDialogShowing) {
            return;
        }
        if (scorePackPlans.isEmpty()) {
            initScorePlans();
        }
        if (scorePurchaseDialog != null && scorePurchaseDialog.isShowing()) {
            return;
        }
        isScorePurchaseDialogShowing = true;
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_score_purchase, null, false);
        TextView tvTitle = dialogView.findViewById(R.id.tvScorePurchaseTitle);
        TextView tvDesc = dialogView.findViewById(R.id.tvScorePurchaseDesc);
        TextView tvLoadingPlans = dialogView.findViewById(R.id.tvLoadingPlans);
        TextView tvGiftTitle = dialogView.findViewById(R.id.tvGiftTitle);
        TextInputEditText etGiftCode = dialogView.findViewById(R.id.etGiftCode);
        MaterialButton btnApplyGift = dialogView.findViewById(R.id.btnApplyGift);
        LinearLayout plansContainer = dialogView.findViewById(R.id.plansContainer);
        LinearLayout loadingContainer = dialogView.findViewById(R.id.loadingContainer);
        MaterialButton btnCancelPurchase = dialogView.findViewById(R.id.btnCancelPurchase);

        tvTitle.setTypeface(MyConfig.getDefaultTypeface());
        tvDesc.setTypeface(MyConfig.getDefaultTypeface());
        tvLoadingPlans.setTypeface(MyConfig.getDefaultTypeface());
        tvGiftTitle.setTypeface(MyConfig.getDefaultTypeface());
        etGiftCode.setTypeface(MyConfig.getDefaultTypeface());
        btnApplyGift.setTypeface(MyConfig.getDefaultTypeface());
        loadingContainer.setVisibility(View.VISIBLE);
        plansContainer.setVisibility(View.GONE);
        plansContainer.removeAllViews();

        boolean canCancel = !locked;
        btnCancelPurchase.setTypeface(MyConfig.getDefaultTypeface());
        btnCancelPurchase.setVisibility(canCancel ? View.VISIBLE : View.GONE);
        btnCancelPurchase.setOnClickListener(v -> {
            if (scorePurchaseDialog != null) {
                scorePurchaseDialog.dismiss();
            }
        });

        btnApplyGift.setOnClickListener(v -> {
            String code = etGiftCode.getText() == null ? "" : etGiftCode.getText().toString().trim();
            if (TextUtils.isEmpty(code)) {
                showMessage(getString(R.string.gift_code_invalid));
                return;
            }
            btnApplyGift.setEnabled(false);
            new Thread(() -> {
                try {
                    GiftCodeApi.GiftResult result = giftCodeApi.applyGiftCode(code);
                    uiHandler.post(() -> {
                        btnApplyGift.setEnabled(true);
                        if (result != null && result.success && result.score > 0) {
                            viewModel.addScore(result.score);
                            if (scorePurchaseDialog != null && scorePurchaseDialog.isShowing()) {
                                scorePurchaseDialog.dismiss();
                            }
                            // پیام بعد از بستن دیالوگ نمایش داده شود تا دیده شود.
                            uiHandler.post(() -> showMessage(getString(R.string.gift_code_success, result.score)));
                        } else {
                            showMessage(getString(R.string.gift_code_invalid));
                        }
                    });
                } catch (Exception e) {
                    uiHandler.post(() -> {
                        btnApplyGift.setEnabled(true);
                        showMessage(getString(R.string.gift_code_error));
                    });
                }
            }).start();
        });

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setCancelable(canCancel)
                .setView(dialogView);

        scorePurchaseDialog = builder.create();
        scorePurchaseDialog.setCanceledOnTouchOutside(canCancel);
        scorePurchaseDialog.setOnDismissListener(dialog -> isScorePurchaseDialogShowing = false);
        scorePurchaseDialog.show();

        fetchScorePlansFromApi(new PlansLoadCallback() {
            @Override
            public void onLoaded(List<BazaarPay.ScorePackPlan> plans) {
                if (isFinishing() || scorePurchaseDialog == null || !scorePurchaseDialog.isShowing()) {
                    return;
                }
                loadingContainer.setVisibility(View.GONE);
                plansContainer.setVisibility(View.VISIBLE);
                scorePackPlans.clear();
                if (plans != null && !plans.isEmpty()) {
                    scorePackPlans.addAll(plans);
                } else {
                    initScorePlans();
                }
                bindPlansToContainer(plansContainer, scorePackPlans);
            }

            @Override
            public void onFailed() {
                if (isFinishing() || scorePurchaseDialog == null || !scorePurchaseDialog.isShowing()) {
                    return;
                }
                loadingContainer.setVisibility(View.GONE);
                plansContainer.setVisibility(View.VISIBLE);
                initScorePlans();
                bindPlansToContainer(plansContainer, scorePackPlans);
                showMessage(getString(R.string.score_purchase_load_failed));
            }
        });
    }

    private void bindPlansToContainer(LinearLayout plansContainer, List<BazaarPay.ScorePackPlan> plans) {
        plansContainer.removeAllViews();
        if (plans == null) {
            return;
        }
        for (BazaarPay.ScorePackPlan plan : plans) {
            View planRow = LayoutInflater.from(this).inflate(R.layout.item_score_plan, plansContainer, false);
            TextView tvPlanScore = planRow.findViewById(R.id.tvPlanScore);
            TextView tvPlanPrice = planRow.findViewById(R.id.tvPlanPrice);
            MaterialButton btnBuyPlan = planRow.findViewById(R.id.btnBuyPlan);

            tvPlanScore.setTypeface(MyConfig.getDefaultTypeface());
            tvPlanPrice.setTypeface(MyConfig.getDefaultTypeface());
            btnBuyPlan.setTypeface(MyConfig.getDefaultTypeface());
            tvPlanScore.setText(getString(R.string.score_purchase_score_format, plan.scoreAmount));
            tvPlanPrice.setText(getString(R.string.score_purchase_price_format, plan.priceToman));
            btnBuyPlan.setOnClickListener(v -> startScorePurchase(plan));
            plansContainer.addView(planRow);
        }
    }

    private interface PlansLoadCallback {
        void onLoaded(List<BazaarPay.ScorePackPlan> plans);
        void onFailed();
    }

    private void fetchScorePlansFromApi(PlansLoadCallback callback) {
        new Thread(() -> {
            try {
                List<BazaarPay.ScorePackPlan> apiPlans = scorePlansApi.fetchPlans();
                if (apiPlans == null || apiPlans.isEmpty()) {
                    uiHandler.post(callback::onFailed);
                    return;
                }
                uiHandler.post(() -> callback.onLoaded(apiPlans));
            } catch (Exception e) {
                uiHandler.post(callback::onFailed);
            }
        }).start();
    }

    private void onLeaderboardClick() {
        String name = getSharedPreferences(LEADERBOARD_PREFS, MODE_PRIVATE).getString(KEY_PLAYER_NAME, "");
        String mobile = getSharedPreferences(LEADERBOARD_PREFS, MODE_PRIVATE).getString(KEY_PLAYER_MOBILE, "");
        // موبایل اختیاری است؛ فقط اگر نام خالی باشد فرم ورودی نمایش داده شود.
        if (TextUtils.isEmpty(name)) {
            showProfileDialogForLeaderboard();
            return;
        }
        showLeaderboardDialog(name, mobile);
    }

    private void showProfileDialogForLeaderboard() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_user_profile, null, false);
        TextView tvTitle = view.findViewById(R.id.tvProfileTitle);
        TextInputEditText etName = view.findViewById(R.id.etPlayerName);
        TextInputEditText etMobile = view.findViewById(R.id.etPlayerMobile);
        tvTitle.setTypeface(MyConfig.getDefaultTypeface());
        etName.setTypeface(MyConfig.getDefaultTypeface());
        etMobile.setTypeface(MyConfig.getDefaultTypeface());

        new MaterialAlertDialogBuilder(this)
                .setView(view)
                .setCancelable(true)
                .setPositiveButton(R.string.leaderboard_save, (dialog, which) -> {
                    String name = etName.getText() == null ? "" : etName.getText().toString().trim();
                    String mobile = etMobile.getText() == null ? "" : etMobile.getText().toString().trim();
                    // موبایل اختیاری است؛ فقط نام اجباری می‌ماند.
                    if (TextUtils.isEmpty(name)) {
                        showMessage(getString(R.string.leaderboard_name_hint));
                        return;
                    }
                    getSharedPreferences(LEADERBOARD_PREFS, MODE_PRIVATE)
                            .edit()
                            .putString(KEY_PLAYER_NAME, name)
                            .putString(KEY_PLAYER_MOBILE, mobile)
                            .apply();
                    showLeaderboardDialog(name, mobile);
                })
                .setNegativeButton(R.string.game_help_dialog_close, null)
                .show();
    }

    private void showLeaderboardDialog(String name, String mobile) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_leaderboard, null, false);
        TextView tvTitle = view.findViewById(R.id.tvLeaderboardTitle);
        TextView tvLoading = view.findViewById(R.id.tvLeaderboardLoading);
        LinearLayout loadingContainer = view.findViewById(R.id.loadingLeaderboardContainer);
        ScrollView scroll = view.findViewById(R.id.leaderboardScroll);
        LinearLayout rows = view.findViewById(R.id.leaderboardRows);
        TextView tvMyRankTitle = view.findViewById(R.id.tvMyRankTitle);
        LinearLayout myRankRow = view.findViewById(R.id.myRankRow);
        TextView tvMyRankName = view.findViewById(R.id.tvMyRankName);
        TextView tvMyRankLevel = view.findViewById(R.id.tvMyRankLevel);
        TextView tvMyRankScore = view.findViewById(R.id.tvMyRankScore);

        tvTitle.setTypeface(MyConfig.getDefaultTypeface());
        tvLoading.setTypeface(MyConfig.getDefaultTypeface());
        tvMyRankTitle.setTypeface(MyConfig.getDefaultTypeface());
        tvMyRankName.setTypeface(MyConfig.getDefaultTypeface());
        tvMyRankLevel.setTypeface(MyConfig.getDefaultTypeface());
        tvMyRankScore.setTypeface(MyConfig.getDefaultTypeface());

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(view)
                .setCancelable(true)
                .setNegativeButton(R.string.game_help_dialog_close, null)
                .create();
        dialog.show();

        loadingContainer.setVisibility(View.VISIBLE);
        scroll.setVisibility(View.GONE);
        tvMyRankTitle.setVisibility(View.GONE);
        myRankRow.setVisibility(View.GONE);
        rows.removeAllViews();

        int level = currentLevelId > 0 ? currentLevelId : 1;
        int score = viewModel.getCurrentScore();
        String serverId = getSharedPreferences(LEADERBOARD_PREFS, MODE_PRIVATE).getString(KEY_PLAYER_SERVER_ID, "");

        new Thread(() -> {
            try {
                LeaderboardResult result = leaderboardApi.submitAndFetch(name, mobile, level, score, serverId);
                uiHandler.post(() -> {
                    if (isFinishing() || !dialog.isShowing()) {
                        return;
                    }
                    loadingContainer.setVisibility(View.GONE);
                    scroll.setVisibility(View.VISIBLE);

                    int rankCounter = 1;
                    for (LeaderboardEntry entry : result.topEntries) {
                        rows.addView(buildLeaderboardRow(entry.name, entry.level, entry.score, rankCounter));
                        rankCounter++;
                    }

                    if (result.selfEntry != null) {
                        if (!TextUtils.isEmpty(result.selfEntry.id)) {
                            getSharedPreferences(LEADERBOARD_PREFS, MODE_PRIVATE)
                                    .edit()
                                    .putString(KEY_PLAYER_SERVER_ID, result.selfEntry.id)
                                    .apply();
                        } else if (!TextUtils.isEmpty(serverId)) {
                            // اگر پاسخ id نداد، id قبلی حذف نشود.
                            getSharedPreferences(LEADERBOARD_PREFS, MODE_PRIVATE)
                                    .edit()
                                    .putString(KEY_PLAYER_SERVER_ID, serverId)
                                    .apply();
                        }
                        tvMyRankTitle.setVisibility(View.VISIBLE);
                        myRankRow.setVisibility(View.VISIBLE);
                        tvMyRankName.setText(result.selfEntry.rank + ". " + result.selfEntry.name);
                        tvMyRankLevel.setText(String.valueOf(result.selfEntry.level));
                        tvMyRankScore.setText(String.valueOf(result.selfEntry.score));
                    }
                });
            } catch (Exception e) {
                uiHandler.post(() -> {
                    if (isFinishing() || !dialog.isShowing()) {
                        return;
                    }
                    loadingContainer.setVisibility(View.GONE);
                    showMessage(getString(R.string.leaderboard_error));
                });
            }
        }).start();
    }

    private View buildLeaderboardRow(String name, int level, int score, int rank) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        row.setPadding(dp(8), dp(8), dp(8), dp(8));
        row.setBackgroundResource(R.drawable.purchase_benefits_bg);
        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        rowLp.bottomMargin = dp(6);
        row.setLayoutParams(rowLp);

        TextView tvRank = new TextView(this);
        tvRank.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.55f));
        tvRank.setTypeface(MyConfig.getDefaultTypeface());
        int goldRankColor = 0xFFB8860B;
        tvRank.setTextColor(rank == 1 ? goldRankColor : colorText);
        tvRank.setGravity(Gravity.CENTER);
        tvRank.setText(rank == 1 ? "★ " + rank : String.valueOf(rank));
        tvRank.setTextSize(rank == 1 ? 18f : 15f);

        TextView tvName = new TextView(this);
        tvName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.15f));
        tvName.setTypeface(MyConfig.getDefaultTypeface());
        tvName.setTextColor(rank == 1 ? goldRankColor : colorText);
        tvName.setText(name);

        TextView tvLevel = new TextView(this);
        tvLevel.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.8f));
        tvLevel.setTypeface(MyConfig.getDefaultTypeface());
        tvLevel.setTextColor(colorText);
        tvLevel.setGravity(Gravity.CENTER);
        tvLevel.setText(String.valueOf(level));

        TextView tvScoreCell = new TextView(this);
        tvScoreCell.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.8f));
        tvScoreCell.setTypeface(MyConfig.getDefaultTypeface());
        tvScoreCell.setTextColor(colorText);
        tvScoreCell.setGravity(Gravity.CENTER);
        tvScoreCell.setText(String.valueOf(score));

        row.addView(tvRank);
        row.addView(tvName);
        row.addView(tvLevel);
        row.addView(tvScoreCell);
        return row;
    }

    private void syncLeaderboardSilentlyIfRegistered(int nextLevel) {
        String serverId = getSharedPreferences(LEADERBOARD_PREFS, MODE_PRIVATE).getString(KEY_PLAYER_SERVER_ID, "");
        if (TextUtils.isEmpty(serverId)) {
            return;
        }
        String name = getSharedPreferences(LEADERBOARD_PREFS, MODE_PRIVATE).getString(KEY_PLAYER_NAME, "");
        String mobile = getSharedPreferences(LEADERBOARD_PREFS, MODE_PRIVATE).getString(KEY_PLAYER_MOBILE, "");
        int score = getSharedPreferences(SCORE_PREFS_NAME, MODE_PRIVATE).getInt("score", INIT_DEFAULT_SCORE);
        final int levelForSubmit = Math.max(1, nextLevel);

        new Thread(() -> {
            try {
                leaderboardApi.submitOnly(name, mobile, levelForSubmit, score, serverId);
            } catch (Exception ignored) {
                // بی‌صدا: کاربر نباید loading/پیام ببیند.
            }
        }).start();
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
                    showScorePurchaseDialogIfNeeded(getSharedPreferences(SCORE_PREFS_NAME, MODE_PRIVATE).getInt("score", INIT_DEFAULT_SCORE));
                });
            }

            @Override
            public void onScorePurchaseFailed(String message) {
                runOnUiThread(() -> {
                    isScorePurchaseInProgress = false;
                    showMessage(message);
                    isScorePurchaseDialogShowing = false;
                    showScorePurchaseDialogIfNeeded(getSharedPreferences(SCORE_PREFS_NAME, MODE_PRIVATE).getInt("score", INIT_DEFAULT_SCORE));
                });
            }
        });
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
        int current = getSharedPreferences(SCORE_PREFS_NAME, MODE_PRIVATE).getInt("score", INIT_DEFAULT_SCORE);
        if (current <= 0) {
            isScoreLocked = true;
            handleScoreLock(current);
            showScorePurchaseDialog(true, current);
        } else {
            handleScoreLock(current);
        }
        viewModel.loadScore();
    }
}
