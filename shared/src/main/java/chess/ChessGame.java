package chess;

import java.util.ArrayList;
import java.util.Collection;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    ChessBoard board = new ChessBoard();
    TeamColor curr;

    public ChessGame() {
        board.resetBoard();
        setTeamTurn(TeamColor.WHITE);
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return curr;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.curr = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    @Override
    public String toString() {
        return "ChessGame{" +
                "board=" + board +
                ", curr=" + curr +
                '}';
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        // i can check if a team is in check; therefore if I am in check after i run the move, if i am still in check
        // then it is not a valid move
        if (board.getPiece(startPosition) == null){
            return null;
        }
        Collection<ChessMove> validM = new ArrayList<>();
        // this loop calls the test function that makes a stub
        ChessPiece piece = board.getPiece(startPosition);
        Collection<ChessMove> all_moves = piece.pieceMoves(board, startPosition);
        for ( ChessMove m : all_moves){
            if (testMove(m, piece.getTeamColor())){
                validM.add(m);
            }
        }
        return validM;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        // get start
        ChessPiece piece = board.getPiece(move.getStartPosition());

        if (piece == null) {
            throw new InvalidMoveException("No piece found");
        }
        if (piece.getTeamColor() != curr) {
            throw new InvalidMoveException("Incorrect team color tried to move");
        }

        Collection<ChessMove> validM = validMoves(move.getStartPosition());
        // see if the move is valid to ensure we can do it;
        // BETTER METHOD?
        if (validM != null && validM.contains(move)) {
            for (ChessMove m : validM) {
                if (m.getEndPosition() ==
                move.getEndPosition()){
                    move.back_location = m.back_location;
                    move.capture_back = m.capture_back;
                    move.double_move = m.double_move;
                }
            }
            board.move(move);
            if (move.getPromotionPiece() != null) {
                board.getPiece(move.getEndPosition()).promote(move.getPromotionPiece());
            }
            setTeamTurn(piece.getTeamColor() == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE);
        } else {
            throw new InvalidMoveException("Invalid move");
        }
    }


    public boolean testMove(ChessMove move, TeamColor team)  {
        // code that brute forces a move for testing, better method?
        ChessGame temp = new ChessGame();
        temp.setBoard(board.deepCopy());
        temp.board.move(move);
        return !temp.isInCheck(team);

    }
    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition klocation = (teamColor == TeamColor.WHITE) ? board.whiteKing : board.blackKing;
        // loop through and find all the moves of all the WHITE or Black pieces
        // if any of them have a move, check to see if it points to the king
        if (klocation == null){
            klocation = board.getKing(teamColor);
        }
        for (int i =1; i <= 8; i ++){
            for (int j = 1; j <= 8; j ++){
                ChessPosition curr = new ChessPosition(i, j);
                if (board.getPiece(curr) != null && board.getPiece(curr).getTeamColor() != teamColor) {
                    Collection<ChessMove> tempMoves = board.getPiece(curr).pieceMoves(board,curr);
                    for (ChessMove move : tempMoves) {
                        if (move.getEndPosition().equals(klocation)){
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * A helper method to check whether or not a team has valid moves
     *
     * @param teamColor team to check out
     * @return
     */
    private boolean hasValidMoves(TeamColor teamColor) {
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPiece curr = board.getPiece(new ChessPosition(i, j));
                if (curr != null && curr.getTeamColor() == teamColor) {
                    Collection<ChessMove> validM = validMoves(new ChessPosition(i, j));
                    if (validM != null && !validM.isEmpty()) {
                        return true; // Return true if valid moves are found
                    }
                }
            }
        }
        return false;
    }






    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        return isInCheck(teamColor) && !hasValidMoves(teamColor);
    }


    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        return !isInCheck(teamColor) && !hasValidMoves(teamColor);
    }


    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return this.board;
    }
}
