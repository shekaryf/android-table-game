package ir.baran.bookPack;

import android.app.AlertDialog;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ir.baran.bookPack.game.domain.model.CellState;
import ir.baran.bookPack.game.domain.model.GameBoard;
import ir.baran.bookPack.game.domain.model.GameCell;
import ir.baran.bookPack.game.presentation.GameViewModel;
import ir.baran.framework.forms.Form;
import ir.baran.framework.utilities.Functions;
import ir.baran.framework.utilities.MyConfig;

/**
 * صفحه اصلی بازی: نمایش جدول، مدیریت کلیک‌ها، نمایش راهنماها و زوم/اسکرول.
 */
public class GamePage extends Form {

    public static final String EXTRA_LEVEL_ID = "level_id";

    private static final int COLOR_BG_PAGE = 0xFFF6F5F2;
    private static final int COLOR_CELL_MOVABLE = 0xFFFFFFFF;
    private static final int COLOR_CELL_SELECTED = 0xFFFFE082;
    private static final int COLOR_CELL_LOCKED = 0xFF81C784;
    private static final int COLOR_CELL_BLOCKED = 0xFFECEFF1;
    private static final int COLOR_TEXT = 0xFF1F2933;
    private static final int COLOR_SUBTEXT = 0xFF52606D;

    private GameViewModel viewModel;
    private GridLayout gridLayout;
    private TextView tvTitle;
    private TextView tvHint;
    private LinearLayout boardWrapper;
    private HorizontalScrollView horizontalScrollView;
    private ScrollView verticalScrollView;

    private float zoomFactor = 1f;
    private ScaleGestureDetector scaleDetector;
    private final Map<String, Drawable> arrowCache = new HashMap<>();
    private GameBoard currentBoard;
    private int currentLevelId = -1;
    private int winHandledLevelId = -1;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(GameViewModel.class);
        subscribeToViewModel();
        viewModel.validateAllLevels();

