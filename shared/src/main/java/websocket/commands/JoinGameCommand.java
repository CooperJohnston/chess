package websocket.commands;

import chess.ChessGame;

public class JoinGameCommand extends UserGameCommand {
  private final ChessGame.TeamColor color;

  public JoinGameCommand(CommandType commandType, String authToken, Integer gameID, ChessGame.TeamColor color) {
    super(commandType, authToken, gameID);
    this.color=color;
  }

  public ChessGame.TeamColor getColor() {
    return this.color;
  }
}