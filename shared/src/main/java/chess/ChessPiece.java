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
    private final ChessGame.TeamColor teamColor;
    private  ChessPiece.PieceType pieceType;
    public boolean doubled = false;
    public boolean seen = false;

    private boolean hasMoved = false;

    public boolean hasMoved() {
        return hasMoved;
    }

    public void setHasMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
    }


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


    @Override
    public String toString() {
        return  "{" + teamColor +
                ", " + pieceType +
                "}";
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
            // Determine direction based on the team color
            int forwardDirection = (teamColor == ChessGame.TeamColor.WHITE) ? 1 : -1;

            // Regular move (one square forward)
            ChessPosition forwardOne = new ChessPosition(currentRow + forwardDirection, currentColumn);
            if (board.getPiece(forwardOne) == null && currentRow + forwardDirection != 8 && currentRow + forwardDirection != 1) {
                moves.add(new ChessMove(myPosition, forwardOne, null));
            }

            // Two squares forward (only on starting row)
            if ((teamColor == ChessGame.TeamColor.WHITE && currentRow == 2) ||
                    (teamColor == ChessGame.TeamColor.BLACK && currentRow == 7)) {
                ChessPosition forwardTwo = new ChessPosition(currentRow + 2 * forwardDirection, currentColumn);

                if (board.getPiece(forwardOne) == null && board.getPiece(forwardTwo) == null) {
                    ChessMove forwadMove = new ChessMove(myPosition, forwardTwo, null);
                    forwadMove.double_move = true;
                    doubled = true;
                    moves.add(forwadMove);
                }
            }

            // Capture diagonally (two directions: left and right)
            int[][] diagonals = {
                    {forwardDirection, -1},  // Diagonal left
                    {forwardDirection, 1}    // Diagonal right
            };

            for (int[] diagonal : diagonals) {
                int diagRow = currentRow + diagonal[0];
                int diagCol = currentColumn + diagonal[1];

                // Check if in bounds
                if (diagRow >= 1 && diagRow <= 8 && diagCol >= 1 && diagCol <= 8) {
                    ChessPosition diagonalPos = new ChessPosition(diagRow, diagCol);
                    ChessPiece pieceAtDiagonal = board.getPiece(diagonalPos);

                    // Capture an opponent's piece diagonally
                    if (pieceAtDiagonal != null && pieceAtDiagonal.getTeamColor() != teamColor) {
                        // Handle promotion if capturing on the last row
                        if (diagRow == 8 || diagRow == 1) {
                            for (PieceType pieceType : ChessPiece.PieceType.values()) {
                                if (pieceType != ChessPiece.PieceType.PAWN && pieceType != ChessPiece.PieceType.KING) {
                                    moves.add(new ChessMove(myPosition, diagonalPos, pieceType));
                                }
                            }
                        }


                        else {
                            moves.add(new ChessMove(myPosition, diagonalPos, null));
                        }

                    }
                    ChessPosition side = new ChessPosition(currentRow, currentColumn + diagonal[1]);
                    if (board.getPiece(side) != null && board.getPiece(side).getTeamColor() != teamColor && board.getPiece(side).getPieceType() == PieceType.PAWN) {
                        if (currentRow == 5 && teamColor == ChessGame.TeamColor.WHITE && board.getPiece(side).doubled) {
                            ChessMove move = new ChessMove(myPosition, diagonalPos, null);
                            move.capture_back = true;
                            move.back_location = side;
                            moves.add(move);
                        }
                        if (currentRow == 4 && teamColor == ChessGame.TeamColor.BLACK && board.getPiece(side).doubled) {
                            ChessMove move = new ChessMove(myPosition, diagonalPos, null);
                            move.capture_back = true;
                            move.back_location = side;
                            moves.add(move);
                        }
                    }
                }
            }

            // Handle pawn promotion on the final row (without capturing)
            if ((teamColor == ChessGame.TeamColor.WHITE && currentRow + forwardDirection == 8) ||
                    (teamColor == ChessGame.TeamColor.BLACK && currentRow + forwardDirection == 1)) {
                for (ChessPiece.PieceType pieceType : ChessPiece.PieceType.values()) {
                    if (pieceType != ChessPiece.PieceType.PAWN && pieceType != ChessPiece.PieceType.KING) {
                        ChessPosition newPosition = new ChessPosition(currentRow + forwardDirection, currentColumn);
                        moves.add(new ChessMove(myPosition, newPosition, pieceType));
                    }
                }
            }
            return moves;
        }

        int[][] rookDirections = {
                {1, 0},
                {-1, 0},
                {0, 1},
                {0, -1}
        };

        int[][] bishopDirections = {
                {1, 1},   // Up-right
                {1, -1},  // Up-left
                {-1, 1},  // Down-right
                {-1, -1}  // Down-left
        };

        int[][] kingDirections = {
                {1, 0},
                {-1, 0},
                {0, 1},
                {0, -1},
                {1, 1},
                {1, -1},
                {-1, 1},
                {-1, -1}
        };
        int[][] knightDirections = {
                {2, 1},
                {2, -1},
                {-2, 1},
                {-2, -1},
                {1, 2},
                {1, -2},
                {-1, 2},
                {-1, -2}
        };

        // Handle piece-specific movement patterns
        int[][] directions = null;
        // better method?
        switch (pieceType) {
            case ROOK:
                directions = rookDirections;
                break;
            case BISHOP:
                directions = bishopDirections;
                break;
            case QUEEN:
                directions = kingDirections;
                break;
            case KING:
                directions = kingDirections;
                break;
            case KNIGHT:
                directions = knightDirections;
                break;
        }

        // Loop through all directions
        for (int[] dir : directions) {
            int rowOffset = dir[0];
            int colOffset = dir[1];
            int currentRowTemp = myPosition.getRow();
            int currentColumnTemp = myPosition.getColumn();

            while (true) {
                currentRowTemp += rowOffset;
                currentColumnTemp += colOffset;

                //  if out of bounds
                if (currentRowTemp < 1 || currentRowTemp > 8 || currentColumnTemp < 1 || currentColumnTemp > 8) {
                    break;
                }

                ChessPosition curr = new ChessPosition(currentRowTemp, currentColumnTemp);
                ChessMove newMove = new ChessMove(myPosition, curr, null);

                // moves and captures
                if (board.getPiece(curr) == null) {
                    moves.add(newMove);
                    // for those that do not move forward constantly, break;
                    if (pieceType == ChessPiece.PieceType.KING || pieceType == ChessPiece.PieceType.KNIGHT) {
                        break;
                    }
                } else if (board.getPiece(curr).getTeamColor() != teamColor) {
                    moves.add(newMove);
                    break;
                } else {
                    break;
                }


            }
        }


        return moves;
    }
    public ChessPiece copy(){
        ChessPiece dupe =  new ChessPiece(teamColor, pieceType);
        dupe.doubled = doubled;
        return dupe;

    }
    public void promote(ChessPiece.PieceType pieceType){
        this.pieceType = pieceType;
    }
}
