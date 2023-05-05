"""
probrogs benchmark | geometric.py

Keep flipping a binary coin until get zero. Count the number of flips.
A common example, but this version adapted to Python from pGCL in:

Lutz Klinkenberg, Kevin Batz, Benjamin Lucien Kaminski, Joost-Pieter Katoen,
Joshua Moerman, Tobias Winkler: Generating Functions for Probabilistic
Programs. LOPSTR 2020: 231-248
"""
import numpy as np
import time
from scipy.stats import bernoulli

np.random.seed(int(time.time()))

c = 0
x = 1 

while x == 1:
    """
    HeyVL.invariant: "...." 
    """
    Annotate.HeyVL(" .... ")
    if bernoulli(0.5).rvs(): 
        x = 0 
    else: 
        x = 1
    c += 1
"""
STORM.This program is PAST.
"""
