package com.dylmay.jlox.Visitors;

import com.dylmay.jlox.assets.Expr;

public class RPNInterpreter implements Expr.Visitor<String>, Interpreter {
  public static final String PRINTER_NAME = "RPNPrinter";

  @Override
  public String interpret(Expr expr) {
    return expr.accept(this);
  }

  @Override
  public String name() {
    return PRINTER_NAME;
  }

  @Override
  public String visitBinaryExpr(Expr.Binary expr) {
    return this.notate(expr.operator.lexeme(), expr.left, expr.right);
  }

  @Override
  public String visitGroupingExpr(Expr.Grouping expr) {
    return this.notate("", expr.expression);
  }

  @Override
  public String visitLiteralExpr(Expr.Literal expr) {
    return expr.value == null ? "nil" : expr.value.toString();
  }

  @Override
  public String visitUnaryExpr(Expr.Unary expr) {
    return this.notate(expr.operator.lexeme(), expr.right);
  }

  @Override
  public String visitTernaryExpr(Expr.Ternary expr) {
    return this.notate("?:", expr.condition, expr.onTrue, expr.onFalse);
  }

  private String notate(String name, Expr... exprs) {
    var builder = new StringBuilder();

    for (var expr : exprs) {
      builder.append(expr.accept(this)).append(' ');
    }
    builder.append(name);

    return builder.toString().stripTrailing();
  }
}
