package probrogs

import org.scalacheck.{Arbitrary, Gen, Prop}, Arbitrary.*, Prop.*
import probrogs.Dist.*, NDist.*

object ParserSpec
  extends org.scalacheck.Properties("Parse"): 

  property("Unbiased coin") =                                                                                                                
    pythonExpr2pgcl("bernoulli(0.5).rvs()") == Bernoulli(CstF(0.5))
