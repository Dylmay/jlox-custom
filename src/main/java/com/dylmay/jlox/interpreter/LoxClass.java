package com.dylmay.jlox.interpreter;

import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

public class LoxClass extends LoxInstance implements LoxCallable {
  final String name;

  private final Map<String, LoxFunction> methods;
  final Map<String, Object> defines;

  @SuppressWarnings("assignment")
  LoxClass(String name, Map<String, LoxFunction> methods, Map<String, Object> defines) {
    this.name = name;
    this.methods = methods;
    this.defines = defines;
    this.cls = this;
  }

  @Override
  public String toString() {
    return this.name;
  }

  @Override
  public int arity() {
    var init = this.findMethod("init");

    return init != null ? init.arity() : 0;
  }

  @Override
  public Object call(Interpreter interpreter, List<Object> args) {
    var instance = new LoxInstance(this);

    var init = this.findMethod("init");
    if (init != null) {
      init.bind(instance).call(interpreter, args);
    }

    return instance;
  }

  @SuppressWarnings("nullness")
  @Nullable
  LoxFunction findMethod(String name) {
    return methods.getOrDefault(name, null);
  }
}
