package ui;

import chess.ChessGame;
import chess.ChessPiece;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static ui.EscapeSequences.*;

public class ChessIllustrator {

  private final PrintStream outStream;

  public ChessIllustrator() {
    outStream=new PrintStream(System.out, true, StandardCharsets.UTF_8);
  }

  public void beginGame() {
    outStream.print(ERASE_SCREEN);
    String[][] chessBoard=init();
    drawBoard(reverseBoard(chessBoard), false, null);
    outStream.println();
    drawBoard(chessBoard, true, null);

  }

  private String[][] reverseBoard(String[][] board) {
    String[][] reversedBoard=new String[8][8];
    for (int i=0; i < 8; i++) {
      reversedBoard[i]=board[7 - i];
      reversedBoard[i]=reverseArray(reversedBoard[i]);
    }
    return reversedBoard;
  }

  private String center(String text, int width) {
    int padding=(width - text.length()) / 2;
    String format="%" + padding + "s%s%" + padding + "s";
    return String.format(format, "", text, "");
  }

  private String[][] init() {
    String[][] board=new String[8][8];
    int width=5;

    board[7]=new String[]{
            center(WHITE_ROOK, width),
            center(WHITE_KNIGHT, width),
            center(WHITE_BISHOP, width),
            center(WHITE_QUEEN, width),
            center(WHITE_KING, width),
            center(WHITE_BISHOP, width),
            center(WHITE_KNIGHT, width),
            center(WHITE_ROOK, width)
    };

    board[6]=new String[]{
            center(WHITE_PAWN, width),
            center(WHITE_PAWN, width),
            center(WHITE_PAWN, width),
            center(WHITE_PAWN, width),
            center(WHITE_PAWN, width),
            center(WHITE_PAWN, width),
            center(WHITE_PAWN, width),
            center(WHITE_PAWN, width)
    };

    for (int i=2; i < 6; i++) {
      board[i]=new String[]{center(EMPTY, width), center(EMPTY, width),
              center(EMPTY, width), center(EMPTY, width), center(EMPTY, width),
              center(EMPTY, width), center(EMPTY, width), center(EMPTY, width)};
    }

    board[1]=new String[]{
            center(BLACK_PAWN, width),
            center(BLACK_PAWN, width),
            center(BLACK_PAWN, width),
            center(BLACK_PAWN, width),
            center(BLACK_PAWN, width),
            center(BLACK_PAWN, width),
            center(BLACK_PAWN, width),
            center(BLACK_PAWN, width)
    };

    board[0]=new String[]{
            center(BLACK_ROOK, width),
            center(BLACK_KNIGHT, width),
            center(BLACK_BISHOP, width),
            center(BLACK_QUEEN, width),
            center(BLACK_KING, width),
            center(BLACK_BISHOP, width),
            center(BLACK_KNIGHT, width),
            center(BLACK_ROOK, width)
    };

    return board;
  }


  public void drawHeaders(boolean isWhiteView) {
    int width=5;
    String[] headers=new String[]{
            center(EMPTY, width), center("a", width), center("b", width),
            center("c", width), center("d", width), center("e", width),
            center("f", width), center("g", width), center("h", width), center(EMPTY, width)
    };

    // Reverse headers for "BLACK VIEW"
    if (!isWhiteView) {
      headers=reverseArray(headers);
    }

    outStream.print(SET_BG_COLOR_DARK_GREEN);
    outStream.print(SET_TEXT_COLOR_WHITE);
    for (String header : headers) {
      outStream.print(header);
    }
    outStream.println(SET_BG_COLOR_WHITE);
    outStream.print(SET_TEXT_COLOR_BLACK);
  }

  // Utility method to reverse the headers array
  private String[] reverseArray(String[] array) {
    String[] reversedArray=new String[array.length];
    for (int i=0; i < array.length; i++) {
      reversedArray[i]=array[array.length - 1 - i];
    }
    return reversedArray;
  }


  public void drawBoard(String[][] board, boolean isWhiteView, boolean[][] moves) {

    outStream.print(SET_BG_COLOR_DARK_GREEN);

    outStream.print(SET_TEXT_COLOR_WHITE);
    drawHeaders(isWhiteView);
    boolean startYellow=true;
    int rowNumber=isWhiteView ? 8 : 1;
    int rowIncrement=isWhiteView ? -1 : 1;
    if (moves == null) {
      moves=new boolean[8][8];
    }

    for (String[] row : board) {
      drawRow(row, startYellow, rowNumber, moves[rowNumber - 1]);
      startYellow=!startYellow;
      rowNumber+=rowIncrement;
    }
    drawHeaders(isWhiteView);


  }

