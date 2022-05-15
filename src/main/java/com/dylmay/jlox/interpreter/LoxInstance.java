package com.dylmay.jlox.interpreter;

import com.dylmay.jlox.assets.Token;
import com.dylmay.jlox.util.RuntimeError;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

public class LoxInstance {
  LoxClass cls;

  Map<String, Object> fields;

  @SuppressWarnings("nullness")
  LoxInstance() {
    this.cls = null;
    this.fields = new HashMap<>();
  }

  LoxInstance(LoxClass cls) {
    this.cls = cls;
    this.fields = new HashMap<>(cls.defines);
  }

  @Override
  @SuppressWarnings("nullness")
  public String toString() {
    return cls.name + " instance";
  }

  @Nullable
  @SuppressWarnings("nullness")
  Object get(Token name) {
    if (cls.isStatic(name)) {
      return cls.get(name);
    }

    if (fields.containsKey(name.lexeme())) {
      return fields.get(name.lexeme());
    }

    var method = cls.findMethod(name.lexeme());
    if (method != null) {
      return method.bind(this);
    }

    throw new RuntimeError(name.position(), "Undefined property '" + name.lexeme() + "'");
  }

  @Nullable
  Object set(Token name, Object value) {
    if (cls.isStatic(name)) {
      return cls.set(name, value);
    }

    if (fields.containsKey(name.lexeme())) {
      return fields.put(name.lexeme(), value);
    }

    throw new RuntimeError(name.position(), "Undefined property '" + name.lexeme() + "'");
  }
}
