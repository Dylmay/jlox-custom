package com.dylmay.jlox.resolver;

import com.dylmay.jlox.assets.Expr;
import com.dylmay.jlox.assets.Expr.Binary;
import com.dylmay.jlox.assets.Expr.Call;
import com.dylmay.jlox.assets.Expr.Fn;
import com.dylmay.jlox.assets.Expr.Grouping;
import com.dylmay.jlox.assets.Expr.Literal;
import com.dylmay.jlox.assets.Expr.Logical;
import com.dylmay.jlox.assets.Expr.Ternary;
import com.dylmay.jlox.assets.Expr.Unary;
import com.dylmay.jlox.assets.Stmt;
import com.dylmay.jlox.assets.Stmt.Break;
import com.dylmay.jlox.assets.Stmt.Continue;
import com.dylmay.jlox.assets.Stmt.Expression;
import com.dylmay.jlox.assets.Stmt.If;
import com.dylmay.jlox.assets.Stmt.Return;
import com.dylmay.jlox.assets.Stmt.While;
import com.dylmay.jlox.assets.Token;
import com.dylmay.jlox.error.ErrorMessage;
import com.dylmay.jlox.error.LoxErrorHandler;
import com.dylmay.jlox.interpreter.Interpreter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
  private static final LoxErrorHandler ERR_HNDLR = LoxErrorHandler.getInstance(Resolver.class);

  private final Interpreter interpreter;
  private final Deque<Map<String, Boolean>> scopes;
  private FunctionType curFunction = FunctionType.NONE;

  private enum FunctionType {
    NONE,
    FUNCTION,
    WHILE,
  }

  public Resolver(Interpreter interpreter) {
    this.interpreter = interpreter;
    this.scopes = new ArrayDeque<>();
  }

  @Override
  public Void visitBlockStmt(Stmt.Block stmt) {
    beginScope();
    resolve(stmt.stmts);
    endScope();

    return null;
  }

  @Override
  public Void visitVarStmt(Stmt.Var stmt) {
    declare(stmt.name);
    if (stmt.initializer != null) {
      resolve(stmt.initializer);
    }
    define(stmt.name);

    return null;
  }

  @Override
  @SuppressWarnings("nullness")
  public Void visitVariableExpr(Expr.Variable expr) {
    if (!scopes.isEmpty() && scopes.peek().get(expr.name.lexeme()) == Boolean.FALSE) {
      ERR_HNDLR.report(
          new ErrorMessage().message("Can't read local variable in its own initializer."));
    }

    resolveLocal(expr, expr.name);

    return null;
  }

  @Override
  public Void visitAssignExpr(Expr.Assign expr) {
    resolve(expr.value);
    resolveLocal(expr, expr.name);

    return null;
  }

  @Override
  public Void visitExpressionStmt(Expression stmt) {
    resolve(stmt.expr);
    return null;
  }

  @Override
  public Void visitIfStmt(If stmt) {
    resolve(stmt.condition);
    resolve(stmt.thenBranch);
    if (stmt.elseBranch != null) {
      resolve(stmt.elseBranch);
    }
    return null;
  }

  @Override
  public Void visitReturnStmt(Return stmt) {
    if (this.curFunction == FunctionType.NONE) {
      ERR_HNDLR.report(new ErrorMessage().position(stmt.keyword.position()).message("Can't return from top-level code"));
    }

    if (stmt.value != null) {
      resolve(stmt.value);
    }
    return null;
  }

  @Override
  public Void visitWhileStmt(While stmt) {
    var parentFn = this.curFunction;
    this.curFunction = FunctionType.WHILE;

    resolve(stmt.condition);
    resolve(stmt.body);

    this.curFunction = parentFn;
    return null;
  }

  @Override
  public Void visitBreakStmt(Break stmt) {
    if (this.curFunction != FunctionType.WHILE) {
      ERR_HNDLR.report(
          new ErrorMessage()
              .position(stmt.keyword.position())
              .message("Break statements can only be present in loop statements"));
    }
    return null;
  }

  @Override
  public Void visitContinueStmt(Continue stmt) {
    if (this.curFunction != FunctionType.WHILE) {
      ERR_HNDLR.report(
          new ErrorMessage()
              .position(stmt.keyword.position())
              .message("Continue statements can only be present in loop statements"));
    }
    return null;
  }

  @Override
  public Void visitBinaryExpr(Binary expr) {
    resolve(expr.left);
    resolve(expr.right);
    return null;
  }

  @Override
  public Void visitTernaryExpr(Ternary expr) {
    resolve(expr.condition);
    resolve(expr.onTrue);
    resolve(expr.onFalse);
    return null;
  }

  @Override
  public Void visitCallExpr(Call expr) {
    resolve(expr.callee);

    for (var arg : expr.args) {
      resolve(arg);
    }

    return null;
  }

  @Override
  public Void visitGroupingExpr(Grouping expr) {
    resolve(expr.expression);
    return null;
  }

  @Override
  public Void visitLiteralExpr(Literal expr) {
    return null;
  }

  @Override
  public Void visitUnaryExpr(Unary expr) {
    resolve(expr.right);
    return null;
  }

  @Override
  public Void visitLogicalExpr(Logical expr) {
    resolve(expr.left);
    resolve(expr.right);
    return null;
  }

  @Override
  public Void visitFnExpr(Fn expr) {
    resolveFunction(expr, FunctionType.FUNCTION);

    return null;
  }

  public void resolve(List<Stmt> statements) {
    statements.forEach(this::resolve);
  }

  private void resolve(Stmt stmt) {
    stmt.accept(this);
  }

  private void resolve(Expr expr) {
    expr.accept(this);
  }

  private void resolveLocal(Expr expr, Token name) {
    var scopeIter = scopes.iterator();

    int depth = 0;
    while (scopeIter.hasNext()) {
      var scope = scopeIter.next();

      if (scope.containsKey(name.lexeme())) {
        interpreter.resolve(expr, depth);
        return;
      }

      depth++;
    }
  }

  private void resolveFunction(Expr.Fn func, FunctionType type) {
    var parentFunction = this.curFunction;
    this.curFunction = type;

    beginScope();
    for (var parm : func.parms) {
      declare(parm);
      define(parm);
    }
    resolve(func.body);
    endScope();

    this.curFunction = parentFunction;
  }

  private void beginScope() {
    this.scopes.push(new HashMap<>());
  }

  private void endScope() {
    this.scopes.pop();
  }

  @SuppressWarnings("nullness")
  private void declare(Token name) {
    if (scopes.isEmpty()) return;

    var scope = scopes.peek();
    if (scope.containsKey(name.lexeme())) {
      ERR_HNDLR.report(new ErrorMessage().position(name.position()).where(name.lexeme()).message("Already a variable with this name in this scope"));
    }

    scopes.peek().put(name.lexeme(), false);
  }

  @SuppressWarnings("nullness")
  private void define(Token name) {
    if (scopes.isEmpty()) return;

    scopes.peek().put(name.lexeme(), true);
  }
}
