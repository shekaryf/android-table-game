package ir.baran.bookPack.game.domain.model;

public class GameCell {

    private final int row;
    private final int col;
    private String letter;
    private CellState state;

    public GameCell(int row, int col, String letter, CellState state) {
        this.row = row;
        this.col = col;
        this.letter = letter;
        this.state = state;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public String getLetter() {
        return letter;
    }

    public void setLetter(String letter) {
        this.letter = letter;
    }

    public CellState getState() {
        return state;
    }

    public void setState(CellState state) {
        this.state = state;
    }

    public boolean isSwappable() {
        return state == CellState.MOVABLE || state == CellState.SELECTED;
    }
}
