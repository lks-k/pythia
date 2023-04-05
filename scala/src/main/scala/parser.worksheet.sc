import scala.io.Source
import probrogs.*
import pythonparse.*
import fastparse.Parsed
import scala.annotation.targetName
import scala.math.BigInt

val p = fastparse
  .parse(
    Source.fromFile("../benchmark/geometric.py").mkString,
    pythonparse.Statements(0).file_input
  )
  .get
  .value
  .toList

def python2pgcl(p: List[Ast.stmt]): Stm =
  stmtList2pgcl(p)

def stmt2pgcl(s: Ast.stmt): List[Stm] = s match
  case _: Ast.stmt.FunctionDef =>
    throw NotImplementedError("Function definitions are not supported")

  case _: Ast.stmt.ClassDef =>
    throw NotImplementedError("Class definitions are not supported")

  case _: Ast.stmt.Return =>
    throw NotImplementedError("Function definitions are not supported")

  case _: Ast.stmt.Delete =>
    throw NotImplementedError("Python 'del' is not supported")

  case Ast.stmt.Assign(Seq(Ast.expr.Name(id, ctx)), e) =>
    List(Stm.Assign(id.name.toId, expr2pgcl(e)))

  case Ast.stmt.Assign(targets, _) if targets.sizeIs > 1 =>
    throw NotImplementedError("Non-single-target assignemts are not supported")

  case Ast.stmt.Assign(t, e) =>
    throw NotImplementedError(s"Assigning target other than name not supported")

  case Ast.stmt.AugAssign(Ast.expr.Name(id, ctx), op, value) =>
    Stm.Assign(
      id.name.toId,
      NDist.BOperator(NDist.Var(id.name.toId), op2pgcl(op), expr2pgcl(value))
    ) :: Nil

  case Ast.stmt.AugAssign(_, _, _) =>
    throw NotImplementedError(s"Assigning target other than name not supported")

  case Ast.stmt.Print(dest, values, nl) =>
    Nil // we ignore print statements

  case _: Ast.stmt.For =>
    throw NotImplementedError("Python for-loops not supported yet")

  case Ast.stmt.While(test, body, Seq()) =>
    List(Stm.While(expr2pgcl(test), stmtList2pgcl(body)))

  case Ast.stmt.While(test, body, orelse) =>
    throw NotImplementedError("Python while-loops with orelse not supported")

  case Ast.stmt.If(test, body, orelse) =>
    List(Stm.If(expr2pgcl(test), stmtList2pgcl(body), stmtList2pgcl(orelse)))

  case _: Ast.stmt.With =>
    throw NotImplementedError("Python 'with' statement is not supported yet")

  case _: Ast.stmt.Import =>
    Nil // ignore imports, as they are useful for exec, but not useful for pgcl

  case _: Ast.stmt.ImportFrom =>
    Nil // ignore imports, as they are useful for exec, but not useful for pgcl

  case Ast.stmt.Expr(
    Ast.expr.Call(
      Ast.expr.Attribute(
        Ast.expr.Attribute(
          Ast.expr.Name(Ast.identifier("np"), _),
          Ast.identifier("random"), _), 
        Ast.identifier(seed), _),
      Seq(initial), Seq(), None, None)) =>
    Nil // skip random number initialization (np.random.seed(...))

  case _: Ast.stmt.Expr =>
    Nil // ignore expression statements as expressions are pure so far

  case _ =>
    throw NotImplementedError(s"Statement not supported $s")

def stmtList2stmt(ss: Seq[Stm]): Stm =
  ss.foldRight(Stm.Skip)(Stm.Seq.apply)

def stmtList2pgcl(ss: Seq[Ast.stmt]): Stm =
  stmtList2stmt(ss.flatMap(stmt2pgcl))

def op2pgcl(op: Ast.operator): Op = op match
  case Ast.operator.Add      => AOp.Plus
  case Ast.operator.Sub      => AOp.Minus
  case Ast.operator.Mult     => AOp.Mult
  case Ast.operator.Div      => AOp.Div
  case Ast.operator.Mod      => AOp.Mod
  case Ast.operator.Pow      => AOp.Pow
  case Ast.operator.LShift   => AOp.LShift
  case Ast.operator.RShift   => AOp.RShift
  case Ast.operator.BitOr    => AOp.BitOr
  case Ast.operator.BitXor   => AOp.BitXor
  case Ast.operator.BitAnd   => AOp.BitAnd
  case Ast.operator.FloorDiv => AOp.FloorDiv

def boolop2pgcl(op: Ast.boolop): BOp = op match
  case Ast.boolop.And => BOp.And
  case Ast.boolop.Or  => BOp.Or

