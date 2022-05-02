package com.dylmay.jlox.Visitors;

import com.dylmay.jlox.assets.Expr;
import com.dylmay.jlox.util.RuntimeError;

public interface Interpreter {
  public abstract String interpret(Expr expr) throws RuntimeError;

  public abstract String name();
}
