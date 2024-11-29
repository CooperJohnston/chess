package server.websocket;

import server.websocket.Connection;
import websocket.messages.Notification;

import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
  //  public final ConcurrentHashMap<String, Connection> connections = ;
  private final Map<Integer, ConcurrentHashMap<String, Connection>> games=new HashMap<>();

  public void add(String visitorName, Session session, int game) {
    var connection=new Connection(visitorName, session);
    if (games.containsKey(game)) {
      var conn=games.get(game);
      conn.put(visitorName, connection);
    } else {
      var temp=new ConcurrentHashMap<String, Connection>();
      temp.put(visitorName, connection);
      games.put(game, temp);
    }


  }


  public void remove(String authToken, int game) {
    var connections=games.get(game);
    connections.remove(authToken);
  }


  public void broadcast(String excludeToken, ServerMessage notification, int game) throws IOException {
    var removeList=new ArrayList<Connection>();
    var connections=games.get(game);
    for (var c : connections.values()) {
      if (c.session.isOpen()) {
        if (!c.authToken.equals(excludeToken)) {
          c.send(notification.toString());
        }
      } else {
        removeList.add(c);
      }
    }

    // Clean up any connections that were left open.
    for (var c : removeList) {
      connections.remove(c.authToken);
    }
  }

  public void sendMessage(String token, ServerMessage notification, int game) throws IOException {
    var removeList=new ArrayList<Connection>();
    var connections=games.get(game);
    for (var c : connections.values()) {
      if (c.session.isOpen()) {
        if (c.authToken.equals(token)) {
          c.send(notification.toString());
        }
      } else {
        removeList.add(c);
      }
    }
    // Clean up any connections that were left open.
    for (var c : removeList) {
      connections.remove(c.authToken);
    }
  }
}