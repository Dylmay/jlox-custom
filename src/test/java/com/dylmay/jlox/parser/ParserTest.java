package com.dylmay.jlox.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dylmay.jlox.assets.Expr;
import com.dylmay.jlox.assets.Position;
import com.dylmay.jlox.assets.Stmt;
import com.dylmay.jlox.assets.Token;
import com.dylmay.jlox.assets.TokenType;
import com.dylmay.jlox.error.LoxErrorHandler;
import com.dylmay.jlox.lexer.Lexer;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ParserTest {
  @Test
  @SuppressWarnings("nullness")
  void testNoTokens() {
    final var tokenList = List.of(new Token(TokenType.EOF, "", null, new Position(1, 0)));

    var errHndler = LoxErrorHandler.getInstance(Parser.class);
    var expr = new Parser(tokenList).parse();
    assertTrue(expr.isEmpty());
  }

  @Test
  @SuppressWarnings("nullness")
  void testValidTokens() {
    var tokenList =
        List.of(
            new Token(TokenType.TRUE, "true", null, Position.NO_POSITION),
            new Token(TokenType.SEMICOLON, ";", null, Position.NO_POSITION),
            new Token(TokenType.EOF, "", null, Position.NO_POSITION));

    var actual = new Parser(tokenList).parse();

    assertEquals(new Stmt.Expression(new Expr.Literal(true, Position.NO_POSITION)), actual.get(0));

    tokenList =
        List.of(
            new Token(TokenType.PRINT, "print", null, Position.NO_POSITION),
            new Token(TokenType.NUMBER, "10", 10d, Position.NO_POSITION),
            new Token(TokenType.PLUS, "+", null, Position.NO_POSITION),
            new Token(TokenType.NUMBER, "50", 50d, Position.NO_POSITION),
            new Token(TokenType.SEMICOLON, ";", null, Position.NO_POSITION),
            new Token(TokenType.EOF, "", null, Position.NO_POSITION));

    actual = new Parser(tokenList).parse();
    var expected =
        new Stmt.Print(
            new Expr.Binary(
                new Expr.Literal(10d, Position.NO_POSITION),
                new Token(TokenType.PLUS, "+", null, Position.NO_POSITION),
                new Expr.Literal(50d, Position.NO_POSITION)));

    assertEquals(expected, actual.get(0));
  }

  @Test
  @SuppressWarnings("nullness")
  public void testCComma() {
    var lexer = new Lexer("1 + 2, 3 + 4, 5 + 6;");

    var actual = new Parser(lexer.scanTokens()).parse();

    var expected =
        new Stmt.Expression(
            new Expr.Binary(
                new Expr.Binary(
                    new Expr.Binary(
                        new Expr.Literal(1d, new Position(1, 0)),
                        new Token(TokenType.PLUS, "+", null, new Position(1, 2)),
                        new Expr.Literal(2d, new Position(1, 4))),
                    new Token(TokenType.COMMA, ",", null, new Position(1, 5)),
                    new Expr.Binary(
                        new Expr.Literal(3d, new Position(1, 7)),
                        new Token(TokenType.PLUS, "+", null, new Position(1, 9)),
                        new Expr.Literal(4d, new Position(1, 11)))),
                new Token(TokenType.COMMA, ",", null, new Position(1, 12)),
                new Expr.Binary(
                    new Expr.Literal(5d, new Position(1, 14)),
                    new Token(TokenType.PLUS, "+", null, new Position(1, 16)),
                    new Expr.Literal(6d, new Position(1, 18)))));

    assertEquals(expected, actual.get(0));
  }

  @Test
  @SuppressWarnings("nullness")
  public void testCTernary() {
    var lexer = new Lexer("1 == 2 ? 1 : 0;");

    var actual = new Parser(lexer.scanTokens()).parse();

    var expected =
        new Stmt.Expression(
            new Expr.Ternary(
                new Expr.Binary(
                    new Expr.Literal(1d, new Position(1, 0)),
                    new Token(TokenType.EQUAL_EQUAL, "==", null, new Position(1, 2)),
                    new Expr.Literal(2d, new Position(1, 5))),
                new Expr.Literal(1d, new Position(1, 9)),
                new Expr.Literal(0d, new Position(1, 13))));

    assertEquals(expected, actual.get(0));
  }
}
