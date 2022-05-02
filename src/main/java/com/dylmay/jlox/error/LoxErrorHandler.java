package com.dylmay.jlox.error;

import java.util.HashMap;

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

  public void report(ErrorMessage msg) {
    System.err.println("<" + this.instanceName + "> " + msg.format());
    this.hasError = true;
  }
}
