examples in HVL

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

# TODO

- make AST
- make parser to pgcl
- pgcl 2 python would also be a useful tool
- make expectations and distributions formal
