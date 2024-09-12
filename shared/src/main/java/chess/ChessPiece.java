package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private ChessGame.TeamColor teamColor;
    private ChessPiece.PieceType pieceType;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.teamColor = pieceColor;
        this.pieceType = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return teamColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return pieceType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChessPiece that)) return false;
      return teamColor == that.teamColor && pieceType == that.pieceType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamColor, pieceType);
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new ArrayList<>();
        int currentRow = myPosition.getRow();
        int currentColumn = myPosition.getColumn();
        /* better method for PAWN?*/
        if (pieceType == ChessPiece.PieceType.PAWN) {
            /* I must check that I am able to capture first */
            if (teamColor == ChessGame.TeamColor.WHITE) {
                /* code to check the corner */
                if(currentColumn > 1 && currentColumn < 8 && currentRow > 1 && currentRow < 8 && board.board[currentRow][currentColumn -2] != null && board.board[currentRow][currentColumn -2].getTeamColor() != teamColor) {

                    if (currentRow == 7){
                        for (PieceType pieceType : PieceType.values()) {
                            if (pieceType != ChessPiece.PieceType.PAWN && pieceType != ChessPiece.PieceType.KING) {
                                ChessPosition newPosition=new ChessPosition(currentRow +1, currentColumn-1);
                                ChessMove newMove=new ChessMove(myPosition, newPosition, pieceType);
                                moves.add(newMove);
                            }
                        }
                    }
                    else {
                        ChessPosition newPosition = new ChessPosition(currentRow+1, currentColumn - 1);
                        ChessMove newMove = new ChessMove(myPosition, newPosition, null);
                        moves.add(newMove);

                    }
                }
                if(currentColumn > 1 && currentColumn < 8 && currentRow > 1 && currentRow < 8 && board.board[currentRow][currentColumn ] != null && board.board[currentRow][currentColumn].getTeamColor() != teamColor) {
                    if (currentRow == 7){
                        for (PieceType pieceType : PieceType.values()) {
                            if (pieceType != ChessPiece.PieceType.PAWN && pieceType != ChessPiece.PieceType.KING) {
                                ChessPosition newPosition=new ChessPosition(currentRow +1, currentColumn+1);
                                ChessMove newMove=new ChessMove(myPosition, newPosition, pieceType);
                                moves.add(newMove);
                            }
                        }
                    }
                    else {
                        ChessPosition newPosition = new ChessPosition(currentRow+1, currentColumn + 1);
                        ChessMove newMove = new ChessMove(myPosition, newPosition, null);
                        moves.add(newMove);

                    }
                }
                /* check straight ahead */
                if (currentRow < 7 && board.board[currentRow][currentColumn-1] == null) {
                    if (currentRow == 2 && board.board[currentRow+1][currentColumn-1] == null){
                        ChessPosition newPosition = new ChessPosition(currentRow + 2, currentColumn);
                        ChessMove newMove = new ChessMove(myPosition, newPosition, null);
                        moves.add(newMove);

                    }
                    ChessPosition newPosition = new ChessPosition(currentRow + 1, currentColumn);
                    ChessMove newMove = new ChessMove(myPosition, newPosition, null);
                    moves.add(newMove);
                }
                /*Check the bottom*/
                if (currentRow == 7 && board.board[currentRow][currentColumn-1] == null) {
                    for (PieceType pieceType : PieceType.values()) {
                        if (pieceType != ChessPiece.PieceType.PAWN && pieceType != ChessPiece.PieceType.KING) {
                            ChessPosition newPosition=new ChessPosition(currentRow + 1, currentColumn);
                            ChessMove newMove=new ChessMove(myPosition, newPosition, pieceType);
                            moves.add(newMove);
                        }
                    }

                }
            }
            /* I must check that I am able to capture first */
            if (teamColor == ChessGame.TeamColor.BLACK) {
                /* code to check the corner */
                if(currentColumn > 1 && currentColumn < 8 && currentRow > 1 && currentRow < 8 && board.board[currentRow-2][currentColumn -2] != null && board.board[currentRow-2][currentColumn -2].getTeamColor() != teamColor) {

                    if (currentRow == 2){
                        for (PieceType pieceType : PieceType.values()) {
                            if (pieceType != ChessPiece.PieceType.PAWN && pieceType != ChessPiece.PieceType.KING) {
                                ChessPosition newPosition=new ChessPosition(currentRow - 1, currentColumn-1);
                                ChessMove newMove=new ChessMove(myPosition, newPosition, pieceType);
                                moves.add(newMove);
                            }
                        }
                    }
                    else {
                    ChessPosition newPosition = new ChessPosition(currentRow-1, currentColumn - 1);
                    ChessMove newMove = new ChessMove(myPosition, newPosition, null);
                    moves.add(newMove);}
                }
                if(currentColumn > 1 && currentColumn < 8 && currentRow > 1 && currentRow < 8 && board.board[currentRow-2][currentColumn ] != null && board.board[currentRow-2][currentColumn].getTeamColor() != teamColor) {
                    if (currentRow == 2){
                        for (PieceType pieceType : PieceType.values()) {
                            if (pieceType != ChessPiece.PieceType.PAWN && pieceType != ChessPiece.PieceType.KING) {
                                ChessPosition newPosition=new ChessPosition(currentRow - 1, currentColumn+1);
                                ChessMove newMove=new ChessMove(myPosition, newPosition, pieceType);
                                moves.add(newMove);
                            }
                        }
                    }
                    else {
                        ChessPosition newPosition = new ChessPosition(currentRow-1, currentColumn + 1);
                        ChessMove newMove = new ChessMove(myPosition, newPosition, null);
                        moves.add(newMove);}
                }
                /* check straight ahead */
                if (currentRow > 2 && board.board[currentRow-2][currentColumn-1] == null) {
                    if (currentRow == 7 && board.board[currentRow-3][currentColumn-1] == null){
                        ChessPosition newPosition = new ChessPosition(currentRow -2, currentColumn);
                        ChessMove newMove = new ChessMove(myPosition, newPosition, null);
                        moves.add(newMove);

                    }
                    ChessPosition newPosition = new ChessPosition(currentRow -1, currentColumn);
                    ChessMove newMove = new ChessMove(myPosition, newPosition, null);
                    moves.add(newMove);
                }
                /*Check the bottom*/
                if (currentRow == 2 && board.board[currentRow-2][currentColumn-1] == null) {
                    for (PieceType pieceType : PieceType.values()) {
                        if (pieceType != ChessPiece.PieceType.PAWN && pieceType != ChessPiece.PieceType.KING) {
                            ChessPosition newPosition=new ChessPosition(currentRow - 1, currentColumn);
                            ChessMove newMove=new ChessMove(myPosition, newPosition, pieceType);
                            moves.add(newMove);
                        }
                    }

                }
            }
        }
        if (pieceType == ChessPiece.PieceType.ROOK) {
            // Define directional offsets: {rowOffset, columnOffset}
            int[][] directions = {
                    {0, 1},   // Right
                    {0, -1},  // Left
                    {1, 0},   // Down
                    {-1, 0}   // Up
            };
            // Loop through all directions
            for (int[] dir : directions) {
                int rowOffset = dir[0];
                int colOffset = dir[1];
                int currentRowTemp = currentRow;
                int currentColumnTemp = currentColumn;
                // Continue in this direction until out of bounds
                while (true) {
                    currentRowTemp += rowOffset;
                    currentColumnTemp += colOffset;
                    // Check if out of bounds
                    if (currentRowTemp < 1 || currentRowTemp > 8 || currentColumnTemp < 1 || currentColumnTemp > 8) {
                        break;
                    }
                    ChessPosition curr = new ChessPosition(currentRowTemp, currentColumnTemp);
                    ChessMove newMove = new ChessMove(myPosition, curr, null);

                    if (board.getPiece(curr) == null) {
                        moves.add(newMove);
                    } else if (board.getPiece(curr).getTeamColor() != teamColor) {
                        moves.add(newMove);
                        break;  // Stop after capturing opponent's piece
                    } else {
                        break;  // Stop when blocked by a piece of the same color
                    }
                }
            }
        }
        return moves;
    }
}
