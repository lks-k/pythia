from .distributions import *

# global variables are ugly, but simple (pyro uses a similar method)
_TRACE = {}

# We define a decorator
# @trace
# def func():
#     ...
# which traces all sample, observe, and factor statements
def trace(func):
    def wrapper(*args, **kwargs):
        global _TRACE
        
        # reset trace
        _TRACE = {}

        # run model function
        retval = func(*args, **kwargs)

        # sum up log probabilities, i.e. logprob is joint probability of model p(X,Y=y)
        logprob = sum(entry['logprob'] for _, entry in _TRACE.items())

        # store some more information
        _TRACE['INPUT'] = (args, kwargs)
        _TRACE['RETVAL'] = retval
        _TRACE['LOGPROB'] = logprob

        return retval, logprob, _TRACE
    
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
    # draw random variable according to distribution
    value = distribution.sample()

    # compute log probability of sampled value
    logprob = distribution.logprob(value)

    # store result in global trace
    _TRACE[address] = {
        'kind': 'sample',
        'value': value,
        'logprob': logprob,
        'distribution': distribution
    }
    # return sampled value
    return value

def observe(value, address: str = None, distribution: Distribution = Dirac(True)):
    if address is None:
        # we provide default (unique) addresses
        address = f"observe_{len(_TRACE)}"

    # compute log probability of observed value
    logprob = distribution.logprob(value)

    # store result in global trace
    _TRACE[address] = {
        'kind': 'observe',
        'value': value,
        'logprob': logprob,
        'distribution': distribution
    }

    # return observed value
    return value

def factor(logfactor, address: str = None):
    if address is None:
        address = f"factor:{len(_TRACE)}"

    # store result in global trace
    _TRACE[address] = {
        'kind': 'factor',
        'logprob': logfactor
    }