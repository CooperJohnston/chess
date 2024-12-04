package server.websocket;


import com.google.gson.Gson;
import server.websocket.Connection;
import websocket.messages.Notification;

import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {

  private record Pair<T, R>(T item1, R item2) {
  }


  private final ConcurrentHashMap<Pair<Integer, String>, Connection> connections=new ConcurrentHashMap<>();

  public void add(String visitorName, Session session, int game) {
    Connection connection=new Connection(game, session, visitorName);
    Pair<Integer, String> userPair=new Pair<>(game, visitorName);
    connections.put(userPair, connection);
  }

  public void remove(String username, int game) {
    Pair<Integer, String> idClientPair=new Pair<>(game, username);
    connections.remove(idClientPair);
  }


  public void broadcast(String excludeToken, ServerMessage notification, int game) throws IOException {

    var removeList=new ArrayList<Connection>();
    for (var connection : connections.values()) {
      if (connection.session.isOpen()) {
        if (Objects.equals(connection.gameID, game) && !connection.username.equals(excludeToken)) {
          connection.send(new Gson().toJson(notification));
        }
      } else {
        removeList.add(connection);
      }
    }

    // Clean up any connections that were left open.
    for (var c : removeList) {
      this.remove(c.username, c.gameID);
    }
  }

  public void remove(Integer game, String username) {
    Pair<Integer, String> idClientPair=new Pair<>(game, username);
    connections.remove(idClientPair);
  }

  public Connection get(Integer game, String username) {
    Pair<Integer, String> idClientPair=new Pair<>(game, username);
    return connections.get(idClientPair);
  }


  public void sendMessage(String username, ServerMessage notification, int game) throws IOException {
    assert connections.containsKey(new Pair<>(game, username));

    Connection connection=get(game, username);
    if (!connection.session.isOpen()) {
      remove(username, game);
      return;
    }
    connection.send(new Gson().toJson(notification));
  }


}
