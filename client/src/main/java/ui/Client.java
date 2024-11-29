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
  private GameState gameState;

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
        int size=serverFacade.list().size();
        return String.format("You created a game named %s! \n It is in the directory as game # %d! ", gameName, size);
      }
    } catch (ResponseException e) {
      return "Sorry, that didn't work. Does this game already exist?";
    }
    return "Try again! please give the name of the game you wish to create.";
  }

  public String logout() throws ResponseException {
    checkAuth();
    try {
      serverFacade.logout();
      state=State.SIGNEDOUT;
      String result=String.format("%s has been logged out.", username);
      username=null;
      password=null;
      return result;
    } catch (ResponseException e) {
      return "Failed to logout. Check your connection and try again.";
    }
  }

  public String observe(String... params) throws ResponseException {
    try {
      if (params.length == 1) {
        checkAuth();
        int gameId=Integer.parseInt(params[0]);
        String gameName="Null";
        ArrayList<GameData> gameData=serverFacade.list();
        for (int i=0; i < gameData.size(); i++) {
          GameData game=gameData.get(i);
          if (i + 1 == gameId) {
            gameId=game.gameID();
            gameName=game.gameName();
            break;
          }
        }
        serverFacade.observe(gameId);
        chessIllustrator.beginGame();
        return String.format("%s is observing game %s.", username, gameName);
      }
    } catch (Exception e) {
      return "We faailed to find a Game with the ID you gave us. " +
              "\n :) Check your game ID is correct.";
    }
    return "We couldn't find a game to observe! Make sure you put in a game number. \n" +
            "Hint: You can find games to join using the 'list' command";
  }

  public String join(String... params) {
    try {
      if (params.length == 2) {
        checkAuth();
        int gameId=Integer.parseInt(params[0]);
        ChessGame.TeamColor playerColor=getColor(params[1].toUpperCase());
        ArrayList<GameData> gameData=serverFacade.list();
        for (int i=0; i < gameData.size(); i++) {
          GameData game=gameData.get(i);
          if (i + 1 == gameId) {
            gameId=game.gameID();
            break;
          }
        }
        serverFacade.join(gameId, playerColor);
        chessIllustrator.beginGame();
        return String.format("%s joined game %s as the %s player", username, Integer.parseInt(params[0]), playerColor);
      }
    } catch (Exception e) {
      return "We couldn't find that game spot. \nMake sure you have: " +
              "\n -a valid game number from the list\n" + " -specified either black or white" +
              "\n -and you are not trying to join a full spot ;)";

    }
    return "Unable to join game. Did you specify a number AND valid team color?" +
            "\n Hint: Please specify either Black or White :)";
  }

  public String list() throws ResponseException {
    try {
      checkAuth();
      ArrayList<GameData> chessGames=serverFacade.list();
      var result=new StringBuilder();
      var gson=new Gson();
      if (chessGames.isEmpty()) {
        return "There are no games to see here. You better make one (hint, hint)!";
      }
      int i=1;
      for (var game : chessGames) {

        result.append(String.format("Game #%d! Name: %s%n", i, game.gameName()));

        result.append("WHITE PLAYER: ");
        if (Objects.isNull(game.whiteUsername())) {
          result.append("Available");
        } else {
          result.append(game.whiteUsername());
        }
        result.append(" BLACK PLAYER: ");
        if (Objects.isNull(game.blackUsername())) {
          result.append("Available");
        } else {
          result.append(game.blackUsername());
        }
        i++;
        result.append("\n\n");
      }

      return result.toString();
    } catch (ResponseException e) {
      return "Unable to list games. An internal error occurred.";
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
        username=params[0];
        password=params[1];
        String email=params[2];
        serverFacade.register(username, password, email);
        state=State.SIGNEDIN;
        return String.format("Registration successful! " +
                "Your username is %s.", username);
      }
    } catch (Exception e) {
      state=State.SIGNEDOUT;
      return "Registration Incorrect: make sure you don't have an account already";
    }
    return "Registration Failed. \n Make sure you have provided a valid username, password and email address.";
  }

  public String login(String... params) throws ResponseException {
    try {
      if (params.length == 2) {
        username=params[0];
        password=params[1];
        serverFacade.login(username, password);
        state=State.SIGNEDIN;
        return String.format("Login successful! Welcome back, %s. \n" +
                "Create or Join a game of chess! Type 'help' for more info :)", username);
      }
    } catch (ResponseException e) {
      state=State.SIGNEDOUT;
      return "Login failed! We could not find you...";
    }
    return "Login failed! Please try again with a valid username and password.";
  }

  public String help() {
    if (this.gameState == GameState.PLAYING) {
      return """
              - Help
              - Redraw
              - Leave
              - Move
              - Resign
              - HighlightMoves
              """;
    }

    if (state == State.SIGNEDOUT) {
      return """
              Type any of the following commands: 
              - register <USERNAME> <PASSWORD> <EMAIL> - join as a new user
              - login <USERNAME> <PASSWORD> for existing users
              - quit
              """;
    }
    return "Hi, " + username + "\n" +
            """
                    Type any of the following commands:
                    - create <GAME_NAME> - creates a new game of chess with the given name.
                    - list - lists all games of chess on the sever in order.
                    - join <GAME_NUMBER> [WHITE|BLACK|] - joins a game of chess.
                    - observe <GAME_NUMBER> - watch a game of chess.
                    - logout
                    """;
  }

  private void checkAuth() throws ResponseException {
    if (state == State.SIGNEDOUT) {
      throw new ResponseException(400, "Sign in to complete this task.");
    }

  }


}

