package com.dylmay.jlox.lexer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dylmay.jlox.assets.Position;
import com.dylmay.jlox.assets.Token;
import com.dylmay.jlox.assets.TokenType;
import com.dylmay.jlox.error.LoxErrorHandler;
import com.dylmay.jlox.lexer.Lexer;

import org.junit.jupiter.api.Test;

public class LexerTest {

  @Test
  void testSingleTokenization() {
    final var singleTokens = "( ) { } , . - + ; * / ! < > = ? :\n";

    var lexerTokens = new Lexer(singleTokens).scanTokens();

    Token[] expectedTokens = {
      new Token(TokenType.LEFT_PAREN, "(", null, new Position(1, 0)),
      new Token(TokenType.RIGHT_PAREN, ")", null, new Position(1, 2)),
      new Token(TokenType.LEFT_BRACE, "{", null, new Position(1, 4)),
      new Token(TokenType.RIGHT_BRACE, "}", null, new Position(1, 6)),
      new Token(TokenType.COMMA, ",", null, new Position(1, 8)),
      new Token(TokenType.DOT, ".", null, new Position(1, 10)),
      new Token(TokenType.MINUS, "-", null, new Position(1, 12)),
      new Token(TokenType.PLUS, "+", null, new Position(1, 14)),
      new Token(TokenType.SEMICOLON, ";", null, new Position(1, 16)),
      new Token(TokenType.STAR, "*", null, new Position(1, 18)),
      new Token(TokenType.SLASH, "/", null, new Position(1, 20)),
      new Token(TokenType.BANG, "!", null, new Position(1, 22)),
      new Token(TokenType.LESS, "<", null, new Position(1, 24)),
      new Token(TokenType.GREATER, ">", null, new Position(1, 26)),
      new Token(TokenType.EQUAL, "=", null, new Position(1, 28)),
      new Token(TokenType.TERNARY, "?", null, new Position(1, 30)),
      new Token(TokenType.TERNARY_SPLIT, ":", null, new Position(1, 32)),
      new Token(TokenType.EOF, "\n", null, new Position(2, 0))
    };

    assertArrayEquals(expectedTokens, lexerTokens.toArray());
  }

  @Test
  void testMultiTokenization() {
    final var multiTokens = "!= == >= <=\n";

    var lexerTokens = new Lexer(multiTokens).scanTokens();

    Token[] expectedTokens = {
      new Token(TokenType.BANG_EQUAL, "!=", null, new Position(1, 0)),
      new Token(TokenType.EQUAL_EQUAL, "==", null, new Position(1, 3)),
      new Token(TokenType.GREATER_EQUAL, ">=", null, new Position(1, 6)),
      new Token(TokenType.LESS_EQUAL, "<=", null, new Position(1, 9)),
      new Token(TokenType.EOF, "\n", null, new Position(2, 0))
    };

    assertArrayEquals(expectedTokens, lexerTokens.toArray());
  }

  @Test
  void testLiteralTokenization() {
    final var literalTokens = "Identifier123 12.345 \"string\"\n";

    var lexerTokens = new Lexer(literalTokens).scanTokens();

    Token[] expectedTokens = {
      new Token(TokenType.IDENTIFIER, "Identifier123", null, new Position(1, 0)),
      new Token(TokenType.NUMBER, "12.345", 12.345d, new Position(1, 14)),
      new Token(TokenType.STRING, "\"string\"", "string", new Position(1, 21)),
      new Token(TokenType.EOF, "\n", null, new Position(2, 0))
    };

    assertArrayEquals(expectedTokens, lexerTokens.toArray());
  }

