package websocket.commands;

import chess.ChessGame;
import chess.ChessMove;

public class MakeMoveCommand extends UserGameCommand {
  ChessMove move;
  ChessGame.TeamColor color;


  public MakeMoveCommand(String authToken, Integer gameID, ChessMove move) {
    super(CommandType.MAKE_MOVE, authToken, gameID);
    this.move=move;
  }

  public ChessMove getMove() {
    return this.move;
  }

  public ChessGame.TeamColor getColor() {
    return this.color;
  }

  @Override
  public String toString() {
    return "MakeMoveCommand{" +
            "chessMove=" + move +
            ", color=" + color +
            '}';
  }
}