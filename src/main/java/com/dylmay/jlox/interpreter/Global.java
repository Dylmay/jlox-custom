package com.dylmay.jlox.interpreter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

class Global {
  private static final HashMap<String, LoxCallable> globals = new HashMap<>();

  static {
    globals.put(
        "clock",
        new LoxCallable() {

          @Override
          public int arity() {
            return 0;
          }

          @Override
          public Object call(Interpreter interpreter, List<Object> args) {
            return System.currentTimeMillis() / 1000.0d;
          }

          @Override
          public String toString() {
            return "<native fn>";
          }
        });

    globals.put(
        "print",
        new LoxCallable() {
          @Override
          public int arity() {
            return 1;
          }

          @Override
          public @Nullable Object call(Interpreter interpreter, List<Object> args) {
            System.out.println(stringify(args.get(0)));

            return null;
          }

          @Override
          public String toString() {
            return "<native fn>";
          }

          static String stringify(@Nullable Object obj) {
            if (obj == null) {
              return "nil";
            }

            var text = obj.toString();

            if (obj instanceof Double && text.endsWith(".0")) {
              text = text.substring(0, text.length() - 2);
            }

            return text;
          }
        });

    globals.put(
        "str",
        new LoxCallable() {
          @Override
          public int arity() {
            return 1;
          }

          @Override
          public @Nullable Object call(Interpreter interpreter, List<Object> args) {
            final var obj = args.get(0);

            if (obj == null) {
              return "nil";
            }

            var text = obj.toString();

            if (obj instanceof Double && text.endsWith(".0")) {
              text = text.substring(0, text.length() - 2);
            }

            return text;
          }
        });
  }

  private Global() {}

  public static Environment create() {
    return new Environment(null, (Map<String, Object>) globals.clone());
  }
}