  @Test
  void testKeywordTokenization() {
    final var keywordTokens =
        "and class else false fun for if nil or print return super this true var while\n";

    var lexerTokens = new Lexer(keywordTokens).scanTokens();

    Token[] expectedTokens = {
      new Token(TokenType.AND, "and", null, new Position(1, 0)),
      new Token(TokenType.CLASS, "class", null, new Position(1, 4)),
      new Token(TokenType.ELSE, "else", null, new Position(1, 10)),
      new Token(TokenType.FALSE, "false", null, new Position(1, 15)),
      new Token(TokenType.FUN, "fun", null, new Position(1, 21)),
      new Token(TokenType.FOR, "for", null, new Position(1, 25)),
      new Token(TokenType.IF, "if", null, new Position(1, 29)),
      new Token(TokenType.NIL, "nil", null, new Position(1, 32)),
      new Token(TokenType.OR, "or", null, new Position(1, 36)),
      new Token(TokenType.PRINT, "print", null, new Position(1, 39)),
      new Token(TokenType.RETURN, "return", null, new Position(1, 45)),
      new Token(TokenType.SUPER, "super", null, new Position(1, 52)),
      new Token(TokenType.THIS, "this", null, new Position(1, 58)),
      new Token(TokenType.TRUE, "true", null, new Position(1, 63)),
      new Token(TokenType.VAR, "var", null, new Position(1, 68)),
      new Token(TokenType.WHILE, "while", null, new Position(1, 72)),
      new Token(TokenType.EOF, "\n", null, new Position(2, 0))
    };

    assertArrayEquals(expectedTokens, lexerTokens.toArray());
  }

  @Test
  void testInvalidLox() {
    final var foreverStr = " 'continuous string ";
    final var allComments = "/*comments";
    final var halfComment = "/*/comments*";

    var errorHandler = LoxErrorHandler.getInstance(Lexer.class);

    new Lexer(foreverStr).scanTokens();
    assertTrue(errorHandler.hasError());

    errorHandler.reset();
    new Lexer(allComments).scanTokens();
    assertTrue(errorHandler.hasError());

    errorHandler.reset();
    new Lexer(halfComment).scanTokens();
    assertTrue(errorHandler.hasError());

    errorHandler.reset();
  }

