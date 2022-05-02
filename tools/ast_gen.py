#!/usr/bin/python
from pathlib import Path
from textwrap import dedent, indent
from functools import reduce
from collections import namedtuple

Parameter = namedtuple("Parameter", "type name nullable", defaults=["", "", False])
Token = namedtuple("Token", "name params")

OUT_PATH = "./src/main/java/com/dylmay/jlox/assets"
BASE_NAME = "Expr.java"
INTERFACE_NAME = "Visitor"
CLASS_NAME = BASE_NAME.split(".")[0]
PACKAGE_NAME = OUT_PATH.split("java/")[1].replace("/", ".")
ACCESS_LEVEL = "public"
IMPORT_LIST = ["javax.annotation.Nullable"]

TOKENS = [
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
    Token("Literal", [
        Parameter("Object", "value", True),
        Parameter("Position", "pos")
        ]),
    Token("Unary", [Parameter("Token", "operator"), Parameter("Expr", "right")]),
]

def has_nullable(token):
    return any(map(lambda param: param.nullable, token.params))



def create_class(token: Parameter) -> str:
    def create_params():
        return ", ".join(
            map(
                lambda params: f"{'@Nullable ' if params.nullable else ''}{params.type} {params.name}",
                token.params,
            )
        )

    def create_assignments():
        return "\n".join(
            map(lambda assign: f"this.{assign.name} = {assign.name};", token.params)
        )

    def create_variables():
        return "\n".join(
            map(
                lambda vara: f"{ACCESS_LEVEL} final {'@Nullable ' if vara.nullable else ''}{vara.type} {vara.name};",
                token.params,
            )
        )

    def create_comparison():
        return (
            "return "
            + "\n   && ".join(
                map(
                    lambda assign: f"this.{assign.name} != null && this.{assign.name}.equals(i.{assign.name})",
                    token.params,
                )
            )
            + ";"
        )

    def create_hashes():
        return "\n".join(
            map(
                lambda assign: f"result = prime * result + (({assign.name} == null) ? 0 : {assign.name}.hashCode());",
                token.params,
            )
        )

    return (
        dedent(
            f"""\
        {ACCESS_LEVEL} static class {token.name} extends {CLASS_NAME} {{
        %s

          {ACCESS_LEVEL} {token.name}({create_params()}) {{
        %s
          }}

          @Override
          {ACCESS_LEVEL} <R> {'@Nullable ' if has_nullable(token) else ''}R accept(Visitor<R> visitor) {{
            return visitor.visit{token.name}Expr(this);
          }}

          @Override
          public boolean equals(@Nullable Object obj) {{
            if (this == obj) return true;

            if (obj instanceof {token.name} i) {{
        %s
            }}

            return false;
          }}

          @Override
          public int hashCode() {{
            final int prime = 31;
            int result = 1;

        %s

            return result;
          }}
        }}"""
        )
        % (
            indent(create_variables(), "  "),
            indent(create_assignments(), "    "),
            indent(create_comparison(), "      "),
            indent(create_hashes(), "    "),
        )
    )


def create_interface(token) -> str:
    return ('@Nullable\n' if has_nullable(token) else '') +  f"R visit{token.name}Expr({token.name} expr);"


def gen_ast():
    # base class information
    interfaces = "\n\n".join(map(lambda clz: create_interface(clz), TOKENS))
    imports = "\n".join(map(lambda imp: f"import {imp};", IMPORT_LIST))
    impl = "\n\n".join(map(lambda clz: create_class(clz), TOKENS))

    with open(Path(OUT_PATH) / BASE_NAME, "w", encoding="utf-8") as ast_file:
        ast_file.write(
            dedent(
                f"""\
            package {PACKAGE_NAME};
            %s
            {ACCESS_LEVEL} abstract class {CLASS_NAME} {{
              {ACCESS_LEVEL} interface {INTERFACE_NAME}<R> {{
            %s
              }}

              {ACCESS_LEVEL} abstract <R> R accept(Visitor<R> visitor);

            %s
            }}
        """
            )
            % (
                ("\n" + imports + "\n") if len(IMPORT_LIST) > 0 else "",
                indent(interfaces, "    "),
                indent(impl, "  "),
            )
        )


if __name__ == "__main__":
    gen_ast()
