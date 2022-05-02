package com.dylmay.JLox.Assets;

import javax.annotation.Nullable;

public record Token(TokenType type, String lexeme, @Nullable Object literal, Position position) {}
