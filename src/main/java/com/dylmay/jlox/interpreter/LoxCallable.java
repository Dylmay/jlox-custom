package com.dylmay.jlox.interpreter;

import java.util.List;
import javax.annotation.Nullable;

public interface LoxCallable {
  int arity();

  @Nullable
  Object call(Interpreter interpreter, List<Object> args);
}
