import scala.io.Source
import probrogs.*
import _root_.{pythonparse => PP}
import fastparse.Parsed
import scala.annotation.targetName
import scala.math.BigInt

System.getProperty("user.dir")

import fastparse.~
import fastparse.NoWhitespace.*
def parseExp[$: fastparse.P] = pythonparse.Expressions.expr(fastparse.P.current) ~ fastparse.End

val p = fastparse
  .parse(
    Source.fromFile("/home/wasowski/work/2023-probrogs/benchmark/flip.py").mkString,
    pythonparse.Statements(0).file_input
  )
  .get
  .value
  .toList

val b = fastparse.parse("bernoulli(0.5).rvs()", parseExp)
  .get
  .value


// unparsing (this will go to syntax or Unparse.scala)

def indent(n: Int)(s: String) =
  val spaces = " " * n
  spaces + s.stripMargin.replaceAll("\n", "\n" + spaces)

val HSKIP = 4

def unparse(indentation: Int)(s: Stm): String = s match
  case Stm.Skip =>
    indent(indentation)("skip")

  case Stm.Assign(id, value) =>
    indent(indentation)(s"${id} = ${unparse(value)}")

  case Stm.Sample(id, distribution) =>
    indent(indentation)(s"${id} = ${unparse(distribution)}")

  case Stm.If(cond, body, orelse) =>
    indent(indentation) {
      s"""if ${unparse(cond)}:
         |${unparse(HSKIP)(body)}
         |else:
         |${unparse(HSKIP)(orelse)}"""
    }

  case Stm.While(cond, body) =>
    indent(indentation) {
      s"""while ${unparse(cond)}:
         |${unparse(HSKIP)(body)}"""
    }

  case Stm.Seq(pred, succ) =>
    indent(indentation) {
      s"""${unparse(0)(pred)}
         |${unparse(0)(succ)}"""
    }

  case Stm.Comment(str) =>
    indent(indentation)(s"'''$str'''")

def unparse(op: Op): String = op match
  case ROp.Eq  => "=="
  case ROp.NEq => "!="
  case ROp.LEq => "<="
  case ROp.Lt  => "<"
  case ROp.GEq => ">="
  case ROp.Gt  => ">"

  case AOp.Plus     => "+"
  case AOp.Minus    => "-"
  case AOp.Mult     => "*"
  case AOp.Div      => "/"
  case AOp.Mod      => "%"
  case AOp.Pow      => "**"
  case AOp.LShift   => "<<"
  case AOp.RShift   => ">>"
  case AOp.BitOr    => "|"
  case AOp.BitXor   => "^"
  case AOp.BitAnd   => ""
  case AOp.FloorDiv => "//"

  case BOp.And => "and"
  case BOp.Or  => "or"

def unparse(op: UOp): String = op match
  case UOp.Not    => "not"
  case UOp.Minus  => "-"
  case UOp.Plus   => "+"
  case UOp.BitNot => "~"

@targetName("unparseExp")
def unparse(e: Exp): String = e match

  case Dist.Bernoulli(bias) =>
    s"bernoulli(${unparse(bias)}).rvs()"

  case Dist.Uniform(left, right) =>
    val l = unparse(left)
    s"uniform(loc=($l), scale=((${unparse(right)})-($l))).rvs()"

  case NDist.CstI(value) => value.toString
  case NDist.CstF(value) => value.toString

  case NDist.BOperator(left, op, right) =>
    s"(${unparse(left)})${unparse(op)}(${unparse(right)})"

  case NDist.UOperator(op, e) =>
    s"${unparse(op)}(${unparse(e)})"

  case NDist.Call(name, args) =>
    s"$name(${args.map(unparse).mkString(",")})"

  case NDist.Var(name) =>
    name.toString

val pgcl = python2pgcl(p)
val str = unparse(0)(pgcl)

print(str)

p(4)


