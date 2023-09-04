# an attempt to perform a manual translation of flip.py to pymc
import pymc as pm
from pytensor import tensor as at

"""
pre-expectation: [ ['x': 0] -> 0.5, 
                   ['x': 1] -> 0.5 ]
"""
"""
post-expectation: 1
"""
def flip():
    with pm.Model() as model:
        x = at.switch(pm.Bernoulli("coin", .5), 1, 0)
        pm.Deterministic("x", x)
    return model

model = flip()
with model:
    trace = pm.sample()
print(trace.posterior)
