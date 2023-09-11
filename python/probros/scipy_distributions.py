# This is an auto-generated file. Do not modify.

import numpy as np
import scipy.stats as stats

class Distribution:
    def sample(self, size=None):
        raise NotImplementedError
    
    def _logprob(self, value):
        raise NotImplementedError

    def logprob(self, value):
        if isinstance(value, list):
            return sum(self._logprob(v) for v in value)
        elif isinstance(value, np.ndarray):
            return self._logprob(value).sum()
        else:
            return self._logprob(value)
        
class IID(Distribution):
    def __init__(self, base: Distribution, n: int) -> None:
        self.base = base
        self.n = n

    def sample(self, size=None):
        if size is not None:
            if isinstance(size, int):
                return self.base.sample(size=(self.n,size))
            else:
                assert isinstance(size, tuple)
                return self.base.sample(size=(self.n,) + size)
        else:
            return self.base.sample(size=self.n)
    
    def logprob(self, value) -> float:
        if isinstance(value, np.ndarray):
            return self.base.logprob(value).sum()
        else:
            assert isinstance(value, list) and len(value) == self.n
            return sum(self.base.logprob(value[i]) for i in range(self.n))
    
    def __repr__(self) -> str:
        return f"IID({self.base}, {self.n})"
    
class Broadcasted(Distribution):
    def __init__(self, base: Distribution) -> None:
        self.base = base

    def sample(self, size=None):
        return self.base.sample(size=size)
    
    def _logprob(self, value):
        return self.base._logprob(value)
    
    def __repr__(self) -> str:
        return f"Broadcasted({self.base})"

class Dirac(Distribution):
    def __init__(self, value):
        self.value = value

    def sample(self, size=None):
        if size is None:
            return self.value
        return np.full(size, self.value)

    def _logprob(self, value):
        if isinstance(value, np.ndarray):
            lp = np.zeros(value.shape)
            lp[value != self.value] = -np.inf
            return lp

        if value == self.value:
            return 0.
        else:
            return -np.inf
        
    def __repr__(self):
        return f"Dirac(value={self.value})"
        
class Beta(Distribution):
    def __init__(self, a, b):
        self.a = a
        self.b = b

    def sample(self, size=None):
        return stats.beta.rvs(a=self.a, b=self.b, size=size)

    def _logprob(self, value):
        return stats.beta.logpdf(value, a=self.a, b=self.b)

    def __repr__(self):
        return "Beta(" + f"a={self.a}, b={self.b}" + ")"

class Cauchy(Distribution):
    def __init__(self, loc, scale):
        self.loc = loc
        self.scale = scale

    def sample(self, size=None):
        return stats.cauchy.rvs(loc=self.loc, scale=self.scale, size=size)

    def _logprob(self, value):
        return stats.cauchy.logpdf(value, loc=self.loc, scale=self.scale)

    def __repr__(self):
        return "Cauchy(" + f"loc={self.loc}, scale={self.scale}" + ")"

class Exponential(Distribution):
    def __init__(self, rate):
        self.scale = 1 / rate

    def sample(self, size=None):
        return stats.expon.rvs(scale=self.scale, size=size)

    def _logprob(self, value):
        return stats.expon.logpdf(value, scale=self.scale)

    def __repr__(self):
        return "Exponential(" + f"scale={self.scale}" + ")"

class Gamma(Distribution):
    def __init__(self, alpha, beta):
        self.a = alpha
        self.scale = 1 / beta

    def sample(self, size=None):
        return stats.gamma.rvs(a=self.a, scale=self.scale, size=size)

    def _logprob(self, value):
        return stats.gamma.logpdf(value, a=self.a, scale=self.scale)

    def __repr__(self):
        return "Gamma(" + f"a={self.a}, scale={self.scale}" + ")"

class HalfCauchy(Distribution):
    def __init__(self, loc, scale):
        self.loc = loc
        self.scale = scale

    def sample(self, size=None):
        return stats.halfcauchy.rvs(loc=self.loc, scale=self.scale, size=size)

    def _logprob(self, value):
        return stats.halfcauchy.logpdf(value, loc=self.loc, scale=self.scale)

    def __repr__(self):
        return "HalfCauchy(" + f"loc={self.loc}, scale={self.scale}" + ")"

class HalfNormal(Distribution):
    def __init__(self, loc, scale):
        self.loc = loc
        self.scale = scale

    def sample(self, size=None):
        return stats.halfnorm.rvs(loc=self.loc, scale=self.scale, size=size)

    def _logprob(self, value):
        return stats.halfnorm.logpdf(value, loc=self.loc, scale=self.scale)

    def __repr__(self):
        return "HalfNormal(" + f"loc={self.loc}, scale={self.scale}" + ")"

