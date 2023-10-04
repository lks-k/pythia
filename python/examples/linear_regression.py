#%%
import math
import probros as pr
import numpy as np
from tqdm import tqdm
import matplotlib.pyplot as plt

# Univariate fixed-noise linear regression

#%%
# x: vector of floats
# y: vector of floats
# slope_prior_mean: float
# slope_prior_sigma: float > 0
# intercept_prior_mean: float
# intercept_prior_sigma: float > 0
# sigma: float > 0
@pr.probabilistic_program
def linear_regression(
    x, y,
    slope_prior_mean, slope_prior_sigma,
    intercept_prior_mean, intercept_prior_sigma,
    sigma):
    
    slope = pr.sample("slope", pr.Normal(slope_prior_mean, slope_prior_sigma))
    intercept = pr.sample("intercept", pr.Normal(intercept_prior_mean, intercept_prior_sigma))

    for i in range(len(x)):
        pr.observe(y[i], pr.IndexedAddress("y",i), pr.Normal(slope * x[i] + intercept, sigma))

    return slope, intercept

@pr.probabilistic_program
def linear_regression_vectorized(
    x, y,
    slope_prior_mean, slope_prior_sigma,
    intercept_prior_mean, intercept_prior_sigma,
    sigma):
    
    slope = pr.sample("slope", pr.Normal(slope_prior_mean, slope_prior_sigma))
    intercept = pr.sample("intercept", pr.Normal(intercept_prior_mean, intercept_prior_sigma))

    pr.observe(y, f"y", pr.Broadcasted(pr.Normal(slope * x + intercept, sigma)))

    return slope, intercept

#%%
# true posteriors for reference
def get_true_posterior_joint(
    x, y,
    slope_prior_mean, slope_prior_sigma,
    intercept_prior_mean, intercept_prior_sigma,
    sigma):
    m0 = np.array([intercept_prior_mean, slope_prior_mean])
    S0 = np.array([[intercept_prior_sigma**2, 0.], [0., slope_prior_sigma**2]])


    Phi = np.vstack((np.ones(len(x)), x)).T
    S = np.linalg.inv(np.linalg.inv(S0) + Phi.T.dot(Phi) / sigma**2)
    m = S.dot(np.linalg.inv(S0).dot(m0) + Phi.T.dot(y) / sigma**2)

    return pr.MultivariateNormal(m, S)

def get_true_posterior_intercept(
    x, y,
    slope_prior_mean, slope_prior_sigma,
    intercept_prior_mean, intercept_prior_sigma,
    sigma):
    joint = get_true_posterior_joint(x, y,
        slope_prior_mean, slope_prior_sigma,
        intercept_prior_mean, intercept_prior_sigma,
        sigma)
    return pr.Normal(joint.mean[0], np.sqrt(joint.cov[0,0]))


def get_true_posterior_slope(
    x, y,
    slope_prior_mean, slope_prior_sigma,
    intercept_prior_mean, intercept_prior_sigma,
    sigma):
    joint = get_true_posterior_joint(x, y,
        slope_prior_mean, slope_prior_sigma,
        intercept_prior_mean, intercept_prior_sigma,
        sigma)
    return pr.Normal(joint.mean[1], np.sqrt(joint.cov[1,1]))


#%%
# Sanity Check
np.random.seed(0)

sigma = 2.

x = np.random.randn(10)
y = 2 * x - 1 + sigma * np.random.randn()

slope_prior_mean, slope_prior_sigma = (0,3)
intercept_prior_mean, intercept_prior_sigma = (0,3)

vectorized = True
model = linear_regression_vectorized if vectorized else linear_regression

samples = [
    model(
        x, y,
        slope_prior_mean, slope_prior_sigma,
        intercept_prior_mean, intercept_prior_sigma,
        sigma
    )
    for _ in tqdm(range(100_000))
]
traces = [trace.entries_by_address() for retval, trace in samples]
lps = np.array([trace.log_likelihood for retval, trace in samples])
p = np.exp(lps)
p = p / p.sum()

#%%
slope = np.array([trace["slope"]["value"] for trace in traces])

slope_posterior = get_true_posterior_slope(x, y,
    slope_prior_mean, slope_prior_sigma,
    intercept_prior_mean, intercept_prior_sigma,
    sigma)

xlim = slope_posterior.loc - 4*slope_posterior.scale, slope_posterior.loc + 4*slope_posterior.scale
ls = np.linspace(*xlim, 200)
mask = (xlim[0] < slope) & (slope < xlim[1])
plt.hist(slope[mask], weights=p[mask], density=True, bins=50)
plt.plot(ls, np.exp(slope_posterior._logprob(ls)))
plt.title("Slope Posterior")
plt.show()

#%%
intercept = np.array([trace["intercept"]["value"] for trace in traces])
intercept_posterior = get_true_posterior_intercept(x, y,
    slope_prior_mean, slope_prior_sigma,
    intercept_prior_mean, intercept_prior_sigma,
    sigma)

xlim = intercept_posterior.loc - 4*intercept_posterior.scale, intercept_posterior.loc + 4*intercept_posterior.scale
ls = np.linspace(*xlim, 200)
mask = (xlim[0] < intercept) & (intercept < xlim[1])
plt.hist(intercept[mask], weights=p[mask], density= True,bins=50)
plt.plot(ls, np.exp(intercept_posterior._logprob(ls)))
plt.title("Interecept Posterior")
plt.show()
# %%
