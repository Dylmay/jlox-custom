package com.dylmay.jlox.parser;

import com.dylmay.jlox.assets.Expr;
import com.dylmay.jlox.assets.Stmt;
import com.dylmay.jlox.assets.Token;
import com.dylmay.jlox.assets.TokenType;
import com.dylmay.jlox.error.ErrorMessage;
import com.dylmay.jlox.error.LoxErrorHandler;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

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

  public List<Stmt> parse() {
    List<Stmt> statements = new ArrayList<>();

    while (!this.isAtEnd()) {
      var stmt = this.declaration();

      if (stmt != null) {
        statements.add(stmt);
      }
    }

    return statements;
  }

  private @Nullable Stmt declaration() {
    try {
      if (match(TokenType.LET)) {
        return this.varDeclaration();
      }

      return statement();
    } catch (ParseException exc) {
      this.synchronize();
      return null;
    }
  }

  private Stmt varDeclaration() {
    Token name = consume(TokenType.IDENTIFIER, "Expected variable name.");
    Expr initializer = match(TokenType.EQUAL) ? expression() : null;

    consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.");

    return new Stmt.Var(name, initializer);
  }

  private Stmt statement() {
    if (match(TokenType.IF)) return ifStatement();

    if (match(TokenType.PRINT)) return printStatement();

    if (match(TokenType.LEFT_BRACE)) return new Stmt.Block(this.block());

    return expressionStatement();
  }

  private Stmt ifStatement() {
    Expr condition = this.expression();
    consume(TokenType.LEFT_BRACE, "Expected '{' at the start of the if condition.");

    Stmt thenBranch = this.statement();
    Stmt elseBranch = null;

    consume(TokenType.RIGHT_BRACE, "Expected '}' at the end of the if condition");
    if (match(TokenType.ELSE)) {
      elseBranch = this.statement();
    }

    return new Stmt.If(condition, thenBranch, elseBranch);
  }

  private Stmt printStatement() {
    var expr = this.expression();

    consume(TokenType.SEMICOLON, "Expect ';' after value.");

    return new Stmt.Print(expr);
  }

  private Stmt expressionStatement() {
    var expr = this.expression();

    consume(TokenType.SEMICOLON, "Expect ';' after expression");

    return new Stmt.Expression(expr);
  }

  private List<Stmt> block() {
    var stmts = new ArrayList<Stmt>();

    while (!check(TokenType.RIGHT_BRACE) && !this.isAtEnd()) {
      var decl = this.declaration();

      if (decl != null) {
        stmts.add(decl);
      }
    }

    consume(TokenType.RIGHT_BRACE, "Expected '}' after block.");

    return stmts;
  }

  private Expr expression() {
    return this.assignment();
  }

  private Expr assignment() {
    var expr = this.ternary();

    if (match(TokenType.EQUAL)) {
      var equals = this.previous();
      var value = this.assignment();

      if (expr instanceof Expr.Variable variable) {
        return new Expr.Assign(variable.name, value);
      }

      this.error(equals, "Invalid assignment target");
    }

    return expr;
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
    if (match(TokenType.FALSE)) {
      return new Expr.Literal(false, this.previous().position());
    }

    if (match(TokenType.TRUE)) {
      return new Expr.Literal(true, this.previous().position());
    }

    if (match(TokenType.NIL)) {
      return new Expr.Literal(null, this.previous().position());
    }

    if (match(TokenType.NUMBER, TokenType.STRING)) {
      return new Expr.Literal(this.previous().literal(), this.previous().position());
    }

    if (match(TokenType.EQUAL)) {
      return new Expr.Variable(this.previous());
    }

    if (match(TokenType.LEFT_PAREN)) {
      var expr = this.expression();

      this.consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");

      return new Expr.Grouping(expr);
    }

    if (match(TokenType.IDENTIFIER)) {
      return new Expr.Variable(this.previous());
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
        case FN:
        case IF:
        case PRINT:
        case RETURN:
        case LET:
        case WHILE:
          return;

        default:
          break;
      }

      advance();
    }
  }
}