class InverseGamma(Distribution):
    def __init__(self, alpha, beta):
        self.a = alpha
        self.scale = beta

    def sample(self, size=None):
        return stats.invgamma.rvs(a=self.a, scale=self.scale, size=size)

    def _logprob(self, value):
        return stats.invgamma.logpdf(value, a=self.a, scale=self.scale)

    def __repr__(self):
        return "InverseGamma(" + f"a={self.a}, scale={self.scale}" + ")"

class Normal(Distribution):
    def __init__(self, loc, scale):
        self.loc = loc
        self.scale = scale

    def sample(self, size=None):
        return stats.norm.rvs(loc=self.loc, scale=self.scale, size=size)

    def _logprob(self, value):
        return stats.norm.logpdf(value, loc=self.loc, scale=self.scale)

    def __repr__(self):
        return "Normal(" + f"loc={self.loc}, scale={self.scale}" + ")"

class StudentT(Distribution):
    def __init__(self, df):
        self.df = df

    def sample(self, size=None):
        return stats.t.rvs(df=self.df, size=size)

    def _logprob(self, value):
        return stats.t.logpdf(value, df=self.df)

    def __repr__(self):
        return "StudentT(" + f"df={self.df}" + ")"

class Uniform(Distribution):
    def __init__(self, low, high):
        self.loc = low
        self.scale = high - low

    def sample(self, size=None):
        return stats.uniform.rvs(loc=self.loc, scale=self.scale, size=size)

    def _logprob(self, value):
        return stats.uniform.logpdf(value, loc=self.loc, scale=self.scale)

    def __repr__(self):
        return "Uniform(" + f"loc={self.loc}, scale={self.scale}" + ")"

class Bernoulli(Distribution):
    def __init__(self, p):
        self.p = p

    def sample(self, size=None):
        return stats.bernoulli.rvs(p=self.p, size=size)

    def _logprob(self, value):
        return stats.bernoulli.logpmf(value, p=self.p)

    def __repr__(self):
        return "Bernoulli(" + f"p={self.p}" + ")"

class Binomial(Distribution):
    def __init__(self, n, p):
        self.n = n
        self.p = p

    def sample(self, size=None):
        return stats.binom.rvs(n=self.n, p=self.p, size=size)

    def _logprob(self, value):
        return stats.binom.logpmf(value, n=self.n, p=self.p)

    def __repr__(self):
        return "Binomial(" + f"n={self.n}, p={self.p}" + ")"

class DiscreteUniform(Distribution):
    def __init__(self, low, high):
        self.low = low
        self.high = high + 1

    def sample(self, size=None):
        return stats.randint.rvs(low=self.low, high=self.high, size=size)

    def _logprob(self, value):
        return stats.randint.logpmf(value, low=self.low, high=self.high)

    def __repr__(self):
        return "DiscreteUniform(" + f"low={self.low}, high={self.high}" + ")"

class Geometric(Distribution):
    def __init__(self, p):
        self.p = p

    def sample(self, size=None):
        return stats.geom.rvs(p=self.p, size=size)

    def _logprob(self, value):
        return stats.geom.logpmf(value, p=self.p)

    def __repr__(self):
        return "Geometric(" + f"p={self.p}" + ")"

class HyperGeometric(Distribution):
    def __init__(self, M, n, N):
        self.M = M
        self.n = n
        self.N = N

    def sample(self, size=None):
        return stats.hypergeom.rvs(M=self.M, n=self.n, N=self.N, size=size)

    def _logprob(self, value):
        return stats.hypergeom.logpmf(value, M=self.M, n=self.n, N=self.N)

    def __repr__(self):
        return "HyperGeometric(" + f"M={self.M}, n={self.n}, N={self.N}" + ")"

class Poisson(Distribution):
    def __init__(self, rate):
        self.mu = rate

    def sample(self, size=None):
        return stats.poisson.rvs(mu=self.mu, size=size)

    def _logprob(self, value):
        return stats.poisson.logpmf(value, mu=self.mu)

    def __repr__(self):
        return "Poisson(" + f"mu={self.mu}" + ")"

class Dirichlet(Distribution):
    def __init__(self, alpha):
        self.alpha = alpha

    def sample(self, size=None):
        return stats.dirichlet.rvs(alpha=self.alpha, size=size)

    def _logprob(self, value):
        return stats.dirichlet.logpdf(value, alpha=self.alpha)

    def __repr__(self):
        return "Dirichlet(" + f"alpha={self.alpha}" + ")"

class MultivariateNormal(Distribution):
    def __init__(self, mean, cov):
        self.mean = mean
        self.cov = cov

    def sample(self, size=None):
        return stats.multivariate_normal.rvs(mean=self.mean, cov=self.cov, size=size)

    def _logprob(self, value):
        return stats.multivariate_normal.logpdf(value, mean=self.mean, cov=self.cov)

    def __repr__(self):
        return "MultivariateNormal(" + f"mean={self.mean}, cov={self.cov}" + ")"
