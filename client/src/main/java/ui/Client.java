package ui;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import com.google.gson.Gson;
import exception.ErrorException;
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
  private ArrayList<GameData> games;
  private final HashMap<String, ChessPosition> positionMap;


  public Client(String url, Repl repl) {

    serverFacade=new ServerFacade(url);
    this.chessIllustrator=new ChessIllustrator();
    this.url=url;
    this.repl=repl;
    positionMap=new HashMap<>();
    initPositionMap();
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
          case "move" -> move(params);
          default -> help();
        };
      } else {
        return switch (cmd) {
          case "list" -> list();
          case "join" -> joinGame(params);
          case "observe" -> observe(params);
          case "logout" -> logout();
          case "quit" -> "quit";
          case "create" -> create(params);
          default -> help();
        };

      }
    } catch (ResponseException | ErrorException e) {
      return e.getMessage();
    }
  }

  private String listMoves(String[] params) throws ResponseException {
    try {
      if (params.length != 1) {
        return "Make sure you provide a command formatted like this:\n" +
                "show <start_position>";
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

  public String observe(String[] params) throws ResponseException {
    checkAuth();
    int id;
    if (params.length == 1) {
      try {
        id=Integer.parseInt(params[0]);
      } catch (NumberFormatException e) {
        throw new ResponseException(401, "input must be a valid index");
      }
      int i=1;
      for (var game : this.games) {
        if (i == id) {
          try {
            websocketFacade=new WebsocketFacade(this.url, this.repl);
            websocketFacade.playGame(null, this.serverFacade.getAuthToken(), game.gameID());
            this.gameState=GameState.PLAYING;
            this.gameId=game.gameID();
            return "";
          } catch (ResponseException e) {
            return e.getMessage();
          }
        }
        i++;
      }
      throw new ResponseException(400, "invalid ID");
    } else {
      throw new ResponseException(400, "Expected: <your name> <password>");
    }

  }

  //return "We failed to find a Game with the ID you gave us. " +
  //"\n :) Check your game ID is correct.";

  //return "We couldn't find a game to observe! Make sure you put in a game number. \n" +
  //"Hint: You can find games to join using the 'list' command";


  public String joinGame(String[] params) throws ResponseException {
    checkAuth();

    if (params.length != 2) {
      throw new ResponseException(400, "Expected: join <gameID> <[BLACK|WHITE]>");
    }

    int id;
    try {
      id=Integer.parseInt(params[0]);
    } catch (NumberFormatException e) {
      throw new ResponseException(401, "input must be a valid index");
    }

    int i=1;
    for (var game : this.games) {
      if (i != id) {
        i++;
        continue;
      }

      String chosenColor=params[1].strip().toUpperCase();
      boolean isWhite=chosenColor.equals("WHITE");
      boolean isBlack=chosenColor.equals("BLACK");

      if (!isWhite && !isBlack) {
        throw new ResponseException(400, "Invalid color. Choose BLACK or WHITE.");
      }
      this.color=isWhite ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
      boolean isUserValid=(isWhite && username.equals(game.whiteUsername())) ||
              (isBlack && username.equals(game.blackUsername()));
      if (isUserValid) {
        this.gameState=GameState.PLAYING;
        this.gameId=game.gameID();
        websocketFacade=new WebsocketFacade(this.url, this.repl);
        websocketFacade.playGame(color, this.serverFacade.getAuthToken(), game.gameID());
        return "";
      }
      serverFacade.join(game.gameID(), color);
      this.gameState=GameState.PLAYING;
      websocketFacade=new WebsocketFacade(this.url, this.repl);
      websocketFacade.playGame(color, this.serverFacade.getAuthToken(), game.gameID());
      this.gameId=game.gameID();
      return "";
    }
    throw new ResponseException(400, "invalid ID");
  }


  public String list() throws ResponseException {
    try {
      checkAuth();
      ArrayList<GameData> chessGames=serverFacade.list();
      this.games=chessGames;
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

  public String move(String... params) throws ResponseException, ErrorException {
    if (params.length < 2) {
      return "Expected Usage: move <START> <END> (optional: <PROMOTION>)";
    }
    checkAuth();
    String startString=params[0];
    String endString=params[1];
    ChessPiece.PieceType promotion=null;

    ChessGame game=null;
    games=serverFacade.list();
    for (GameData gameData : games) {
      if (gameData.gameID() == this.gameId) {
        game=gameData.game();
      }
    }
    ChessPosition startPosition=positionMap.get(startString);
    ChessPosition endPosition=positionMap.get(endString);

    ChessPiece startPiece=game.getBoard().getPiece(startPosition);


    //validate promotion


    if (params.length == 3) {
      if (startPiece.getPieceType().equals(ChessPiece.PieceType.PAWN)) {
        promotion=switch (params[2]) {
          case "ROOK", "rook", "r", "R" -> ChessPiece.PieceType.ROOK;
          case "BISHOP", "bishop", "b", "B" -> ChessPiece.PieceType.BISHOP;
          case "KNIGHT", "knight", "n", "N" -> ChessPiece.PieceType.KNIGHT;
          case "QUEEN", "queen", "q", "Q" -> ChessPiece.PieceType.QUEEN;
          default -> throw new ResponseException(500, "Invalid promotion");
        };
      } else {
        throw new ResponseException(500, "can only promote pawns");
      }

    }

    ChessMove move=new ChessMove(startPosition, endPosition, promotion);
    WebsocketFacade ws=new WebsocketFacade(url, this.repl);

    ws.move(gameId, move, serverFacade.getAuthToken(), color);
    return "Moving";
  }

  private void initPositionMap() {
    String[] letterArray={"a", "b", "c", "d", "e", "f", "g", "h"};
    for (int i=0; i < 8; i++) {
      for (int j=0; j < 8; j++) {
        String positionString=String.format("%s%d", letterArray[j], i + 1);
        positionMap.put(positionString, new ChessPosition(i + 1, j + 1));
      }
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
    this.games=serverFacade.list();
    for (GameData gameData1 : games) {
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

  public static ChessPosition assertCord(String move) throws ResponseException {
    if (move.length() != 2) {
      throw new ResponseException(500, "Try to format it like this: <row><col>");
    }
    int col=move.charAt(0) - 'a' + 1;
    int row=Character.getNumericValue(move.charAt(1));
    if (row < 0) {
      row*=-1;
    }
    if (col >= 1 && col <= 8 && row >= 1 && row <= 8) {
      return new ChessPosition(row, col);
    }
    throw new ResponseException(500, "position out of board");
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

