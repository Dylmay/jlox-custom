package com.dylmay.jlox.assets;

import java.util.List;
import javax.annotation.Nullable;

public abstract class Stmt {
  public interface Visitor<R> {
    R visitExpressionStmt(Expression stmt);

    @Nullable
    R visitVarStmt(Var stmt);

    R visitBlockStmt(Block stmt);

    @Nullable
    R visitIfStmt(If stmt);

    @Nullable
    R visitReturnStmt(Return stmt);

    R visitWhileStmt(While stmt);

    R visitBreakStmt(Break stmt);

    R visitContinueStmt(Continue stmt);

    R visitClassStmt(Class stmt);
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

  public static class Var extends Stmt {
    public final Token name;
    public final @Nullable Expr initializer;
    public final boolean mutable;

    public Var(Token name, @Nullable Expr initializer, boolean mutable) {
      this.name = name;
      this.initializer = initializer;
      this.mutable = mutable;
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
            && this.initializer.equals(i.initializer)
            && this.mutable == i.mutable;
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
        return this.condition != null
            && this.condition.equals(i.condition)
            && this.thenBranch != null
            && this.thenBranch.equals(i.thenBranch)
            && this.elseBranch != null
            && this.elseBranch.equals(i.elseBranch);
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

  public static class Return extends Stmt {
    public final Token keyword;
    public final @Nullable Expr value;

    public Return(Token keyword, @Nullable Expr value) {
      this.keyword = keyword;
      this.value = value;
    }

    @Override
    public <R> @Nullable R accept(Visitor<R> visitor) {
      return visitor.visitReturnStmt(this);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
      if (this == obj) return true;

      if (obj instanceof Return i) {
        return this.keyword != null
            && this.keyword.equals(i.keyword)
            && this.value != null
            && this.value.equals(i.value);
      }

      return false;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;

      result = prime * result + ((keyword == null) ? 0 : keyword.hashCode());
      result = prime * result + ((value == null) ? 0 : value.hashCode());

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
        return this.condition != null
            && this.condition.equals(i.condition)
            && this.body != null
            && this.body.equals(i.body);
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

  public static class Break extends Stmt {
    public final Token keyword;

    public Break(Token keyword) {
      this.keyword = keyword;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitBreakStmt(this);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
      if (this == obj) return true;

      if (obj instanceof Break i) {
        return this.keyword != null && this.keyword.equals(i.keyword);
      }

      return false;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;

      result = prime * result + ((keyword == null) ? 0 : keyword.hashCode());

      return result;
    }
  }

  public static class Continue extends Stmt {
    public final Token keyword;

    public Continue(Token keyword) {
      this.keyword = keyword;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitContinueStmt(this);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
      if (this == obj) return true;

      if (obj instanceof Continue i) {
        return this.keyword != null && this.keyword.equals(i.keyword);
      }

      return false;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;

      result = prime * result + ((keyword == null) ? 0 : keyword.hashCode());

      return result;
    }
  }

  public static class Class extends Stmt {
    public final Token name;
    public final List<Stmt.Var> decls;

    public Class(Token name, List<Stmt.Var> decls) {
      this.name = name;
      this.decls = decls;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitClassStmt(this);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
      if (this == obj) return true;

      if (obj instanceof Class i) {
        return this.name != null
            && this.name.equals(i.name)
            && this.decls != null
            && this.decls.equals(i.decls);
      }

      return false;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;

      result = prime * result + ((name == null) ? 0 : name.hashCode());
      result = prime * result + ((decls == null) ? 0 : decls.hashCode());

      return result;
    }
  }
}
