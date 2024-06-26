#!/usr/bin/python
from ast import Param
from ast_gen import Token, Parameter, gen_ast


def gen_expr():
    class_name = "Expr"
    out_path = "./src/main/java/com/dylmay/jlox/assets"
    package_name = out_path.split("java/")[1].replace("/", ".")

    import_list = ["javax.annotation.Nullable", "java.util.List"]

    tokens = [
        Token(
            "Binary",
            [
                Parameter("Expr", "left"),
                Parameter("Token", "operator"),
                Parameter("Expr", "right"),
            ],
        ),
        Token(
            "Ternary",
            [
                Parameter("Expr", "condition"),
                Parameter("Expr", "onTrue"),
                Parameter("Expr", "onFalse"),
            ],
        ),
        Token(
            "Call",
            [
                Parameter("Expr", "callee"),
                Parameter("Token", "paren"),
                Parameter("List<Expr>", "args"),
            ],
        ),
        Token("Grouping", [Parameter("Expr", "expression")]),
        Token(
            "Literal",
            [Parameter("Object", "value", True), Parameter("Position", "pos")],
        ),
        Token("Unary", [Parameter("Token", "operator"), Parameter("Expr", "right")]),
        Token("This", [Parameter("Token", "keyword")]),
        Token("Variable", [Parameter("Token", "name")]),
        Token("Assign", [Parameter("Token", "name"), Parameter("Expr", "value")]),
        Token(
            "Logical",
            [
                Parameter("Expr", "left"),
                Parameter("Token", "operator"),
                Parameter("Expr", "right"),
            ],
        ),
        Token(
            "Fn",
            [
                Parameter("Position", "pos"),
                Parameter("List<Token>", "parms"),
                Parameter("List<Stmt>", "body"),
            ],
        ),
        Token("Get", [Parameter("Expr", "object"), Parameter("Token", "name")]),
        Token(
            "Set",
            [
                Parameter("Expr", "object"),
                Parameter("Token", "name"),
                Parameter("Expr", "value"),
            ],
        ),
    ]

    gen_ast(class_name, tokens, import_list, out_path, package_name)


if __name__ == "__main__":
    gen_expr()
