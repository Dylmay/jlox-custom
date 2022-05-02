package com.dylmay.jlox.assets;

import javax.annotation.Nullable;

public final record Item(@Nullable Object result, Position position) {
  public <T> @Nullable T as(Class<T> clazz) {
    return clazz.cast(this.result);
  }

  public <T> boolean is(Class<T> clazz) {
    return clazz.isInstance(this.result);
  }
}
