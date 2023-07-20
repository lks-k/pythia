import random
import math

# to be able to run models we have to be able to draw a random value for a given distribution,
# and compute the (log-) probability of this value.
class Distribution:
    def sample(self):
        raise NotImplementedError
    def logprob(self, value):
        raise NotImplementedError

class UnivariateDistribution(Distribution):
    def _logprob(self, value):
        raise NotImplementedError
    def logprob(self, value):
        if isinstance(value, list):
            return sum(self._logprob(v) for v in value)
        else:
            return self._logprob(value)


# I have quickly implemented a few distributions,
# but we can also use an existing package:
# e.g. https://docs.pyro.ai/en/stable/distributions.html#pyro-distributions

# We also have to consider that distribution packages have different conventions.
# For example in Turing (Distributions.jl) Gamma(alpha,beta) corresponds to the parameterisation shape=alpha, rate=beta,
# whereas in Pyro (torch.distributions) Gamma(k,theta) corresponds to the parameterisation shape=k, scale=theta
# see https://en.wikipedia.org/wiki/Gamma_distribution

class Bernoulli(UnivariateDistribution):
    # 1 with probability p, 0 with probability 1-p
    def __init__(self, p):
        self.p = p
    def sample(self):
        return random.random() < self.p
    def _logprob(self, value) -> float:
        if value == 1:
            return math.log(self.p)
        else:
            return math.log(1-self.p)
    def __repr__(self) -> str:
        return f"Bernoulli({self.p})"
        
class Dirac(UnivariateDistribution):
    # x with probability 1
    def __init__(self, x):
        self.x = x
    def sample(self):
        return self.x
    def _logprob(self, value) -> float:
        if value == self.x:
            return 0
        else:
            return -math.inf
    def __repr__(self) -> str:
        return f"Dirac({self.x})"
        
class Uniform(UnivariateDistribution):
    # pdf(x) = 1/(b-a) 1_[a,b](x)
    def __init__(self, low, high):
        self.low = low
        self.high = high
    def sample(self):
        return self.low + (self.high - self.low) * random.random()
    def _logprob(self, value) -> float:
        if self.low <= value and value <= self.high:
            return -math.log(self.high-self.low)
        else:
            return -math.inf
    def __repr__(self) -> str:
        return f"Uniform({self.low}, {self.high})"
    
class Normal(UnivariateDistribution):
    # pdf(x) = 1/sqrt(2 pi sigma**2) * exp( -(x - mu)^2 / (2 sigma^2) )
    def __init__(self, mu, sigma):
        self.mu = mu
        self.sigma = sigma
    def sample(self):
        return random.gauss(self.mu, self.sigma)
    def _logprob(self, value) -> float:
        return -1/2 * ((value - self.mu) /self.sigma)**2 - math.log(self.sigma * math.sqrt(2*math.pi))
    def __repr__(self) -> str:
        return f"Normal({self.mu}, {self.sigma})"
    
class IID(UnivariateDistribution):
    def __init__(self, base: Distribution, n: int) -> None:
        self.base = base
        self.n = n
    def sample(self):
        return [self.base.sample() for _ in range(self.n)]
    def logprob(self, value) -> float:
        assert isinstance(value, list) and len(value) == self.n
        return sum(self.base.logprob(value[i]) for i in range(self.n))
    def __repr__(self) -> str:
        return f"IID({self.base}, {self.n})"