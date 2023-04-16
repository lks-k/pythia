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

def python2pgcl(p: List[PP.Ast.stmt]): Stm =
  stmtList2pgcl(p)

def stmt2pgcl(s: PP.Ast.stmt): List[Stm] = s match
  case _: PP.Ast.stmt.FunctionDef =>
    throw NotImplementedError("Function definitions are not supported")

  case _: PP.Ast.stmt.ClassDef =>
    throw NotImplementedError("Class definitions are not supported")

  case _: PP.Ast.stmt.Return =>
    throw NotImplementedError("Function definitions are not supported")

  case _: PP.Ast.stmt.Delete =>
    throw NotImplementedError("Python 'del' is not supported")

  case PP.Ast.stmt.Assign(Seq(PP.Ast.expr.Name(id, ctx)), e) =>
    List(Stm.Assign(id.name.toId, expr2pgcl(e)))

  case PP.Ast.stmt.Assign(targets, _) if targets.sizeIs > 1 =>
    throw NotImplementedError("Non-single-target assignemts are not supported")

  case PP.Ast.stmt.Assign(t, e) =>
    throw NotImplementedError(s"Assigning target other than name not supported")

  case PP.Ast.stmt.AugAssign(PP.Ast.expr.Name(id, ctx), op, value) =>
    Stm.Assign(
      id.name.toId,
      NDist.BOperator(NDist.Var(id.name.toId), op2pgcl(op), expr2pgcl(value))
    ) :: Nil

  case PP.Ast.stmt.AugAssign(_, _, _) =>
    throw NotImplementedError(s"Assigning target other than name not supported")

  case PP.Ast.stmt.Print(dest, values, nl) =>
    Nil // we ignore print statements

  case _: PP.Ast.stmt.For =>
    throw NotImplementedError("Python for-loops not supported yet")

  case PP.Ast.stmt.While(test, body, Seq()) =>
    List(Stm.While(expr2pgcl(test), stmtList2pgcl(body)))

  case PP.Ast.stmt.While(test, body, orelse) =>
    throw NotImplementedError("Python while-loops with orelse not supported")

  case PP.Ast.stmt.If(test, body, orelse) =>
    List(Stm.If(expr2pgcl(test), stmtList2pgcl(body), stmtList2pgcl(orelse)))

  case _: PP.Ast.stmt.With =>
    throw NotImplementedError("Python 'with' statement is not supported yet")

  case _: PP.Ast.stmt.Import =>
    Nil // ignore imports, as they are useful for exec, but not useful for pgcl

  case _: PP.Ast.stmt.ImportFrom =>
    Nil // ignore imports, as they are useful for exec, but not useful for pgcl

  case PP.Ast.stmt.Expr(
    PP.Ast.expr.Call(
      PP.Ast.expr.Attribute(
        PP.Ast.expr.Attribute(
          PP.Ast.expr.Name(PP.Ast.identifier("np"), _),
          PP.Ast.identifier("random"), _), 
        PP.Ast.identifier(seed), _),
      Seq(initial), Seq(), None, None)) =>
    Nil // skip random number initialization (np.random.seed(...))

  case _: PP.Ast.stmt.Expr =>
    Nil // ignore expression statements as expressions are pure so far

  case _ =>
    throw NotImplementedError(s"Statement not supported $s")

def stmtList2stmt(ss: Seq[Stm]): Stm =
  ss.foldRight(Stm.Skip)(Stm.Seq.apply)

def stmtList2pgcl(ss: Seq[PP.Ast.stmt]): Stm =
  stmtList2stmt(ss.flatMap(stmt2pgcl))

def op2pgcl(op: PP.Ast.operator): Op = op match
  case PP.Ast.operator.Add      => AOp.Plus
  case PP.Ast.operator.Sub      => AOp.Minus
  case PP.Ast.operator.Mult     => AOp.Mult
  case PP.Ast.operator.Div      => AOp.Div
  case PP.Ast.operator.Mod      => AOp.Mod
  case PP.Ast.operator.Pow      => AOp.Pow
  case PP.Ast.operator.LShift   => AOp.LShift
  case PP.Ast.operator.RShift   => AOp.RShift
  case PP.Ast.operator.BitOr    => AOp.BitOr
  case PP.Ast.operator.BitXor   => AOp.BitXor
  case PP.Ast.operator.BitAnd   => AOp.BitAnd
  case PP.Ast.operator.FloorDiv => AOp.FloorDiv

def boolop2pgcl(op: PP.Ast.boolop): BOp = op match
  case PP.Ast.boolop.And => BOp.And
  case PP.Ast.boolop.Or  => BOp.Or

