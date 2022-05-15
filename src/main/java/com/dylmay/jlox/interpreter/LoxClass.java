package com.dylmay.jlox.interpreter;

import com.dylmay.jlox.assets.Token;
import com.dylmay.jlox.util.RuntimeError;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

public class LoxClass extends LoxInstance implements LoxCallable {
  final String name;

  private final Map<String, LoxFunction> methods;
  final Map<String, Object> defines;

  final Set<String> statics;

  @SuppressWarnings("assignment")
  LoxClass(
      String name,
      Map<String, LoxFunction> methods,
      Map<String, Object> defines,
      Set<String> statics) {
    this.name = name;
    this.methods = methods;
    this.defines = defines;
    this.statics = statics;
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

  boolean isStatic(Token name) {
    return this.statics.contains(name.lexeme());
  }

  @Nullable
  @SuppressWarnings("nullness")
  @Override
  Object get(Token name) {
    if (!this.isStatic(name)) {
      throw new RuntimeError(name.position(), "Undefined static " + name.lexeme() + "'");
    }

    if (this.defines.containsKey(name.lexeme())) {
      return this.defines.get(name.lexeme());
    }

    var method = cls.findMethod(name.lexeme());
    if (method != null) {
      return method.bind(this);
    }

    throw new RuntimeError(name.position(), "Undefined property '" + name.lexeme() + "'");
  }

  @Nullable
  @Override
  Object set(Token name, Object value) {
    if (!this.isStatic(name)) {
      throw new RuntimeError(name.position(), "Undefined static " + name.lexeme() + "'");
    }

    if (this.defines.containsKey(name.lexeme())) {
      return this.defines.put(name.lexeme(), value);
    }

    throw new RuntimeError(name.position(), "Undefined property '" + name.lexeme() + "'");
  }
}
