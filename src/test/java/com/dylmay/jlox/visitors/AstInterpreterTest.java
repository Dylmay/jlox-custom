package com.dylmay.jlox.visitors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dylmay.jlox.Visitors.AstInterpreter;
import com.dylmay.jlox.assets.Expr;
import com.dylmay.jlox.assets.Position;
import com.dylmay.jlox.assets.Token;
import com.dylmay.jlox.assets.TokenType;
import org.junit.jupiter.api.Test;

public class AstInterpreterTest {
  @Test
  public void testAstInterpreter() {
    var expr =
        new Expr.Binary(
            new Expr.Unary(
                new Token(TokenType.MINUS, "-", null, new Position(1, 0)),
                new Expr.Literal(123, Position.NO_POSITION)),
            new Token(TokenType.STAR, "*", null, Position.NO_POSITION),
            new Expr.Grouping(new Expr.Literal(45.67, Position.NO_POSITION)));

    var astPrinter = new AstInterpreter();

    assertEquals("(* (- 123) (group 45.67))", astPrinter.interpret(expr));
  }
}
