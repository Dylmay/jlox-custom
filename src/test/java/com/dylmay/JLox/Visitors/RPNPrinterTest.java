package com.dylmay.JLox.Visitors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dylmay.JLox.Assets.Expr;
import com.dylmay.JLox.Assets.Position;
import com.dylmay.JLox.Assets.Token;
import com.dylmay.JLox.Assets.TokenType;
import org.junit.jupiter.api.Test;

public class RPNPrinterTest {
  @Test
  public void testRPNPrinter() {
    var expr =
        new Expr.Binary(
            new Expr.Unary(
                new Token(TokenType.MINUS, "-", null, new Position(1, 0)), new Expr.Literal(123)),
            new Token(TokenType.STAR, "*", null, new Position(1, 0)),
            new Expr.Grouping(new Expr.Literal(45.67)));

    var rpnPrinter = new RPNPrinter();

    assertEquals("123 - 45.67 *", rpnPrinter.traverse(expr));

    expr =
        new Expr.Binary(
            new Expr.Grouping(
                new Expr.Binary(
                    new Expr.Literal(1),
                    new Token(TokenType.PLUS, "+", null, new Position(1, 0)),
                    new Expr.Literal(2))),
            new Token(TokenType.STAR, "*", null, new Position(1, 0)),
            new Expr.Grouping(
                new Expr.Binary(
                    new Expr.Literal(4),
                    new Token(TokenType.MINUS, "-", null, new Position(1, 0)),
                    new Expr.Literal(3))));

    assertEquals("1 2 + 4 3 - *", rpnPrinter.traverse(expr));
  }
}
