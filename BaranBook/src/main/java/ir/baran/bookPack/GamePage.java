package ir.baran.bookPack;

import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ir.baran.bookPack.game.domain.model.CellState;
import ir.baran.bookPack.game.domain.model.GameBoard;
import ir.baran.bookPack.game.domain.model.GameCell;
import ir.baran.bookPack.game.presentation.GameViewModel;
import ir.baran.framework.forms.Form;
import ir.baran.framework.utilities.Functions;
import ir.baran.framework.utilities.MyConfig;

public class GamePage extends Form {

    public static final String EXTRA_LEVEL_ID = "level_id";

    private static final int COLOR_BG_PAGE = 0xFFF6F5F2;
    private static final int COLOR_CELL_MOVABLE = 0xFFFFFFFF;
    private static final int COLOR_CELL_SELECTED = 0xFFFFE082;
    private static final int COLOR_CELL_LOCKED = 0xFF81C784;
    private static final int COLOR_CELL_BLOCKED = 0xFFCFD8DC;
    private static final int COLOR_TEXT = 0xFF1F2933;
    private static final int COLOR_SUBTEXT = 0xFF52606D;

    private GameViewModel viewModel;
    private GridLayout gridLayout;
    private TextView tvTitle;
    private TextView tvHint;
    private TextView tvClues;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyConfig._FirstForm = this;
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

        tvTitle = new TextView(this);
        tvTitle.setText("Scramble & Swap");
        tvTitle.setTextColor(COLOR_TEXT);
        tvTitle.setTypeface(Typeface.DEFAULT_BOLD);
        tvTitle.setTextSize(22f);
        tvTitle.setPadding(0, 0, 0, dp(6));
        llContent.addView(tvTitle, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        tvHint = new TextView(this);
        tvHint.setText("Select two movable letters to swap. Locked letters are green.");
        tvHint.setTextColor(COLOR_SUBTEXT);
        tvHint.setTextSize(14f);
        tvHint.setPadding(0, 0, 0, dp(10));
        llContent.addView(tvHint, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        tvClues = new TextView(this);
        tvClues.setTextColor(COLOR_SUBTEXT);
        tvClues.setTextSize(13f);
        tvClues.setPadding(dp(10), dp(8), dp(10), dp(8));
        tvClues.setBackground(createCluesBackground());
        LinearLayout.LayoutParams cluesLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cluesLp.bottomMargin = dp(12);
        llContent.addView(tvClues, cluesLp);

        gridLayout = new GridLayout(this);
        gridLayout.setUseDefaultMargins(true);
        llContent.addView(gridLayout, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
    }

    private void subscribeToViewModel() {
        viewModel.getBoardLiveData().observe(this, this::renderBoard);

        viewModel.getWinLiveData().observe(this, isWin -> {
            if (Boolean.TRUE.equals(isWin)) {
                showMessage("Level completed! +20 coins");
            }
        });

        viewModel.getErrorLiveData().observe(this, error -> {
            if (error != null && !error.trim().isEmpty()) {
                showMessage(error);
            }
        });

        viewModel.getValidationErrorsLiveData().observe(this, errors -> {
            if (errors == null) {
                return;
            }
            if (errors.isEmpty()) {
                showMessage("Level data validation passed.");
                return;
            }

            StringBuilder sb = new StringBuilder("Level data issues: ");
            int count = Math.min(3, errors.size());
            for (int i = 0; i < count; i++) {
                if (i > 0) {
                    sb.append(" | ");
                }
                sb.append(errors.get(i));
            }
            if (errors.size() > count) {
                sb.append(" ... +").append(errors.size() - count).append(" more");
            }
            showMessage(sb.toString());
        });
    }

    private void renderBoard(GameBoard board) {
        if (board == null || board.getCells() == null) {
            return;
        }

        List<List<GameCell>> rows = board.getCells();
        int rowCount = rows.size();
        int colCount = rowCount > 0 ? rows.get(0).size() : 0;

        tvTitle.setText("Scramble & Swap - Level " + board.getLevelId());
        tvClues.setText(buildCluesText(board.getCluesDataJson()));

        gridLayout.removeAllViews();
        gridLayout.setRowCount(rowCount);
        gridLayout.setColumnCount(colCount);

        int cellSize = calculateCellSize(colCount);

        for (int r = 0; r < rowCount; r++) {
            List<GameCell> row = rows.get(r);
            for (int c = 0; c < colCount; c++) {
                GameCell cell = row.get(c);
                TextView cellView = buildCellView(cell, cellSize);
                gridLayout.addView(cellView);
            }
        }
    }

    private TextView buildCellView(GameCell cell, int cellSize) {
        TextView tv = new TextView(this);
        tv.setGravity(Gravity.CENTER);
        if (cell.getState() == CellState.BLOCKED) {
            tv.setTextColor(COLOR_SUBTEXT);
            tv.setTypeface(Typeface.DEFAULT);
            tv.setTextSize(12f);
            tv.setText(safeLetter(cell.getLetter()));
        } else {
            tv.setTextColor(COLOR_TEXT);
            tv.setTypeface(Typeface.DEFAULT_BOLD);
            tv.setTextSize(18f);
            tv.setText(safeLetter(cell.getLetter()));
        }

        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = cellSize;
        lp.height = cellSize;
        tv.setLayoutParams(lp);

        tv.setBackground(createCellBackground(cell.getState()));

        if (cell.getState() == CellState.MOVABLE || cell.getState() == CellState.SELECTED) {
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    runTapAnimation(v);
                    viewModel.onCellTapped(cell.getRow(), cell.getCol());
                }
            });
        }

        return tv;
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

    private GradientDrawable createCluesBackground() {
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(dp(12));
        shape.setColor(0xFFFFFFFF);
        shape.setStroke(dp(1), 0xFFD9E2EC);
        return shape;
    }

    private String buildCluesText(String cluesJson) {
        if (cluesJson == null || cluesJson.trim().isEmpty()) {
            return "Clues: -";
        }

        try {
            JSONArray array = new JSONArray(cluesJson);
            if (array.length() == 0) {
                return "Clues: -";
            }

            List<String> lines = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject clueObj = array.optJSONObject(i);
                if (clueObj == null) {
                    continue;
                }
                String clue = clueObj.optString("clue", "").trim();
                int row = clueObj.optInt("row", -1);
                int col = clueObj.optInt("col", -1);
                String direction = clueObj.optString("direction", "").trim();

                if (!clue.isEmpty()) {
                    String meta = "(" + row + "," + col + ")";
                    if (!direction.isEmpty()) {
                        meta = meta + " " + direction;
                    }
                    lines.add((i + 1) + ". " + clue + " " + meta);
                }
            }

            if (lines.isEmpty()) {
                return "Clues: -";
            }

            StringBuilder sb = new StringBuilder("Clues:\n");
            for (String line : lines) {
                sb.append(line).append("\n");
            }
            return sb.toString().trim();

        } catch (Exception e) {
            return "Clues: unavailable";
        }
    }

    private int calculateCellSize(int colCount) {
        if (colCount <= 0) {
            return dp(42);
        }

        int totalHorizontalPadding = dp(24);
        int totalWidth = getScreenWidth() - totalHorizontalPadding;
        int maxSizeByWidth = totalWidth / colCount;
        int maxCap = dp(56);
        int minCap = dp(36);

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
        if (letter == null) {
            return "";
        }
        return letter.trim();
    }
}
