package com.dylmay.jlox.visitors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dylmay.jlox.Visitors.RPNInterpreter;
import com.dylmay.jlox.assets.Expr;
import com.dylmay.jlox.assets.Position;
import com.dylmay.jlox.assets.Token;
import com.dylmay.jlox.assets.TokenType;

import org.junit.jupiter.api.Test;

public class RPNInterpreterTest {
  @Test
  public void testRPNInterpreter() {
    var expr =
        new Expr.Binary(
            new Expr.Unary(
                new Token(TokenType.MINUS, "-", null, new Position(1, 0)),
                new Expr.Literal(123, Position.NO_POSITION)),
            new Token(TokenType.STAR, "*", null, Position.NO_POSITION),
            new Expr.Grouping(new Expr.Literal(45.67, Position.NO_POSITION)));

    var rpnPrinter = new RPNInterpreter();

    assertEquals("123 - 45.67 *", rpnPrinter.interpret(expr));

    expr =
        new Expr.Binary(
            new Expr.Grouping(
                new Expr.Binary(
                    new Expr.Literal(1, Position.NO_POSITION),
                    new Token(TokenType.PLUS, "+", null, Position.NO_POSITION),
                    new Expr.Literal(2, Position.NO_POSITION))),
            new Token(TokenType.STAR, "*", null, Position.NO_POSITION),
            new Expr.Grouping(
                new Expr.Binary(
                    new Expr.Literal(4, Position.NO_POSITION),
                    new Token(TokenType.MINUS, "-", null, Position.NO_POSITION),
                    new Expr.Literal(3, Position.NO_POSITION))));

    assertEquals("1 2 + 4 3 - *", rpnPrinter.interpret(expr));
  }
}
