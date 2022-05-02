package com.dylmay.jlox.util;

import com.dylmay.jlox.assets.Position;

public class RuntimeError extends RuntimeException {
  public final transient Position position;

  public RuntimeError(Position position, String message) {
    super(message);
    this.position = position;
  }
}
