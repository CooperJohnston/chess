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
    private boolean enPassant = false;

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
                if (m.capture_back && !enPassant){
                    break;
                }
                validM.add(m);
            }
        }
        int kingRow = startPosition.getRow();
        if (!piece.hasMoved() && piece.getPieceType() == ChessPiece.PieceType.KING) {
            if (canCastleKingside(piece.getTeamColor())) {
                validM.add(new ChessMove(startPosition, new ChessPosition(kingRow, 7),null)); // Move king to the kingside castling position (g1 or g8)
            }
            if (canCastleQueenside(piece.getTeamColor())) {
                validM.add(new ChessMove(startPosition, new ChessPosition(kingRow, 3), null)); // Move king to the queenside castling position (c1 or c8)
            }
        }

        return validM;
    }
    private boolean canCastleKingside(TeamColor teamColor) {
        ChessPosition kingPos = (teamColor == TeamColor.WHITE) ? new ChessPosition(1, 5) : new ChessPosition(8, 5);
        ChessPosition rookPos = (teamColor == TeamColor.WHITE) ? new ChessPosition(1, 8) : new ChessPosition(8, 8);

        //  if king and rook haven't moved
        if (board.getPiece(kingPos) == null || board.getPiece(rookPos) == null ||board.getPiece(kingPos).hasMoved() || board.getPiece(rookPos).hasMoved()) {
            return false;
        }

        for (int i = 6; i <= 7; i++) {
            ChessPosition pos = new ChessPosition(kingPos.getRow(), i);
            if (board.getPiece(pos) != null) {
                return false;
            }
        }

        if (isInCheck(teamColor) || !willPassThroughCheck(kingPos, 6) || !willPassThroughCheck(kingPos, 7)) {
            return false;
        }

        return true;
    }

    private boolean canCastleQueenside(TeamColor teamColor) {
        ChessPosition kingPos = (teamColor == TeamColor.WHITE) ? new ChessPosition(1, 5) : new ChessPosition(8, 5);
        ChessPosition rookPos = (teamColor == TeamColor.WHITE) ? new ChessPosition(1, 1) : new ChessPosition(8, 1);

        // if king and rook haven't moved
        if (board.getPiece(kingPos) == null || board.getPiece(rookPos) == null ||board.getPiece(kingPos).hasMoved() || board.getPiece(rookPos).hasMoved()) {
            return false;
        }

        for (int i = 2; i <= 4; i++) {
            ChessPosition pos = new ChessPosition(kingPos.getRow(), i);
            if (board.getPiece(pos) != null) {
                return false;
            }
        }

        if (isInCheck(teamColor) || !willPassThroughCheck(kingPos, 4) || !willPassThroughCheck(kingPos, 3)) {
            return false;
        }

        return true;
    }

    private boolean willPassThroughCheck(ChessPosition kingPos, int column) {
        ChessPosition tempPos = new ChessPosition(kingPos.getRow(), column);
        // Simulate king moving through this position and check if it's attacked
        ChessMove tempMove = new ChessMove(kingPos, tempPos, null);
        return testMove(tempMove, board.getPiece(kingPos).getTeamColor());
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
                if (m.getEndPosition().equals(move.getEndPosition())
                ){  // get the right data into the piece for en passant... better method
                    move.back_location = m.back_location;
                    move.capture_back = m.capture_back;
                    move.double_move = m.double_move;
                }
            }
            board.move(move);
            // Handle castling
            if (piece.getPieceType() == ChessPiece.PieceType.KING && Math.abs(move.getStartPosition().getColumn() - move.getEndPosition().getColumn()) == 2) {
                if (move.getEndPosition().getColumn() == 7) { // Kingside castling
                    ChessPosition rookStart = new ChessPosition(move.getStartPosition().getRow(), 8);
                    ChessPosition rookEnd = new ChessPosition(move.getStartPosition().getRow(), 6);
                    board.move(new ChessMove(rookStart, rookEnd, null));
                } else if (move.getEndPosition().getColumn() == 3) { // Queenside castling
                    ChessPosition rookStart = new ChessPosition(move.getStartPosition().getRow(), 1);
                    ChessPosition rookEnd = new ChessPosition(move.getStartPosition().getRow(), 4);
                    board.move(new ChessMove(rookStart, rookEnd, null));
                }
            }
            if (move.getPromotionPiece() != null) {
                board.getPiece(move.getEndPosition()).promote(move.getPromotionPiece());
            }
            if (move.double_move){
                this.enPassant = true;
            }
            else {
                this.enPassant = false;
            }
            setTeamTurn(piece.getTeamColor() == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE);
        } else {
            throw new InvalidMoveException("Invalid move");
        }
    }
    /**
     *
     * Checks to see if executing a move puts the team in check mate
     *
     * **/

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
