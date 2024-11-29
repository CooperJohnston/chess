package ui;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import com.google.gson.Gson;
import exception.ResponseException;
import model.GameData;
import websocket.WebsocketFacade;

import java.util.*;

import static ui.EscapeSequences.*;
import static ui.EscapeSequences.WHITE_BISHOP;

public class Client {
  public ChessGame.TeamColor color;
  private String username=null;
  private String password=null;
  private ChessIllustrator chessIllustrator;
  private final ServerFacade serverFacade;
  private State state=State.SIGNEDOUT;
  private GameState gameState;
  WebsocketFacade websocketFacade;
  int identificaton;
  ChessGame.TeamColor teamColor;
  ArrayList<GameData> gameData;
  private final String url;
  private final ServerNotifications serverNotifications;

  public Client(String url, ServerNotifications notifications) {
    this.serverNotifications=notifications;
    serverFacade=new ServerFacade(url);
    this.chessIllustrator=new ChessIllustrator();
    this.url=url;
  }

  public String eval(String input) {
    try {
      var tokens=input.toLowerCase().split(" ");
      var cmd=(tokens.length > 0) ? tokens[0] : "help";
      var params=Arrays.copyOfRange(tokens, 1, tokens.length);
      return switch (cmd) {
        case "logout" -> logout();
        case "login" -> login(params);
        case "list" -> list();
        case "playgame" -> join(params);
        case "create" -> create(params);
        case "observe" -> observe(params);
        case "register" -> register(params);
        case "redraw" -> redraw();
        case "leave" -> leave();
        case "move" -> makeMove(params);
        case "resign" -> resign();
        case "highlightmoves" -> listMoves(params);
        case "quit" -> "quit";
        default -> help();

      };
    } catch (ResponseException e) {
      return e.getMessage();
    }
  }

  private String makeMove(String[] params) throws ResponseException {
    checkAuth();
    if (gameState != GameState.PLAYING) {
      return "you are not playing a game";
    }
    if (params.length != 2 && params.length != 3) {
      throw new ResponseException(500, "expected: makeMove <from> <to>");
    }
    ChessPosition start=checkCoord(params[0]);
    ChessPosition end=checkCoord(params[1]);
    String promotion="null";
    if (params.length == 3) {
      promotion=params[2];
    }
    this.gameData=this.serverFacade.list();
    GameData game=null;
    for (var c : this.gameData) {
      if (c.gameID() == this.identificaton) {
        game=c;
      }
    }
    assert game != null;
    ChessPiece piece=game.game().getBoard().getPiece(start);
    if (piece == null) {
      return "no piece to move";

    }
    if (piece.getTeamColor() != this.color) {
      return "You cannot move that piece";
    }
    try {
      websocketFacade.makeMove(start, end, promotion, serverFacade.getAuthToken(), game.gameID(), this.color);
      return "";
    } catch (ResponseException e) {
      throw new ResponseException(500, e.getMessage());
    }
  }

  private String resign() throws ResponseException {
    checkAuth();
    if (gameState != GameState.PLAYING) {
      return "you are not playing a game";
    }
    System.out.print("Do you want to resign?\nY|N\n");
    Scanner scanner=new Scanner(System.in);
    String res=scanner.nextLine().strip();
    if (res.equalsIgnoreCase("y")) {
      websocketFacade.resign(serverFacade.getAuthToken(), this.identificaton);
    }
    return "";
  }

  private String redraw() throws ResponseException {
    checkAuth();
    if (gameState != GameState.PLAYING) {
      return "you are not playing a game";
    }

    this.gameData=this.serverFacade.list();
    for (var c : this.gameData) {
      if (c.gameID() == this.identificaton) {
        if (this.color == ChessGame.TeamColor.BLACK) {
          printBoard(c.game(), false, null, null);
          return "";
        }
        printBoard(c.game(), true, null, null);
        return "";
      }
    }
    return "can't find your game brother, sorry";
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
      for (var game : this.gameData) {
        if (i == id) {
          websocketFacade=new WebsocketFacade(this.url, serverNotifications);
          websocketFacade.playGame("", this.serverFacade.getAuthToken(), game.gameID());
          this.gameState=GameState.PLAYING;
          return "";
        }
        i++;
      }
      throw new ResponseException(400, "invalid ID");
    } else {
      throw new ResponseException(400, "Expected: <your name> <password>");
    }
  }

  public String join(String[] params) throws ResponseException {
    checkAuth();

    if (params.length != 2) {
      throw new ResponseException(400, "Expected: playgame <gameID> <[BLACK|WHITE]>");
    }

    int id;
    try {
      id=Integer.parseInt(params[0]);
    } catch (NumberFormatException e) {
      throw new ResponseException(401, "input must be a valid index");
    }

    int i=1;
    for (var game : this.gameData) {
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
        this.identificaton=game.gameID();
        websocketFacade=new WebsocketFacade(this.url, serverNotifications);
        websocketFacade.playGame(params[1], this.serverFacade.getAuthToken(), game.gameID());
        return "";
      }
      serverFacade.join(game.gameID(), this.color);
      this.gameState=GameState.PLAYING;
      websocketFacade=new WebsocketFacade(this.url, serverNotifications);
      websocketFacade.playGame(params[1], this.serverFacade.getAuthToken(), game.gameID());
      this.identificaton=game.gameID();
      return "";
    }
    throw new ResponseException(400, "invalid ID");
  }

  private String listMoves(String[] params) throws ResponseException {
    if (params.length != 1) {
      throw new ResponseException(500, "expected: HighlightMoves <location>");
    }
    ChessPosition start=checkCoord(params[0]);
    this.gameData=serverFacade.list();
    for (var c : this.gameData) {
      if (c.gameID() == this.identificaton) {
        Collection<ChessMove> moves=c.game().validMoves(start);
//        System.out.println(this.getPiece(c.game().getBoard().getPiece(start)));
        if (this.color == ChessGame.TeamColor.BLACK) {

          printBoard(c.game(), false, moves, start);
          return "";
        } else {
          printBoard(c.game(), true, moves, start);
          return "";

        }
      }
    }
    throw new ResponseException(500, "could not validate game");
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


  public void printBoard(ChessGame game, boolean isWhiteView, Collection<ChessMove> moves, ChessPosition start) {
    ChessPiece[][] board=game.getBoard().getBoard();
    String[][] stringBoard=convertBoard(board);
    boolean[][] possibleMoves=addMoves(moves, start);
    chessIllustrator.drawBoard(stringBoard, isWhiteView, possibleMoves);
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

  public static String getPiece(ChessPiece piece) {
    if (piece == null) {
      return "   ";
    }
    String[] symbols=piece.getTeamColor() == ChessGame.TeamColor.BLACK
            ? new String[]{BLACK_PAWN, BLACK_ROOK, BLACK_KNIGHT, BLACK_KING, BLACK_QUEEN, BLACK_BISHOP}
            : new String[]{WHITE_PAWN, WHITE_ROOK, WHITE_KNIGHT, WHITE_KING, WHITE_QUEEN, WHITE_BISHOP};

    return getString(piece, symbols);
  }

  public static ChessPosition checkCoord(String move) throws ResponseException {
    if (move.length() != 2) {
      throw new ResponseException(500, "expect: <row><col>");
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


  public String leave() throws ResponseException {
    checkAuth();
    if (gameState != GameState.PLAYING) {
      return "you are not playing a game";
    }
    websocketFacade.leaveGame(serverFacade.getAuthToken(), this.identificaton);
    identificaton=-1;
    websocketFacade=null;
    gameState=GameState.NOTPLAYING;
    return "You have left the game";
  }

}

