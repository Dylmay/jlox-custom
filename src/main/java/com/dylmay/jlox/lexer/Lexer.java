package com.dylmay.jlox.lexer;

import com.dylmay.jlox.assets.Position;
import com.dylmay.jlox.assets.Token;
import com.dylmay.jlox.assets.TokenType;
import com.dylmay.jlox.error.ErrorMessage;
import com.dylmay.jlox.error.LoxErrorHandler;
import com.dylmay.jlox.util.CharUtil;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

public class Lexer {
  private static final LoxErrorHandler ERR_HNDLR = LoxErrorHandler.getInstance(Lexer.class);
  private final List<Token> tokens;
  private final String src;

  private int tokenStart;
  private int symCurrent;

  private int newlineCnt;
  private int newlineOffset;

  public Lexer(String source) {
    this.src = source;
    this.tokens = new ArrayList<>();
    this.tokenStart = 0;
    this.symCurrent = 0;
    this.newlineCnt = 0;
    this.newlineOffset = 0;
  }

  public List<Token> scanTokens() {
    while (!this.isEOF()) {
      this.tokenStart = this.symCurrent;
      this.nextToken();
    }

    this.addToken(TokenType.EOF);
    return this.tokens;
  }

  private boolean isEOF() {
    return this.symCurrent >= src.length();
  }

  private void nextToken() {
    char nextChar = this.advance();

    switch (nextChar) {
      case '(':
        this.addToken(TokenType.LEFT_PAREN);
        break;

      case ')':
        this.addToken(TokenType.RIGHT_PAREN);
        break;

      case '{':
        this.addToken(TokenType.LEFT_BRACE);
        break;

      case '}':
        this.addToken(TokenType.RIGHT_BRACE);
        break;

      case ',':
        this.addToken(TokenType.COMMA);
        break;

      case '.':
        this.addToken(TokenType.DOT);
        break;

      case '-':
        this.addToken(TokenType.MINUS);
        break;

      case '+':
        this.addToken(TokenType.PLUS);
        break;

      case ';':
        this.addToken(TokenType.SEMICOLON);
        break;

      case '*':
        this.addToken(TokenType.STAR);
        break;

      case '?':
        this.addToken(TokenType.TERNARY);
        break;

      case ':':
        this.addToken(TokenType.TERNARY_SPLIT);
        break;

      case '!':
        this.addToken(this.match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
        break;

      case '=':
        this.addToken(this.match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
        break;

      case '<':
        this.addToken(this.match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
        break;

      case '>':
        this.addToken(this.match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
        break;

      case '/':
        if (this.match('*')) { // block comment
          procBlockComment();
        } else if (this.match('/')) { // line comment
          procLineComment();
        } else {
          addToken(TokenType.SLASH);
        }
        break;

      case '"':
      case '\'':
        procStringToken(nextChar);
        break;

      case ' ':
      case '\r':
      case '\t':
      case '\n':
        break;

      default:
        if (CharUtil.isDigit(nextChar)) {
          procNumToken();
        } else if (CharUtil.isAlpha(nextChar)) {
          procIdentifier();
        } else {
          ERR_HNDLR.report(
              new ErrorMessage()
                  .message("Unexpected character: " + nextChar)
                  .position(this.getCurPos()));
        }

        break;
    }
  }

  private void incIfNewline() {
    if (this.peek() == '\n') {
      this.newlineCnt++;
      this.newlineOffset = this.symCurrent + 1;
    }
  }

  private String getLexeme() {
    return this.src.substring(this.tokenStart, this.symCurrent);
  }

  private void procIdentifier() {
    while (CharUtil.isAlphaNumeric(peek())) advance();

    TokenType keyword = TokenKeywords.match(getLexeme());

    addToken(keyword != null ? keyword : TokenType.IDENTIFIER);
  }

  private void procLineComment() {
    while (!this.match('\n') && !this.isEOF()) advance();
  }

  private void procBlockComment() {
    var badBlock = new ErrorMessage().message("Unterminated comment. must end with \"*/\"");

    while (true) {
      if (this.isEOF()) {
        ERR_HNDLR.report(badBlock.position(this.getCurPos()));
        return;
      }

      if (this.match('*')) {
        if (this.isEOF()) {
          ERR_HNDLR.report(badBlock.position(this.getCurPos()));
          return;
        }

        if (this.match('/')) {
          break;
        }
      }

      this.advance();
    }
  }

  private void procStringToken(char stringIdentifier) {
    while (peek() != stringIdentifier && !this.isEOF()) advance();

    if (this.isEOF()) {
      ERR_HNDLR.report(
          new ErrorMessage()
              .message("Unterminated string. Must end with \"" + stringIdentifier + '"')
              .position(this.getCurPos()));
      return;
    }

    advance();

    addToken(TokenType.STRING, this.src.substring(this.tokenStart + 1, this.symCurrent - 1));
  }

  private void procNumToken() {
    while (CharUtil.isDigit(peek())) advance();

    if (peek() == '.' && CharUtil.isDigit(peek(1))) {
      advance();

      while (CharUtil.isDigit(peek())) advance();
    }

    addToken(TokenType.NUMBER, Double.parseDouble(this.getLexeme()));
  }

  private char peek() {
    return this.peek(0);
  }

  private char peek(int depth) {
    if (this.isEOF()) return 0;

    return this.src.charAt(this.symCurrent + depth);
  }

  private char advance() {
    this.incIfNewline();
    return this.src.charAt(this.symCurrent++);
  }

  private boolean match(char expected) {
    if (this.peek() != expected) return false;

    this.incIfNewline();
    this.symCurrent++;
    return true;
  }

  private void addToken(TokenType type) {
    addToken(type, null);
  }

  private void addToken(TokenType type, @Nullable Object literal) {
    tokens.add(new Token(type, this.getLexeme(), literal, this.getCurPos()));
  }

  private Position getCurPos() {
    return new Position(this.newlineCnt + 1, Math.max(this.tokenStart - this.newlineOffset, 0));
  }
}
