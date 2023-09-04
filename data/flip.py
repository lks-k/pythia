"""
probprogs benchmark | flip.py
Flip a binary coin and record the outcome. Possibly the simplest
probabilistic program possible.
"""
import sys
sys.path.insert(0, '../python')
import probros as pr

"""
pre-expectation: [ ['x': 0] -> 0.5, 
                   ['x': 1] -> 0.5 ]
"""
"""
post-expectation: 1
"""
@pr.trace
def flip():
    if pr.sample("coin", pr.Bernoulli(.5)): 
        x = 0 
    else: 
        x = 1
    return x
    
print(flip())
