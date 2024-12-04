package server.websocket;

import chess.*;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.DatabaseGameDAO;
import dataaccess.GameDAO;
import exception.ErrorException;
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
import websocket.messages.LoadGameMessage;
import websocket.messages.Notification;
import websocket.messages.ServerMessage;
import websocket.messages.Error;

import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

@WebSocket
public class WebSocketHandler {
  private final ConnectionManager connectionManager=new ConnectionManager();
  private final AuthService authService;
  private final GameService gameService;
  private final UserService userService;

  private final GameDAO gameDAO;

  {
    try {
      gameDAO=new DatabaseGameDAO();
    } catch (DataAccessException e) {
      throw new RuntimeException(e);
    }
  }


  public WebSocketHandler(AuthService authService, GameService gameService, UserService userService) {
    this.authService=authService;
    this.gameService=gameService;
    this.userService=userService;
  }

  @OnWebSocketMessage
  public void onMessage(Session session, String message) throws IOException {


    UserGameCommand command=new Gson().fromJson(message, UserGameCommand.class);
    try {

      authService.authenticate(command.getAuthToken());
      String username=authService.getAuthData(command.getAuthToken()).username();
      connectionManager.add(username, session, command.getGameID());

      switch (command.getCommandType()) {
        case CONNECT -> connect(command.getGameID(), username);
        case MAKE_MOVE -> makeMove(message, username);
        case RESIGN -> resign(command.getGameID(), username);
        case LEAVE -> leave(command.getGameID(), username);
      }
    } catch (Exception e) {
      sendError(session, e.getMessage());
    }
  }

  private void sendError(Session session, String errorMessage) throws IOException {
    Error error=new Error(ServerMessage.ServerMessageType.ERROR, errorMessage);
    session.getRemote().sendString(new Gson().toJson(error));
  }


