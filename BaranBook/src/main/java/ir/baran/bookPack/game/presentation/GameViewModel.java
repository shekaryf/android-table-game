package ir.baran.bookPack.game.presentation;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ir.baran.bookPack.game.data.model.LevelEntity;
import ir.baran.bookPack.game.data.repository.GameRepository;
import ir.baran.bookPack.game.domain.model.CellState;
import ir.baran.bookPack.game.domain.model.GameBoard;
import ir.baran.bookPack.game.domain.model.GameCell;

/**
 * منطق اصلی بازی: بارگذاری مرحله، درهم‌سازی، جابه‌جایی، قفل صحیح‌ها و برد.
 */
public class GameViewModel extends AndroidViewModel {

    private static final String BLOCK_TOKEN = "*";
    private static final String BLOCK_TOKEN_ALT = "#";
    private static final String CLUE_PREFIX = "C:";
    private static final String LETTER_PREFIX = "L:";

    private final GameRepository repository;
    private final MutableLiveData<GameBoard> boardLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> winLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<String>> validationErrorsLiveData = new MutableLiveData<>();

    private LiveData<LevelEntity> activeLevelLiveData;
    private final Observer<LevelEntity> levelObserver = this::handleLevelLoaded;

    private int activeLevelId = -1;
    private int rows = 0;
    private int cols = 0;

    private String[][] answerGrid;
    private String[][] currentGrid;
    private boolean[][] blockedCells;
    private boolean[][] lockedCells;
    private String cluesDataJson;

    private int selectedRow = -1;
    private int selectedCol = -1;

    private static class ParsedGrid {
        final String[][] answerGrid;
        final boolean[][] nonPlayableCells;

        ParsedGrid(String[][] answerGrid, boolean[][] nonPlayableCells) {
            this.answerGrid = answerGrid;
            this.nonPlayableCells = nonPlayableCells;
        }
    }

    public GameViewModel(@NonNull Application application) {
        super(application);
        repository = new GameRepository(application.getApplicationContext());
    }

    public LiveData<GameBoard> getBoardLiveData() {
        return boardLiveData;
    }

