package com.dylmay.jlox.error;

import com.dylmay.jlox.assets.Position;
import javax.annotation.Nullable;

public class ErrorMessage {
  private @Nullable String message;
  private @Nullable String where;
  private @Nullable Position position;
  private @Nullable Exception exception;

  public ErrorMessage() {
    this.message = null;
    this.where = null;
    this.position = null;
    this.exception = null;
  }

  public ErrorMessage message(String message) {
    this.message = message;

    return this;
  }

  public ErrorMessage where(String where) {
    this.where = where;

    return this;
  }

  public ErrorMessage position(Position position) {
    this.position = position;

    return this;
  }

  public ErrorMessage exception(Exception exc) {
    this.exception = exc;

    return this;
  }

  public String format() {
    StringBuilder formattedString = new StringBuilder();

    if (position != null) {
      formattedString.append(position);
    }

    if (where != null) {
      formattedString.append(" Error at '").append(where).append("'.");
    }

    if (message != null) {
      formattedString.append(" Message: ").append(message).append('.');
    }

    if (exception != null) {
      var localMsg = exception.getLocalizedMessage();

      if (localMsg != null) {
        formattedString.append(" Localised Msg: ").append(localMsg).append('.');
      }
    }

    return formattedString.toString().trim();
  }
}
