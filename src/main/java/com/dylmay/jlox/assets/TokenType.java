package com.dylmay.jlox.assets;

public enum TokenType {
  // Single char tokens
  LEFT_PAREN,
  RIGHT_PAREN,
  LEFT_BRACE,
  RIGHT_BRACE,
  COMMA,
  DOT,
  MINUS,
  PLUS,
  SEMICOLON,
  SLASH,
  STAR,
  TERNARY,
  TERNARY_SPLIT,

  // Multi char tokens
  BANG,
  BANG_EQUAL,
  EQUAL,
  EQUAL_EQUAL,
  GREATER,
  GREATER_EQUAL,
  LESS,
  LESS_EQUAL,
  PLUS_EQUAL,
  SLASH_EQUAL,
  MINUS_EQUAL,
  STAR_EQUAL,

  // Literals
  IDENTIFIER,
  STRING,
  NUMBER,

  // Keywords
  AND,
  CLASS,
  ELSE,
  FALSE,
  FN,
  FOR,
  IF,
  NIL,
  OR,
  RETURN,
  SUPER,
  THIS,
  TRUE,
  LET,
  WHILE,
  CONTINUE,
  BREAK,
  MUT,
  STATIC,

  EOF,
}
