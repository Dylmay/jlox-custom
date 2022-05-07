package com.dylmay.jlox.interpreter;

import com.dylmay.jlox.assets.Expr;
import com.dylmay.jlox.assets.Expr.Call;
import com.dylmay.jlox.assets.Expr.Logical;
import com.dylmay.jlox.assets.Item;
import com.dylmay.jlox.assets.Position;
import com.dylmay.jlox.assets.Stmt;
import com.dylmay.jlox.assets.Stmt.Function;
import com.dylmay.jlox.assets.Stmt.If;
import com.dylmay.jlox.assets.Stmt.While;
import com.dylmay.jlox.assets.TokenType;
import com.dylmay.jlox.error.ErrorMessage;
import com.dylmay.jlox.error.LoxErrorHandler;
import com.dylmay.jlox.util.RuntimeError;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

public class Interpreter implements Expr.Visitor<Item>, Stmt.Visitor<Void> {
  private static final LoxErrorHandler ERR_HNDLR = LoxErrorHandler.getInstance(Interpreter.class);

  private final Environment globals = new Environment();
  private Environment env;

  public Interpreter() {
    this.env = globals;

    globals.define(
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
    globals.define(
        "print",
        new LoxCallable() {
          @Override
          public int arity() {
            return 1;
          }

          @Override
          public @Nullable Object call(Interpreter interpreter, List<Object> args) {
            System.out.println(args.get(0).toString());

            return null;
          }

          @Override
          public String toString() {
            return "<native fn>";
          }
        });
    // globals.define("fmt", new LoxCallable() {
    //   @Override
    //   public int arity() {
    //     return 2;
    //   }

    //   @Override
    //   public Object call(Interpreter interpreter, List<Object> args) {
    //     final var pattern = Pattern.compile("{.*}");
    //     var sb = new StringBuilder(args.get(0).toString());

    //     return sb.toString();
    //   }
    // });
  }

  @Override
  @SuppressWarnings("nullness")
  public Item visitBinaryExpr(Expr.Binary expr) {
    Item left = this.evaluate(expr.left);
    Item right = this.evaluate(expr.right);

    switch (expr.operator.type()) {
      case MINUS:
        assertIsNumber(left, right);
        return new Item(left.as(Double.class) - right.as(Double.class), expr.operator.position());

      case SLASH:
        assertIsNumber(left, right);
        return new Item(left.as(Double.class) / right.as(Double.class), expr.operator.position());

      case PLUS:
        if (left.result() instanceof Double ld && right.result() instanceof Double rd)
          return new Item(ld + rd, expr.operator.position());
        if (left.result() instanceof String ls && right.result() instanceof String rs)
          return new Item(ls + rs, expr.operator.position());

        break;

      case STAR:
        assertIsNumber(left, right);
        return new Item(left.as(Double.class) * right.as(Double.class), expr.operator.position());

      case GREATER:
        assertIsNumber(left, right);
        return new Item(left.as(Double.class) > right.as(Double.class), expr.operator.position());

      case GREATER_EQUAL:
        assertIsNumber(left, right);
        return new Item(left.as(Double.class) >= right.as(Double.class), expr.operator.position());

      case LESS:
        assertIsNumber(left, right);
        return new Item(left.as(Double.class) < right.as(Double.class), expr.operator.position());

      case LESS_EQUAL:
        assertIsNumber(left, right);
        return new Item(left.as(Double.class) <= right.as(Double.class), expr.operator.position());

      case BANG_EQUAL:
        assertIsNumber(left, right);
        return new Item(
            !this.isEqual(left.as(Double.class), right.as(Double.class)), expr.operator.position());

      case EQUAL_EQUAL:
        assertIsNumber(left, right);
        return new Item(
            this.isEqual(left.as(Double.class), right.as(Double.class)), expr.operator.position());

      case COMMA:
        return right;
    }

    throw new RuntimeError(expr.operator.position(), "Unknown Binary Expression");
  }

  @Override
  public Item visitTernaryExpr(Expr.Ternary expr) {
    var condition = this.evaluate(expr.condition);

    if (condition.result() instanceof Boolean || condition.result() instanceof Double) {
      return Boolean.TRUE.equals(condition.result())
          ? this.evaluate(expr.onTrue)
          : this.evaluate(expr.onFalse);
    }

    // TODO: implement token fetching
    // TODO: decide whether to return false on bad token
    throw new RuntimeError(condition.position(), "Unknown Ternary Condition");
  }

  @Override
  public Item visitGroupingExpr(Expr.Grouping expr) {
    return this.evaluate(expr.expression);
  }

  @Override
  public @Nullable Item visitLiteralExpr(Expr.Literal expr) {
    return new Item(expr.value, expr.pos);
  }

  @Override
  @SuppressWarnings("nullness")
  public Item visitUnaryExpr(Expr.Unary expr) {
    Item right = this.evaluate(expr.right);

    switch (expr.operator.type()) {
      case BANG:
        return new Item(!this.isTruthy(right), expr.operator.position());

      case MINUS:
        return new Item(-(double) right.result(), expr.operator.position());

      case PLUS:
        return new Item(right.result(), expr.operator.position());

      default:
        throw new RuntimeError(expr.operator.position(), "Unknown Unary operator");
    }
  }

  private Item evaluate(Expr expr) {
    return expr.accept(this);
  }

  private boolean isTruthy(@Nullable Item item) {
    if (item == null) return false;

    var result = item.result();

    if (result == null) return false;
    if (result instanceof Boolean b) return b;

    return true;
  }

  private boolean isEqual(Object a, Object b) {
    if (a == null && b == null) return true;
    if (a == null) return false;

    return a.equals(b);
  }

  private void assertIsNumber(Item... values) {
    for (var item : values) {
      if (!item.is(Double.class)) {
        throw new RuntimeError(item.position(), "Operand must be a number");
      }
    }
  }

  private Void execute(Stmt stmt) {
    return stmt.accept(this);
  }

  public void interpret(List<Stmt> statements) throws RuntimeError {
    try {
      statements.forEach(this::execute);
    } catch (RuntimeError error) {
      var msg = error.getMessage();
      var issue = new ErrorMessage().position(error.position);

      if (msg != null) {
        issue.message(msg);
      }

      ERR_HNDLR.report(issue);
    }
  }

  private String stringify(@Nullable Object obj) {
    if (obj == null) {
      return "nil";
    }

    var text = obj.toString();

    if (obj instanceof Double && text.endsWith(".0")) {
      text = text.substring(0, text.length() - 2);
    }

    return text;
  }

  @Override
  public Void visitExpressionStmt(Stmt.Expression stmt) {
    this.evaluate(stmt.expr);
    return null;
  }

  @Override
  @SuppressWarnings("nullable")
  public Void visitVarStmt(Stmt.Var stmt) {
    Object value = null;

    if (stmt.initializer != null) {
      value = this.evaluate(stmt.initializer).result();
    }

    if (!this.env.define(stmt.name.lexeme(), value)) {
      throw new RuntimeError(
          stmt.name.position(), "Variable '" + stmt.name.lexeme() + "' is not defined.");
    }

    // return stmt.name.lexeme() + " ==> " + (value != null ? value.toString() : "nil");
    return null;
  }

  @Override
  public Item visitVariableExpr(Expr.Variable expr) {
    // TODO: tidy up to use object
    return new Item(this.env.get(expr.name), Position.NO_POSITION);
  }

  @Override
  public Item visitAssignExpr(Expr.Assign expr) {
    var value = this.evaluate(expr.value);

    if (!this.env.assign(expr.name.lexeme(), value != null ? value.result() : null)) {
      throw new RuntimeError(
          expr.name.position(), "Undefined variable '" + expr.name.lexeme() + "'.");
    }

    return value;
  }

  @Override
  public Void visitBlockStmt(Stmt.Block stmt) {
    this.executeBlock(stmt.stmts, new Environment(this.env));

    return null;
  }

  private Void executeBlock(List<Stmt> statements, Environment newEnv) {
    var prevEnv = this.env;

    try {
      this.env = newEnv;

      for (Stmt stmt : statements) {
        this.execute(stmt);
      }

    } finally {
      this.env = prevEnv;
    }

    return null;
  }

  @Override
  public Void visitIfStmt(If stmt) {
    if (this.isTruthy(this.evaluate(stmt.condition))) {
      this.execute(stmt.thenBranch);
    } else if (stmt.elseBranch != null) {
      this.execute(stmt.elseBranch);
    }
    return null;
  }

  @Override
  public Item visitLogicalExpr(Logical expr) {
    var left = this.evaluate(expr.left);

    if (expr.operator.type() == TokenType.OR) {
      if (isTruthy(left)) return left;
    } else {
      if (!isTruthy(left)) return left;
    }

    return evaluate(expr.right);
  }

  @Override
  public Void visitWhileStmt(While stmt) {
    while (isTruthy(evaluate(stmt.condition))) {
      execute(stmt.body);
    }

    return null;
  }

  @Override
  @SuppressWarnings("nullness")
  public Item visitCallExpr(Call expr) {
    var callee = this.evaluate(expr.callee);

    var args = new ArrayList<>();
    for (var arg : expr.args) {
      args.add(this.evaluate(arg).result());
    }

    if (callee.result() instanceof LoxCallable function && args.size() == function.arity()) {
      return new Item(function.call(this, args), expr.paren.position());
    }

    throw new RuntimeError(expr.paren.position(), "Can only call functions and classess");
  }

  @Override
  public Void visitFunctionStmt(Function stmt) {
    // TODO Auto-generated method stub
    return null;
  }
}
