package com.dylmay.JLox.Visitors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dylmay.JLox.Assets.Expr;
import com.dylmay.JLox.Assets.Position;
import com.dylmay.JLox.Assets.Token;
import com.dylmay.JLox.Assets.TokenType;
import org.junit.jupiter.api.Test;

public class AstPrinterTest {
  @Test
  public void testAstPrinter() {
    var expr =
        new Expr.Binary(
            new Expr.Unary(
                new Token(TokenType.MINUS, "-", null, new Position(1, 0)), new Expr.Literal(123)),
            new Token(TokenType.STAR, "*", null, new Position(1, 0)),
            new Expr.Grouping(new Expr.Literal(45.67)));

    var astPrinter = new AstPrinter();

    assertEquals("(* (- 123) (group 45.67))", astPrinter.traverse(expr));
  }
}
