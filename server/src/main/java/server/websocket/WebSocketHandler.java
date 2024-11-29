package server.websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import service.AuthService;
import service.GameService;
import service.UserService;
import websocket.commands.JoinGameCommand;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.Error;
import websocket.messages.LoadGameMessage;
import websocket.messages.Notification;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

@WebSocket
public class WebSocketHandler {
  private final ConnectionManager connectionManager=new ConnectionManager();
  private final AuthService authService;
  private final GameService gameService;
  private final UserService userService;

  public WebSocketHandler(AuthService authService, GameService gameService, UserService userService) {
    this.authService=authService;
    this.gameService=gameService;
    this.userService=userService;
  }


  @OnWebSocketMessage
  public void onMessage(String message, Session session) throws Exception {
    UserGameCommand command=new Gson().fromJson(message, UserGameCommand.class);
    String token=authService.getAuthData(command.getAuthToken()).authToken();
    GameData game=getGame(command.getGameID());

    if (token == null) {
      Error error=new Error(ServerMessage.ServerMessageType.ERROR, "invalid game authtoken");
      session.getRemote().sendString(error.toString());
      return;
    }
    if (game == null) {

      Error error=new Error(ServerMessage.ServerMessageType.ERROR, "invalid game id");
      session.getRemote().sendString(error.toString());
      return;
    }
    connectionManager.add(command.getAuthToken(), session, command.getGameID());
    switch (command.getCommandType()) {
      case RESIGN -> resign(command.getAuthToken(), command.getGameID(), command);
      case CONNECT -> connect(message, session);
      case LEAVE -> leave(command.getAuthToken(), command);
      case MAKE_MOVE -> makeMove(message);
    }
  }


