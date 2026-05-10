package ir.baran.bookPack.game.data.validation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ir.baran.bookPack.game.data.model.LevelEntity;

public final class GridDataValidator {

    private static final String BLOCK_TOKEN = "*";
    private static final String BLOCK_TOKEN_ALT = "#";
    private static final String CLUE_PREFIX = "C:";
    private static final String LETTER_PREFIX = "L:";

    private GridDataValidator() {
    }

    public static List<String> validateLevel(LevelEntity level) {
        List<String> errors = new ArrayList<>();
        if (level == null) {
            errors.add("level is null");
            return errors;
        }

        Integer levelId = level.getId();
        String prefix = "levelId=" + (levelId == null ? "null" : levelId) + ": ";

        if (level.getGridData() == null || level.getGridData().trim().isEmpty()) {
            errors.add(prefix + "grid_data is empty");
            return errors;
        }

        try {
            JSONArray rowsArray = new JSONArray(level.getGridData());
            int rows = rowsArray.length();
            if (rows == 0) {
                errors.add(prefix + "grid_data has zero rows");
                return errors;
            }

            int cols = rowsArray.getJSONArray(0).length();
            if (cols == 0) {
                errors.add(prefix + "grid_data has zero cols");
                return errors;
            }

            if (level.getGridRows() > 0 && level.getGridRows() != rows) {
                errors.add(prefix + "grid_rows mismatch. expected=" + level.getGridRows() + " actual=" + rows);
            }
            if (level.getGridCols() > 0 && level.getGridCols() != cols) {
                errors.add(prefix + "grid_cols mismatch. expected=" + level.getGridCols() + " actual=" + cols);
            }

            int playableCount = 0;
            for (int r = 0; r < rows; r++) {
                JSONArray rowArray = rowsArray.getJSONArray(r);
                if (rowArray.length() != cols) {
                    errors.add(prefix + "row " + r + " has invalid col count " + rowArray.length() + " (expected " + cols + ")");
                    continue;
                }

                for (int c = 0; c < cols; c++) {
                    Object raw = rowArray.get(c);
                    CellInfo cell = parseCell(raw);
                    if (cell.error != null) {
                        errors.add(prefix + "cell(" + r + "," + c + ") " + cell.error);
                    }
                    if (cell.playable) {
                        playableCount++;
                    }
                }
            }

            if (playableCount < 2) {
                errors.add(prefix + "not enough playable cells for swap mechanic: " + playableCount);
            }

        } catch (JSONException e) {
            errors.add(prefix + "invalid JSON: " + e.getMessage());
        }

        return errors;
    }

    private static CellInfo parseCell(Object raw) {
        if (raw == null || raw == JSONObject.NULL) {
            return new CellInfo(false, null, null);
        }

        if (raw instanceof JSONObject) {
            JSONObject obj = (JSONObject) raw;
            String type = obj.optString("type", "letter").trim().toLowerCase();
            String value = obj.optString("value", "").trim();
            if ("block".equals(type) || "blocked".equals(type)) {
                return new CellInfo(false, "", null);
            }
            if ("clue".equals(type) || "hint".equals(type)) {
                return new CellInfo(false, value, null);
            }
            if (!"letter".equals(type) && !"char".equals(type)) {
                return new CellInfo(false, null, "unknown object type='" + type + "'");
            }
            if (value.isEmpty()) {
                return new CellInfo(false, null, "letter object value is empty");
            }
            return new CellInfo(true, value, null);
        }

        String token = String.valueOf(raw).trim();
        if (token.isEmpty() || BLOCK_TOKEN.equals(token) || BLOCK_TOKEN_ALT.equals(token)) {
            return new CellInfo(false, token, null);
        }
        if (token.startsWith(CLUE_PREFIX)) {
            return new CellInfo(false, token.substring(CLUE_PREFIX.length()).trim(), null);
        }
        if (token.startsWith(LETTER_PREFIX)) {
            String value = token.substring(LETTER_PREFIX.length()).trim();
            if (value.isEmpty()) {
                return new CellInfo(false, null, "L: token has empty value");
            }
            return new CellInfo(true, value, null);
        }
        return new CellInfo(true, token, null);
    }

    private static class CellInfo {
        final boolean playable;
        final String value;
        final String error;

        CellInfo(boolean playable, String value, String error) {
            this.playable = playable;
            this.value = value;
            this.error = error;
        }
    }
}
