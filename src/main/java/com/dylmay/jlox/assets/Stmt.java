package com.dylmay.jlox.assets;

import java.util.List;
import javax.annotation.Nullable;

public abstract class Stmt {
  public interface Visitor<R> {
    R visitExpressionStmt(Expression stmt);

    R visitPrintStmt(Print stmt);

    @Nullable
    R visitVarStmt(Var stmt);

    R visitBlockStmt(Block stmt);
  }

  public abstract <R> R accept(Visitor<R> visitor);

  public static class Expression extends Stmt {
    public final Expr expr;

    public Expression(Expr expr) {
      this.expr = expr;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitExpressionStmt(this);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
      if (this == obj) return true;

      if (obj instanceof Expression i) {
        return this.expr != null && this.expr.equals(i.expr);
      }

      return false;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;

      result = prime * result + ((expr == null) ? 0 : expr.hashCode());

      return result;
    }
  }

  public static class Print extends Stmt {
    public final Expr expr;

    public Print(Expr expr) {
      this.expr = expr;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitPrintStmt(this);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
      if (this == obj) return true;

      if (obj instanceof Print i) {
        return this.expr != null && this.expr.equals(i.expr);
      }

      return false;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;

      result = prime * result + ((expr == null) ? 0 : expr.hashCode());

      return result;
    }
  }

  public static class Var extends Stmt {
    public final Token name;
    public final @Nullable Expr initializer;

    public Var(Token name, @Nullable Expr initializer) {
      this.name = name;
      this.initializer = initializer;
    }

    @Override
    public <R> @Nullable R accept(Visitor<R> visitor) {
      return visitor.visitVarStmt(this);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
      if (this == obj) return true;

      if (obj instanceof Var i) {
        return this.name != null
            && this.name.equals(i.name)
            && this.initializer != null
            && this.initializer.equals(i.initializer);
      }

      return false;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;

      result = prime * result + ((name == null) ? 0 : name.hashCode());
      result = prime * result + ((initializer == null) ? 0 : initializer.hashCode());

      return result;
    }
  }

  public static class Block extends Stmt {
    public final List<Stmt> stmts;

    public Block(List<Stmt> stmts) {
      this.stmts = stmts;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitBlockStmt(this);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
      if (this == obj) return true;

      if (obj instanceof Block i) {
        return this.stmts != null && this.stmts.equals(i.stmts);
      }

      return false;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;

      result = prime * result + ((stmts == null) ? 0 : stmts.hashCode());

      return result;
    }
  }
}
