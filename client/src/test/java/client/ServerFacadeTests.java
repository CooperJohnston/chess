package client;

import chess.ChessGame;
import exception.ResponseException;
import model.GameData;
import org.junit.jupiter.api.*;
import server.Server;
import ui.ServerFacade;

import java.util.ArrayList;


public class ServerFacadeTests {

  private static Server server;
  static ServerFacade facade;

  @BeforeAll
  public static void init() throws Exception {
    server=new Server();
    var port=server.run(8080);
    System.out.println("Started test HTTP server on " + port);
    var serverURL="http://localhost:8080";
    facade=new ServerFacade(serverURL);
    facade.clear();
  }

  @BeforeEach
  public void initTest() throws Exception {
    facade.clear();
  }

  @AfterAll
  static void stopServer() {
    server.stop();
  }


  @Test
  public void registerPass() throws Exception {
    Assertions.assertDoesNotThrow(() -> facade.register("test", "test", "test"));
  }

  @Test
  public void registerFail() throws ResponseException {
    facade.register("test1", "test2", "test3");
    Assertions.assertThrows(ResponseException.class, () ->
            facade.register("test1", "test2", "test3"));
  }

  @Test
  public void loginPass() throws ResponseException {
    facade.register("test5", "test6", "test7");
    facade.logout();
    Assertions.assertDoesNotThrow(() -> facade.login("test5", "test6"));
  }

  @Test
  public void loginFail() throws ResponseException {
    Assertions.assertThrows(ResponseException.class,
            () -> facade.login("Naruto", "Hokage"));
  }

  @Test
  public void logoutPass() throws ResponseException {
    facade.register("SasukeUchiha", "Avenger", "one_and_only@uchiha.net");
    Assertions.assertDoesNotThrow(() -> facade.logout());
  }

  @Test
  public void logoutFail() throws ResponseException {
    facade.register("P:oo]]", "pee", "morepoo");
    facade.logout();
    Assertions.assertThrows(ResponseException.class, () -> facade.logout());
  }

  @Test
  public void createPass() throws ResponseException {
    facade.register("NarutoUzamaki", "Ramen", "rasegan@hokage.net");
    Assertions.assertDoesNotThrow(() -> facade.create("BelieveIt"));
  }

  @Test
  public void createFail() throws ResponseException {
    facade.register("Boruto", "baby", "nerf");
    facade.logout();
    Assertions.assertThrows(ResponseException.class, () -> facade.create("BelieveIt"));
  }

  @Test
  public void listPass() throws ResponseException {
    facade.register("morgan", "winston", "lulul");
    Assertions.assertNotNull(facade.list());
  }

  @Test
  public void listFail() throws ResponseException {
    Assertions.assertThrows(ResponseException.class, () -> facade.list());
  }

  @Test
  public void joinPass() throws ResponseException {
    facade.register("test400", "test200", "test003");
    facade.create("testGame");
    ArrayList<GameData> games=facade.list();
    for (GameData game : games) {
      if (game.gameName().equals("testGame")) {
        Assertions.assertDoesNotThrow(() -> facade.join(game.gameID(), ChessGame.TeamColor.WHITE));

      }
    }

  }

  @Test
  public void joinFail() throws ResponseException {
    facade.register("test400", "test200", "test003");
    Assertions.assertThrows(ResponseException.class, () -> facade.join(5, ChessGame.TeamColor.WHITE));

  }

  @Test
  public void observePass() throws ResponseException {
    facade.register("observeAttempt", "pass", "yahoo");
    facade.create("testGame");
    ArrayList<GameData> chessGames=facade.list();
    for (GameData game : chessGames) {
      if (game.gameName().equals("testGame")) {
        int charles=game.gameID();
        Assertions.assertDoesNotThrow(() -> facade.observe(game.gameID()));
      }
    }

  }

  @Test
  public void observeFail() throws ResponseException {
    facade.register("badObserve", "passwoid", "emoil");
    facade.create("take two wiht phoneas and forb");
    Assertions.assertThrows(ResponseException.class, () -> facade.observe(7));
  }

  @Test
  public void clearPass() throws ResponseException {
    Assertions.assertDoesNotThrow(() -> facade.clear());
  }

}