  public void drawRow(String[] row, boolean startYellow, int rowNumber, boolean[] moves) {
    outStream.print(SET_BG_COLOR_DARK_GREEN);

    outStream.print(SET_TEXT_COLOR_WHITE);
    int width=5;
    outStream.print(center(String.valueOf(rowNumber), width));
    boolean isYellow=startYellow;
    int i=0;
    for (String space : row) {
      boolean coloredSpot;
      if (moves[i] == true) {
        coloredSpot=true;
      } else {
        coloredSpot=false;
      }
      if (coloredSpot && isYellow) {
        outStream.print(SET_BG_COLOR_YELLOW);
        color(space);
      } else if (coloredSpot && !isYellow) {
        outStream.print(SET_BG_COLOR_GREEN);
        color(space);
      } else if (isYellow && !coloredSpot) {
        outStream.print(SET_BG_COLOR_LIGHT_GREY);
        color(space);
      } else {
        outStream.print(SET_BG_COLOR_DARK_GREY);
        color(space);
      }

      outStream.print(space);
      isYellow=!isYellow;
      i++;
    }
    outStream.print(SET_BG_COLOR_DARK_GREEN);

    outStream.print(SET_TEXT_COLOR_WHITE);
    outStream.print(center(String.valueOf(rowNumber), width));
    outStream.println(SET_BG_COLOR_WHITE);


  }

  private void color(String space) {
    if (space.contains(BLACK_BISHOP) || space.contains(BLACK_KNIGHT) || space.contains(BLACK_ROOK)
            || space.contains(BLACK_PAWN) || space.contains(BLACK_QUEEN) || space.contains(BLACK_KING)) {
      outStream.print(SET_TEXT_COLOR_RED);
    } else {
      outStream.print(SET_TEXT_COLOR_BLUE);
    }
  }

  public void drawBoard(ChessGame game, boolean isWhiteView) {
    String[][] convertedBoard=convertBoard(game.getBoard().getBoard());
    if (!isWhiteView) {
      convertedBoard=reverseBoard(convertedBoard);
    }
    drawBoard(convertedBoard, isWhiteView, null);
  }

  String[][] convertBoard(ChessPiece[][] board) {
    int rows=board.length;
    int cols=board[0].length;
    String[][] stringBoard=new String[rows][cols];
    for (int i=0; i < rows; i++) {
      for (int j=0; j < cols; j++) {
        stringBoard[i][j]=getPiece(board[i][j]);
      }
    }
    return stringBoard;
  }

  public static String getString(ChessPiece piece, String[] symbols) {
    return switch (piece.getPieceType()) {
      case ChessPiece.PieceType.PAWN -> symbols[0];
      case ChessPiece.PieceType.ROOK -> symbols[1];
      case ChessPiece.PieceType.KNIGHT -> symbols[2];
      case ChessPiece.PieceType.KING -> symbols[3];
      case ChessPiece.PieceType.QUEEN -> symbols[4];
      case ChessPiece.PieceType.BISHOP -> symbols[5];
    };
  }

  public String getPiece(ChessPiece piece) {
    int width=5;
    if (piece == null) {
      return this.center(EMPTY, width);
    }
    String[] symbols=piece.getTeamColor() == ChessGame.TeamColor.WHITE
            ? new String[]{center(BLACK_PAWN, width), center(BLACK_ROOK, width),
            center(BLACK_KNIGHT, width), center(BLACK_KING, width), center(BLACK_QUEEN, width),
            center(BLACK_BISHOP, width)}
            : new String[]{center(WHITE_PAWN, width), center(WHITE_ROOK, width), center(WHITE_KNIGHT, width),
            center(WHITE_KING, width), center(WHITE_QUEEN, width), center(WHITE_BISHOP, width)};
    return getString(piece, symbols);
  }

  public void drawBoard(ChessPiece[][] board, boolean isWhiteView, boolean[][] validMoves) {
    String[][] convertedBoard=convertBoard(board);
    if (!isWhiteView) {
      convertedBoard=reverseBoard(convertedBoard);
    }
    drawBoard(convertedBoard, isWhiteView, validMoves);

  }
}
