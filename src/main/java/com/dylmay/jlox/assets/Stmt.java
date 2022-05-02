package com.dylmay.jlox.assets;

import javax.annotation.Nullable;

public abstract class Stmt {
  public interface Visitor<R> {
    R visitExpressionExpr(Expression expr);

    R visitPrintExpr(Print expr);
  }

  public abstract <R> R accept(Visitor<R> visitor);

  public static class Expression extends Stmt {
    public final Expr expression;

    public Expression(Expr expression) {
      this.expression = expression;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitExpressionExpr(this);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
      if (this == obj) return true;

      if (obj instanceof Expression i) {
        return this.expression != null && this.expression.equals(i.expression);
      }

      return false;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;

      result = prime * result + ((expression == null) ? 0 : expression.hashCode());

      return result;
    }
  }

  public static class Print extends Stmt {
    public final Expr expression;

    public Print(Expr expression) {
      this.expression = expression;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitPrintExpr(this);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
      if (this == obj) return true;

      if (obj instanceof Print i) {
        return this.expression != null && this.expression.equals(i.expression);
      }

      return false;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;

      result = prime * result + ((expression == null) ? 0 : expression.hashCode());

      return result;
    }
  }
}