    public LiveData<Boolean> getWinLiveData() {
        return winLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public LiveData<List<String>> getValidationErrorsLiveData() {
        return validationErrorsLiveData;
    }

    public void validateAllLevels() {
        repository.validateAllLevelsAsync(errors -> validationErrorsLiveData.postValue(errors));
    }

    public void loadLevel(int levelId) {
        // شروع/تعویض مرحله جاری و ریست وضعیت برد
        winLiveData.setValue(false);
        clearSelection();

        if (activeLevelLiveData != null) {
            activeLevelLiveData.removeObserver(levelObserver);
        }

        activeLevelLiveData = repository.observeLevel(levelId);
        activeLevelLiveData.observeForever(levelObserver);
    }

    public void onCellTapped(int row, int col) {
        // فقط سلول‌های قابل‌جابجایی پردازش می‌شوند
        if (!isInsideGrid(row, col) || blockedCells == null || blockedCells[row][col] || lockedCells[row][col]) {
            return;
        }

        if (selectedRow == -1) {
            selectedRow = row;
            selectedCol = col;
            publishBoard();
            return;
        }

        if (selectedRow == row && selectedCol == col) {
            clearSelection();
            publishBoard();
            return;
        }

        if (lockedCells[selectedRow][selectedCol] || blockedCells[selectedRow][selectedCol]) {
            clearSelection();
            publishBoard();
            return;
        }

        swapCells(selectedRow, selectedCol, row, col);
        clearSelection();
        updateLockedCellsByCorrectLetters();
        publishBoard();

        if (isWin()) {
            winLiveData.setValue(true);
            repository.completeLevelAndRewardUser(activeLevelId);
        }
    }

    private void handleLevelLoaded(LevelEntity levelEntity) {
        if (levelEntity == null) {
            errorLiveData.postValue("Level not found.");
            return;
        }
        if (levelEntity.getId() == null) {
            errorLiveData.postValue("Level id is null.");
            return;
        }
        activeLevelId = levelEntity.getId();
        initializeBoardFromLevel(levelEntity);
    }

    private void initializeBoardFromLevel(LevelEntity level) {
        // خواندن جواب صحیح از grid_data و ساخت حالت اولیه مرحله
        if (TextUtils.isEmpty(level.getGridData())) {
            errorLiveData.postValue("Level grid_data is empty.");
            return;
        }

        try {
            ParsedGrid parsed = parseGrid(level.getGridData(), level.getGridRows(), level.getGridCols());
            answerGrid = parsed.answerGrid;
            blockedCells = parsed.nonPlayableCells;
        } catch (JSONException e) {
            errorLiveData.postValue("Invalid grid_data JSON.");
            return;
        }

        cluesDataJson = level.getCluesData();

        rows = answerGrid.length;
        cols = rows > 0 ? answerGrid[0].length : 0;
        currentGrid = new String[rows][cols];
        lockedCells = new boolean[rows][cols];

        scrambleMovableLetters();
        updateLockedCellsByCorrectLetters();
        publishBoard();
    }

    private ParsedGrid parseGrid(String gridDataJson, int expectedRows, int expectedCols) throws JSONException {
        JSONArray rowsArray = new JSONArray(gridDataJson);
        int parsedRows = rowsArray.length();
        if (parsedRows == 0) {
            return new ParsedGrid(new String[0][0], new boolean[0][0]);
        }

        int parsedCols = rowsArray.getJSONArray(0).length();
        if (expectedRows > 0 && expectedRows != parsedRows) {
            throw new JSONException("grid_rows mismatch. expected=" + expectedRows + " actual=" + parsedRows);
        }
        if (expectedCols > 0 && expectedCols != parsedCols) {
            throw new JSONException("grid_cols mismatch. expected=" + expectedCols + " actual=" + parsedCols);
        }

        String[][] letters = new String[parsedRows][parsedCols];
        boolean[][] nonPlayable = new boolean[parsedRows][parsedCols];

        for (int r = 0; r < parsedRows; r++) {
            JSONArray rowArray = rowsArray.getJSONArray(r);
            if (rowArray.length() != parsedCols) {
                throw new JSONException("All rows must have equal columns");
            }
            for (int c = 0; c < parsedCols; c++) {
                Object raw = rowArray.get(c);
                ParsedCell cell = parseCell(raw);
                letters[r][c] = cell.value;
                nonPlayable[r][c] = !cell.playable;
            }
        }

        return new ParsedGrid(letters, nonPlayable);
    }

    private ParsedCell parseCell(Object raw) throws JSONException {
        if (raw == null || raw == JSONObject.NULL) {
            return new ParsedCell("", false);
        }

        if (raw instanceof JSONObject) {
            JSONObject obj = (JSONObject) raw;
            String type = obj.optString("type", "letter").trim().toLowerCase();
            String value = obj.optString("value", "").trim();
            if ("block".equals(type) || "blocked".equals(type)) {
                return new ParsedCell("", false);
            }
            if ("clue".equals(type) || "hint".equals(type)) {
                return new ParsedCell(value, false);
            }
            return new ParsedCell(normalizeLetter(value), true);
        }

        String token = String.valueOf(raw).trim();
        if (token.isEmpty() || BLOCK_TOKEN.equals(token) || BLOCK_TOKEN_ALT.equals(token)) {
            return new ParsedCell("", false);
        }
        if (token.startsWith(CLUE_PREFIX)) {
            return new ParsedCell(token.substring(CLUE_PREFIX.length()).trim(), false);
        }
        if (token.startsWith(LETTER_PREFIX)) {
            return new ParsedCell(normalizeLetter(token.substring(LETTER_PREFIX.length())), true);
        }
        return new ParsedCell(normalizeLetter(token), true);
    }

    private String normalizeLetter(String value) {
        return value == null ? "" : value.trim();
    }

    private static class ParsedCell {
        final String value;
        final boolean playable;

        ParsedCell(String value, boolean playable) {
            this.value = value;
            this.playable = playable;
        }
    }

    private void scrambleMovableLetters() {
        // حروف قابل بازی جمع‌آوری و به صورت تصادفی بین خانه‌های قابل‌جابجایی پخش می‌شوند
        List<String> movableLetters = new ArrayList<>();
        List<int[]> movablePositions = new ArrayList<>();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                String answer = answerGrid[r][c];
                if (blockedCells[r][c]) {
                    currentGrid[r][c] = answer;
                } else {
                    movableLetters.add(answer);
                    movablePositions.add(new int[]{r, c});
                }
            }
        }

