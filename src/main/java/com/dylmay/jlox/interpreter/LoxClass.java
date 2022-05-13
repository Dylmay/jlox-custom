package com.dylmay.jlox.interpreter;

import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

public class LoxClass implements LoxCallable {
  final String name;

  private final Map<String, LoxFunction> methods;

  LoxClass(String name, Map<String, LoxFunction> methods) {
    this.name = name;
    this.methods = methods;
  }

  @Override
  public String toString() {
    return this.name;
  }

  @Override
  public int arity() {
    return 0;
  }

  @Override
  public Object call(Interpreter interpreter, List<Object> args) {
    var instance = new LoxInstance(this);

    return instance;
  }

  @SuppressWarnings("nullness")
  @Nullable
  LoxFunction findMethod(String name) {
    return methods.getOrDefault(name, null);
  }
}
