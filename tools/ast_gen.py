#!/usr/bin/python
from pathlib import Path
from textwrap import dedent, indent
from functools import reduce
from collections import namedtuple

Parameter = namedtuple(
    "Parameter", ["type", "name", "nullable"], defaults=["", "", False]
)
Token = namedtuple("Token", ["name", "params"])


def has_nullable(token):
    return any(map(lambda param: param.nullable, token.params))


def create_class(token: Parameter, access_level: str, class_name: str) -> str:
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
                lambda vara: f"{access_level} final {'@Nullable ' if vara.nullable else ''}{vara.type} {vara.name};",
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
        {access_level} static class {token.name} extends {class_name} {{
        %s

          {access_level} {token.name}({create_params()}) {{
        %s
          }}

          @Override
          {access_level} <R> {'@Nullable ' if has_nullable(token) else ''}R accept(Visitor<R> visitor) {{
            return visitor.visit{token.name}{class_name}(this);
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


def create_interface(token, class_name) -> str:
    return (
        "@Nullable\n" if has_nullable(token) else ""
    ) + f"R visit{token.name}{class_name}({token.name} {class_name.lower()});"


def gen_ast(
    class_name: str,
    tokens: list[Token],
    imports: list[str],
    out_path: str,
    package_name: str,
    interface_name="Visitor",
    access_level="public",
):
    # base class information
    interfaces = "\n\n".join(map(lambda clz: create_interface(clz, class_name), tokens))
    import_str = "\n".join(map(lambda imp: f"import {imp};", imports))
    impl = "\n\n".join(
        map(lambda clz: create_class(clz, access_level, class_name), tokens)
    )

    with open(
        Path(out_path) / (class_name + ".java"), "w", encoding="utf-8"
    ) as ast_file:
        ast_file.write(
            dedent(
                f"""\
            package {package_name};
            %s
            {access_level} abstract class {class_name} {{
              {access_level} interface {interface_name}<R> {{
            %s
              }}

              {access_level} abstract <R> R accept(Visitor<R> visitor);

            %s
            }}
        """
            )
            % (
                ("\n" + import_str + "\n") if len(imports) > 0 else "",
                indent(interfaces, "    "),
                indent(impl, "  "),
            )
        )
