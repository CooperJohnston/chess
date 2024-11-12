package ui;

import exception.ResponseException;

import java.util.Arrays;

public class Client {
  private String username = null;
  private String password = null;
  private ChessIllustrator chessIllustrator;
  private final ServerFacade serverFacade;
  private State state = State.SIGNEDOUT;
  public Client(String url) {
    serverFacade=new ServerFacade(url);
    this.chessIllustrator=new ChessIllustrator();
  }

  public String eval(String input){
    try{
      var tokens = input.toLowerCase().split(" ");
      var cmd = (tokens.length > 0) ? tokens[0] : "help";
      var params = Arrays.copyOfRange(tokens, 1, tokens.length);
      if (state == State.SIGNEDOUT) {
        return switch (cmd){
          case "register" -> register(params);
          case "login" -> login(params);
          case "quit" -> "quit";
          default -> help();
        };
        else {

        }
      }

    }
    catch (ResponseException e) {
      return e.getMessage();
    }
  }
  public String register(String... params) throws ResponseException {
    try {
      if (params.length == 3) {
        state = State.SIGNEDIN;
        username = params[0];
        password = params[1];
        String email = params[2];
        serverFacade.register(username, password, email);
        return String.format("Registration successful! " +
                "Your username is %s.", username);
      }
    }
    catch (ResponseException e) {
      return "Registration Incorrect: " + e.getMessage();
    }
    return "Registration Failed";
  }

  public String login(String... params) throws ResponseException {
    try {
      if (params.length == 2) {
        state=State.SIGNEDIN;
        username=params[0];
        password=params[1];
        serverFacade.login(username, password);
        return String.format("Login successful! Welcome back, %s.", username);
      }
    } catch (ResponseException e) {
      return "Login error exception: " + e.getMessage();
    }
    return "Login error: incorrect usage";
  }

    public String help() {
    if (state == State.SIGNEDOUT) {
      return """
                    - register <USERNAME> <PASSWORD> <EMAIL>
                    - login <USERNAME> <PASSWORD>
                    - quit
                    """;
    }
    return """
                - create <NAME>
                - list
                - join <ID> [WHITE|BLACK|<empty>]
                - observe <ID>
                - logout
                - quit
                """;
  }


}

