package com.dylmay.JLox;

import com.dylmay.JLox.Assets.Position;
import com.dylmay.JLox.Assets.Token;
import com.dylmay.JLox.Assets.TokenType;
import java.util.HashMap;
import java.util.Optional;

public class LoxErrorHandler {
  private static final HashMap<String, LoxErrorHandler> handlers = new HashMap<>();

  private boolean hasError;
  public final String instanceName;

  private LoxErrorHandler(String instanceName) {
    this.hasError = false;
    this.instanceName = instanceName;
  }

  public static <T> LoxErrorHandler getInstance(Class<T> clazz) {
    return LoxErrorHandler.getInstance(clazz.getName());
  }

  public static LoxErrorHandler getInstance(String instanceName) {
    var handler = handlers.get(instanceName);

    if (handler == null) {
      handler = new LoxErrorHandler(instanceName);
      handlers.putIfAbsent(instanceName, handler);
    }

    return handler;
  }

  public boolean hasError() {
    return this.hasError;
  }

  public void reset() {
    this.hasError = false;
  }

  public void error(Position position, String message) {
    report(position, "", message);
  }

  public ErrorMessage createError(String message) {
    return ErrorMessage.issue(this.instanceName).message(message);
  }

  public void report(Token token, String message) {
    this.report(ErrorMessage.issue(this.instanceName).message(message).token(token));
  }

  public void report(Position position, String where, String message) {
    this.report(
        ErrorMessage.issue(this.instanceName).message(message).where(where).position(position));
  }

  void report(ErrorMessage msg) {
    msg.report();
    this.hasError = true;
  }

  public void error(Token token, String msg) {
    if (token.type() == TokenType.EOF) {
      this.report(token.position(), "end", msg);
    } else {
      this.report(token.position(), token.lexeme(), msg);
    }
  }

  static class ErrorMessage {
    private String instance;
    private Optional<String> message;
    private Optional<String> where;
    private Optional<Position> position;
    private Optional<Exception> error;

    private ErrorMessage(
        String instance,
        Optional<String> message,
        Optional<String> where,
        Optional<Position> position,
        Optional<Exception> error) {
      this.instance = instance;
      this.message = message;
      this.where = where;
      this.position = position;
      this.error = error;
    }

    static ErrorMessage issue(String instanceName) {
      return new ErrorMessage(
          instanceName, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }

    public ErrorMessage message(String message) {
      this.message = Optional.of(message);

      return this;
    }

    public ErrorMessage where(String where) {
      this.where = Optional.of(where);

      return this;
    }

    public ErrorMessage position(Position position) {
      this.position = Optional.of(position);

      return this;
    }

    public ErrorMessage token(Token token) {
      this.position = Optional.of(token.position());
      this.where = Optional.of(token.lexeme());

      return this;
    }

    public ErrorMessage error(Exception error) {
      this.error = Optional.of(error);

      return this;
    }

    public String format() {
      StringBuilder formattedString = new StringBuilder();

      position.ifPresent(formattedString::append);
      where.ifPresent(token -> formattedString.append(" Error at '").append(token).append("'."));
      message.ifPresent(msg -> formattedString.append(" Message: ").append(msg).append('.'));
      error.ifPresent(
          err ->
              formattedString
                  .append(" Localised Msg: ")
                  .append(err.getLocalizedMessage())
                  .append('.'));

      return formattedString.toString().trim();
    }

    public void report() {
      System.err.println("<" + this.instance + "> " + this.format());
    }
  }
}
