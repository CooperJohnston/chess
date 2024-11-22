package server.websocket;

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
      GameData realGame=null;
      ArrayList<GameData> gameData=gameService.getAllGames();
      int id=command.getGameID();
      int i=1;
      for (GameData game : gameData) {
        if (i == id) {
          realGame=game;
          break;
        }
        i++;
      }
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
}
