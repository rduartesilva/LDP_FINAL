import java.util.ArrayList;
import java.util.List;

public class Horse {
    private int row;
    private int col;

    public Horse(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public void moveTo(int newRow, int newCol) {
        this.row = newRow;
        this.col = newCol;
    }

    public List<int[]> getPossibleMoves(int boardSize, boolean[][] blockedCells) {
        int[][] moves = {
            {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
            {1, -2}, {1, 2}, {2, -1}, {2, 1}
        };

        List<int[]> validMoves = new ArrayList<>();

        for (int[] move : moves) {
            int newRow = row + move[0];
            int newCol = col + move[1];

            if (newRow >= 0 && newRow < boardSize && newCol >= 0 && newCol < boardSize && !blockedCells[newRow][newCol]) {
                validMoves.add(new int[]{newRow, newCol});
            }
        }

        return validMoves;
    }
}
