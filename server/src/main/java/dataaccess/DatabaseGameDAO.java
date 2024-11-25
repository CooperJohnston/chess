package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class DatabaseGameDAO implements GameDAO {
  public DatabaseGameDAO() throws DataAccessException {
    DatabaseManager.fillTables();

  }

  @Override
  public void createGame(GameData game) throws DataAccessException {
    try (Connection connection=DatabaseManager.getConnection()) {
      String query="INSERT INTO GameData (gameID, whiteUsername, blackUsername, " +
              "gameName, game) VALUES (?, ?, ?, ?, ?)";
      try (PreparedStatement statement=connection.prepareStatement(query)) {
        statement.setInt(1, game.gameID());
        statement.setString(2, game.whiteUsername());
        statement.setString(3, game.blackUsername());
        statement.setString(4, game.gameName());
        ChessGame chessGame=game.game();
        Gson gson=new Gson();
        String json=gson.toJson(chessGame);
        statement.setString(5, json);
        statement.executeUpdate();
      }
    } catch (SQLException | DataAccessException e) {
      throw new DataAccessException(e.getMessage());
    }
  }

  @Override
  public GameData getGame(int id) throws DataAccessException {
    try (Connection connection=DatabaseManager.getConnection()) {
      String query="SELECT * FROM GameData WHERE gameID = ?";
      try (PreparedStatement preparedStatement=connection.prepareStatement(query)) {
        preparedStatement.setInt(1, id);
        ResultSet resultSet=preparedStatement.executeQuery();
        if (resultSet.next()) {
          var game=resultSet.getString("game");
          Gson gson=new Gson();
          ChessGame chessGame=gson.fromJson(resultSet.getString("game"), ChessGame.class);
          return new GameData(id, resultSet.getString("whiteUsername"), resultSet.getString("blackUsername"),
                  resultSet.getString("gameName"), chessGame);
        }
      }
    } catch (SQLException | DataAccessException e) {
      throw new DataAccessException(e.getMessage());
    }
    return null;
  }


  private boolean checkGameExists(String query, Object parameter) throws DataAccessException {
    try (Connection connection=DatabaseManager.getConnection()) {
      try (PreparedStatement preparedStatement=connection.prepareStatement(query)) {
        if (parameter instanceof Integer) {
          preparedStatement.setInt(1, (Integer) parameter);
        } else if (parameter instanceof String) {
          preparedStatement.setString(1, (String) parameter);
        }
        ResultSet resultSet=preparedStatement.executeQuery();
        if (resultSet.next()) {
          return resultSet.getInt("count") > 0;
        }
      }
    } catch (SQLException | DataAccessException e) {
      throw new DataAccessException(e.getMessage());
    }
    return false;
  }

  @Override
  public boolean checkGame(int gameID) throws DataAccessException {
    String query="SELECT COUNT(*) AS count FROM GameData WHERE gameID = ?";
    return checkGameExists(query, gameID);
  }

  @Override
  public boolean checkGame(String gameName) throws DataAccessException {
    String query="SELECT COUNT(*) AS count FROM GameData WHERE gameName = ?";
    return checkGameExists(query, gameName);
  }


  @Override
  public void updateGame(GameData game) throws DataAccessException {
    String query="UPDATE GameData SET whiteUsername = ?, blackUsername = ? WHERE gameID = ?";
    if (getGame(game.gameID()) == null) {
      throw new DataAccessException("Game does not exist");
    }

    String whiteUsername=game.whiteUsername();
    String blackUsername=game.blackUsername();
    try (Connection connection=DatabaseManager.getConnection()) {
      PreparedStatement preparedStatement=connection.prepareStatement(query);
      preparedStatement.setString(1, whiteUsername);
      preparedStatement.setString(2, blackUsername);
      preparedStatement.setInt(3, game.gameID());
      preparedStatement.executeUpdate();
    } catch (SQLException | DataAccessException e) {
      throw new DataAccessException(e.getMessage());
    }

  }

  @Override
  public ArrayList<GameData> getAllGames() throws DataAccessException {
    ArrayList<GameData> games=new ArrayList<>();
    try (Connection connection=DatabaseManager.getConnection()) {
      String query="SELECT * FROM GameData";
      try (PreparedStatement preparedStatement=connection.prepareStatement(query)) {
        ResultSet resultSet=preparedStatement.executeQuery();
        while (resultSet.next()) {
          var game=resultSet.getString("game");
          Gson gson=new Gson();
          ChessGame chessGame=gson.fromJson(resultSet.getString("game"), ChessGame.class);
          GameData gameData=new GameData(resultSet.getInt("gameID"), resultSet.getString("whiteUsername"),
                  resultSet.getString("blackUsername"), resultSet.getString("gameName"), chessGame);
          games.add(gameData);
        }
      }

    } catch (SQLException | DataAccessException e) {
      throw new DataAccessException(e.getMessage());
    }
    return games;
  }

  @Override
  public void clear() throws DataAccessException {
    try (Connection connection=DatabaseManager.getConnection()) {
      String query="DELETE FROM GameData";
      try (PreparedStatement preparedStatement=connection.prepareStatement(query)) {
        preparedStatement.executeUpdate();

      }

    } catch (SQLException | DataAccessException e) {
      throw new DataAccessException(e.getMessage());
    }
  }

  public ChessGame getWinner(int id) throws DataAccessException {
    ArrayList<GameData> games=getAllGames();
    int i=1;
    for (GameData game : games) {
      if (i == id) {
        return game.game();
      }
    }
    return null;
  }
}
