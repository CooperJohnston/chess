package ui;

import java.util.Scanner;

public class Repl {
  private final Client client;

  public Repl(String url) {
    client=new Client(url);
  }

  public void run() {
    System.out.println("Welcome to Chess.");
    System.out.print(client.help());

    Scanner scanner=new Scanner(System.in);
    var result="";
    while (!result.equals("quit")) {
      printPrompt();
      String line=scanner.nextLine();

      try {
        result=client.eval(line);
        System.out.print(result);
      } catch (Throwable e) {
        var msg=e.toString();
        System.out.print(msg);
      }
    }
    System.out.println();
  }

  private void printPrompt() {
    System.out.print("\n" + ">>> ");
  }

}