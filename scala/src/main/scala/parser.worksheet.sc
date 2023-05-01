// A temporary development file, plus usage examples
import scala.io.Source
import probrogs.*
import _root_.{pythonparse => PP}
import fastparse.Parsed
import scala.math.BigInt


val pgcl = pythonFile2pgcl("/home/wasowski/work/2023-probrogs/benchmark/flip.py")
val str = unparse(0)(pgcl)

print(str)

pythonExpr2pgcl("bernoulli(0.5).rvs()")

val flipSrc = s"""import numpy as np
             |import time
             |from scipy.stats import bernoulli
             |np.random.seed(int(time.time()))
             |c = 0
             |x = 1 
             |while x == 1:
             |    if bernoulli(0.5).rvs(): 
             |        x = 0 
             |    else: 
             |        x = 1
             |    c += 1\n""".stripMargin

val flipPath = "/home/wasowski/work/2023-probrogs/benchmark/flip.py"
val flipAst = pythonFile2pgcl(flipPath)
val flipSrcAst = python2pgcl(flipSrc)

print(unparse(0)(flipSrcAst).indent(1))