def cmpop2pgcl(op: Ast.cmpop): ROp = op match
  case Ast.cmpop.Eq    => ROp.Eq
  case Ast.cmpop.NotEq => ROp.NEq
  case Ast.cmpop.Lt    => ROp.Lt
  case Ast.cmpop.LtE   => ROp.LEq
  case Ast.cmpop.Gt    => ROp.Gt
  case Ast.cmpop.GtE   => ROp.GEq

  case Ast.cmpop.Is =>
    throw NotImplementedError("The 'is' operator not supported")
  case Ast.cmpop.IsNot =>
    throw NotImplementedError("The 'is not' operator not supported")
  case Ast.cmpop.In =>
    throw NotImplementedError("The 'in' operator not supported")
  case Ast.cmpop.NotIn =>
    throw NotImplementedError("The 'not in' operator not supported")

def unaryop2pgcl(op: Ast.unaryop): UOp = op match
  case Ast.unaryop.Invert => UOp.BitNot
  case Ast.unaryop.Not    => UOp.Not
  case Ast.unaryop.UAdd   => UOp.Plus
  case Ast.unaryop.USub   => UOp.Minus

def expr2pgcl(e: Ast.expr): Exp = e match
  case Ast.expr.BoolOp(op, Seq(left, right)) =>
    NDist.BOperator(expr2pgcl(left), boolop2pgcl(op), expr2pgcl(right))

  case Ast.expr.BoolOp(op, _) =>
    throw NotImplementedError(s"Nonbinary Boolean operators not supported: $e")

  case Ast.expr.BinOp(left, op, right) =>
    NDist.BOperator(expr2pgcl(left), op2pgcl(op), expr2pgcl(right))

  case Ast.expr.UnaryOp(op, operand) =>
    NDist.UOperator(unaryop2pgcl(op), expr2pgcl(operand))

  case Ast.expr.Lambda(args, body) =>
    throw NotImplementedError(s"Python λ-expressions are not supported!")

  case Ast.expr.IfExp(test, body, orelse) =>
    throw NotImplementedError(s"Python ternary expressions not supported!")

  case Ast.expr.Dict(keys, values) =>
    throw NotImplementedError(s"Python dictionary expressions not supported!")

  case Ast.expr.Set(elts) =>
    throw NotImplementedError(s"Python set expressions not supported!")

  case Ast.expr.ListComp(elt, generators) =>
    throw NotImplementedError(s"Python list comprehensions not supported!")

  case Ast.expr.SetComp(elt, generators) =>
    throw NotImplementedError(s"Python set comprehensions not supported!")

  case Ast.expr.DictComp(key, value, generators) =>
    throw NotImplementedError(s"Python dict comprehensions not supported!")

  case Ast.expr.GeneratorExp(elt, generators) =>
    throw NotImplementedError(s"Python generator expressions not supported!")

  case Ast.expr.Yield(value) =>
    throw NotImplementedError(s"Python yield not supported!")

  case Ast.expr.Compare(left, Seq(op), Seq(right)) =>
    NDist.BOperator(expr2pgcl(left), cmpop2pgcl(op), expr2pgcl(right))

  case _: Ast.expr.Compare =>
    throw NotImplementedError(s"Non-binary comparisons are not supported")
    
  case Ast.expr.Call(
        Ast.expr.Attribute(
          Ast.expr.Name(Ast.identifier("bernoulli"), ctx),
          Ast.identifier("rvs"), _),
        Seq(bias), Seq(), None, None) =>
    Dist.Bernoulli(expr2pgcl(bias))

  // case Ast.expr.Repr(value: expr) => ???

  case _: Ast.expr.Call =>
    NDist.CstI(43)
    // TODO throw NotImplementedError(s"General function calls are not supported")

  case Ast.expr.Num(n: Int) =>
    NDist.CstI(n)

  case Ast.expr.Num(n: BigInt) if n.isValidInt =>
    NDist.CstI(n.toInt)

  case Ast.expr.Num(x: Double) =>
    NDist.CstF(x)

  // case Ast.expr.Str(s: string)    => ??? // need to raw: specify, unicode, etc?
  // // other bools: Option[literals]?

  // // the following expression can appear in assignment context
  // case Ast.expr.Attribute(value: expr, attr: identifier, ctx: expr_context) =>
  //   ???
  // case Ast.expr.Subscript(value: expr, slice: slice, ctx: expr_context) => ???
  case Ast.expr.Name(id, ctx) =>
    NDist.Var(id.name.toId)

  case Ast.expr.List(elts, ctx) =>
    throw NotImplementedError("Lists literals are not supported")

  case Ast.expr.Tuple(elts, ctx) =>
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

val str = unparse(0)(python2pgcl(p))
print(str)

p(4)