        int levelId = getIntent() != null ? getIntent().getIntExtra(EXTRA_LEVEL_ID, 1) : 1;
        viewModel.loadLevel(levelId);
    }

    @Override
    public void initContent(LinearLayout llContent) {
        MyConfig._FirstForm = this;
        llContent.setBackgroundColor(COLOR_BG_PAGE);
        llContent.setOrientation(LinearLayout.VERTICAL);
        llContent.setPadding(dp(12), dp(12), dp(12), dp(12));
        llContent.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);

        tvTitle = new TextView(this);
        tvTitle.setText("Scramble & Swap");
        tvTitle.setTextColor(COLOR_TEXT);
        tvTitle.setTypeface(Typeface.DEFAULT_BOLD);
        tvTitle.setTextSize(22f);
        tvTitle.setGravity(Gravity.CENTER_HORIZONTAL);
        llContent.addView(tvTitle, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        tvHint = new TextView(this);
        tvHint.setText("دو حرف را انتخاب کن تا جابه‌جا شوند. بزرگنمایی با دو انگشت فعال است.");
        tvHint.setTextColor(COLOR_SUBTEXT);
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
        boardWrapper.addView(gridLayout, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

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
                showMessage("تبریک! مرحله " + finishedLevel + " کامل شد. ورود به مرحله بعد...");

                uiHandler.postDelayed(() -> {
                    int nextLevel = finishedLevel + 1;
                    viewModel.loadLevel(nextLevel);
                    showMessage("مرحله " + nextLevel + " شروع شد.");
                }, 900);
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
            String first = errors.get(0);
            showMessage("خطای داده مرحله: " + first);
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
        tvTitle.setText("Level " + board.getLevelId());

        Map<String, List<ClueItem>> clueMap = parseCluesByAnchor(board.getCluesDataJson());

        gridLayout.removeAllViews();
        gridLayout.setRowCount(rowCount);
        gridLayout.setColumnCount(colCount);

        // اندازه هر خانه بر اساس عرض صفحه و سطح زوم محاسبه می‌شود
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
            return buildClueCell(cell, cellSize, cluesAtCell);
        }

        TextView tv = new TextView(this);
        tv.setGravity(Gravity.CENTER);
        tv.setTextColor(cell.getState() == CellState.BLOCKED ? COLOR_SUBTEXT : COLOR_TEXT);
        tv.setTypeface(cell.getState() == CellState.BLOCKED ? Typeface.DEFAULT : Typeface.DEFAULT_BOLD);
        tv.setTextSize(cell.getState() == CellState.BLOCKED ? 12f : 18f);
        tv.setText(safeLetter(cell.getLetter()));

        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = cellSize;
        lp.height = cellSize;
        tv.setLayoutParams(lp);
        tv.setBackground(createCellBackground(cell.getState()));

        if (cell.getState() == CellState.MOVABLE || cell.getState() == CellState.SELECTED) {
            tv.setOnClickListener(v -> {
                runTapAnimation(v);
                viewModel.onCellTapped(cell.getRow(), cell.getCol());
            });
        }

        return tv;
    }

    private View buildClueCell(GameCell cell, int cellSize, List<ClueItem> cluesAtCell) {
        // سلول راهنما: نمایش متن کوتاه + فلش جهت، و نمایش متن کامل با کلیک
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_VERTICAL);
        root.setPadding(dp(4), dp(4), dp(4), dp(4));

        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = cellSize;
        lp.height = cellSize;
        root.setLayoutParams(lp);
        root.setBackground(createCellBackground(CellState.BLOCKED));

        int count = Math.min(2, cluesAtCell.size());
        for (int i = 0; i < count; i++) {
            ClueItem clue = cluesAtCell.get(i);
            LinearLayout line = new LinearLayout(this);
            line.setOrientation(LinearLayout.HORIZONTAL);
            line.setGravity(Gravity.CENTER_VERTICAL);

            TextView tv = new TextView(this);
            tv.setTextColor(COLOR_SUBTEXT);
            tv.setTextSize(10f);
            tv.setSingleLine(true);
            tv.setEllipsize(TextUtils.TruncateAt.END);
            tv.setText(clue.clue);

            LinearLayout.LayoutParams textLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            line.addView(tv, textLp);

            Drawable arrow = getArrowDrawable(clue.direction);
            if (arrow != null) {
                ImageView iv = new ImageView(this);
                iv.setImageDrawable(arrow);
                LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(dp(10), dp(10));
                iconLp.leftMargin = dp(3);
                line.addView(iv, iconLp);
            }

            root.addView(line, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    1f
            ));
        }

        root.setOnClickListener(v -> showFullCluesDialog(cluesAtCell));
        return root;
    }

    private void showFullCluesDialog(List<ClueItem> cluesAtCell) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cluesAtCell.size(); i++) {
            ClueItem clue = cluesAtCell.get(i);
            if (i > 0) {
                sb.append("\n\n");
            }
            sb.append(clue.clue);
            if (!TextUtils.isEmpty(clue.direction)) {
                sb.append(" ( ").append(clue.direction).append(" )");
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("متن کامل راهنما")
                .setMessage(sb.toString())
                .setPositiveButton("بستن", null)
                .show();
    }

    private Map<String, List<ClueItem>> parseCluesByAnchor(String cluesJson) {
        // گروه‌بندی راهنماها بر اساس مختصات سلول لنگر (row/col)
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
                if (row < 0 || col < 0 || clue.isEmpty()) {
                    continue;
                }
                String key = key(row, col);
                List<ClueItem> list = map.get(key);
                if (list == null) {
                    list = new ArrayList<>();
                    map.put(key, list);
                }
                list.add(new ClueItem(clue, direction));
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
            case "right":
                fileName = "right.png";
                break;
            case "up":
                fileName = "up.png";
                break;
            case "down":
                fileName = "down.png";
                break;
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
            case "down_right":
            case "right_down":
                fileName = "down_right.png";
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
            fillColor = COLOR_CELL_BLOCKED;
            strokeColor = 0xFFB0BEC5;
        } else if (state == CellState.SELECTED) {
            fillColor = COLOR_CELL_SELECTED;
            strokeColor = 0xFFF9A825;
        } else if (state == CellState.LOCKED) {
            fillColor = COLOR_CELL_LOCKED;
            strokeColor = 0xFF2E7D32;
        } else {
            fillColor = COLOR_CELL_MOVABLE;
            strokeColor = 0xFFD7CCC8;
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

    private String safeLetter(String letter) {
        return letter == null ? "" : letter.trim();
    }

    private String key(int row, int col) {
        return row + "_" + col;
    }

    private static class ClueItem {
        final String clue;
        final String direction;

        ClueItem(String clue, String direction) {
            this.clue = clue;
            this.direction = direction;
        }
    }
}
