import ast
import logging

# TODO: extract this dynamically from `probros` to future-proof for changes?
_DECORATOR_NAME = "probabilistic_program"


class Linter(ast.NodeVisitor):
    """A linter to validate any probabilistic programs found within the code.

    This linter focuses on programs implementing probabilistic programs
    according to the specifications in `docs.ipynb`. Therefore, only functions
    annotated as such are checked, any other part of the code is ignored.

    Attributes:
        errors (list[str]): A list to store found errors
    """

    def __init__(self) -> None:
        """Initialize the linter."""
        self.errors: list[str] = []
        logging.debug(f"Initialized linter with {self.errors=}.")

    def visit_FunctionDef(self, node: ast.FunctionDef) -> None:
        """Hand off analyzing probabilistic programs, ignore anything else.

        Note that this only checks for the string of the decorator to match
        `_DECORATOR_NAME` currently, no actual testing is done to ensure the
        origin of the decorator. This may lead to incorrect identification of
        functions in case other decorators share that name.

        Args:
            node: The node to be analyzed.
        """

        if any(
            isinstance(decorator, ast.Attribute)
            and decorator.attr == _DECORATOR_NAME
            or isinstance(decorator, ast.Name)
            and decorator.id == _DECORATOR_NAME
            for decorator in node.decorator_list
        ):
            logging.debug(
                "Found probabilistic program, calling specialized linter…"
            )
            pplinter = PPLinter()
            pplinter.visit(node)
            self.errors += pplinter.errors


class PPLinter(ast.NodeVisitor):
    """A linter to validate individual probabilistic programs.

    This linter is geared to analyze individual probabilistic programs
    according to the definition in `docs.ipynb`. Thus, this linter is not
    designed to be used as a standalone linter.

    Attributes:
        errors (list[str]): A list to store found errors
    """

    def __init__(self) -> None:
        """Initialize the linter."""
        self.errors: list[str] = []
        logging.debug(
            f"Initialized probabilistic program linter with {self.errors=}."
        )


def lint_code(code: str) -> list[str] | None:
    """Lint the provided python code.

    Args:
        code: The Python code to be linted.

    Returns:
        The errors found by the linter as strings or `None` in case of errors.
        All errors identified by the linter and any runtime errors are logged.
    """

    escaped_snippet = code[:25].encode("unicode_escape").decode()
    logging.debug("Running linter on given code " + f"'{escaped_snippet}…'.")
    linter = Linter()
    try:
        tree = ast.parse(code)
    except ValueError:
        logging.warn("Received invalid data.")
        return None

    linter.visit(tree)
    errors = linter.errors
    logging.info(f"Linter ran successfully, found {len(errors)} errors.")

    return errors


def lint_file(filepath: str) -> list[str] | None:
    """Lint the provided file.

    This currently reads the whole file into memory and parses it afterwards.
    Therefore, this may cause problems with very large files.

    Args:
        code: The path to the file containing the Python code to be linted.

    Returns:
        The errors found by the linter as strings or `None` in case of errors.
        All errors identified by the linter and any runtime errors are logged.
    """

    logging.debug(f"Reading file '{filepath}'.")
    try:
        with open(filepath, "r") as file:
            code = file.read()
    except IOError as error:
        logging.fatal(f"Failed to open file '{filepath}': {error}")
        return None

    return lint_code(code)


def main() -> None:
    """Parse CLI arguments and execute the linter.

    This uses `argparse` to decypher any arguments. Valid arguments are:
    - `-v` / `--verbose` to print debugging messages, and
    - either a filepath as a positional argument, or
    - `-c` / `--code` with the code to analyze.
      The filepath and `-c`/`--code` are mutually exclusive but one is
      required.
    """

    import argparse
    import sys

    parser = argparse.ArgumentParser()
    parser.add_argument(
        "-v",
        "--verbose",
        action="store_true",
        help="Print verbose messages",
    )
    code_origin = parser.add_mutually_exclusive_group(required=True)
    code_origin.add_argument(
        "filepath", help="File to run the linter on", type=str, nargs="?"
    )
    code_origin.add_argument("-c", "--code", help="The code to lint", type=str)
    args = parser.parse_args()

    if args.verbose:
        # Use two different handlers to print the standard / debugging
        # information. This also allows redirecting the output if required.
        # Preprend debugging messages with `* ` to differentiate them from
        # normal outputs.

        standard = logging.StreamHandler(sys.stdout)
        standard.addFilter(lambda record: record.levelno != logging.DEBUG)

        verbose = logging.StreamHandler(sys.stdout)
        verbose.addFilter(lambda record: record.levelno == logging.DEBUG)
        verbose.setFormatter(logging.Formatter("* %(message)s"))

        logging.basicConfig(
            format="%(message)s",
            level=logging.DEBUG,
            handlers=(standard, verbose),
        )
    else:
        logging.basicConfig(format="%(message)s", level=logging.INFO)

    if args.filepath and not args.code:
        lint_file(args.filepath)
    elif args.code and not args.filepath:
        lint_code(args.code)
    else:
        raise RuntimeError("Reached unreachable code.")


if __name__ == "__main__":
    main()
