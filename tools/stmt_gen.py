#!/usr/bin/python
from ast_gen import Token, Parameter, gen_ast


def gen_stmt():
    class_name = "Stmt"
    out_path = "./src/main/java/com/dylmay/jlox/assets"
    package_name = out_path.split("java/")[1].replace("/", ".")

    import_list = ["javax.annotation.Nullable", "java.util.List"]

    tokens = [
        Token("Expression", [Parameter("Expr", "expr")]),
        Token(
            "Var",
            [
                Parameter("Token", "name"),
                Parameter("Expr", "initializer", True),
                Parameter("boolean", "mutable"),
                Parameter("boolean", "isStatic"),
            ],
        ),
        Token("Block", [Parameter("List<Stmt>", "stmts")]),
        Token(
            "If",
            [
                Parameter("Expr", "condition"),
                Parameter("Stmt", "thenBranch"),
                Parameter("Stmt", "elseBranch", True),
            ],
        ),
        Token(
            "Return", [Parameter("Token", "keyword"), Parameter("Expr", "value", True)]
        ),
        Token("While", [Parameter("Expr", "condition"), Parameter("Stmt", "body")]),
        Token("Break", [Parameter("Token", "keyword")]),
        Token("Continue", [Parameter("Token", "keyword")]),
        Token(
            "Class",
            [
                Parameter("Token", "name"),
                Parameter("List<Stmt.Var>", "decls"),
                Parameter("Expr.Variable", "superclass", True),
            ],
        ),
    ]

    gen_ast(class_name, tokens, import_list, out_path, package_name)


if __name__ == "__main__":
    gen_stmt()
