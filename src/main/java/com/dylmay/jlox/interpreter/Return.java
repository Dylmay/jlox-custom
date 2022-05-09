package com.dylmay.jlox.interpreter;

import javax.annotation.Nullable;

public class Return extends RuntimeException {
  final @Nullable Object value;

  Return(@Nullable Object value) {
    super(null, null, false, false);

    this.value = value;
  }
}
