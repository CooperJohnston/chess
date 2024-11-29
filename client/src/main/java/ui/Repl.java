package ui;

import ui.EscapeSequences.*;

import java.util.Scanner;

import static ui.EscapeSequences.*;

public class Repl {
  private final Client client;

  public Repl(String url) {
    client=new Client(url);
  }

  public void run() {
    System.out.println(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + SET_TEXT_BOLD + "Welcome to CS 240 Chess!");
    String[] pieces=new String[]{BLACK_BISHOP, BLACK_ROOK, BLACK_QUEEN, BLACK_KING, BLACK_KNIGHT, BLACK_PAWN,
            BLACK_BISHOP, BLACK_ROOK, BLACK_QUEEN, BLACK_KING, BLACK_KNIGHT, BLACK_PAWN};
    for (String piece : pieces) {
      System.out.print(piece);
    }
    System.out.println(RESET_TEXT_BOLD_FAINT);
    System.out.println("Register or Login to get started!");
    System.out.println("Type 'help' for more information.");
    for (String piece : pieces) {
      System.out.print(piece);
    }
    System.out.println();
    System.out.println(SET_TEXT_FAINT + "© 2024 Vandalay Industries");
    System.out.println(RESET_TEXT_BOLD_FAINT);
    Scanner scanner=new Scanner(System.in);
    var result="";
    while (!result.equals("quit")) {
      printPrompt();
      String line=scanner.nextLine();

      try {
        for (String piece : pieces) {
          System.out.print(piece);
        }
        System.out.println();
        result=client.eval(line);
        System.out.print(result);
        System.out.println();
        for (String piece : pieces) {
          System.out.print(piece);
        }
        System.out.println();
      } catch (Throwable e) {
        var msg=e.toString();
        System.out.print(msg);
      }
    }

  }


  private void printPrompt() {
    System.out.print(SET_TEXT_COLOR_BLUE + "\n" + SET_BG_COLOR_BLACK + ">>> ");
    System.out.print(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK);

  }
}