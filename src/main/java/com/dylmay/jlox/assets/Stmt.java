package com.dylmay.jlox.assets;

import javax.annotation.Nullable;
import java.util.List;

public abstract class Stmt {
  public interface Visitor<R> {
    R visitExpressionStmt(Expression stmt);

    R visitFunctionStmt(Function stmt);

    @Nullable
    R visitVarStmt(Var stmt);

    R visitBlockStmt(Block stmt);

    @Nullable
    R visitIfStmt(If stmt);

    R visitWhileStmt(While stmt);
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

  public static class Function extends Stmt {
    public final Token name;
    public final List<Token> parms;

    public Function(Token name, List<Token> parms) {
      this.name = name;
      this.parms = parms;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitFunctionStmt(this);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
      if (this == obj) return true;

      if (obj instanceof Function i) {
        return this.name != null && this.name.equals(i.name)
           && this.parms != null && this.parms.equals(i.parms);
      }

      return false;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;

      result = prime * result + ((name == null) ? 0 : name.hashCode());
      result = prime * result + ((parms == null) ? 0 : parms.hashCode());

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
        return this.name != null && this.name.equals(i.name)
           && this.initializer != null && this.initializer.equals(i.initializer);
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

  public static class If extends Stmt {
    public final Expr condition;
    public final Stmt thenBranch;
    public final @Nullable Stmt elseBranch;

    public If(Expr condition, Stmt thenBranch, @Nullable Stmt elseBranch) {
      this.condition = condition;
      this.thenBranch = thenBranch;
      this.elseBranch = elseBranch;
    }

    @Override
    public <R> @Nullable R accept(Visitor<R> visitor) {
      return visitor.visitIfStmt(this);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
      if (this == obj) return true;

      if (obj instanceof If i) {
        return this.condition != null && this.condition.equals(i.condition)
           && this.thenBranch != null && this.thenBranch.equals(i.thenBranch)
           && this.elseBranch != null && this.elseBranch.equals(i.elseBranch);
      }

      return false;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;

      result = prime * result + ((condition == null) ? 0 : condition.hashCode());
      result = prime * result + ((thenBranch == null) ? 0 : thenBranch.hashCode());
      result = prime * result + ((elseBranch == null) ? 0 : elseBranch.hashCode());

      return result;
    }
  }

  public static class While extends Stmt {
    public final Expr condition;
    public final Stmt body;

    public While(Expr condition, Stmt body) {
      this.condition = condition;
      this.body = body;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitWhileStmt(this);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
      if (this == obj) return true;

      if (obj instanceof While i) {
        return this.condition != null && this.condition.equals(i.condition)
           && this.body != null && this.body.equals(i.body);
      }

      return false;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;

      result = prime * result + ((condition == null) ? 0 : condition.hashCode());
      result = prime * result + ((body == null) ? 0 : body.hashCode());

      return result;
    }
  }
}
