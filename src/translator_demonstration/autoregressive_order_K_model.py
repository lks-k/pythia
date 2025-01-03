# See: https://github.com/stan-dev/posteriordb/blob/master/posterior_database/models/stan/arK.stan
@probabilistic_program
def autoregressive_order_K_model(y, K):
    alpha = sample("alpha", Normal(0, 10))
    beta = sample("beta", IID(Normal(0, 10), K))
    sigma = sample("sigma", HalfCauchy(2.5))
    for t in range(K, len(y)):
        mu = alpha
        for k in range(0, K):
            mu = mu + beta[k] * y[t - k]
        observe(y[t], IndexedAddress("y", t), Normal(mu, sigma))