  public void connect(String message, Session session) throws Exception {
    JoinGameCommand command=new Gson().fromJson(message, JoinGameCommand.class);
    connectionManager.add(command.getAuthToken(), session, command.getGameID());


    String name=authService.getAuthData(command.getAuthToken()).username();
    ChessGame game=getGame(command.getGameID()).game();
    if (command.getColor() == null) {
      String messageReturn=String.format("%s has joined your game", name);
      var obs=new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME, game);
      connectionManager.sendMessage(command.getAuthToken(), obs, command.getGameID());

      var notification=new Notification(ServerMessage.ServerMessageType.NOTIFICATION, messageReturn);
      connectionManager.broadcast(command.getAuthToken(), notification, command.getGameID());

    } else {
      var obs=new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME, game);
      connectionManager.sendMessage(command.getAuthToken(), obs, command.getGameID());
      String messageReturn=String.format("%s has joined your game as %s", name, command.getColor());
      var notification=new Notification(ServerMessage.ServerMessageType.NOTIFICATION, messageReturn);
      connectionManager.broadcast(command.getAuthToken(), notification, command.getGameID());

    }

  }


  public void resign(String authToken, int gameID, UserGameCommand command) throws Exception {
    String name=authService.getAuthData(authToken).username();
    GameData chessGame=getGame(gameID);
    ChessGame game=chessGame.game();
    String username=authService.getAuthData(command.getAuthToken()).username();
    GameData data=getGame(gameID);
    ChessGame.TeamColor winner=game.getWinner();
    if (winner != null) {
      var error=new Error(ServerMessage.ServerMessageType.ERROR, "game is already over");
      connectionManager.sendMessage(command.getAuthToken(), error, command.getGameID());
      return;
    }
    if (!Objects.equals(name, data.blackUsername()) && !Objects.equals(username, data.whiteUsername())) {
      Error error=new Error(ServerMessage.ServerMessageType.ERROR, "observers can't resign");
      connectionManager.sendMessage(command.getAuthToken(), error, command.getGameID());
      return;
    }


    String winn;
    if (game.getTeamTurn() == ChessGame.TeamColor.WHITE) {
      winn=chessGame.blackUsername();
      game.resign(ChessGame.TeamColor.WHITE);
      gameService.updateGame((new GameData(data.gameID(), data.whiteUsername(),
              data.blackUsername(), data.gameName(), game)));
    } else {
      winn=chessGame.whiteUsername();
      game.resign(ChessGame.TeamColor.BLACK);
      gameService.updateGame((new GameData(data.gameID(), data.whiteUsername(),
              data.blackUsername(), data.gameName(), game)));
    }
    Notification winMess=new Notification(ServerMessage.ServerMessageType.NOTIFICATION, String.format("%s has resigned", winn));
    connectionManager.broadcast(authToken, winMess, command.getGameID());
    connectionManager.sendMessage(authToken, winMess, command.getGameID());
  }

  public void leave(String authToken, UserGameCommand command) throws Exception {
    connectionManager.remove(authToken, command.getGameID());
    var gameID=command.getGameID();
    String username=authService.getAuthData(command.getAuthToken()).username();
    GameData game=getGame(gameID);
    ChessGame chessGame=game.game();
    String name=authService.getAuthData(authToken).username();
    String message=String.format("%s has left your game", name);
    var notification=new Notification(ServerMessage.ServerMessageType.NOTIFICATION, message);

    if (Objects.equals(game.blackUsername(), username)) {
      game=new GameData(game.gameID(), game.whiteUsername(), null, game.gameName(), game.game());
    } else if (Objects.equals(game.whiteUsername(), username)) {
      game=new GameData(game.gameID(), null, game.blackUsername(), game.gameName(), game.game());
    }
    gameService.updateGame(game);
    var sender=new Notification(ServerMessage.ServerMessageType.NOTIFICATION, "you have left the game");
    connectionManager.sendMessage(authToken, sender, command.getGameID());
    connectionManager.remove(authToken, command.getGameID());
    connectionManager.broadcast(authToken, notification, command.getGameID());
  }

  private GameData getGame(int gameID) throws IOException {
    try {
      GameData realGame=null;
      ArrayList<GameData> gameData=gameService.getAllGames();
      int id=gameID;
      int i=1;
      for (GameData game : gameData) {
        if (i == id) {
          realGame=game;
          break;
        }
        i++;
      }
      return realGame;
    } catch (Exception e) {
      throw new IOException(e.getMessage());
    }
  }

  private void makeMove(String message) throws Exception {
    MakeMoveCommand command=new Gson().fromJson(message, MakeMoveCommand.class);
    GameData gameData=getGame(command.getGameID());

    if (gameData == null || gameData.game() == null) {
      connectionManager.sendMessage(command.getAuthToken(), new Error(ServerMessage.ServerMessageType.ERROR, "Invalid game ID"), command.getGameID());
      return;
    }

    ChessGame chessGame=gameData.game();
    ChessGame.TeamColor turn=chessGame.getTeamTurn();
    String username=authService.getAuthData(command.getAuthToken()).username();

    if (turn == null) {
      connectionManager.sendMessage(command.getAuthToken(), new Error(ServerMessage.ServerMessageType.ERROR, "Invalid turn"), command.getGameID());
      return;
    }

    ChessGame.TeamColor yourColor=null;
    if (Objects.equals(gameData.blackUsername(), username)) {
      yourColor=ChessGame.TeamColor.BLACK;
    } else if (Objects.equals(gameData.whiteUsername(), username)) {
      yourColor=ChessGame.TeamColor.WHITE;
    }

    if (!Objects.equals(yourColor, turn)) {
      connectionManager.sendMessage(command.getAuthToken(), new Error(ServerMessage.ServerMessageType.ERROR, "Not your turn or observer core"), command.getGameID());
      return;
    }

    try {
      chessGame.makeMove(command.getMove());
      gameService.updateGame(gameData);
    } catch (Exception e) {
      String errorM=String.format("Move from %s to %s is invalid",
              convertCoords(command.getMove().getStartPosition().getRow(), command.getMove().getStartPosition().getColumn(),
                      command.getMove().getEndPosition().getColumn(), command.getMove().getEndPosition().getRow()));
      connectionManager.sendMessage(command.getAuthToken(), new Error(ServerMessage.ServerMessageType.ERROR, errorM), command.getGameID());
      return;
    }

    // Broadcast notifications
    String note=String.format("%s moved %s", username,
            convertCoords(command.getMove().getStartPosition().getRow(), command.getMove().getStartPosition().getColumn(),
                    command.getMove().getEndPosition().getColumn(), command.getMove().getEndPosition().getRow()));
    connectionManager.broadcast(command.getAuthToken(), new Notification(ServerMessage.ServerMessageType.NOTIFICATION, note), command.getGameID());

    if (chessGame.isInCheckmate(chessGame.getTeamTurn())) {
      chessGame.resign(chessGame.getTeamTurn());
      String winner=(chessGame.getTeamTurn() == ChessGame.TeamColor.BLACK) ? gameData.whiteUsername() : gameData.blackUsername();
      Notification winMess=new Notification(ServerMessage.ServerMessageType.NOTIFICATION, String.format("%s is in checkmate", winner));
      connectionManager.broadcast(command.getAuthToken(), winMess, command.getGameID());
      gameService.updateGame(gameData);
      return;
    }

    if (chessGame.isInStalemate(chessGame.getTeamTurn())) {
      Notification stalemate=new Notification(ServerMessage.ServerMessageType.NOTIFICATION, "Stalemate");
      connectionManager.broadcast(command.getAuthToken(), stalemate, command.getGameID());
      return;
    }

    if (chessGame.isInCheck(chessGame.getTeamTurn())) {
      Notification check=new Notification(ServerMessage.ServerMessageType.NOTIFICATION, String.format("%s is in check", chessGame.getTeamTurn()));
      connectionManager.broadcast(command.getAuthToken(), check, command.getGameID());
    }
  }


  public String convertCoords(int row1, int col1, int col2, int row2) {

    char columnLetter1=(char) ('a' + col1 - 1); // '1' becomes 'a', '2' becomes 'b', etc.
    char columnLetter2=(char) ('a' + col2 - 1);

    String coord1="" + columnLetter1 + row1;
    String coord2="" + columnLetter2 + row2;

    return coord1 + " " + coord2;
  }
}



