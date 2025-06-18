import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Horse {
    private int row;
    private int col;
    private Set<String> visitedPositions; // para marcar as casas já visitadas

    public Horse(int row, int col) {
        this.row = row;
        this.col = col;
        this.visitedPositions = new HashSet<>();
        markVisited(row, col);
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    // Marca uma posição como visitada
    private void markVisited(int r, int c) {
        visitedPositions.add(r + "," + c);
    }

    public Set<String> getVisitedPositions() {
        return visitedPositions;
    }

    // Move o cavalo para uma nova posição e marca essa casa como visitada
    public void moveTo(int newRow, int newCol) {
        this.row = newRow;
        this.col = newCol;
        markVisited(newRow, newCol);
    }

    // Retorna os movimentos possíveis, bloqueando as casas visitadas
    public List<int[]> getPossibleMoves(int boardSize, boolean[][] blockedCells) {
        int[][] moves = {
            {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
            {1, -2}, {1, 2}, {2, -1}, {2, 1}
        };

        List<int[]> validMoves = new ArrayList<>();

        for (int[] move : moves) {
            int newRow = row + move[0];
            int newCol = col + move[1];

            // Se está dentro do tabuleiro, não está bloqueado e não foi visitado
            if (newRow >= 0 && newRow < boardSize && newCol >= 0 && newCol < boardSize
                && !blockedCells[newRow][newCol] 
                && !visitedPositions.contains(newRow + "," + newCol)) {

                validMoves.add(new int[]{newRow, newCol});
            }
        }

        return validMoves;
    }
}
