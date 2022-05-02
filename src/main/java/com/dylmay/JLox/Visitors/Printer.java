package com.dylmay.JLox.Visitors;

import com.dylmay.JLox.Assets.Expr;

public interface Printer {
  public abstract String traverse(Expr expr);

  public abstract String name();
}
