from ast_gen import Token, Parameter, gen_ast

if __name__ == "__main__":
    class_name = "Expr"
    out_path = "./src/main/java/com/dylmay/jlox/assets"
    package_name = out_path.split("java/")[1].replace("/", ".")

    import_list = ["javax.annotation.Nullable"]

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
        Token("Grouping", [Parameter("Expr", "expression")]),
        Token(
            "Literal",
            [Parameter("Object", "value", True), Parameter("Position", "pos")],
        ),
        Token("Unary", [Parameter("Token", "operator"), Parameter("Expr", "right")]),
    ]

    gen_ast(class_name, tokens, import_list, out_path, package_name)