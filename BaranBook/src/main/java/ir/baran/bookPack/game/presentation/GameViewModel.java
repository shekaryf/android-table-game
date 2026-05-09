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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ir.baran.bookPack.game.data.model.LevelEntity;
import ir.baran.bookPack.game.data.repository.GameRepository;
import ir.baran.bookPack.game.domain.model.CellState;
import ir.baran.bookPack.game.domain.model.GameBoard;
import ir.baran.bookPack.game.domain.model.GameCell;

public class GameViewModel extends AndroidViewModel {

    private static final String BLOCK_TOKEN = "*";

    private final GameRepository repository;
    private final MutableLiveData<GameBoard> boardLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> winLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

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

    public void loadLevel(int levelId) {
        winLiveData.setValue(false);
        clearSelection();

        if (activeLevelLiveData != null) {
            activeLevelLiveData.removeObserver(levelObserver);
        }

        activeLevelLiveData = repository.observeLevel(levelId);
        activeLevelLiveData.observeForever(levelObserver);
    }

    public void onCellTapped(int row, int col) {
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
        validateAndLockLines();
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
        activeLevelId = levelEntity.getId();
        initializeBoardFromLevel(levelEntity);
    }

    private void initializeBoardFromLevel(LevelEntity level) {
        if (TextUtils.isEmpty(level.getGridData())) {
            errorLiveData.postValue("Level grid_data is empty.");
            return;
        }

        try {
            answerGrid = parseGrid(level.getGridData());
        } catch (JSONException e) {
            errorLiveData.postValue("Invalid grid_data JSON.");
            return;
        }

        cluesDataJson = level.getCluesData();

        rows = answerGrid.length;
        cols = rows > 0 ? answerGrid[0].length : 0;
        currentGrid = new String[rows][cols];
        blockedCells = new boolean[rows][cols];
        lockedCells = new boolean[rows][cols];

        scrambleMovableLetters();
        validateAndLockLines();
        publishBoard();
    }

    private String[][] parseGrid(String gridDataJson) throws JSONException {
        JSONArray rowsArray = new JSONArray(gridDataJson);
        int parsedRows = rowsArray.length();
        if (parsedRows == 0) {
            return new String[0][0];
        }

        int parsedCols = rowsArray.getJSONArray(0).length();
        String[][] result = new String[parsedRows][parsedCols];

        for (int r = 0; r < parsedRows; r++) {
            JSONArray rowArray = rowsArray.getJSONArray(r);
            for (int c = 0; c < parsedCols; c++) {
                String token = rowArray.optString(c, "").trim();
                result[r][c] = token;
            }
        }

        return result;
    }

    private void scrambleMovableLetters() {
        List<String> movableLetters = new ArrayList<>();
        List<int[]> movablePositions = new ArrayList<>();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                String answer = answerGrid[r][c];
                boolean isBlocked = isBlockedToken(answer);
                blockedCells[r][c] = isBlocked;

                if (isBlocked) {
                    currentGrid[r][c] = answer;
                } else {
                    movableLetters.add(answer);
                    movablePositions.add(new int[]{r, c});
                }
            }
        }

        Collections.shuffle(movableLetters);

        for (int i = 0; i < movablePositions.size(); i++) {
            int[] pos = movablePositions.get(i);
            currentGrid[pos[0]][pos[1]] = movableLetters.get(i);
        }
    }

    private void validateAndLockLines() {
        for (int r = 0; r < rows; r++) {
            if (isRowCorrect(r)) {
                for (int c = 0; c < cols; c++) {
                    if (!blockedCells[r][c]) {
                        lockedCells[r][c] = true;
                    }
                }
            }
        }

        for (int c = 0; c < cols; c++) {
            if (isColumnCorrect(c)) {
                for (int r = 0; r < rows; r++) {
                    if (!blockedCells[r][c]) {
                        lockedCells[r][c] = true;
                    }
                }
            }
        }
    }

    private boolean isRowCorrect(int row) {
        for (int c = 0; c < cols; c++) {
            if (blockedCells[row][c]) {
                continue;
            }
            if (!safeEquals(currentGrid[row][c], answerGrid[row][c])) {
                return false;
            }
        }
        return true;
    }

    private boolean isColumnCorrect(int col) {
        for (int r = 0; r < rows; r++) {
            if (blockedCells[r][col]) {
                continue;
            }
            if (!safeEquals(currentGrid[r][col], answerGrid[r][col])) {
                return false;
            }
        }
        return true;
    }

    private boolean isWin() {
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

    private boolean isBlockedToken(String token) {
        return TextUtils.isEmpty(token) || BLOCK_TOKEN.equals(token);
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
