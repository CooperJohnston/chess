package ui;

import chess.ChessGame;
import com.google.gson.Gson;
import exception.ResponseException;
import model.GameData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class Client {
  private String username=null;
  private String password=null;
  private ChessIllustrator chessIllustrator;
  private final ServerFacade serverFacade;
  private State state=State.SIGNEDOUT;

  public Client(String url) {

    serverFacade=new ServerFacade(url);
    this.chessIllustrator=new ChessIllustrator();
  }

  public String eval(String input) {
    try {
      var tokens=input.toLowerCase().split(" ");
      var cmd=(tokens.length > 0) ? tokens[0] : "help";
      var params=Arrays.copyOfRange(tokens, 1, tokens.length);
      if (state == State.SIGNEDOUT) {
        return switch (cmd) {
          case "register" -> register(params);
          case "login" -> login(params);
          case "quit" -> "quit";
          default -> help();
        };
      } else {
        return switch (cmd) {
          case "list" -> list();
          case "join" -> join(params);
          case "observe" -> observe(params);
          case "logout" -> logout();
          case "quit" -> "quit";
          case "create" -> create(params);
          default -> help();
        };

      }
    } catch (ResponseException e) {
      return e.getMessage();
    }
  }

  public String create(String... params) throws ResponseException {
    try {
      if (params.length == 1) {
        checkAuth();
        String gameName=params[0];
        serverFacade.create(gameName);
        return String.format("Created game %s.", gameName);
      }
    } catch (ResponseException e) {
      return "Create Game error. Does this game already exist?";
    }
    return "Try again! please give the name of the game you wish to create.";
  }

  public String logout() throws ResponseException {
    checkAuth();
    try {
      serverFacade.logout();
      state=State.SIGNEDOUT;
      return String.format("%s has been logged out.", username);
    } catch (ResponseException e) {
      return "Failed to logout: " + e.getMessage();
    }
  }

  public String observe(String... params) throws ResponseException {
    try {
      if (params.length == 1) {
        checkAuth();
        int gameId=Integer.parseInt(params[0]);
        serverFacade.observe(gameId);
        chessIllustrator.beginGame();
        return String.format("%s observing game %s.", username, gameId);
      }
    } catch (ResponseException e) {
      return "Failed to observe: " + e.getMessage();
    }
    return "Unable to observe game.";
  }

  public String join(String... params) {
    try {
      if (params.length == 2) {
        checkAuth();
        int gameId=Integer.parseInt(params[0]);
        ChessGame.TeamColor playerColor=getColor(params[1].toUpperCase());
        serverFacade.join(gameId, playerColor);
        chessIllustrator.beginGame();
        return String.format("%s joined game %s as the %s player", username, gameId, playerColor);
      }
    } catch (ResponseException e) {
      return "Error joining game. " + e.getMessage();
    }
    return "Unable to game.";
  }

  public String list() throws ResponseException {
    try {
      checkAuth();
      ArrayList<GameData> chessGames=serverFacade.list();
      var result=new StringBuilder();
      var gson=new Gson();
      if (chessGames.isEmpty()) {
        result.append("There are no games!");
      }
      for (var game : chessGames) {
        result.append(String.format("Game ID: %d. Game name: %s%n", game.gameID(), game.gameName()));

        result.append("WHITE PLAYER: ");
        if (Objects.isNull(game.whiteUsername())) {
          result.append("None");
        } else {
          result.append(game.whiteUsername());
        }
        result.append(" BLACK PLAYER: ");
        if (Objects.isNull(game.blackUsername())) {
          result.append("None");
        } else {
          result.append(game.blackUsername());
        }
        result.append("\n");
      }

      return result.toString();
    } catch (ResponseException e) {
      return "Unable to list games.";
    }

  }

  ChessGame.TeamColor getColor(String color) throws ResponseException {
    switch (color) {
      case "BLACK":
        return ChessGame.TeamColor.BLACK;
      case "WHITE":
        return ChessGame.TeamColor.WHITE;
    }
    throw new ResponseException(400, "Invalid color: " + color);
  }

  public String register(String... params) throws ResponseException {
    try {
      if (params.length == 3) {
        state=State.SIGNEDIN;
        username=params[0];
        password=params[1];
        String email=params[2];
        serverFacade.register(username, password, email);
        return String.format("Registration successful! " +
                "Your username is %s.", username);
      }
    } catch (ResponseException e) {
      return "Registration Incorrect: make sure you don't have an account already";
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

  private void checkAuth() throws ResponseException {
    if (state == State.SIGNEDOUT) {
      throw new ResponseException(400, "Sign in to complete this task.");
    }

  }

}

