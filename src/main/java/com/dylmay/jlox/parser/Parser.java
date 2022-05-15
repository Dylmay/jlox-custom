package com.dylmay.jlox.parser;

import com.dylmay.jlox.assets.Expr;
import com.dylmay.jlox.assets.Stmt;
import com.dylmay.jlox.assets.Token;
import com.dylmay.jlox.assets.TokenType;
import com.dylmay.jlox.error.ErrorMessage;
import com.dylmay.jlox.error.LoxErrorHandler;
import java.util.ArrayList;
import java.util.Arrays;
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
        return this.varDeclaration(false);
      }

      if (match(TokenType.CLASS)) {
        return this.classDeclaration();
      }

      if (match(TokenType.FN)) {
        return stmtFunction("function", false);
      }

      return statement();
    } catch (ParseException exc) {
      this.synchronize();
      return null;
    }
  }

  private Stmt classDeclaration() {
    var name = consume(TokenType.IDENTIFIER, "Expected class name.");

    Expr.Variable superclass = null;
    if (match(TokenType.COLON)) {
      consume(TokenType.IDENTIFIER, "Expected superclass name.");
      superclass = new Expr.Variable(this.previous());
    }

    consume(TokenType.LEFT_BRACE, "Expected '{' before class body.");
    var methods = new ArrayList<Stmt.Var>();

    while (!check(TokenType.RIGHT_BRACE) && !this.isAtEnd()) {
      var isStatic = match(TokenType.STATIC);

      if (match(TokenType.FN)) {
        methods.add((Stmt.Var) this.stmtFunction("method", isStatic));
      } else if (match(TokenType.LET)) {
        methods.add((Stmt.Var) this.varDeclaration(isStatic));
      } else {
        ERR_HDNLR.report(
            new ErrorMessage()
                .where(this.peek().lexeme())
                .position(this.peek().position())
                .message("Expected declaration"));

        this.advance();
      }
    }

    consume(TokenType.RIGHT_BRACE, "Expected '}' at class end.");

    return new Stmt.Class(name, methods, superclass);
  }

  private Expr.Fn exprFn(String kind) {
    var parms = new ArrayList<Token>();
    var funcTkn = this.previous();

    consume(TokenType.LEFT_PAREN, "Expected '(' after " + kind + " name.");
    if (!check(TokenType.RIGHT_PAREN)) {
      do {
        if (parms.size() >= 255) {
          error(this.peek(), "Can't have more than 255 parameters.");
        }
        parms.add(consume(TokenType.IDENTIFIER, "Expected parameter name."));
      } while (match(TokenType.COMMA));
    }
    consume(TokenType.RIGHT_PAREN, "Expected ')' after parameters.");
    consume(TokenType.LEFT_BRACE, "Expected '{' before " + kind + " body.");

    return new Expr.Fn(funcTkn.position(), parms, this.block());
  }

  private Stmt stmtFunction(String kind, boolean isStatic) {
    var name = consume(TokenType.IDENTIFIER, "Expected " + kind + " name.");

    var isMutable = match(TokenType.MUT);

    return new Stmt.Var(name, this.exprFn(kind), isMutable, isStatic);
  }

  private Stmt varDeclaration(boolean isStatic) {
    var isMutable = match(TokenType.MUT);

    Token name = consume(TokenType.IDENTIFIER, "Expected variable name.");
    Expr initializer = match(TokenType.EQUAL) ? expression() : null;

    consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.");

    return new Stmt.Var(name, initializer, isMutable, isStatic);
  }

  private Stmt statement() {
    if (match(TokenType.IF)) return ifStatement();
    if (match(TokenType.FOR)) return forStatement();
    if (match(TokenType.WHILE)) return whileStatement();
    if (match(TokenType.RETURN)) return returnStatement();
    if (match(TokenType.LEFT_BRACE)) return new Stmt.Block(this.block());
    if (match(TokenType.CONTINUE)) return continueStatement();
    if (match(TokenType.BREAK)) return breakStatement();

    return expressionStatement();
  }

  private Stmt breakStatement() {
    consume(TokenType.SEMICOLON, "Expected ';' after break.");

    return new Stmt.Break(this.previous());
  }

  private Stmt continueStatement() {
    consume(TokenType.SEMICOLON, "Expected ';' after continue.");

    return new Stmt.Continue(this.previous());
  }

  private Stmt returnStatement() {
    var keyword = this.previous();
    Expr value = this.check(TokenType.SEMICOLON) ? null : this.expression();

    consume(TokenType.SEMICOLON, "Expected ';' after return value.");

    return new Stmt.Return(keyword, value);
  }

  private Stmt forStatement() {
    Stmt initializer;

    var token = this.peek();

    if (match(TokenType.SEMICOLON)) {
      initializer = null;
    } else if (match(TokenType.LET)) {
      initializer = varDeclaration(false);
    } else {
      initializer = expressionStatement();
    }

    Expr condition = null;
    if (!check(TokenType.SEMICOLON)) {
      condition = expression();
    }
    consume(TokenType.SEMICOLON, "Expect ';' after loop condiiton.");

    Expr increment = null;
    if (!check(TokenType.RIGHT_PAREN)) {
      increment = expression();
    }

    Stmt body = bracedStatement();

    if (increment != null) {
      body = new Stmt.Block(Arrays.asList(body, new Stmt.Expression(increment)));
    }
    if (condition == null) {
      condition = new Expr.Literal(true, token.position());
    }

    body = new Stmt.While(condition, body);

    if (initializer != null) {
      body = new Stmt.Block(Arrays.asList(initializer, body));
    }

    return body;
  }

  private Stmt ifStatement() {
    Expr condition = this.expression();

    Stmt thenBranch = this.bracedStatement();
    Stmt elseBranch = null;

    if (match(TokenType.ELSE)) {
      elseBranch = (this.check(TokenType.IF)) ? this.statement() : this.bracedStatement();
    }

    return new Stmt.If(condition, thenBranch, elseBranch);
  }

  private Stmt bracedStatement() {
    if (!this.check(TokenType.LEFT_BRACE)) {
      error(this.peek(), "Expected '{' before else statement");
    }

    return this.statement();
  }

  private Stmt whileStatement() {
    var condition = this.expression();

    var body = this.bracedStatement();

    return new Stmt.While(condition, body);
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

    if ((expr instanceof Expr.Variable || expr instanceof Expr.Get)
        && match(
            TokenType.STAR_EQUAL,
            TokenType.MINUS_EQUAL,
            TokenType.PLUS_EQUAL,
            TokenType.SLASH_EQUAL,
            TokenType.EQUAL)) {
      var token = this.previous();
      var value = this.assignment();

      switch (token.type()) {
        case STAR_EQUAL:
          value =
              new Expr.Binary(expr, new Token(TokenType.STAR, "*", null, token.position()), value);
          break;

        case SLASH_EQUAL:
          value =
              new Expr.Binary(expr, new Token(TokenType.SLASH, "/", null, token.position()), value);
          break;

        case MINUS_EQUAL:
          value =
              new Expr.Binary(expr, new Token(TokenType.MINUS, "-", null, token.position()), value);
          break;

        case PLUS_EQUAL:
          value =
              new Expr.Binary(expr, new Token(TokenType.PLUS, "+", null, token.position()), value);
          break;

        case EQUAL:
          break;

        default:
          this.error(this.previous(), "Invalid assignment target");
          break;
      }

      if (expr instanceof Expr.Variable variable) {
        return new Expr.Assign(variable.name, value);
      } else if (expr instanceof Expr.Get get) {
        return new Expr.Set(get.object, get.name, value);
      }
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

    return call();
  }

  private Expr call() {
    var expr = this.primary();

    while (true) {
      if (match(TokenType.LEFT_PAREN)) {
        expr = finishCall(expr);
      } else if (match(TokenType.DOT)) {
        var name = consume(TokenType.IDENTIFIER, "Expect property name after '.'.");
        expr = new Expr.Get(expr, name);
      } else {
        break;
      }
    }

    return expr;
  }

  private Expr finishCall(Expr callee) {
    var args = new ArrayList<Expr>(5);

    if (!check(TokenType.RIGHT_PAREN)) {
      do {
        if (args.size() >= 255) {
          error(peek(), "Can't have more than 255 arguments.");
        }
        args.add(this.expression());
      } while (match(TokenType.COMMA));
    }

    var paren = consume(TokenType.RIGHT_PAREN, "Expect ')' after arguments.");

    return new Expr.Call(callee, paren, args);
  }

  private Expr primary() {
    if (match(TokenType.FALSE)) {
      return new Expr.Literal(false, this.previous().position());
    }

    if (match(TokenType.THIS)) {
      return new Expr.This(this.previous());
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

    if (match(TokenType.FN)) {
      return this.exprFn("Lambda");
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

  private Expr or() {
    var expr = and();

    while (match(TokenType.OR)) {
      var operator = previous();
      var right = and();

      expr = new Expr.Logical(expr, operator, right);
    }

    return expr;
  }

  private Expr and() {
    var expr = this.equality();

    while (match(TokenType.AND)) {
      var operator = previous();
      var right = and();

      expr = new Expr.Logical(expr, operator, right);
    }

    return expr;
  }

  private Expr ternary() {
    var expr = or();

    if (match(TokenType.TERNARY)) {
      var onTrue = this.expression();

      if (match(TokenType.COLON)) {
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
