Grid token format for puzzle levels

Supported cell formats in Levels.grid_data (JSON 2D array):

1) Plain string (backward-compatible)
- "A"      -> playable letter cell (answer letter A)
- "*" or "#" or "" -> non-playable block cell
- "C:1"    -> non-playable clue/anchor cell, displays "1" on board
- "L:A"    -> playable letter cell (explicit form)

2) Object cell (strict/recommended)
- {"type":"letter","value":"A"} -> playable letter cell
- {"type":"block"}                 -> non-playable block
- {"type":"clue","value":"1"}   -> non-playable clue/anchor cell

Validation rules enforced by GameViewModel:
- grid_data must be a rectangular matrix (all rows same length)
- grid_rows and grid_cols (if > 0) must match matrix dimensions

Gameplay notes:
- Only playable letter cells can be swapped.
- Non-playable cells remain fixed and can show clue markers.
- After each swap, each correctly placed letter is locked (green).
