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
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
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
  public void onMessage(String message, Session session) throws IOException {
    try {
      UserGameCommand command=new Gson().fromJson(message, UserGameCommand.class);
      boolean authorized=authService.authenticate(command.getAuthToken());
      GameData realGame=getGame(command.getGameID());
      if (realGame == null) {
        ErrorMessage error=new ErrorMessage(ServerMessage.ServerMessageType.ERROR, "invalid game id");
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

    } catch (Exception e) {
      ErrorMessage error=new ErrorMessage(ServerMessage.ServerMessageType.ERROR, "invalid game authtoken");
      session.getRemote().sendString(error.toString());
    }

  }

  public void connect(String message, Session session) throws IOException {
    try {
      JoinGameCommand command=new Gson().fromJson(message, JoinGameCommand.class);
      connectionManager.add(command.getAuthToken(), session, command.getGameID());
      String username=authService.getAuthData(command.getAuthToken()).username();
      ChessGame chessGame=getGame(command.getGameID()).game();
      if (command.getColor() == null) {
        String messageReturn=String.format("%s has joined your game", username);
        var obs=new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME, chessGame);
        connectionManager.sendMessage(command.getAuthToken(), obs, command.getGameID());

        var notification=new Notification(ServerMessage.ServerMessageType.NOTIFICATION, messageReturn);
        connectionManager.broadcast(command.getAuthToken(), notification, command.getGameID());

      } else {
        var obs=new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME, chessGame);
        connectionManager.sendMessage(command.getAuthToken(), obs, command.getGameID());
        String messageReturn=String.format("%s has joined your game as %s", username, command.getColor());
        var notification=new Notification(ServerMessage.ServerMessageType.NOTIFICATION, messageReturn);
        connectionManager.broadcast(command.getAuthToken(), notification, command.getGameID());

      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }


  public void resign(String authToken, int gameID, UserGameCommand command) throws IOException {
    try {
      String userOne=authService.getAuthData(authToken).username();
      GameData currData=getGame(command.getGameID());
      ChessGame chessGame=currData.game();
      ChessGame.TeamColor turn=chessGame.getTeamTurn();
      String userTwo=authService.getAuthData(command.getAuthToken()).username();
      ChessGame.TeamColor winner=chessGame.getWinner();
      if (winner != null) {
        var error=new ErrorMessage(ServerMessage.ServerMessageType.ERROR, "game is already over");
        connectionManager.sendMessage(command.getAuthToken(), error, command.getGameID());
        return;
      }
      if (!Objects.equals(userOne, currData.blackUsername()) && !Objects.equals(userTwo, currData.whiteUsername())) {
        ErrorMessage error=new ErrorMessage(ServerMessage.ServerMessageType.ERROR, "observers can't resign");
        connectionManager.sendMessage(command.getAuthToken(), error, command.getGameID());
        return;
      }

      String victor;
      if (turn == ChessGame.TeamColor.WHITE) {
        victor=currData.blackUsername();
        chessGame.resign(ChessGame.TeamColor.WHITE);
        GameData winnerData=new GameData(currData.gameID(), userTwo, userOne, currData.gameName(), chessGame);
        gameService.addWinner(winnerData);
      } else {
        victor=currData.whiteUsername();
        chessGame.resign(ChessGame.TeamColor.BLACK);
        GameData winnerData=new GameData(currData.gameID(), userTwo, userOne, currData.gameName(), chessGame);
        gameService.addWinner(winnerData);

      }
      Notification winMess=new Notification(ServerMessage.ServerMessageType.NOTIFICATION, String.format("%s has resigned", victor));
      connectionManager.broadcast(authToken, winMess, command.getGameID());
      connectionManager.sendMessage(authToken, winMess, command.getGameID());

    } catch (Exception e) {
      ErrorMessage error=new ErrorMessage(ServerMessage.ServerMessageType.ERROR, "invalid game authtoken");


    }


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


}
