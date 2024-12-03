package ui;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import com.google.gson.Gson;
import exception.ResponseException;
import model.GameData;
import websocket.WebsocketFacade;

import java.util.*;

public class Client {
  public ChessGame.TeamColor color;
  private String username=null;
  private String password=null;
  private ChessIllustrator chessIllustrator;
  private final ServerFacade serverFacade;
  private State state=State.SIGNEDOUT;
  private GameState gameState;
  WebsocketFacade websocketFacade;
  private final String url;
  private Repl repl;
  private int gameId;
  private Object games;

  public Client(String url, Repl repl) {

    serverFacade=new ServerFacade(url);
    this.chessIllustrator=new ChessIllustrator();
    this.url=url;
    this.repl=repl;
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
      }
      if (gameState == GameState.PLAYING) {
        return switch (cmd) {
          case "leave" -> leave();
          case "redraw" -> redraw();
          case "resign" -> resign();
          case "show" -> listMoves(params);
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

  private String listMoves(String[] params) throws ResponseException {
    try {
      if (params.length != 1) {
        return "Make sure you provide a command formatted like this:\n" +
                "show <start_postion>";
      }
      ChessPosition start=assertCord(params[0]);
      ArrayList<GameData> gameData=serverFacade.list();
      for (GameData gameData1 : gameData) {
        if (gameData1.gameID() == this.gameId) {
          Collection<ChessMove> moves=gameData1.game().validMoves(start);

          if (this.color == ChessGame.TeamColor.BLACK) {

            return printValidBlack(start, moves, gameData1.game());

          } else {
            return printValidWhite(start, moves, gameData1.game());


          }
        }
      }
    } catch (Exception e) {
      return "Make sure you provide coordinates with this format:\n" +
              "<row><column>";
    }
    return "Game redrawn!";
  }

  private String printValidBlack(ChessPosition start, Collection<ChessMove> moves, ChessGame game) {
    boolean[][] validMoves=addMoves(moves, start);
    chessIllustrator.drawBoard(game.getBoard().getBoard(), false, validMoves);
    return "Here are all the valid moves for this position :)";
  }

  private String printValidWhite(ChessPosition start, Collection<ChessMove> moves, ChessGame game) {
    boolean[][] validMoves=addMoves(moves, start);
    chessIllustrator.drawBoard(game.getBoard().getBoard(), true, validMoves);
    return "Here are all the valid moves for this position :)";
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
        String gameName=null;
        ArrayList<GameData> gameData=serverFacade.list();
        for (int i=0; i < gameData.size(); i++) {
          GameData game=gameData.get(i);
          if (i + 1 == gameId) {
            gameId=game.gameID();
            gameName=game.gameName();
            break;
          }
        }
        assert gameName != null;
        serverFacade.observe(gameId);
        websocketFacade=new WebsocketFacade(this.url, this.repl);
        websocketFacade.playGame(null, serverFacade.getAuthToken(), gameId);
        this.gameState=GameState.PLAYING;
        this.gameId=gameId;
        return String.format("%s is observing game %s.", username, gameName);
      }
    } catch (Exception e) {
      return "We failed to find a Game with the ID you gave us. " +
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
        this.gameId=gameId;
        serverFacade.join(gameId, playerColor);
        this.gameState=GameState.PLAYING;
        websocketFacade=new WebsocketFacade(url, this.repl);
        try {
          websocketFacade.playGame(playerColor, serverFacade.getAuthToken(), gameId);
        } catch (Exception e) {
          return "We are having trouble connecting you to other players. \n" +
                  "Please try again later :)";
        }
        return String.format("%s joined game %s as the %s player", username, Integer.parseInt(params[0]), playerColor);
      }
    } catch (Exception e) {
      return "Be sure to format your command with:" +
              "\n -the number of the game " +
              "\n -the color you wish to use" + "\n" +
              "For example: 'join 2 black' ";

    }
    return "We couldn't find that game spot. \nMake sure you have: " +
            "\n -a valid game number from the list\n" + " -specified either black or white" +
            "\n -and you are not trying to join a full spot ;)";
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

  private String resign() throws ResponseException {
    try {
      checkAuth();
      if (gameState != GameState.PLAYING) {
        return "You are not playing a game, how can you resign?";
      }
      System.out.println("Resigning game...");
      System.out.print("Are you sure you want to resign?\nY|N\n");
      Scanner scanner=new Scanner(System.in);
      String res=scanner.nextLine().strip();
      if (res.equalsIgnoreCase("y")) {
        websocketFacade.resign(serverFacade.getAuthToken(), this.gameId);
      }
      return "Goodbye! Play again soon!";
    } catch (Exception e) {
      return "We couldn't resign your game.";
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
              When you are playing a game of chess, you can run any of these commands :)
              
              - Redraw - Draws and refreshes the Chess Board.
              - Leave - Exits gameplay
              - Move <Start> <End> - moves a piece from <Start> to <End>
              - Resign - Surrender and end the game.
              - Show <Start> - Highlights all moves that can be taken at this location.
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

  public String leave() throws ResponseException {
    try {
      checkAuth();
      if (gameState != GameState.PLAYING) {
        return "you are not playing a game";
      }
      websocketFacade.leaveGame(serverFacade.getAuthToken(), this.gameId);
      this.gameId-=1;
      websocketFacade=null;
      gameState=GameState.NOTPLAYING;
      return "You have left the game";
    } catch (Exception e) {
      return "couldn't leave the game";
    }
  }


  public void printWhite(ChessGame game) {
    chessIllustrator.drawBoard(game, true);
  }

  public void printBlack(ChessGame game) {
    chessIllustrator.drawBoard(game, false);
  }

  private String redraw() throws ResponseException {
    checkAuth();
    if (gameState != GameState.PLAYING) {
      return "You are not playing a game, so we can't draw it :)";
    }
    ArrayList<GameData> gameData=serverFacade.list();
    for (GameData gameData1 : gameData) {
      if (gameData1.gameID() == this.gameId) {
        if (this.color == ChessGame.TeamColor.BLACK) {
          printBlack(gameData1.game());
          return "Game redrawn!";
        }
        printWhite(gameData1.game());
        return "Game redrawn!";
      }
    }
    return "We couldn't find the game you want us to draw. " +
            "\nCheck your connection :)";
  }

  public static ChessPosition assertCord(String move) throws records.ResponseException {
    if (move.length() != 2) {
      throw new records.ResponseException(500, "expect: <row><col>");
    }
    int col=move.charAt(0) - 'a' + 1;
    int row=Character.getNumericValue(move.charAt(1));
    if (row < 0) {
      row*=-1;
    }
    if (col >= 1 && col <= 8 && row >= 1 && row <= 8) {
      return new ChessPosition(row, col);
    }
    throw new records.ResponseException(500, "position out of board");
  }

  public boolean[][] addMoves(Collection<ChessMove> moves, ChessPosition start) {
    if (moves != null && start != null) {
      boolean[][] result=new boolean[8][8];
      int x=start.getRow() - 1;
      int y=start.getColumn() - 1;
      result[x][y]=true;
      for (ChessMove move : moves) {
        x=move.getEndPosition().getRow() - 1;
        y=move.getEndPosition().getColumn() - 1;
        result[x][y]=true;
      }
      return result;
    }
    return null;
  }
}

