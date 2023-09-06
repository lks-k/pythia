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
        
import torch
import pyro.distributions as pyro_dist

def UnivariateDistributionFactory(name: str, base: pyro_dist.Distribution):
    def uvd__init__(self, *args, **kwargs):
        self.base = base(
            *[torch.tensor(arg).float() for arg in args],
            **{k: torch.tensor(v).float() for k,v in kwargs.items()}
            )
    def uvd_sample(self):
        return self.base.sample().item()
    def uvd_logprob(self, value):
        return self.base.log_prob(torch.tensor(value)).item()
    
    return type(
        name,
        (UnivariateDistribution,),
        {
            "__init__": uvd__init__,
            "sample": uvd_sample,
            "_logprob": uvd_logprob
        })


Bernoulli = UnivariateDistributionFactory("Bernoulli", pyro_dist.Bernoulli)
Beta = UnivariateDistributionFactory("Beta", pyro_dist.Beta)
Binomial = UnivariateDistributionFactory("Binomial", pyro_dist.Binomial)
Categorical = UnivariateDistributionFactory("Categorical", pyro_dist.Categorical)
Cauchy = UnivariateDistributionFactory("Cauchy", pyro_dist.Cauchy)
Chi2 = UnivariateDistributionFactory("Chi2", pyro_dist.Chi2)
Exponential = UnivariateDistributionFactory("Exponential", pyro_dist.Exponential)
Gamma = UnivariateDistributionFactory("Gamma", pyro_dist.Gamma)
Geometric = UnivariateDistributionFactory("Geometric", pyro_dist.Geometric)
Gumbel = UnivariateDistributionFactory("Gumbel", pyro_dist.Gumbel)
HalfCauchy = UnivariateDistributionFactory("HalfCauchy", pyro_dist.HalfCauchy)
HalfNormal = UnivariateDistributionFactory("HalfNormal", pyro_dist.HalfNormal)
Laplace = UnivariateDistributionFactory("Laplace", pyro_dist.Laplace)
LogNormal = UnivariateDistributionFactory("LogNormal", pyro_dist.LogNormal)
NegativeBinomial = UnivariateDistributionFactory("NegativeBinomial", pyro_dist.NegativeBinomial)
Normal = UnivariateDistributionFactory("Normal", pyro_dist.Normal)
Pareto = UnivariateDistributionFactory("Pareto", pyro_dist.Pareto)
Poisson = UnivariateDistributionFactory("Poisson", pyro_dist.Poisson)
StudentT = UnivariateDistributionFactory("StudentT", pyro_dist.StudentT)
Uniform = UnivariateDistributionFactory("Uniform", pyro_dist.Uniform)
Dirac = UnivariateDistributionFactory("Dirac", pyro_dist.Delta)
InverseGamma = UnivariateDistributionFactory("InverseGamma", pyro_dist.InverseGamma)
Logistic = UnivariateDistributionFactory("Logistic", pyro_dist.Logistic)