package com.dylmay.JLox.Parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dylmay.JLox.Assets.Expr;
import com.dylmay.JLox.Assets.Position;
import com.dylmay.JLox.Assets.Token;
import com.dylmay.JLox.Assets.TokenType;
import com.dylmay.JLox.Lexer.Lexer;
import com.dylmay.JLox.LoxErrorHandler;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ParserTest {
  @Test
  void testNoTokens() {
    final var tokenList = List.of(new Token(TokenType.EOF, "", null, new Position(1, 0)));

    var errHndler = LoxErrorHandler.getInstance(Parser.class);
    var expr = new Parser(tokenList).parse();
    assertTrue(errHndler.hasError());
    assertTrue(expr.isEmpty());
  }

  @Test
  void testInvalidTokens() {
    var tokenList =
        List.of(
            new Token(TokenType.TRUE, "true", null, new Position(1, 0)),
            new Token(TokenType.EOF, "", null, new Position(1, 0)));

    var actual = new Parser(tokenList).parse().get();

    assertEquals(new Expr.Literal(true), actual);

    tokenList =
        List.of(
            new Token(TokenType.NUMBER, "10", 10d, new Position(1, 0)),
            new Token(TokenType.PLUS, "+", null, new Position(1, 0)),
            new Token(TokenType.NUMBER, "50", 50d, new Position(1, 0)),
            new Token(TokenType.EOF, "", null, new Position(1, 0)));

    actual = new Parser(tokenList).parse().get();
    var expected =
        new Expr.Binary(
            new Expr.Literal(10d),
            new Token(TokenType.PLUS, "+", null, new Position(1, 0)),
            new Expr.Literal(50d));

    assertEquals(
        new Expr.Binary(
            new Expr.Literal(10d),
            new Token(TokenType.PLUS, "+", null, new Position(1, 0)),
            new Expr.Literal(50d)),
        new Expr.Binary(
            new Expr.Literal(10d),
            new Token(TokenType.PLUS, "+", null, new Position(1, 0)),
            new Expr.Literal(50d)));

    assertEquals(actual, expected);
  }

  @Test
  public void testCComma() {
    var lexer = new Lexer("1 + 2, 3 + 4, 5 + 6");

    var actual = new Parser(lexer.scanTokens()).parse();

    var expected =
        new Expr.Binary(
            new Expr.Binary(
                new Expr.Binary(
                    new Expr.Literal(1d),
                    new Token(TokenType.PLUS, "+", null, new Position(1, 2)),
                    new Expr.Literal(2d)),
                new Token(TokenType.COMMA, ",", null, new Position(1, 5)),
                new Expr.Binary(
                    new Expr.Literal(3d),
                    new Token(TokenType.PLUS, "+", null, new Position(1, 9)),
                    new Expr.Literal(4d))),
            new Token(TokenType.COMMA, ",", null, new Position(1, 12)),
            new Expr.Binary(
                new Expr.Literal(5d),
                new Token(TokenType.PLUS, "+", null, new Position(1, 16)),
                new Expr.Literal(6d)));

    assertEquals(expected, actual.get());
  }

  @Test
  public void testCTernary() {
    var lexer = new Lexer("1 == 2 ? 1 : 0");

    var actual = new Parser(lexer.scanTokens()).parse();

    var expected =
        new Expr.Ternary(
            new Expr.Binary(
                new Expr.Literal(1d),
                new Token(TokenType.EQUAL_EQUAL, "==", null, new Position(1, 2)),
                new Expr.Literal(2d)),
            new Expr.Literal(1d),
            new Expr.Literal(0d));

    assertEquals(expected, actual.get());
  }
}
