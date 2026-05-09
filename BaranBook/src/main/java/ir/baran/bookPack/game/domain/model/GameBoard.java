package ir.baran.bookPack.game.domain.model;

import java.util.List;

public class GameBoard {

    private final int levelId;
    private final int rows;
    private final int cols;
    private final List<List<GameCell>> cells;

    public GameBoard(int levelId, int rows, int cols, List<List<GameCell>> cells) {
        this.levelId = levelId;
        this.rows = rows;
        this.cols = cols;
        this.cells = cells;
    }

    public int getLevelId() {
        return levelId;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public List<List<GameCell>> getCells() {
        return cells;
    }
}
