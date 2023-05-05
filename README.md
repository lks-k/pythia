# PROBabilistic pROgramS (PROBROS)

# The Benchmark

The goal is that each program in the benchmark can be directly
executed in Python (while also being in a sufficiently small subset
that we can see it as a pGCL program or a Bayesian hierarchical
model).

# Parsing

- Imports are ignored, so that the program can be used to run in
  Python directly (but also the same program can be analyzed, using
  an abstract semantics for libraries).

- initialization of np.random.seed is presently ignored by the parser

# Scala API

The easiest way to access the benchmark examples in Scala is to
install the project's API library. It already contains all the files,
and the code to parse them.

Install the jar file in your project. The jar file contains code for
abstract syntax, parsing, and unparsing the benchmark files, as well
as all the benchmark files themselves, so you should not need anything
more to try the benchmarks.

Accessing `probros.benchmark.pgcl.all` gives you a value of type
`Map[String, Stm]` mapping benchmark names (without extension) to PGCL
abstract syntax trees.  If you do not need the map, but just a
sequence of syntax trees use `probros.benchmark.pgcl.all.values` (all
maps listed below can be converted to value lists this way).

For convenience we also expose the following
values to access collections for all benchmarks:

```
probros.benchmark.name.all: Map[String, String]
  - maps names of benchmarks to themselves

probros.benchmark.name.text: Map[String, String]
  - maps names of benchmarks to their program code as String

probros.benchmark.name.python: Map[String, Seq[pythonparse.Ast.stmt]]
  - maps names of benchmarks to their syntax trees in Python (using
    pythonparse.Ast.stmt)

probros.benchmark.name.pgcl: Map[String, Stm]
  - maps names of benchmarks to their syntax trees in PGCL

probros.benchmark.name.unparsed: Map[String, String]
  - maps names of benchmarks to their program text after unparsing
    from PGCL to Python concrete syntax
```

If you need to access individual benchmarks we offer several ways.
Let's assume you want to access the benchmark named `flip`. Then:

```
probros.benchmark.flip: Benchmark
```
gives the object representing the benchmark. It exposes properties
`name`, `text`, `python`, `pgcl`, and `unparsed`. For example:
`probros.benchmark.flip.text` gives a string representing the program
text, and `probros.benchmark.flip.pgcl` gives its PGCL syntax tree.

Finally, you can also select "flip" from the collections. For instance
to access its PGCL all the following are equivalent:

```scala
probros.benchmark.pgcl.all("flip")
probros.benchmark.pgcl("flip")
probros.benchmark.pgcl.flip
```

An example of a scala-cli file configured to use this data (the jar
file produce with `sbt package` is assumed to be dropped into the same
directory as the example file):
```scala
//> using jar "probros_3-0.1.0.jar"
//> using dep "com.lihaoyi::pythonparse:3.0.1"

@main def main(): Unit =
  print(probros.benchmark.pgcl.flip.toString)
```
