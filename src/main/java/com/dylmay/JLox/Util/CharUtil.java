package com.dylmay.JLox.Util;

public final class CharUtil {
  private CharUtil() {}

  public static boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }

  public static boolean isAlpha(char c) {
    return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
  }

  public static boolean isAlphaNumeric(char c) {
    return CharUtil.isDigit(c) || CharUtil.isAlpha(c);
  }
}
