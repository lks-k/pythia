import numpy as np
import scipy.stats as stats

class Distribution:
    def sample(self, size=None):
        raise NotImplementedError
    
    def _logprob(self, value):
        raise NotImplementedError

    def logprob(self, value):
        if isinstance(value, list):
            return sum(self._logprob(v) for v in value)
        elif isinstance(value, np.ndarray):
            return self._logprob(value).sum()
        else:
            return self._logprob(value)
        
class IID(Distribution):
    def __init__(self, base: Distribution, n: int) -> None:
        self.base = base
        self.n = n

    def sample(self, size=None):
        if size is not None:
            if isinstance(size, int):
                return self.base.sample(size=(self.n,size))
            else:
                assert isinstance(size, tuple)
                return self.base.sample(size=(self.n,) + size)
        else:
            return self.base.sample(size=self.n)
    
    def logprob(self, value) -> float:
        if isinstance(value, np.ndarray):
            return self.base.logprob(value).sum()
        else:
            assert isinstance(value, list) and len(value) == self.n
            return sum(self.base.logprob(value[i]) for i in range(self.n))
    
    def __repr__(self) -> str:
        return f"IID({self.base}, {self.n})"
    
class Broadcasted(Distribution):
    def __init__(self, base: Distribution) -> None:
        self.base = base

    def sample(self, size=None):
        return self.base.sample(size=size)
    
    def _logprob(self, value):
        return self.base._logprob(value)
    
    def __repr__(self) -> str:
        return f"Broadcasted({self.base})"

class Dirac(Distribution):
    def __init__(self, value):
        self.value = value

    def sample(self, size=None):
        if size is None:
            return self.value
        return np.full(size, self.value)

    def _logprob(self, value):
        if isinstance(value, np.ndarray):
            lp = np.zeros(value.shape)
            lp[value != self.value] = -np.inf
            return lp

        if value == self.value:
            return 0.
        else:
            return -np.inf
        
    def __repr__(self):
        return f"Dirac(value={self.value})"
        