def cmpop2pgcl(op: PP.Ast.cmpop): ROp = op match
  case PP.Ast.cmpop.Eq    => ROp.Eq
  case PP.Ast.cmpop.NotEq => ROp.NEq
  case PP.Ast.cmpop.Lt    => ROp.Lt
  case PP.Ast.cmpop.LtE   => ROp.LEq
  case PP.Ast.cmpop.Gt    => ROp.Gt
  case PP.Ast.cmpop.GtE   => ROp.GEq

  case PP.Ast.cmpop.Is =>
    throw NotImplementedError("The 'is' operator not supported")
  case PP.Ast.cmpop.IsNot =>
    throw NotImplementedError("The 'is not' operator not supported")
  case PP.Ast.cmpop.In =>
    throw NotImplementedError("The 'in' operator not supported")
  case PP.Ast.cmpop.NotIn =>
    throw NotImplementedError("The 'not in' operator not supported")

def unaryop2pgcl(op: PP.Ast.unaryop): UOp = op match
  case PP.Ast.unaryop.Invert => UOp.BitNot
  case PP.Ast.unaryop.Not    => UOp.Not
  case PP.Ast.unaryop.UAdd   => UOp.Plus
  case PP.Ast.unaryop.USub   => UOp.Minus

def obj2pgcl(a: Any) = a match 
  case n: Int => NDist.CstI(n)
  case x: Double => NDist.CstF(x)
  case x: BigDecimal => NDist.CstF(x.toDouble)
  case _ => throw NotImplementedError(s"An unsupported literal type '${a.getClass}'")

def expr2pgcl(e: PP.Ast.expr): Exp = e match
  case PP.Ast.expr.BoolOp(op, Seq(left, right)) =>
    NDist.BOperator(expr2pgcl(left), boolop2pgcl(op), expr2pgcl(right))

  case PP.Ast.expr.BoolOp(op, _) =>
    throw NotImplementedError(s"Nonbinary Boolean operators not supported: $e")

  case PP.Ast.expr.BinOp(left, op, right) =>
    NDist.BOperator(expr2pgcl(left), op2pgcl(op), expr2pgcl(right))

  case PP.Ast.expr.UnaryOp(op, operand) =>
    NDist.UOperator(unaryop2pgcl(op), expr2pgcl(operand))

  case PP.Ast.expr.Lambda(args, body) =>
    throw NotImplementedError(s"Python λ-expressions are not supported!")

  case PP.Ast.expr.IfExp(test, body, orelse) =>
    throw NotImplementedError(s"Python ternary expressions not supported!")

  case PP.Ast.expr.Dict(keys, values) =>
    throw NotImplementedError(s"Python dictionary expressions not supported!")

  case PP.Ast.expr.Set(elts) =>
    throw NotImplementedError(s"Python set expressions not supported!")

  case PP.Ast.expr.ListComp(elt, generators) =>
    throw NotImplementedError(s"Python list comprehensions not supported!")

  case PP.Ast.expr.SetComp(elt, generators) =>
    throw NotImplementedError(s"Python set comprehensions not supported!")

  case PP.Ast.expr.DictComp(key, value, generators) =>
    throw NotImplementedError(s"Python dict comprehensions not supported!")

  case PP.Ast.expr.GeneratorExp(elt, generators) =>
    throw NotImplementedError(s"Python generator expressions not supported!")

  case PP.Ast.expr.Yield(value) =>
    throw NotImplementedError(s"Python yield not supported!")

  case PP.Ast.expr.Compare(left, Seq(op), Seq(right)) =>
    NDist.BOperator(expr2pgcl(left), cmpop2pgcl(op), expr2pgcl(right))

  case _: PP.Ast.expr.Compare =>
    throw NotImplementedError(s"Non-binary comparisons are not supported")
    
  case PP.Ast.expr.Call(
        PP.Ast.expr.Attribute(
          PP.Ast.expr.Call(
            PP.Ast.expr.Name(PP.Ast.identifier("bernoulli"), ctx),
            List(PP.Ast.expr.Num(bias)), Nil, None, None),
          PP.Ast.identifier("rvs"), _), Nil, Nil, None, None) =>
    Dist.Bernoulli(obj2pgcl(bias))

  // case Ast.expr.Repr(value: expr) => ???

  case _: PP.Ast.expr.Call =>
    NDist.CstI(43)
    // TODO throw NotImplementedError(s"General function calls are not supported")

  case PP.Ast.expr.Num(n: Int) =>
    NDist.CstI(n)

  case PP.Ast.expr.Num(n: BigInt) if n.isValidInt =>
    NDist.CstI(n.toInt)

  case PP.Ast.expr.Num(x: Double) =>
    NDist.CstF(x)

  // case Ast.expr.Str(s: string)    => ??? // need to raw: specify, unicode, etc?
  // // other bools: Option[literals]?

  // // the following expression can appear in assignment context
  // case Ast.expr.Attribute(value: expr, attr: identifier, ctx: expr_context) =>
  //   ???
  // case Ast.expr.Subscript(value: expr, slice: slice, ctx: expr_context) => ???
  case PP.Ast.expr.Name(id, ctx) =>
    NDist.Var(id.name.toId)

  case PP.Ast.expr.List(elts, ctx) =>
    throw NotImplementedError("Lists literals are not supported")

  case PP.Ast.expr.Tuple(elts, ctx) =>
    throw NotImplementedError("Tuple literals are not supported")

  case _ => NDist.CstI(42)

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


