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
import com.dylmay.jlox.assets.Stmt.Class;
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
  private final Deque<Map<String, VariableDefine>> scopes;
  private FunctionType curFunction = FunctionType.NONE;

  private enum FunctionType {
    NONE,
    FUNCTION,
    WHILE,
    METHOD,
  }

  public Resolver(Interpreter interpreter) {
    this.interpreter = interpreter;
    this.scopes = new ArrayDeque<>();
    this.scopes.push(new HashMap<>());
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
    declare(stmt.name, stmt.mutable);
    if (stmt.initializer != null) {
      resolve(stmt.initializer);
    }
    define(stmt.name);

    return null;
  }

  @Override
  @SuppressWarnings("nullness")
  public Void visitVariableExpr(Expr.Variable expr) {
    if (!scopes.isEmpty()
        && scopes.peek().get(expr.name.lexeme()) != null
        && !scopes.peek().get(expr.name.lexeme()).isDefined) {
      ERR_HNDLR.report(
          new ErrorMessage().message("Can't read local variable in its own initializer."));
    }

    resolveLocal(expr, expr.name);

    return null;
  }

  @Override
  @SuppressWarnings("nullness")
  public Void visitAssignExpr(Expr.Assign expr) {
    resolve(expr.value);
    resolveLocal(expr, expr.name);

    if (this.scopes.peek().containsKey(expr.name.lexeme())
        && !this.scopes.peek().get(expr.name.lexeme()).isMutable) {
      ERR_HNDLR.report(
          new ErrorMessage()
              .where(expr.name.lexeme())
              .position(expr.name.position())
              .message("mutable variables must be declared with 'let mut'."));
    }

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
      ERR_HNDLR.report(
          new ErrorMessage()
              .position(stmt.keyword.position())
              .message("Can't return from top-level code"));
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
      declare(parm, false);
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
  private void declare(Token name, boolean isMutable) {
    if (scopes.isEmpty()) return;

    var scope = scopes.peek();
    if (scope.containsKey(name.lexeme())) {
      ERR_HNDLR.report(
          new ErrorMessage()
              .position(name.position())
              .where(name.lexeme())
              .message("Already a variable with this name in this scope"));
    }

    scopes.peek().put(name.lexeme(), new VariableDefine(false, isMutable));
  }

  @SuppressWarnings("nullness")
  private void define(Token name) {
    if (scopes.isEmpty()) return;

    if (scopes.peek().containsKey(name.lexeme())) {
      scopes.peek().get(name.lexeme()).isDefined = true;
    } else {
      scopes.peek().put(name.lexeme(), new VariableDefine(true, false));
    }
  }

  @Override
  public Void visitClassStmt(Class stmt) {
    declare(stmt.name, false);

    for (var decl : stmt.decls) {
      if (decl.initializer instanceof Expr.Fn method) {
        resolveFunction(method, FunctionType.METHOD);
      } else {
        ERR_HNDLR.report(
            new ErrorMessage()
                .position(decl.name.position())
                .where(decl.name.lexeme())
                .message("Unknown class declaration type"));
      }
    }

    define(stmt.name);
    return null;
  }

  @Override
  public Void visitGetExpr(Expr.Get expr) {
    resolve(expr.object);
    return null;
  }

  @Override
  public Void visitSetExpr(Expr.Set expr) {
    resolve(expr.value);
    resolve(expr.object);
    return null;
  }

  private static class VariableDefine {
    boolean isDefined;
    boolean isMutable;

    public VariableDefine(boolean isDefined, boolean isMutable) {
      this.isDefined = isDefined;
      this.isMutable = isMutable;
    }
  }
}
