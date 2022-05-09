package com.dylmay.jlox.interpreter.call;

public abstract class Call extends RuntimeException {
  Call() {
    super(null, null, false, false);
  }
}
