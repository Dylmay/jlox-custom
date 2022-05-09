package com.dylmay.jlox.interpreter;

import com.dylmay.jlox.assets.Expr;
import com.dylmay.jlox.assets.Position;
import com.dylmay.jlox.assets.Token;
import com.dylmay.jlox.assets.TokenType;
import com.dylmay.jlox.interpreter.call.Return;
import java.util.List;

class LoxFunction implements LoxCallable {
  private final Token name;
  private final Expr.Fn fn;
  private final Environment closure;

  LoxFunction(Token nameTkn, Expr.Fn decl, Environment closure) {
    this.name = nameTkn;
    this.fn = decl;
    this.closure = closure;
  }

  LoxFunction(Expr.Fn decl, Environment closure) {
    this(new Token(TokenType.FN, "fn", "Anonymous", Position.NO_POSITION), decl, closure);
  }

  @Override
  @SuppressWarnings("nullness")
  public Object call(Interpreter interpreter, List<Object> args) {
    var env = new Environment(closure);
    for (int i = 0; i < fn.parms.size(); i++) {
      env.define(fn.parms.get(i).lexeme(), args.get(i));
    }

    try {
      interpreter.executeBlock(fn.body, env);
    } catch (Return retval) {
      return retval.value;
    }

    return null;
  }

  @Override
  public int arity() {
    return fn.parms.size();
  }

  @Override
  public String toString() {
    return "<fn " + name.lexeme() + ">";
  }
}
