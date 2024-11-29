package ui;

import chess.*;
import com.google.gson.Gson;
import exception.ResponseException;
import model.GameData;
import websocket.WebsocketFacade;

import java.util.*;

import static ui.EscapeSequences.*;
import static websocket.WebsocketFacade.*;

public class Client {
  ServerFacade server;
  State state;
  ArrayList<GameData> games;
  List<String> letters;
  String username;
  GameState playing;
  ServerNotifications observer;
  WebsocketFacade ws;
  String site;
  int gameID;
  ChessGame.TeamColor color;

  public Client(String site, ServerNotifications observer) {
    this.server=new ServerFacade(site);
    this.state=State.SIGNEDOUT;
    this.letters=Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h");
    this.playing=GameState.NOTPLAYING;
    this.observer=observer;
    this.site=site;

  }

  public String help() {
    if (this.playing == GameState.PLAYING) {
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
              - login <username> <password>
              - quit - get outta here
              - help
              - register <name> <password> <email>
              """;
    }
    return """
            - list - gets all games
            - create <game name>
            - playGame <game ID> [white|black]
            - observe <game ID>
            - logout
            - quit
            """;
  }

  public String eval(String input) {
    try {
      var tokens=input.toLowerCase().split(" ");
      var cmd=(tokens.length > 0) ? tokens[0] : "help";
      var params=Arrays.copyOfRange(tokens, 1, tokens.length);
      return switch (cmd) {
        case "logout" -> logout();
        case "login" -> login(params);
        case "list" -> listGames();
        case "playgame" -> joinGame(params);
        case "create" -> newGame(params);
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
    } catch (ResponseException ex) {
      return ex.getMessage();
    }
  }

  private String makeMove(String[] params) throws ResponseException {
    assertSignedIn();
    if (playing != GameState.PLAYING) {
      return "you are not playing a game";
    }
    if (params.length != 2 && params.length != 3) {
      throw new ResponseException(500, "expected: makeMove <from> <to>");
    }
    ChessPosition start=assertCord(params[0]);
    ChessPosition end=assertCord(params[1]);
    String promotion="null";
    if (params.length == 3) {
      promotion=params[2];
    }
    this.games=this.server.list();
    GameData game=null;
    for (var c : this.games) {
      if (c.gameID() == this.gameID) {
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
      ws.makeMove(start, end, promotion, server.getAuthToken(), game.gameID(), this.color);
      return "";
    } catch (ResponseException e) {
      throw new ResponseException(500, e.getMessage());
    }

  }

  private String resign() throws ResponseException {
    assertSignedIn();
    if (playing != GameState.PLAYING) {
      return "you are not playing a game";
    }
    System.out.print("Do you want to resign?\nY|N\n");
    Scanner scanner=new Scanner(System.in);
    String res=scanner.nextLine().strip();
    if (res.equalsIgnoreCase("y")) {
      ws.resign(server.getAuthToken(), this.gameID);
    }
    return "";
  }

  private String redraw() throws ResponseException {
    assertSignedIn();
    if (playing != GameState.PLAYING) {
      return "you are not playing a game";
    }

    this.games=this.server.list();
    for (var c : this.games) {
      if (c.gameID() == this.gameID) {
        if (this.color == ChessGame.TeamColor.BLACK) {
          return printBlackBoard(c.game());
        }
        return printWhite(c.game());
      }
    }
    return "can't find your game brother, sorry";
  }

  private String newGame(String[] params) throws ResponseException {
    assertSignedIn();
    if (params.length == 1) {
      server.create(params[0]);
      return "Bingo";
    } else {
      throw new ResponseException(400, "Expected: <gameName>");
    }
  }

  public String listGames() throws ResponseException {
    assertSignedIn();
    this.games=server.list();
    var result=new StringBuilder();
    var gson=new Gson();
    int i=1;
    if (games.isEmpty()) {
      System.out.println("No games available");
    }
    for (var game : this.games) {
      result.append(String.format("%d. %s%n", i, game.gameName()));

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
      i++;
    }

    return result.toString();
  }

  public String joinGame(String[] params) throws ResponseException {
    assertSignedIn();

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
        this.playing=GameState.PLAYING;
        this.gameID=game.gameID();
        ws=new WebsocketFacade(this.site, observer);
        ws.playGame(params[1], this.server.getAuthToken(), game.gameID());
        return "";
      }
      server.join(game.gameID(), color);
      this.playing=GameState.PLAYING;
      ws=new WebsocketFacade(this.site, observer);
      ws.playGame(params[1], this.server.getAuthToken(), game.gameID());
      this.gameID=game.gameID();
      return "";
    }
    throw new ResponseException(400, "invalid ID");
  }


  public String observe(String[] params) throws ResponseException {
    assertSignedIn();
    int id;
    if (params.length == 1) {
//      records.ResponseObject response = server.joinGame(params[0], params[0]);
      try {
        id=Integer.parseInt(params[0]);
      } catch (NumberFormatException e) {
        throw new ResponseException(401, "input must be a valid index");
      }
      int i=1;
      for (var game : this.games) {
        if (i == id) {
          ws=new WebsocketFacade(this.site, observer);
          ws.playGame("", this.server.getAuthToken(), game.gameID());
          this.playing=GameState.PLAYING;
          return "";
        }
        i++;
      }
      throw new ResponseException(400, "invalid ID");
    } else {
      throw new ResponseException(400, "Expected: <your name> <password>");
    }
  }

  public String printWhite(ChessGame game) {
    ChessBoard board=game.getBoard();
    var result=new StringBuilder();
    appendColumnLabels(result);
    for (int row=8; row >= 1; row--) {
      reverseRow(result, row, board);
    }
    appendColumnLabels(result);
    return result.toString();
  }

  public String printBlackBoard(ChessGame game) {
    ChessBoard board=game.getBoard();
    var result=new StringBuilder();
    appendReverseLabels(result);
    for (int row=1; row <= 8; row++) {
      appendRow(result, row, board);
    }
    appendReverseLabels(result);
    return result.toString();
  }

  private void appendColumnLabels(StringBuilder result) {
    result.append("   ");
    for (String c : letters) {
      result.append("  ").append(c).append("  ");
    }
    result.append("\n");
  }

  private void appendReverseLabels(StringBuilder result) {
    result.append("   ");
    for (String c : letters.reversed()) {
      result.append("  ").append(c).append("  ");
    }
    result.append("\n");
  }

  private void appendRow(StringBuilder result, int row, ChessBoard board) {
    result.append(" ").append(row).append(" ");
    boardBuilder(result, row, board);
    result.append(RESET_BG_COLOR).append(" ").append(row).append("\n");
  }

  private void reverseRow(StringBuilder result, int row, ChessBoard board) {
    result.append(" ").append(row).append(" "); // Row label
    reversedBuilder(result, row, board); // Build row content
    result.append(RESET_BG_COLOR).append(" ").append(row).append("\n");
  }

  public String logout() throws ResponseException {
    assertSignedIn();
    try {
      if (this.playing == GameState.PLAYING) {
        this.leave();
      }
      server.logout();
      state=State.SIGNEDOUT;
      return "You have been logged out";
    } catch (Exception e) {
      throw new ResponseException(500, "Failed to logout");
    }
  }

  public String register(String[] params) throws ResponseException {
    if (params.length == 3) {
      server.register(params[0], params[1], params[2]);
      username=params[0];
      state=State.SIGNEDIN;
      return "";
    } else {
      throw new ResponseException(400, "Expected: <your name> <password> <email>");
    }
  }

  public String login(String[] params) throws ResponseException {
    if (params.length == 2) {
      server.login(params[0], params[1]);
      state=State.SIGNEDIN;
      username=params[0];
      return "";
    } else {
      throw new ResponseException(400, "Expected: <your name> <password> <email>");
    }
  }

  private void assertSignedIn() throws ResponseException {
    if (state == State.SIGNEDOUT) {
      throw new ResponseException(400, "You must sign in");
    }
  }

  public void boardBuilder(StringBuilder result, int row, ChessBoard board) {
    for (int col=8; col >= 1; col--) {
      ChessPiece piece=board.getPiece(new ChessPosition(row, col));
      if ((row + col) % 2 != 0) {
        result.append(SET_BG_COLOR_LIGHT_GREY);
      } else {
        result.append(SET_BG_COLOR_DARK_GREEN);
      }
      result.append(" ").append(getPiece(piece)).append(" ");
    }
    result.append(RESET_BG_COLOR).append(" ");
  }

  public void reversedBuilder(StringBuilder result, int row, ChessBoard board) {

    for (int col=1; col <= 8; col++) {
      ChessPiece piece=board.getPiece(new ChessPosition(row, col));
      if ((row + col) % 2 == 0) {
        result.append(SET_BG_COLOR_DARK_GREEN);
      } else {
        result.append(SET_BG_COLOR_LIGHT_GREY);
      }
      result.append(" ").append(getPiece(piece)).append(" ");
    }
    result.append(RESET_BG_COLOR).append(" ");
  }

  public String leave() throws ResponseException {
    assertSignedIn();
    if (playing != GameState.PLAYING) {
      return "you are not playing a game";
    }
    ws.leaveGame(server.getAuthToken(), this.gameID);
    gameID=-1;
    ws=null;
    playing=GameState.NOTPLAYING;
    return "You have left the game";
  }

  private String listMoves(String[] params) throws ResponseException {
    if (params.length != 1) {
      throw new ResponseException(500, "expected: HighlightMoves <location>");
    }
    ChessPosition start=assertCord(params[0]);
    this.games=this.server.list();
    for (var c : this.games) {
      if (c.gameID() == this.gameID) {
        Collection<ChessMove> moves=c.game().validMoves(start);
//        System.out.println(this.getPiece(c.game().getBoard().getPiece(start)));
        if (this.color == ChessGame.TeamColor.BLACK) {

          return printValidBlack(start, moves, c.game());
        } else {
          return printValidWhite(start, moves, c.game());

        }
      }
    }
    throw new ResponseException(500, "could not validate game");
  }

  private String printValidBlack(ChessPosition start, Collection<ChessMove> moves, ChessGame game) {
    ChessBoard board=game.getBoard();
    var result=new StringBuilder();
    appendReverseLabels(result);

    for (int row=1; row <= 8; row++) {
      result.append(" ").append(row).append(" ");
      highlightBuilder(result, row, board, start, moves);
      result.append(RESET_BG_COLOR).append(" ").append(row).append("\n");
    }
    appendReverseLabels(result);

    return result.toString();
  }

  private String printValidWhite(ChessPosition start, Collection<ChessMove> moves, ChessGame game) {
    ChessBoard board=game.getBoard();
    var result=new StringBuilder();
    appendColumnLabels(result);
    for (int row=8; row >= 1; row--) {
      result.append(" ").append(row).append(" ");
      reverseHighlight(result, row, board, start, moves);
      result.append(RESET_BG_COLOR).append(" ").append(row).append("\n");
    }
    appendColumnLabels(result);
    return result.toString();
  }

  public void highlightBuilder(StringBuilder result, int row, ChessBoard board, ChessPosition start, Collection<ChessMove> moves) {

    for (int col=8; col >= 1; col--) {
      reverseRow(result, row, board, start, moves, col);
    }
    result.append(RESET_BG_COLOR).append(" ");
  }

  public void reverseHighlight(StringBuilder result, int row, ChessBoard board, ChessPosition start, Collection<ChessMove> moves) {
    for (int col=1; col <= 8; col++) {
      colorRow(result, row, board, start, moves, col);
    }
    result.append(RESET_BG_COLOR).append(" ");
  }

  private void reverseRow(StringBuilder result, int row, ChessBoard board, ChessPosition start, Collection<ChessMove> moves, int col) {

    repeatPrint(result, row, board, start, moves, col);
  }
}