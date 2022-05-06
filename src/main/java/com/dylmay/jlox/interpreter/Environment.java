package com.dylmay.jlox.interpreter;

import com.dylmay.jlox.assets.Token;
import com.dylmay.jlox.util.RuntimeError;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

class Environment {
  private final @Nullable Environment parent;
  private final Map<String, Object> values;

  public Environment(@Nullable Environment parent) {
    this.parent = parent;
    this.values = new HashMap<>();
  }

  public Environment() {
    this(null);
  }

  @Nullable
  Object get(Token name) {
    if (values.containsKey(name.lexeme())) {
      return values.get(name.lexeme());
    }

    if (this.parent != null) {
      return this.parent.get(name);
    }

    throw new RuntimeError(name.position(), "Undefined variable '" + name.lexeme() + "'.");
  }

  @SuppressWarnings("nullness")
  boolean define(String name, @Nullable Object value) {
    if (!this.values.containsKey(name)) {
      this.values.put(name, value);

      return true;
    }

    return false;
  }

  @SuppressWarnings("nullness")
  boolean assign(String name, @Nullable Object value) {
    if (this.values.containsKey(name)) {
      this.values.put(name, value);
      return true;
    }

    if (this.parent != null) {
      return this.parent.assign(name, value);
    }

    return false;
  }
}