  @Test
  void testValidLox() {
    final var loxTokens =
        """

        /**
         * Lox example class
         */
        class LoxCode {
          fun helloName(name) {
            var helloStr = 'Hello' + name;

            return helloStr;
          }

          fun main() {
            // prints HelloDylan forever
            while (true)
              print helloWorld('Dylan');

            if (false)
              print nil;

            return 10 == 2.0 or
                (3 >= 3 and 'abc' == 'abc')
                ? 0
                : 1;
          }
        }

        """;

    var lexerTokens = new Lexer(loxTokens).scanTokens();

    Token[] expectedTokens = {
      new Token(TokenType.CLASS, "class", null, new Position(5, 0)),
      new Token(TokenType.IDENTIFIER, "LoxCode", null, new Position(5, 6)),
      new Token(TokenType.LEFT_BRACE, "{", null, new Position(5, 14)),
      new Token(TokenType.FUN, "fun", null, new Position(6, 2)),
      new Token(TokenType.IDENTIFIER, "helloName", null, new Position(6, 6)),
      new Token(TokenType.LEFT_PAREN, "(", null, new Position(6, 15)),
      new Token(TokenType.IDENTIFIER, "name", null, new Position(6, 16)),
      new Token(TokenType.RIGHT_PAREN, ")", null, new Position(6, 20)),
      new Token(TokenType.LEFT_BRACE, "{", null, new Position(6, 22)),
      new Token(TokenType.VAR, "var", null, new Position(7, 4)),
      new Token(TokenType.IDENTIFIER, "helloStr", null, new Position(7, 8)),
      new Token(TokenType.EQUAL, "=", null, new Position(7, 17)),
      new Token(TokenType.STRING, "'Hello'", "Hello", new Position(7, 19)),
      new Token(TokenType.PLUS, "+", null, new Position(7, 27)),
      new Token(TokenType.IDENTIFIER, "name", null, new Position(7, 29)),
      new Token(TokenType.SEMICOLON, ";", null, new Position(7, 33)),
      new Token(TokenType.RETURN, "return", null, new Position(9, 4)),
      new Token(TokenType.IDENTIFIER, "helloStr", null, new Position(9, 11)),
      new Token(TokenType.SEMICOLON, ";", null, new Position(9, 19)),
      new Token(TokenType.RIGHT_BRACE, "}", null, new Position(10, 2)),
      new Token(TokenType.FUN, "fun", null, new Position(12, 2)),
      new Token(TokenType.IDENTIFIER, "main", null, new Position(12, 6)),
      new Token(TokenType.LEFT_PAREN, "(", null, new Position(12, 10)),
      new Token(TokenType.RIGHT_PAREN, ")", null, new Position(12, 11)),
      new Token(TokenType.LEFT_BRACE, "{", null, new Position(12, 13)),
      new Token(TokenType.WHILE, "while", null, new Position(14, 4)),
      new Token(TokenType.LEFT_PAREN, "(", null, new Position(14, 10)),
      new Token(TokenType.TRUE, "true", null, new Position(14, 11)),
      new Token(TokenType.RIGHT_PAREN, ")", null, new Position(14, 15)),
      new Token(TokenType.PRINT, "print", null, new Position(15, 6)),
      new Token(TokenType.IDENTIFIER, "helloWorld", null, new Position(15, 12)),
      new Token(TokenType.LEFT_PAREN, "(", null, new Position(15, 22)),
      new Token(TokenType.STRING, "'Dylan'", "Dylan", new Position(15, 23)),
      new Token(TokenType.RIGHT_PAREN, ")", null, new Position(15, 30)),
      new Token(TokenType.SEMICOLON, ";", null, new Position(15, 31)),
      new Token(TokenType.IF, "if", null, new Position(17, 4)),
      new Token(TokenType.LEFT_PAREN, "(", null, new Position(17, 7)),
      new Token(TokenType.FALSE, "false", null, new Position(17, 8)),
      new Token(TokenType.RIGHT_PAREN, ")", null, new Position(17, 13)),
      new Token(TokenType.PRINT, "print", null, new Position(18, 6)),
      new Token(TokenType.NIL, "nil", null, new Position(18, 12)),
      new Token(TokenType.SEMICOLON, ";", null, new Position(18, 15)),
      new Token(TokenType.RETURN, "return", null, new Position(20, 4)),
      new Token(TokenType.NUMBER, "10", 10d, new Position(20, 11)),
      new Token(TokenType.EQUAL_EQUAL, "==", null, new Position(20, 14)),
      new Token(TokenType.NUMBER, "2.0", 2.0d, new Position(20, 17)),
      new Token(TokenType.OR, "or", null, new Position(20, 21)),
      new Token(TokenType.LEFT_PAREN, "(", null, new Position(21, 8)),
      new Token(TokenType.NUMBER, "3", 3d, new Position(21, 9)),
      new Token(TokenType.GREATER_EQUAL, ">=", null, new Position(21, 11)),
      new Token(TokenType.NUMBER, "3", 3d, new Position(21, 14)),
      new Token(TokenType.AND, "and", null, new Position(21, 16)),
      new Token(TokenType.STRING, "'abc'", "abc", new Position(21, 20)),
      new Token(TokenType.EQUAL_EQUAL, "==", null, new Position(21, 26)),
      new Token(TokenType.STRING, "'abc'", "abc", new Position(21, 29)),
      new Token(TokenType.RIGHT_PAREN, ")", null, new Position(21, 34)),
      new Token(TokenType.TERNARY, "?", null, new Position(22, 8)),
      new Token(TokenType.NUMBER, "0", 0d, new Position(22, 10)),
      new Token(TokenType.TERNARY_SPLIT, ":", null, new Position(23, 8)),
      new Token(TokenType.NUMBER, "1", 1d, new Position(23, 10)),
      new Token(TokenType.SEMICOLON, ";", null, new Position(23, 11)),
      new Token(TokenType.RIGHT_BRACE, "}", null, new Position(24, 2)),
      new Token(TokenType.RIGHT_BRACE, "}", null, new Position(25, 0)),
      new Token(TokenType.EOF, "\n", null, new Position(27, 0)),
    };

    assertArrayEquals(expectedTokens, lexerTokens.toArray());

    final var commentToken = "//comment";
    assertEquals(
        new Token(TokenType.EOF, "//comment", null, new Position(1, 0)),
        new Lexer(commentToken).scanTokens().get(0));

    final var blockToken = "/*comment*/";
    assertEquals(
        new Token(TokenType.EOF, "/*comment*/", null, new Position(1, 0)),
        new Lexer(blockToken).scanTokens().get(0));

    final var emptyLine = "";
    assertEquals(
        new Token(TokenType.EOF, "", null, new Position(1, 0)),
        new Lexer(emptyLine).scanTokens().get(0));

    final var addition = "10 + 50\n";
    Token[] addToken = {
      new Token(TokenType.NUMBER, "10", 10d, new Position(1, 0)),
      new Token(TokenType.PLUS, "+", null, new Position(1, 3)),
      new Token(TokenType.NUMBER, "50", 50d, new Position(1, 5)),
      new Token(TokenType.EOF, "\n", null, new Position(2, 0))
    };
    assertArrayEquals(addToken, new Lexer(addition).scanTokens().toArray());
  }
}
