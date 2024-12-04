package exception;

public class ErrorException extends Exception {
  private final int errorCode;

  public ErrorException(int errorCode, String message) {
    super("Error: " + message);
    this.errorCode=errorCode;
  }

  public int getErrorCode() {
    return errorCode;
  }
}