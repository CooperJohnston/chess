package websocket;


import chess.*;
import com.google.gson.Gson;
import exception.ErrorException;
import exception.ResponseException;
import ui.ServerObserver;
import websocket.commands.JoinGameCommand;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;


import javax.print.DocFlavor;
import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.WebSocket;
import java.util.Collection;
import java.util.Locale;

import static ui.EscapeSequences.*;
import static ui.EscapeSequences.SET_BG_COLOR_DARK_GREEN;


public class WebsocketFacade extends Endpoint {
  ServerObserver notificationHandler;
  Session session;

  public WebsocketFacade(String url, ServerObserver notificationHandler) throws ResponseException {
    try {
      url=url.replace("http", "ws");
      URI socketURI=new URI(url + "/ws");
      this.notificationHandler=notificationHandler;

      WebSocketContainer container=ContainerProvider.getWebSocketContainer();
      this.session=container.connectToServer(this, socketURI);

      session.addMessageHandler(new MessageHandler.Whole<String>() {

        @Override
        public void onMessage(String message) {
          notificationHandler.notify(message);
        }
      });
    } catch (DeploymentException | IOException | URISyntaxException ex) {
      throw new ResponseException(500, ex.getMessage());
    }
  }

  @Override
  public void onOpen(Session session, EndpointConfig endpointConfig) {
  }


  public void playGame(ChessGame.TeamColor passColor, String authToken, int gameID) throws ResponseException {
    try {
      JoinGameCommand action;
      if (passColor == null) {
        //System.out.println("ACTION");
        action=new JoinGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID, null);
      } else {
        //System.out.println("WATCHING");
        action=new JoinGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID, passColor);
      }
      this.session.getBasicRemote().sendText(new Gson().toJson(action));
    } catch (IOException ex) {
      throw new ResponseException(500, ex.getMessage());
    }
  }

  public void leaveGame(String authToken, int gameID) throws ResponseException {
    try {
      var action=new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID);
      this.session.getBasicRemote().sendText(new Gson().toJson(action));
    } catch (IOException e) {
      throw new ResponseException(500, e.getMessage());
    }

  }

  public void resign(String authToken, int gameID) throws ResponseException {
    UserGameCommand action=new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID);
    try {
      this.session.getBasicRemote().sendText(new Gson().toJson(action));
    } catch (IOException e) {
      throw new ResponseException(500, e.getMessage());
    }

  }

  public void move(Integer gameID, ChessMove move, String auth, ChessGame.TeamColor color) throws ErrorException {
    try {
      MakeMoveCommand cmd=new MakeMoveCommand(auth, gameID, move);
      this.session.getBasicRemote().sendText(new Gson().toJson(cmd));
    } catch (IOException e) {
      throw new ErrorException(500, e.getMessage());
    }
  }


}




