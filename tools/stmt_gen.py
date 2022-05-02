from ast_gen import Token, Parameter, gen_ast

if __name__ == "__main__":
    class_name = "Stmt"
    out_path = "./src/main/java/com/dylmay/jlox/assets"
    package_name = out_path.split("java/")[1].replace("/", ".")

    import_list = ["javax.annotation.Nullable"]

    tokens = [
        Token("Expression", [Parameter("Expr", "expression")]),
        Token("Print", [Parameter("Expr", "expression")]),
    ]

    gen_ast(class_name, tokens, import_list, out_path, package_name)
