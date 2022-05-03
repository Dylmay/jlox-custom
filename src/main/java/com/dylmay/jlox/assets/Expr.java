package com.dylmay.jlox.assets;

import javax.annotation.Nullable;

public abstract class Expr {
  public interface Visitor<R> {
    R visitBinaryExpr(Binary expr);

    R visitTernaryExpr(Ternary expr);

    R visitGroupingExpr(Grouping expr);

    @Nullable
    R visitLiteralExpr(Literal expr);

    R visitUnaryExpr(Unary expr);

    R visitVariableExpr(Variable expr);

    R visitAssignExpr(Assign expr);
  }

  public abstract <R> R accept(Visitor<R> visitor);

  public static class Binary extends Expr {
    public final Expr left;
    public final Token operator;
    public final Expr right;

    public Binary(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitBinaryExpr(this);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
      if (this == obj) return true;

      if (obj instanceof Binary i) {
        return this.left != null
            && this.left.equals(i.left)
            && this.operator != null
            && this.operator.equals(i.operator)
            && this.right != null
            && this.right.equals(i.right);
      }

      return false;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;

      result = prime * result + ((left == null) ? 0 : left.hashCode());
      result = prime * result + ((operator == null) ? 0 : operator.hashCode());
      result = prime * result + ((right == null) ? 0 : right.hashCode());

      return result;
    }
  }

  public static class Ternary extends Expr {
    public final Expr condition;
    public final Expr onTrue;
    public final Expr onFalse;

    public Ternary(Expr condition, Expr onTrue, Expr onFalse) {
      this.condition = condition;
      this.onTrue = onTrue;
      this.onFalse = onFalse;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitTernaryExpr(this);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
      if (this == obj) return true;

      if (obj instanceof Ternary i) {
        return this.condition != null
            && this.condition.equals(i.condition)
            && this.onTrue != null
            && this.onTrue.equals(i.onTrue)
            && this.onFalse != null
            && this.onFalse.equals(i.onFalse);
      }

      return false;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;

      result = prime * result + ((condition == null) ? 0 : condition.hashCode());
      result = prime * result + ((onTrue == null) ? 0 : onTrue.hashCode());
      result = prime * result + ((onFalse == null) ? 0 : onFalse.hashCode());

      return result;
    }
  }

  public static class Grouping extends Expr {
    public final Expr expression;

    public Grouping(Expr expression) {
      this.expression = expression;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitGroupingExpr(this);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
      if (this == obj) return true;

      if (obj instanceof Grouping i) {
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

  public static class Literal extends Expr {
    public final @Nullable Object value;
    public final Position pos;

    public Literal(@Nullable Object value, Position pos) {
      this.value = value;
      this.pos = pos;
    }

    @Override
    public <R> @Nullable R accept(Visitor<R> visitor) {
      return visitor.visitLiteralExpr(this);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
      if (this == obj) return true;

      if (obj instanceof Literal i) {
        return this.value != null
            && this.value.equals(i.value)
            && this.pos != null
            && this.pos.equals(i.pos);
      }

      return false;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;

      result = prime * result + ((value == null) ? 0 : value.hashCode());
      result = prime * result + ((pos == null) ? 0 : pos.hashCode());

      return result;
    }
  }

  public static class Unary extends Expr {
    public final Token operator;
    public final Expr right;

    public Unary(Token operator, Expr right) {
      this.operator = operator;
      this.right = right;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitUnaryExpr(this);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
      if (this == obj) return true;

      if (obj instanceof Unary i) {
        return this.operator != null
            && this.operator.equals(i.operator)
            && this.right != null
            && this.right.equals(i.right);
      }

      return false;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;

      result = prime * result + ((operator == null) ? 0 : operator.hashCode());
      result = prime * result + ((right == null) ? 0 : right.hashCode());

      return result;
    }
  }

  public static class Variable extends Expr {
    public final Token name;

    public Variable(Token name) {
      this.name = name;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitVariableExpr(this);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
      if (this == obj) return true;

      if (obj instanceof Variable i) {
        return this.name != null && this.name.equals(i.name);
      }

      return false;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;

      result = prime * result + ((name == null) ? 0 : name.hashCode());

      return result;
    }
  }

  public static class Assign extends Expr {
    public final Token name;
    public final Expr value;

    public Assign(Token name, Expr value) {
      this.name = name;
      this.value = value;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitAssignExpr(this);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
      if (this == obj) return true;

      if (obj instanceof Assign i) {
        return this.name != null
            && this.name.equals(i.name)
            && this.value != null
            && this.value.equals(i.value);
      }

      return false;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;

      result = prime * result + ((name == null) ? 0 : name.hashCode());
      result = prime * result + ((value == null) ? 0 : value.hashCode());

      return result;
    }
  }
}
