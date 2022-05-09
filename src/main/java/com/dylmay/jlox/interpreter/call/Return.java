package com.dylmay.jlox.interpreter.call;

import javax.annotation.Nullable;

public class Return extends Call {
  public final @Nullable Object value;

  public Return(@Nullable Object value) {
    this.value = value;
  }
}