  public void connect(int gameID, String username) throws IOException, ErrorException {

    try {
      GameData gameData=gameDAO.getGame(gameID);
      ChessGame game=gameData.game();
      LoadGameMessage loadGameMessage=new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME, game);
      connectionManager.sendMessage(username, loadGameMessage, gameID);

      String participantString=getParticipantString(username, gameData);

      Notification rootJoinedNotification=new Notification(ServerMessage.ServerMessageType.NOTIFICATION, username + " joined game as " + participantString);
      connectionManager.broadcast(username, rootJoinedNotification, gameID);
    } catch (DataAccessException e) {
      throw new ErrorException(500, e.getMessage());
    }
  }

  private String getParticipantString(String username, GameData gameData) {
    if (username.equals(gameData.whiteUsername())) {
      return "WHITE player";
    } else if (username.equals(gameData.blackUsername())) {
      return "BLACK player";
    } else {
      return "an observer";
    }
  }


  public void resign(Integer gameID, String rootClient) throws IOException, ErrorException {
    try {
      GameData gameData=gameDAO.getGame(gameID);

      if (!rootClient.equals(gameData.whiteUsername()) && !rootClient.equals(gameData.blackUsername())) {
        throw new ErrorException(500, "Observers can't resign game");
      }
      if (gameData.game().isOver()) {
        throw new ErrorException(500, "Game is over, can't resign");
      }

      gameData.game().resign();
      gameService.updateGame(gameData);
      Notification notification=new Notification(ServerMessage.ServerMessageType.NOTIFICATION, rootClient + " resigned the game");
      connectionManager.broadcast("", notification, gameID);
    } catch (DataAccessException e) {
      throw new ErrorException(500, e.getMessage());
    }
  }


  public void leave(Integer gameID, String rootClient) throws IOException, ErrorException {
    try {
      GameData gameData=gameDAO.getGame(gameID);
      GameData newData=null;

      if (rootClient.equals(gameData.whiteUsername())) {
        newData=new GameData(gameID, null, gameData.blackUsername(), gameData.gameName(), gameData.game());
      } else if (rootClient.equals(gameData.blackUsername())) {
        newData=new GameData(gameID, gameData.whiteUsername(), null, gameData.gameName(), gameData.game());
      }

      if (newData != null) {
        gameService.updateGame(newData);
      }

      Notification notification=new Notification(ServerMessage.ServerMessageType.NOTIFICATION, rootClient + " left the game");
      connectionManager.broadcast(rootClient, notification, gameID);
      connectionManager.remove(gameID, rootClient);

    } catch (DataAccessException e) {
      throw new ErrorException(500, e.getMessage());
    }

  }

  private GameData getGame(int gameID) throws IOException {
    try {
      GameData realGame=null;
      ArrayList<GameData> gameData=gameService.getAllGames();
      for (GameData game : gameData) {
        if (game.gameID() == gameID) {
          realGame=game;
          break;
        }
      }
      return realGame;
    } catch (Exception e) {
      throw new IOException(e.getMessage());
    }
  }

  private void makeMove(String message, String rootClient) throws ErrorException, IOException, DataAccessException, InvalidMoveException {

    MakeMoveCommand cmd=new Gson().fromJson(message, MakeMoveCommand.class);
    GameData gameData=gameDAO.getGame(cmd.getGameID());

    if (!rootClient.equals(gameData.whiteUsername()) && !rootClient.equals(gameData.blackUsername())) {
      throw new ErrorException(500, "Observers can't move pieces");
    }

    ChessGame game=gameData.game();
    ChessMove move=cmd.getMove();

    if (game.isOver()) {
      throw new ErrorException(500, "Game is over, can't move pieces");
    }


    checkMove(gameData, rootClient, move);

    game.makeMove(move);

    GameData newGameData=new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), game);
    gameService.updateGame(newGameData);

    LoadGameMessage loadMsg=new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME, newGameData.game());
    connectionManager.broadcast("", loadMsg, gameData.gameID());
    Notification moveNotification=new Notification(ServerMessage.ServerMessageType.NOTIFICATION,
            rootClient + " moved ");
    connectionManager.broadcast(rootClient, moveNotification, gameData.gameID());


    if (staleMateNotification(gameData) || checkMateNotification(gameData) || checkNotification(gameData)) {
      return;
    }
  }

  private void checkMove(GameData gameData, String rootClient, ChessMove move) throws ErrorException {
    ChessGame game=gameData.game();

    ChessGame.TeamColor myColor=null;

    boolean whiteTeam=rootClient.equals(gameData.whiteUsername());
    boolean blackTeam=rootClient.equals(gameData.blackUsername());

    if (whiteTeam) {
      myColor=ChessGame.TeamColor.WHITE;
    } else if (blackTeam) {
      myColor=ChessGame.TeamColor.BLACK;
    }

    if (myColor == null) {
      throw new ErrorException(500, "Can't move pieces when observing");
    }
    ChessPiece pieceToMove=game.getBoard().getPiece(move.getStartPosition());
    if (!myColor.equals(pieceToMove.getTeamColor())) {
      throw new ErrorException(500, "Can't move opponent's piece");
    }
  }

  private boolean staleMateNotification(GameData gameData) throws IOException {
    ChessGame game=gameData.game();

    boolean stalemateWhite=game.isInStalemate(ChessGame.TeamColor.WHITE);
    boolean stalemateBlack=game.isInStalemate(ChessGame.TeamColor.BLACK);

    if (stalemateWhite && stalemateBlack) {
      Notification stalemateNotification=new Notification(ServerMessage.ServerMessageType.NOTIFICATION,
              "Game is in stalemate!");
      connectionManager.broadcast("", stalemateNotification, gameData.gameID());
      return true;
    }
    return false;
  }

  private boolean checkMateNotification(GameData gameData) throws IOException {
    ChessGame game=gameData.game();

    boolean whiteCheckMate=game.isInCheckmate(ChessGame.TeamColor.WHITE);
    boolean blackCheckMate=game.isInCheckmate(ChessGame.TeamColor.BLACK);

    if (whiteCheckMate) {
      Notification whiteNotification=new Notification(ServerMessage.ServerMessageType.NOTIFICATION,
              gameData.whiteUsername() + " is in checkmate");
      connectionManager.broadcast("", whiteNotification, gameData.gameID());
      return true;
    } else if (blackCheckMate) {
      Notification blackNotification=new Notification(ServerMessage.ServerMessageType.NOTIFICATION,
              gameData.blackUsername() + " is in checkmate");
      connectionManager.broadcast("", blackNotification, gameData.gameID());
      return true;
    }
    return false;
  }

  private boolean checkNotification(GameData gameData) throws IOException {
    ChessGame game=gameData.game();

    boolean whiteCheck=game.isInCheck(ChessGame.TeamColor.WHITE);
    boolean blackCheck=game.isInCheck(ChessGame.TeamColor.BLACK);

    if (whiteCheck) {
      Notification whiteCheckNotification=new Notification(ServerMessage.ServerMessageType.NOTIFICATION,
              gameData.whiteUsername() + " is in check");
      connectionManager.broadcast("", whiteCheckNotification, gameData.gameID());
      return true;
    } else if (blackCheck) {
      Notification blackCheckNotification=new Notification(ServerMessage.ServerMessageType.NOTIFICATION,
              gameData.blackUsername() + " is in check");
      connectionManager.broadcast("", blackCheckNotification, gameData.gameID());

      return true;
    }
    return false;
  }


}



