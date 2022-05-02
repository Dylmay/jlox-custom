package com.dylmay.JLox.Visitors;

import com.dylmay.JLox.Assets.Expr;

public class AstPrinter implements Expr.Visitor<String>, Printer {
  public static final String PRINTER_NAME = "AstPrinter";

  @Override
  public String traverse(Expr expr) {
    return expr.accept(this);
  }

  @Override
  public String name() {
    return PRINTER_NAME;
  }

  @Override
  public String visitBinaryExpr(Expr.Binary expr) {
    return this.parenthesize(expr.operator.lexeme(), expr.left, expr.right);
  }

  @Override
  public String visitGroupingExpr(Expr.Grouping expr) {
    return this.parenthesize("group", expr.expression);
  }

  @Override
  public String visitLiteralExpr(Expr.Literal expr) {
    return expr.value == null ? "nil" : expr.value.toString();
  }

  @Override
  public String visitUnaryExpr(Expr.Unary expr) {
    return this.parenthesize(expr.operator.lexeme(), expr.right);
  }

  @Override
  public String visitTernaryExpr(Expr.Ternary expr) {
    return parenthesize("?:", expr.condition, expr.onTrue, expr.onFalse);
  }

  private String parenthesize(String name, Expr... exprs) {
    var builder = new StringBuilder();

    builder.append("(").append(name);
    for (var expr : exprs) {
      builder.append(" ").append(expr.accept(this));
    }
    builder.append(")");

    return builder.toString();
  }
}
