public class Player {
    private String name;
    private int[] position;
    private char symbol;
    private boolean isLocalPlayer;

    public Player(String name, int startRow, int startCol, char symbol, boolean isLocalPlayer) {
        this.name = name;
        this.position = new int[]{startRow, startCol};
        this.symbol = symbol;
        this.isLocalPlayer = isLocalPlayer;
    }

    public String getName() {
        return name;
    }

    public int[] getPosition() {
        return position;
    }

    public int getRow() {
        return position[0];
    }

    public int getCol() {
        return position[1];
    }

    public void setPosition(int row, int col) {
        this.position[0] = row;
        this.position[1] = col;
    }

    public char getSymbol() {
        return symbol;
    }

    public boolean isLocalPlayer() {
        return isLocalPlayer;
    }
}
