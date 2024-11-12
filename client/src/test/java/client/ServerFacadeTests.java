package client;

import org.junit.jupiter.api.*;
import server.Server;
import ui.ServerFacade;


public class ServerFacadeTests {

  private static Server server;
  static ServerFacade facade;

  @BeforeAll
  public static void init() {
    server=new Server();
    var port=server.run(8080);
    System.out.println("Started test HTTP server on " + port);
    var serverURL="http://localhost:8080";
    facade=new ServerFacade(serverURL);
  }

  @AfterAll
  static void stopServer() {
    server.stop();
  }


  @Test
  public void registerPass() {

  }

}
