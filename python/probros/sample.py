from .scipy_distributions import Distribution, Dirac
import numpy as np
from tqdm import tqdm
from collections import defaultdict

# global variables are ugly, but simple (pyro uses a similar method)
_TRACE = None
class Trace:
    def __init__(self) -> None:
        self.trace = []
        self.log_prior = 0.
        self.log_likelihood = 0.
        self.log_joint = 0.
        self.input = None
        self.retval = None
    def __repr__(self) -> str:
        s = f"Trace(input={self.input})\n"
        for i, entry in enumerate(self.trace):
            s += f"{i}.: {entry}\n"
        s += f"retval={self.retval}\n"
        s += f"log prior: {self.log_prior}\n"
        s += f"log likelihood: {self.log_likelihood}\n"
        s += f"log joint: {self.log_joint}"
        return s
    def __getitem__(self, i):
        return self.trace[i]
    def entries_by_address(self):
        return {entry['address']: entry for entry in self.trace}


# We define a decorator
# @trace
# def func():
#     ...
# which traces all sample, observe, and factor statements
def probabilistic_program(func):
    def wrapper(*args, **kwargs):
        global _TRACE
        
        # reset trace
        _TRACE = Trace()

        # run model function
        retval = func(*args, **kwargs)

        # sum up log probabilities, i.e. lp_jiont is joint probability of model p(X,Y=y)
        lp_prior = sum(entry['logprob'] for entry in _TRACE.trace if entry["kind"] == "sample")
        lp_lik = sum(entry['logprob'] for entry in _TRACE.trace if entry["kind"] != "sample")
        lp_joint = lp_prior + lp_lik

        # store some more information
        _TRACE.input = (args, kwargs)
        _TRACE.retval = retval
        _TRACE.log_prior = lp_prior
        _TRACE.log_likelihood = lp_lik
        _TRACE.log_joint = lp_joint

        return retval, _TRACE
    
    return wrapper

# sample statements have an address, because sometimes you may want to
# have multiple statements corresponding to the same variable, but storing it
# in different program identifiers. We require an explicit address (variable name)
# e.g.
# if test:
#     x = sample("a", Bernoulli(0.9))
#     ...
# else:
#     y = sample("a", Bernoulli(0.1))
# Random value draws are logged in the trace at `address`.

def sample(address: str, distribution: Distribution):
    if address is None:
        # we provide default (unique) addresses
        address = f"sample_{len(_TRACE.trace)}"

    # draw random variable according to distribution
    value = distribution.sample()

    # compute log probability of sampled value
    logprob = distribution.logprob(value)

    # store result in global trace
    _TRACE.trace.append({
        'address': address,
        'kind': 'sample',
        'value': value,
        'logprob': logprob,
        'distribution': distribution
    })
    # return sampled value
    return value

def observe(value, address: str = None, distribution: Distribution = Dirac(True)):
    if address is None:
        # we provide default (unique) addresses
        address = f"observe_{len(_TRACE.trace)}"

    # compute log probability of observed value
    logprob = distribution.logprob(value)

    # store result in global trace
    _TRACE.trace.append({
        'address': address,
        'kind': 'observe',
        'value': value,
        'logprob': logprob,
        'distribution': distribution
    })

    # return observed value
    return value

def factor(logfactor, address: str = None):
    if address is None:
        address = f"factor:{len(_TRACE.trace)}"

    # store result in global trace
    _TRACE.trace.append({
        'address': address,
        'kind': 'factor',
        'logprob': logfactor
    })

def estimate_moments(n_iter: int, K: int, model, *args, **kwargs):
    collected_trace = defaultdict(list)
    lps = np.zeros(n_iter)
    for i in tqdm(range(n_iter)):
        retval, trace = model(*args, **kwargs)
        lps[i] = trace.log_likelihood
        #collected_trace['__RETVAL__'].append(retval)
        for entry in trace.trace:
            if entry['kind'] == 'sample':
                collected_trace[entry['address']].append(entry["value"])

    # normalise
    m = np.max(lps)
    lps = lps - (m + np.log(np.sum(np.exp(lps - m))))
    p = np.exp(lps)

    for (address, values) in collected_trace.items():
        first = np.shape(values[0])
        all_equal_shape = all(first == np.shape(x) for x in values)
        assert all_equal_shape, f"Values for address {address} have varying shapes."
        values = np.array(values) # (n_iter, *first)
        print(address + ":")
        sample_moment = p.dot(values)
        for moment in range(1,K+1):
            if moment == 1:
                print(f"  E[X] ≈", sample_moment)
            else:
                print(f"  E[(X - mu)^{moment}] ≈", p.dot((values - sample_moment) ** moment))


def IndexedAddress(base: str, *index):
    if len(index) == 1:
        return f"{base}[{index[0]}]"
    i = ",".join(map(str, index))
    return f"{base}[{i}]"


class Vector:
    def __init__(self, n: int, t=None, fill=None):
        self.n = n
        self.list = [fill] * n
    def __len__(self):
        return self.n
    def __getitem__(self, key):
        return self.list[key]
    def __setitem__(self, key, value):
        self.list[key] = value
    def __repr__(self) -> str:
        return "Vector(" + repr(self.list) + ")"


class Array:
    def __init__(self, shape: tuple[int], t=None, fill=None):
        self.shape = shape
        self.array = np.full(shape, fill)
    def __len__(self):
        return self.n
    def __getitem__(self, key):
        return self.array[key]
    def __setitem__(self, key, value):
        self.array[key] = value
    def __repr__(self) -> str:
        return "Array(" + repr(self.array) + ")"