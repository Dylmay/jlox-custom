package com.dylmay.JLox.Assets;

public record Position(int lineNum, int lineOffset) {
  public static final Position NO_POSITION = new Position(-1, -1);

  @Override
  public String toString() {
    return String.format("[line %s; offset %s]", this.lineNum(), this.lineOffset());
  }
}
