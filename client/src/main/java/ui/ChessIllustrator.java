package ui;

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
    drawHeaders();
    drawBoard(chessBoard);


  }

  private String[][] init() {
    String[][] board=new String[8][8];
    int width=5;
    String format="%" + width + "s";

    board[0]=new String[]{
            String.format(format, WHITE_ROOK),
            String.format(format, WHITE_KNIGHT),
            String.format(format, WHITE_BISHOP),
            String.format(format, WHITE_QUEEN),
            String.format(format, WHITE_KING),
            String.format(format, WHITE_BISHOP),
            String.format(format, WHITE_KNIGHT),
            String.format(format, WHITE_ROOK)
    };

    board[1]=new String[]{
            String.format(format, WHITE_PAWN),
            String.format(format, WHITE_PAWN),
            String.format(format, WHITE_PAWN),
            String.format(format, WHITE_PAWN),
            String.format(format, WHITE_PAWN),
            String.format(format, WHITE_PAWN),
            String.format(format, WHITE_PAWN),
            String.format(format, WHITE_PAWN)
    };

    for (int i=2; i < 6; i++) {
      board[i]=new String[]{String.format(format, EMPTY), String.format(format, EMPTY),
              String.format(format, EMPTY), String.format(format, EMPTY), String.format(format, EMPTY),
              String.format(format, EMPTY), String.format(format, EMPTY), String.format(format, EMPTY)};
    }

    board[6]=new String[]{
            String.format(format, BLACK_PAWN),
            String.format(format, BLACK_PAWN),
            String.format(format, BLACK_PAWN),
            String.format(format, BLACK_PAWN),
            String.format(format, BLACK_PAWN),
            String.format(format, BLACK_PAWN),
            String.format(format, BLACK_PAWN),
            String.format(format, BLACK_PAWN)
    };

    board[7]=new String[]{
            String.format(format, BLACK_ROOK),
            String.format(format, BLACK_KNIGHT),
            String.format(format, BLACK_BISHOP),
            String.format(format, BLACK_QUEEN),
            String.format(format, BLACK_KING),
            String.format(format, BLACK_BISHOP),
            String.format(format, BLACK_KNIGHT),
            String.format(format, BLACK_ROOK)
    };

    return board;
  }


  public void drawHeaders() {
    int width=3;
    String format="%" + width + "s";
    String[] headers=new String[]{
            String.format(format, EMPTY), String.format(format, "a"), String.format(format, "b"),
            String.format(format, "c"), String.format(format, "d"), String.format(format, "e"),
            String.format(format, "f"), String.format(format, "g"), String.format(format, "h"), String.format(format, EMPTY)};
    outStream.print(SET_BG_COLOR_BLUE);
    outStream.print(SET_TEXT_COLOR_WHITE);
    for (String header : headers) {
      outStream.print(header);
    }

  }

  public void drawBoard(String[][] board) {
    outStream.print(SET_BG_COLOR_BLUE);
    outStream.print(SET_TEXT_COLOR_WHITE);
    for (String[] header : board) {

      for (String line : header) {
        outStream.println(line);
      }
    }


  }


}
