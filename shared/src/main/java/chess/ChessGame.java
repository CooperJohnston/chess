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
        ChessPiece piece = board.getPiece(move.getStartPosition());
        if (piece == null){
            throw new InvalidMoveException("no piece found");
        }
        if (piece.getTeamColor() != curr) {
            throw new InvalidMoveException("Incorrect team color");
        }
        Collection<ChessMove> validM = validMoves(move.getStartPosition());
        for (ChessMove m : validM) {
            if (move.equals(m)) {
                board.move(move);
                ChessPiece promo = board.getPiece(move.getEndPosition());
                if (move.getPromotionPiece() != null ) {
                    promo.promote(move.getPromotionPiece());
                }
                if (promo.getTeamColor() == TeamColor.WHITE) {
                    setTeamTurn(TeamColor.BLACK);
                }
                else {
                    setTeamTurn(TeamColor.WHITE);
                }
                return;
            }
        }
        throw new InvalidMoveException("Invalid move");
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
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        // First, check if the team is in check
        if (isInCheck(teamColor)) {
            // Iterate through all the pieces on the board
            for (int i = 1; i <= 8; i++) {
                for (int j = 1; j <= 8; j++) {
                    ChessPiece curr = board.getPiece(new ChessPosition(i, j));
                    // Check if the piece belongs to the team in question
                    if (curr != null && curr.getTeamColor() == teamColor) {
                        // Get valid moves for the current piece
                        Collection<ChessMove> validM = validMoves(new ChessPosition(i, j));
                        // If the piece has valid moves, it's not checkmate
                        if (validM != null && !validM.isEmpty()) {
                            return false;
                        }
                    }
                }
            }
            // If no piece has valid moves and the team is in check, it's checkmate
            return true;
        }
        // If the team is not in check, it's not checkmate
        return false;
    }


    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        // First, ensure the team is NOT in check
        if (!isInCheck(teamColor)) {
            // Iterate through all the pieces on the board
            for (int i = 1; i <= 8; i++) {
                for (int j = 1; j <= 8; j++) {
                    ChessPiece curr = board.getPiece(new ChessPosition(i, j));
                    // Check if the piece belongs to the specified team
                    if (curr != null && curr.getTeamColor() == teamColor) {
                        // Get valid moves for the current piece
                        Collection<ChessMove> validM = validMoves(new ChessPosition(i, j));
                        // If the piece has any valid moves, it's not a stalemate
                        if (validM != null && !validM.isEmpty()) {
                            return false;
                        }
                    }
                }
            }
            // If no piece has valid moves and the team is not in check, it's a stalemate
            return true;
        }
        // If the team is in check, it's not a stalemate
        return false;
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
