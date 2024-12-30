package game;

public abstract class ChessPiece {

    // position is in format first is the horizontal index (row) and then the vertical index (column)
    protected int[] position;

    // character could be also named as handle
    protected String character;
    protected String color;
    protected String name;

    public ChessPiece(String gamePiece, String color, int[] position) {
        // white game pieces ar in lowercase
        if (color.equals("W")) {
            this.character = gamePiece.toLowerCase();
        } else {
            this.character = gamePiece;
        }
        this.color = color;
        this.position = position;
    }

    public String toString() {
        return character;
    }

    /**
     * abstract method used to move all the different pieces in the game
     *
     * @param board  the instance of the game board with all the pieces
     * @param moveTo coordinates where the player wants to move the given piece
     * @return return true if the move was good
     */
    public final boolean move(Board board, int[] moveTo, boolean check, boolean checkMate) {
        int[] oldPosition = this.position;
        ChessPiece piece = board.board[moveTo[0]][moveTo[1]];

        boolean isValid = checkValidMove(piece, check, checkMate, board);

        if (!isValid) {
            System.out.println("Invalid move");
            return false;
        }

        // make move visible on game board
        int row = moveTo[0];
        int column = moveTo[1];
        updateBoard(board, row, column, oldPosition);
        return true;

    }

    /**
     * This method calculates all the possible moves for the piece and the
     * checks if a possivle move is the same as a selected move
     *
     * @param pieceToReplace: This is the spot where the selected piece would like to move
     * @param check
     * @param checkMate
     * @param board: Instance of the game board
     * @return true when valid move
     */
    public abstract boolean checkValidMove(ChessPiece pieceToReplace, boolean check, boolean checkMate, Board board);

    /**
     * this method updated the view of the game board after a move
     * @param board pointer to the instance of the game board
     * @param row index row where the piece will be drawn
     * @param column index column where the piece will be drawn
     */
    protected void updateBoard(Board board, int row, int column, int[] oldPosition) {
        // Create a copy of oldPosition for the Space object
        int[] spacePosition = new int[]{oldPosition[0], oldPosition[1]};

        // Move space to pawn's last position
        board.board[oldPosition[0]][oldPosition[1]] = new Space(spacePosition);

        // Move piece to new location
        this.position[0] = row;
        this.position[1] = column;
        board.board[row][column] = this;

        // Print the updated board
        board.printBoard();
    }
}