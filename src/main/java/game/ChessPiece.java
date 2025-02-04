package game;

import java.util.List;

public abstract class ChessPiece {
    public int[] position;    // position is in format first is the horizontal index (row) and then the vertical index (column)
    protected String character;  // character could be also named as handle
    protected String color;      // White or black
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
    public final boolean move(Board board, int[] moveTo) {
        int[] oldPosition = this.position;
        ChessPiece target = board.board[moveTo[0]][moveTo[1]];

        boolean isValid = canMoveTo(target, board);

        if (!isValid) {
            System.out.println("Invalid move");
            return false;
        }

        // Check if the move leaves the king in check
        if (isKingCheckedAfterMove(board, moveTo)) {
            System.out.println("Invalid move, own king would be checked");
            return false;
        }

        // Perform the move
        board.board[oldPosition[0]][oldPosition[1]] = new Space(oldPosition);
        board.board[moveTo[0]][moveTo[1]] = this;
        this.position = moveTo;


        // Check if the opponent's king is in check
        if (isCheckingKing(board)) {
            board.setCheckingPiece(this);
            board.setCheck(true);
        } else {
            board.setCheck(false);
            board.setCheckingPiece(null);
        }

        // Update the board
        updateBoard(board, moveTo[0], moveTo[1], oldPosition);
        if (board.isCheck()) {
            System.out.println("King is checked");
        }
        return true;
    }


    /**
     * This method checks if the players given target position matches with a possible move
     * (abstract because of pawns first move logic)
     * @param pieceToReplace: This is the spot where the selected piece would like to move
     * @param board: Instance of the game board
     * @return true when valid move
     */
    public abstract boolean canMoveTo(ChessPiece pieceToReplace, Board board);

    /**
     * returns all the possible moves for given piece in its current location
     * @param board: instance of the game board
     * @return list of available moves
     */
    public abstract List<int[]> getMoves(Board board);

    public boolean isCheckingKing(Board board) {
        List<int[]> moves = this.getMoves(board);
        if (moves == null) return false;
        for (int[] move: moves) {
            ChessPiece piece = board.board[move[0]][move[1]];
            if (piece instanceof King & !piece.color.equals(this.color)) {
                return true;
            }
        }

        return false;
    }

    /**
     * This method updates the view of the game board after a move,
     * including promotion and en passant capture.
     *
     * @param board     Pointer to the instance of the game board
     * @param row       Index row where the piece will be drawn
     * @param column    Index column where the piece will be drawn
     * @param oldPosition Previous position of the moving piece
     */
    protected void updateBoard(Board board, int row, int column, int[] oldPosition) {
        // Create a copy of oldPosition for the Space object
        int[] spacePosition = new int[]{oldPosition[0], oldPosition[1]};

        // Handle en passant capture logic
        if (this instanceof Pawn && board.enPassantActive) {
            int enPassantRow = this.color.equals("W") ? row + 1 : row - 1;

            if (column == board.enPassant.position[1] && Math.abs(row - oldPosition[0]) == 1) {
                // Remove the pawn that was captured by en passant
                board.board[enPassantRow][column] = new Space(new int[]{enPassantRow, column});
            }
        }

        // Move space to pawn's last position
        board.board[oldPosition[0]][oldPosition[1]] = new Space(spacePosition);

        // Move piece to new location
        this.position[0] = row;
        this.position[1] = column;

        int lastRow = this.color.equals("W") ? 0 : 7;

        if (this instanceof Pawn && lastRow == row) {
            // Pawn promotion to Queen
            board.board[row][column] = new Queen(this.color, new int[]{row, column});
        } else {
            board.board[row][column] = this;
        }

        // Print the updated board
        board.printBoard();
    }

    /**
     * This method checks if moving a piece unintentionally checks your own king
     * @param clonedBoard   cloned board of simulated move
     * @return              true if own king is checked
     */
    private boolean unsafeMove(Board clonedBoard) {
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 7; j++) {
                ChessPiece piece = clonedBoard.board[i][j];
                if (piece.color.equals(this.color) | piece instanceof Space) continue;
                if (piece.isCheckingKing(clonedBoard)) return true;
            }
        }
        return false;
    }

    private boolean isKingCheckedAfterMove(Board board, int[] moveTo) {
        // Save the original state of the board
        ChessPiece originalTarget = board.board[moveTo[0]][moveTo[1]];
        int[] originalPosition = this.position.clone();

        // Simulate the move
        board.board[originalPosition[0]][originalPosition[1]] = new Space(originalPosition);
        board.board[moveTo[0]][moveTo[1]] = this;
        this.position = moveTo;

        // Check if own king is in check
        King ownKing = findKing(this.color, board);
        boolean isInCheck = false;
        if (ownKing != null) {
            isInCheck = ownKing.isUnderAttack(board);
        }

        // Revert the move
        board.board[originalPosition[0]][originalPosition[1]] = this;
        board.board[moveTo[0]][moveTo[1]] = originalTarget;
        this.position = originalPosition;

        return isInCheck;
    }

    public King findKing(String color, Board board) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                ChessPiece piece = board.board[i][j];
                if (piece instanceof King && piece.color.equals(color)) {
                    return (King) piece;
                }
            }
        }
        return null; // If no king is found (shouldn't happen in a valid game)
    }
    public boolean isUnderAttack(Board board) {
        for (int i = 0; i < board.board.length; i++) {
            for (int j = 0; j < board.board[i].length; j++) {
                ChessPiece piece = board.board[i][j];
                // Check if the piece is an opponent and can attack the king's position
                if (piece != null && !piece.color.equals(this.color)) {
                    List<int[]> moves = piece.getMoves(board);
                    if (moves != null) {
                        for (int[] move : moves) {
                            if (move[0] == this.position[0] && move[1] == this.position[1]) {
                                return true; // King is under attack
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Simulates a move by temporarily placing the piece on a target square.
     * Reverts the board state after the simulation completes.
     *
     * @param board     The game board instance.
     * @param moveTo    The target position to simulate the move [row, column].
     * @return The cloned board state after simulating the move.
     */
    public Board simulateMove(Board board, int[] moveTo) {
        // Clone the board to avoid altering the real game state
        Board simulatedBoard = board.clone();

        // Save the original state of the move
        int[] originalPosition = this.position.clone();
        ChessPiece targetPiece = simulatedBoard.board[moveTo[0]][moveTo[1]];

        // Simulate the move on the cloned board
        simulatedBoard.board[originalPosition[0]][originalPosition[1]] = new Space(originalPosition);
        simulatedBoard.board[moveTo[0]][moveTo[1]] = this;
        this.position = moveTo;

        // Revert this piece's position in the simulation after returning the new board
        this.position = originalPosition;

        return simulatedBoard;
    }


    public String getColor() {
        return color;
    }
}
