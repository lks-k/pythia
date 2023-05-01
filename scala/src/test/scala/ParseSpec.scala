package probrogs

import org.scalacheck.{Arbitrary, Gen, Prop}, Arbitrary.*, Prop.*
import probrogs.Dist.*, NDist.*, Stm.*

import pythonparse.Ast.expr.{Call => pCall, Attribute => pAttribute, 
                             Name => pName, Num => pNum}
import pythonparse.Ast.stmt.{Import, If => pIf, Assign => pAssign}
import pythonparse.Ast.expr_context.{Load => pLoad}
import pythonparse.Ast.{identifier => pIdentifier}

object ParserSpec
  extends org.scalacheck.Properties("Parse"): 

  property("regression 00: Unbiased coin") =                                                                                                                
    pythonExpr2pgcl("bernoulli(0.5).rvs()") == Bernoulli(CstF(0.5))
  
  property("regression 01: Unbiased coin in an if condition") =
    val result = python2pgcl("""if bernoulli(0.5).rvs(): 
                             |    x = 0""".stripMargin)
    val expected = Seq(
      If(Bernoulli(CstF(0.5)),Seq(Assign("x".toId,CstI(0)),Skip),Skip),
      Skip
    )
    result == expected
