package websocket.commands;

import chess.ChessGame;
import chess.ChessMove;

public class MakeMoveCommand extends UserGameCommand {
  ChessMove chessMove;
  ChessGame.TeamColor color;


  public MakeMoveCommand(CommandType commandType, String authToken, Integer gameID, ChessMove move, ChessGame.TeamColor color) {
    super(commandType, authToken, gameID);
    this.chessMove=move;
    this.color=color;
  }

  public ChessMove getMove() {
    return this.chessMove;
  }

  public ChessGame.TeamColor getColor() {
    return this.color;
  }
}