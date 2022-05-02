package com.dylmay.jlox.parser;

import java.util.List;
import javax.annotation.Nullable;

import com.dylmay.jlox.assets.Expr;
import com.dylmay.jlox.assets.Token;
import com.dylmay.jlox.assets.TokenType;
import com.dylmay.jlox.error.ErrorMessage;
import com.dylmay.jlox.error.LoxErrorHandler;

public class Parser {
  private static class ParseException extends RuntimeException {}

  private interface ExprFunc {
    Expr expr();
  }

  private static final LoxErrorHandler ERR_HDNLR = LoxErrorHandler.getInstance(Parser.class);
  private final List<Token> tokens;
  private int current;

  public Parser(List<Token> tokens) {
    this.tokens = tokens;
    this.current = 0;
  }

  public @Nullable Expr parse() {
    try {
      return this.expression();
    } catch (ParseException exc) {
      return null;
    }
  }

  private Expr expression() {
    return this.ternary();
  }

  private Expr equality() {
    return this.findBinaryMatch(this::comparison, TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL);
  }

  private Expr comparison() {
    return this.findBinaryMatch(
        this::term,
        TokenType.GREATER,
        TokenType.GREATER_EQUAL,
        TokenType.LESS,
        TokenType.LESS_EQUAL);
  }

  private Expr term() {
    return this.findBinaryMatch(this::factor, TokenType.MINUS, TokenType.PLUS);
  }

  private Expr factor() {
    return this.findBinaryMatch(this::unary, TokenType.SLASH, TokenType.STAR);
  }

  private Expr unary() {
    if (match(TokenType.BANG, TokenType.MINUS, TokenType.PLUS)) {
      var operator = this.previous();
      var right = this.unary();

      return new Expr.Unary(operator, right);
    }

    return primary();
  }

  private Expr primary() {
    if (match(TokenType.FALSE)) return new Expr.Literal(false, this.previous().position());
    if (match(TokenType.TRUE)) return new Expr.Literal(true, this.previous().position());
    if (match(TokenType.NIL)) return new Expr.Literal(null, this.previous().position());

    if (match(TokenType.NUMBER, TokenType.STRING)) {
      return new Expr.Literal(this.previous().literal(), this.previous().position());
    }

    if (match(TokenType.LEFT_PAREN)) {
      var expr = this.expression();

      this.consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");

      return new Expr.Grouping(expr);
    }

    throw this.error(this.peek(), "Expected expression.");
  }

  private Expr findBinaryMatch(ExprFunc func, TokenType... matches) {
    var expr = func.expr();

    while (match(matches)) {
      var operator = previous();
      var right = func.expr();

      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr ternary() {
    var expr = this.findBinaryMatch(this::equality, TokenType.COMMA);

    if (match(TokenType.TERNARY)) {
      var onTrue = this.expression();

      if (match(TokenType.TERNARY_SPLIT)) {
        var onFalse = this.expression();

        return new Expr.Ternary(expr, onTrue, onFalse);
      } else {
        throw this.error(this.peek(), "Expected ternary split :");
      }
    }

    return expr;
  }

  private Token consume(TokenType type, String msg) {
    if (this.check(type)) return this.advance();

    throw error(this.peek(), msg);
  }

  private boolean match(TokenType... types) {
    for (var type : types) {
      if (check(type)) {
        advance();
        return true;
      }
    }

    return false;
  }

  private boolean check(TokenType type) {
    if (this.isAtEnd()) return false;

    return this.peek().type() == type;
  }

  private boolean isAtEnd() {
    return this.peek().type() == TokenType.EOF;
  }

  private Token advance() {
    if (!this.isAtEnd()) current++;

    return this.previous();
  }

  private Token peek() {
    return tokens.get(current);
  }

  private Token previous() {
    return tokens.get(current - 1);
  }

  private ParseException error(Token token, String message) {
    ERR_HDNLR.report(
        new ErrorMessage().where(token.lexeme()).position(token.position()).message(message));

    return new ParseException();
  }

  private void synchronize() {
    advance();
    while (!this.isAtEnd()) {
      if (previous().type() == TokenType.SEMICOLON) return;

      switch (this.peek().type()) {
        case CLASS:
        case FOR:
        case FUN:
        case IF:
        case PRINT:
        case RETURN:
        case VAR:
        case WHILE:
          return;

        default:
          break;
      }

      advance();
    }
  }
}
