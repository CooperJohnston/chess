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
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
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
