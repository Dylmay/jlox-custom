package com.dylmay.jlox.lexer;

import com.dylmay.jlox.assets.TokenType;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

class TokenKeywords {
  private TokenKeywords() {}

  private static final Map<String, TokenType> KEYWORDS;

  static {
    KEYWORDS = new HashMap<>();
    KEYWORDS.put("and", TokenType.AND);
    KEYWORDS.put("class", TokenType.CLASS);
    KEYWORDS.put("else", TokenType.ELSE);
    KEYWORDS.put("false", TokenType.FALSE);
    KEYWORDS.put("for", TokenType.FOR);
    KEYWORDS.put("fn", TokenType.FN);
    KEYWORDS.put("if", TokenType.IF);
    KEYWORDS.put("nil", TokenType.NIL);
    KEYWORDS.put("or", TokenType.OR);
    KEYWORDS.put("print", TokenType.PRINT);
    KEYWORDS.put("super", TokenType.SUPER);
    KEYWORDS.put("this", TokenType.THIS);
    KEYWORDS.put("true", TokenType.TRUE);
    KEYWORDS.put("let", TokenType.LET);
    KEYWORDS.put("while", TokenType.WHILE);
    KEYWORDS.put("return", TokenType.RETURN);
  }

  public static @Nullable TokenType match(String reservedWord) {
    return KEYWORDS.get(reservedWord);
  }
}
