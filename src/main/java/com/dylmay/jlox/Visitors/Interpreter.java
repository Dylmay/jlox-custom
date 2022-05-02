package com.dylmay.jlox.Visitors;

import com.dylmay.jlox.assets.Expr;
import com.dylmay.jlox.assets.Expr.Binary;
import com.dylmay.jlox.assets.Expr.Grouping;
import com.dylmay.jlox.assets.Expr.Literal;
import com.dylmay.jlox.assets.Expr.Ternary;
import com.dylmay.jlox.assets.Expr.Unary;
import com.dylmay.jlox.assets.Expr.Variable;
import com.dylmay.jlox.assets.Item;
import com.dylmay.jlox.assets.Stmt;
import com.dylmay.jlox.assets.Stmt.Expression;
import com.dylmay.jlox.assets.Stmt.Print;
import com.dylmay.jlox.assets.Stmt.Var;
import com.dylmay.jlox.error.ErrorMessage;
import com.dylmay.jlox.error.LoxErrorHandler;
import com.dylmay.jlox.util.RuntimeError;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class Interpreter implements Expr.Visitor<Item>, Stmt.Visitor<String> {
  private static final LoxErrorHandler ERR_HNDLR =
      LoxErrorHandler.getInstance(Interpreter.class);

  @SuppressWarnings("nullness")
  @Override
  public Item visitBinaryExpr(Binary expr) {
    Item left = this.evaluate(expr.left);
    Item right = this.evaluate(expr.right);

    switch (expr.operator.type()) {
      case MINUS:
        assertIsNumber(left, right);
        return new Item(left.as(double.class) - right.as(double.class), expr.operator.position());

      case SLASH:
        assertIsNumber(left, right);
        return new Item(left.as(double.class) / right.as(double.class), expr.operator.position());

      case PLUS:
        if (left.result() instanceof Double ld && right.result() instanceof Double rd)
          return new Item(ld + rd, expr.operator.position());
        if (left.result() instanceof String ls && right.result() instanceof String rs)
          return new Item(ls + rs, expr.operator.position());

        break;

      case STAR:
        assertIsNumber(left, right);
        return new Item(left.as(double.class) * right.as(double.class), expr.operator.position());

      case GREATER:
        assertIsNumber(left, right);
        return new Item(left.as(double.class) > right.as(double.class), expr.operator.position());

      case GREATER_EQUAL:
        assertIsNumber(left, right);
        return new Item(left.as(double.class) >= right.as(double.class), expr.operator.position());

      case LESS:
        assertIsNumber(left, right);
        return new Item(left.as(double.class) < right.as(double.class), expr.operator.position());

      case LESS_EQUAL:
        assertIsNumber(left, right);
        return new Item(left.as(double.class) <= right.as(double.class), expr.operator.position());

      case BANG_EQUAL:
        assertIsNumber(left, right);
        return new Item(
            !this.isEqual(left.as(double.class), right.as(double.class)), expr.operator.position());

      case EQUAL_EQUAL:
        assertIsNumber(left, right);
        return new Item(
            this.isEqual(left.as(double.class), right.as(double.class)), expr.operator.position());
    }

    throw new RuntimeError(expr.operator.position(), "Unknown Binary Expression");
  }

  @Override
  public Item visitTernaryExpr(Ternary expr) {
    var condition = this.evaluate(expr.condition);

    if (condition.result() instanceof Boolean || condition.result() instanceof Double) {
      return Boolean.TRUE.equals(condition.result())
          ? this.evaluate(expr.onTrue)
          : this.evaluate(expr.onFalse);
    }

    // TODO: implement token fetching
    // TODO: decide whether to return false on bad token
    throw new RuntimeError(condition.position(), "Unknown Ternary Condition");
  }

  @Override
  public Item visitGroupingExpr(Grouping expr) {
    return this.evaluate(expr.expression);
  }

  @Override
  public @Nullable Item visitLiteralExpr(Literal expr) {
    return new Item(expr.value, expr.pos);
  }

  @Override
  @SuppressWarnings("nullness")
  public Item visitUnaryExpr(Unary expr) {
    Item right = this.evaluate(expr.right);

    switch (expr.operator.type()) {
      case BANG:
        return new Item(this.isTruthy(right.result()), expr.operator.position());

      case MINUS:
        return new Item(-(double) right.result(), expr.operator.position());

      case PLUS:
        return new Item(right.result(), expr.operator.position());

      default:
        throw new RuntimeError(expr.operator.position(), "Unknown Unary operator");
    }
  }

  private Item evaluate(Expr expr) {
    return expr.accept(this);
  }

  private boolean isTruthy(Object object) {
    if (object == null) return false;
    if (object instanceof Boolean b) return b;
    return true;
  }

  private boolean isEqual(Object a, Object b) {
    if (a == null && b == null) return true;
    if (a == null) return false;

    return a.equals(b);
  }

  private void assertIsNumber(Item... values) {
    for (var item : values) {
      if (!item.is(double.class)) {
        throw new RuntimeError(item.position(), "Operand must be a number");
      }
    }
  }

  private String execute(Stmt stmt) {
    return stmt.accept(this);
  }

  public List<String> interpret(List<Stmt> statements) throws RuntimeError {
    List<String> interpretations = new ArrayList<>();

    try {
      for (var stmt : statements) {
        var result = this.execute(stmt);

        if(!result.isEmpty()) {
          interpretations.add(this.execute(stmt));
        }
      }
    } catch (RuntimeError error) {
      var msg = error.getMessage();
      var issue = new ErrorMessage().position(error.position);

      if (msg != null) {
        issue.message(msg);
      }

      ERR_HNDLR.report(issue);
    }

    return interpretations;
  }

  private String stringify(@Nullable Object obj) {
    if (obj == null) {
      return "nil";
    }

    var text = obj.toString();

    if (obj instanceof Double && text.endsWith(".0")) {
      text = text.substring(0, text.length() - 2);
    }

    return text;
  }

  @Override
  public String visitExpressionStmt(Expression stmt) {
    this.evaluate(stmt.expr);

    return "";
  }

  @Override
  public String visitPrintStmt(Print stmt) {
    var result = this.evaluate(stmt.expression);

    return result != null ? this.stringify(result.result()) : "";
  }

}
