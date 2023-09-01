"""
probprogs benchmark | flip.py
Flip a binary coin and record the outcome. Possibly the simplest
probabilistic program possible.
"""

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
    


"""This is some thoughts on how to translate it"""

def flip():
  with pm.Model() as model:
      pr_coin = pm.Bernouli ("coin", .5)
      # difficult, requires data-flow analysis?
      pr_x = pm.Deterministic ("x", 0 if pr_coin else 1)
  return model

m = flip()
trace = m.sample()
# here put some basic output