        if (movablePositions.size() <= 1) {
            for (int i = 0; i < movablePositions.size(); i++) {
                int[] pos = movablePositions.get(i);
                currentGrid[pos[0]][pos[1]] = movableLetters.get(i);
            }
            return;
        }

        // Try to create a board where no movable cell starts with its correct letter.
        int attempts = 0;
        boolean validShuffle = false;
        while (attempts < 200 && !validShuffle) {
            Collections.shuffle(movableLetters);
            validShuffle = true;
            for (int i = 0; i < movablePositions.size(); i++) {
                int[] pos = movablePositions.get(i);
                if (safeEquals(movableLetters.get(i), answerGrid[pos[0]][pos[1]])) {
                    validShuffle = false;
                    break;
                }
            }
            attempts++;
        }

        for (int i = 0; i < movablePositions.size(); i++) {
            int[] pos = movablePositions.get(i);
            currentGrid[pos[0]][pos[1]] = movableLetters.get(i);
        }

        // Fallback fix for duplicate-letter cases: reduce fixed points as much as possible.
        for (int i = 0; i < movablePositions.size(); i++) {
            int[] a = movablePositions.get(i);
            if (!safeEquals(currentGrid[a[0]][a[1]], answerGrid[a[0]][a[1]])) {
                continue;
            }
            for (int j = i + 1; j < movablePositions.size(); j++) {
                int[] b = movablePositions.get(j);
                String aLetter = currentGrid[a[0]][a[1]];
                String bLetter = currentGrid[b[0]][b[1]];
                boolean aWouldBeCorrect = safeEquals(bLetter, answerGrid[a[0]][a[1]]);
                boolean bWouldBeCorrect = safeEquals(aLetter, answerGrid[b[0]][b[1]]);
                if (!aWouldBeCorrect && !bWouldBeCorrect) {
                    currentGrid[a[0]][a[1]] = bLetter;
                    currentGrid[b[0]][b[1]] = aLetter;
                    break;
                }
            }
        }
    }

    private void updateLockedCellsByCorrectLetters() {
        // بعد از هر حرکت، حروفی که دقیقاً در جای صحیح هستند قفل می‌شوند
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (blockedCells[r][c] || lockedCells[r][c]) {
                    continue;
                }
                if (safeEquals(currentGrid[r][c], answerGrid[r][c])) {
                    lockedCells[r][c] = true;
                }
            }
        }
    }

    private boolean isWin() {
        // برد زمانی است که تمام خانه‌های قابل بازی با جواب برابر شوند
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (blockedCells[r][c]) {
                    continue;
                }
                if (!safeEquals(currentGrid[r][c], answerGrid[r][c])) {
                    return false;
                }
            }
        }
        return true;
    }

    private void swapCells(int r1, int c1, int r2, int c2) {
        String tmp = currentGrid[r1][c1];
        currentGrid[r1][c1] = currentGrid[r2][c2];
        currentGrid[r2][c2] = tmp;
    }

    private void publishBoard() {
        List<List<GameCell>> rowsList = new ArrayList<>();

        for (int r = 0; r < rows; r++) {
            List<GameCell> rowCells = new ArrayList<>();
            for (int c = 0; c < cols; c++) {
                CellState state;
                if (blockedCells[r][c]) {
                    state = CellState.BLOCKED;
                } else if (selectedRow == r && selectedCol == c) {
                    state = CellState.SELECTED;
                } else if (lockedCells[r][c]) {
                    state = CellState.LOCKED;
                } else {
                    state = CellState.MOVABLE;
                }

                rowCells.add(new GameCell(r, c, currentGrid[r][c], state));
            }
            rowsList.add(rowCells);
        }

        boardLiveData.setValue(new GameBoard(activeLevelId, rows, cols, rowsList, cluesDataJson));
    }

    private void clearSelection() {
        selectedRow = -1;
        selectedCol = -1;
    }

    private boolean isInsideGrid(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }


    private boolean safeEquals(String a, String b) {
        return (a == null && b == null) || (a != null && a.equals(b));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (activeLevelLiveData != null) {
            activeLevelLiveData.removeObserver(levelObserver);
        }
    }
}
