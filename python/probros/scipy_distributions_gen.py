
distributions = [
    # continuous
    [
        "Beta",
        "stats.beta",
        ["a", "b"],
        {"a": "a", "b": "b"},
        "continuous"
    ],
    [
        "Cauchy",
        "stats.cauchy",
        ["loc", "scale"],
        {"loc": "loc", "scale": "scale"},
        "continuous"
    ],
    [
        "Exponential",
        "stats.expon",
        ["rate"],
        {"scale": "1 / rate"},
        "continuous"
    ],
    [
        "Gamma",
        "stats.gamma",
        ["alpha", "beta"],
        {"a": "alpha", "scale": "1 / beta"},
        "continuous"
    ],
    [
        "HalfCauchy",
        "stats.halfcauchy",
        ["loc", "scale"],
        {"loc": "loc", "scale": "scale"},
        "continuous"
    ],
    [
        "HalfNormal",
        "stats.halfnorm",
        ["loc", "scale"],
        {"loc": "loc", "scale": "scale"},
        "continuous"
    ],
    [
        "InverseGamma",
        "stats.invgamma",
        ["alpha", "beta"],
        {"a": "alpha", "scale": "beta"},
        "continuous"
    ],
    [
        "Normal",
        "stats.norm",
        ["loc", "scale"],
        {"loc": "loc", "scale": "scale"},
        "continuous"
    ],
    [
        "StudentT",
        "stats.t",
        ["df"],
        {"df": "df"},
        "continuous"
    ],
    [
        "Uniform",
        "stats.uniform",
        ["low", "high"],
        {"loc": "low", "scale": "high - low"},
        "continuous"
    ],
    # discrete
    [
        "Bernoulli",
        "stats.bernoulli",
        ["p"],
        {"p": "p"},
        "discrete"
    ],
    [
        "Binomial",
        "stats.binom",
        ["n", "p"],
        {"n": "n", "p": "p"},
        "discrete"
    ],
    [
        "DiscreteUniform",
        "stats.randint",
        ["low", "high"],
        {"low": "low", "high": "high + 1"},
        "discrete"
    ],
    [
        "Geometric",
        "stats.geom",
        ["p"],
        {"p": "p"},
        "discrete"
    ],
    [
        "HyperGeometric",
        "stats.hypergeom",
        ["M", "n", "N"],
        {"M": "M", "n": "n", "N": "N"},
        "discrete"
    ],
    [
        "Poisson",
        "stats.poisson",
        ["rate"],
        {"mu": "rate"},
        "discrete"
    ],
    # multivariate
    [
        "Dirichlet",
        "stats.dirichlet",
        ["alpha"],
        {"alpha": "alpha"},
        "continuous"
    ],
    [
        "MultivariateNormal",
        "stats.multivariate_normal",
        ["mean", "cov"],
        {"mean": "mean", "cov": "cov"},
        "continuous"
    ]
]

def generate(name, scipy_stats_class, params, internal_param_map, t):
    tab = " "*4
    s = f"class {name}(Distribution):\n"
    init_params = ", ".join(params)
    s += f"{tab}def __init__(self, {init_params}):\n"
    for p, expr in internal_param_map.items():
        s += f"{tab}{tab}self.{p} = {expr}\n"

    internal_params = ", ".join(k + "=self." + k for k,_ in internal_param_map.items())
    s += "\n"
    s += f"{tab}def sample(self, size=None):\n"
    s += f"{tab}{tab}return {scipy_stats_class}.rvs({internal_params}, size=size)\n"

    lp = "logpmf" if t == "discrete" else "logpdf" 

    s += "\n"
    s += f"{tab}def _logprob(self, value):\n"
    s += f"{tab}{tab}return {scipy_stats_class}.{lp}(value, {internal_params})\n"

    s += "\n"
    s += f"{tab}def __repr__(self):\n"
    fstr = "f\"" + ", ".join(k + "={self." + k + "}" for k,_ in internal_param_map.items()) + "\""
    s += f"{tab}{tab}return \"{name}(\" + {fstr} + \")\"\n"

    return s

if __name__ == "__main__":
    with open("scipy_distributions_base.py", "r") as f:
        base = f.read()

    for distribution in distributions:
        print("-", distribution[0])

    with open("scipy_distributions.py", "w") as f:
        f.write("# This is an auto-generated file. Do not modify.\n\n")
        f.write(base)

        for distribution in distributions:
            f.write("\n")
            f.write(generate(*distribution))