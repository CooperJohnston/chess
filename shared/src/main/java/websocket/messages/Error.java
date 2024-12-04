package websocket.messages;

public class Error extends ServerMessage {
  private final String errorMessage;

  public Error(ServerMessageType type, String errorMessage) {
    super(type);
    this.errorMessage=errorMessage;
  }

  public String getErrorMessage() {
    return this.errorMessage;
  }
}