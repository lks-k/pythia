"""
probprogs benchmark | flip.py
Flip a binary coin and record the outcome. Possibly the simplest
probabilistic program possible.
"""
import numpy as np
import time
from scipy.stats import bernoulli

np.random.seed(int(time.time()))

"""
pre-expectation: [ ['x': 0] -> 0.5, 
                   ['x': 1] -> 0.5 ]
"""
if bernoulli(0.5).rvs(): 
    x = 0 
else: 
    x = 1

print(f"{x}")
"""
post-expectation: 1
"""
