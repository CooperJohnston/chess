package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
  private final ChessGame.TeamColor teamColor;
  private ChessPiece.PieceType pieceType;
  public boolean doubled=false;
  public boolean seen=false;

  private boolean hasMoved=false;

  public boolean hasMoved() {
    return hasMoved;
  }

  public void setHasMoved(boolean hasMoved) {
    this.hasMoved=hasMoved;
  }


  public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
    this.teamColor=pieceColor;
    this.pieceType=type;
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
    if (this == o) {
      return true;
    }
    if (!(o instanceof ChessPiece that)) {
      return false;
    }
    return teamColor == that.teamColor && pieceType == that.pieceType;
  }

  @Override
  public int hashCode() {
    return Objects.hash(teamColor, pieceType);
  }


  @Override
  public String toString() {
    return "{" + teamColor +
            ", " + pieceType +
            "}";
  }

  private static int[][] rookDirections={{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
  private static int[][] bishopDirections={{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
  private static int[][] kingDirections={{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
  private static int[][] knightDirections={{2, 1}, {2, -1}, {-2, 1}, {-2, -1}, {1, 2}, {1, -2}, {-1, 2}, {-1, -2}};

  private static final Map<PieceType, int[][]> DIRECTION_MAP=Map.of(
          ChessPiece.PieceType.ROOK, rookDirections,
          ChessPiece.PieceType.BISHOP, bishopDirections,
          ChessPiece.PieceType.QUEEN, kingDirections,
          ChessPiece.PieceType.KING, kingDirections,
          ChessPiece.PieceType.KNIGHT, knightDirections
  );

  /**
   * Calculates all the positions a chess piece can move to
   * Does not take into account moves that are illegal due to leaving the king in
   * danger
   *
   * @return Collection of valid moves
   */
  public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
    Collection<ChessMove> moves=new ArrayList<>();
    int currentRow=myPosition.getRow();
    int currentColumn=myPosition.getColumn();

    if (pieceType == ChessPiece.PieceType.PAWN) {
      addPawnMoves(board, myPosition, moves);
    } else {
      addOtherPieceMoves(board, myPosition, moves);
    }

    return moves;
  }

  private void addPawnMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves) {
    int currentRow=myPosition.getRow();
    int currentColumn=myPosition.getColumn();
    int forwardDirection=(teamColor == ChessGame.TeamColor.WHITE) ? 1 : -1;

    // Forward move
    ChessPosition forwardOne=new ChessPosition(currentRow + forwardDirection, currentColumn);
    if (board.getPiece(forwardOne) == null) {
      // Check for promotion on forward move
      if ((teamColor == ChessGame.TeamColor.WHITE && currentRow + forwardDirection == 8) ||
              (teamColor == ChessGame.TeamColor.BLACK && currentRow + forwardDirection == 1)) {
        for (ChessPiece.PieceType pieceType : ChessPiece.PieceType.values()) {
          if (pieceType != ChessPiece.PieceType.PAWN && pieceType != ChessPiece.PieceType.KING) {
            moves.add(new ChessMove(myPosition, forwardOne, pieceType));
          }
        }
      } else {
        moves.add(new ChessMove(myPosition, forwardOne, null));
      }
    }

    // Double forward move
    if ((teamColor == ChessGame.TeamColor.WHITE && currentRow == 2) ||
            (teamColor == ChessGame.TeamColor.BLACK && currentRow == 7)) {
      ChessPosition forwardTwo=new ChessPosition(currentRow + 2 * forwardDirection, currentColumn);
      if (board.getPiece(forwardOne) == null && board.getPiece(forwardTwo) == null) {
        ChessMove forwardMove=new ChessMove(myPosition, forwardTwo, null);
        forwardMove.doubleMove=true;
        doubled=true;
        moves.add(forwardMove);
      }
    }

    // Diagonal capture and en passant
    int[][] diagonals={{forwardDirection, -1}, {forwardDirection, 1}};
    for (int[] diagonal : diagonals) {
      addPawnDiagonalMoves(board, myPosition, moves, diagonal, forwardDirection);
    }
  }

  private void addPawnDiagonalMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves, int[] diagonal, int forwardDirection) {
    int currentRow=myPosition.getRow();
    int currentColumn=myPosition.getColumn();
    int diagRow=currentRow + diagonal[0];
    int diagCol=currentColumn + diagonal[1];

    if (diagRow >= 1 && diagRow <= 8 && diagCol >= 1 && diagCol <= 8) {
      ChessPosition diagonalPos=new ChessPosition(diagRow, diagCol);
      ChessPiece pieceAtDiagonal=board.getPiece(diagonalPos);

      // Capture and promotion
      if (pieceAtDiagonal != null && pieceAtDiagonal.getTeamColor() != teamColor) {
        if (diagRow == 8 || diagRow == 1) {
          for (ChessPiece.PieceType pieceType : ChessPiece.PieceType.values()) {
            if (pieceType != ChessPiece.PieceType.PAWN && pieceType != ChessPiece.PieceType.KING) {
              moves.add(new ChessMove(myPosition, diagonalPos, pieceType));
            }
          }
        } else {
          moves.add(new ChessMove(myPosition, diagonalPos, null));
        }
      }

      // En passant capture
      ChessPosition side=new ChessPosition(currentRow, currentColumn + diagonal[1]);
      if (board.getPiece(side) != null && board.getPiece(side).getTeamColor() != teamColor &&
              board.getPiece(side).getPieceType() == ChessPiece.PieceType.PAWN) {
        if ((teamColor == ChessGame.TeamColor.WHITE && currentRow == 5 && board.getPiece(side).doubled) ||
                (teamColor == ChessGame.TeamColor.BLACK && currentRow == 4 && board.getPiece(side).doubled)) {
          ChessMove move=new ChessMove(myPosition, diagonalPos, null);
          move.captureBack=true;
          move.backLocation=side;
          moves.add(move);
        }
      }
    }
  }

  private void addOtherPieceMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves) {
    int[][] directions=DIRECTION_MAP.get(pieceType);
    for (int[] dir : directions) {
      int rowOffset=dir[0];
      int colOffset=dir[1];

      for (int currentRowTemp=myPosition.getRow() + rowOffset, currentColumnTemp=myPosition.getColumn() + colOffset;
           currentRowTemp >= 1 && currentRowTemp <= 8 && currentColumnTemp >= 1 && currentColumnTemp <= 8;
           currentRowTemp+=rowOffset, currentColumnTemp+=colOffset) {
        ChessPosition curr=new ChessPosition(currentRowTemp, currentColumnTemp);
        ChessMove newMove=new ChessMove(myPosition, curr, null);
        ChessPiece pieceAtPos=board.getPiece(curr);

        if (pieceAtPos == null) {
          moves.add(newMove);
          if (pieceType == ChessPiece.PieceType.KING || pieceType == ChessPiece.PieceType.KNIGHT) {
            break;
          }
        } else {
          if (pieceAtPos.getTeamColor() != teamColor) {
            moves.add(newMove);
          }
          break;
        }
      }
    }
  }

  public ChessPiece copy() {
    ChessPiece dupe=new ChessPiece(teamColor, pieceType);
    dupe.doubled=doubled;
    return dupe;

  }

  public void promote(ChessPiece.PieceType pieceType) {
    this.pieceType=pieceType;
  }
}
