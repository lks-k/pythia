// A Scala Parser for benchmark files. Not really a parser, 
// but a converter from fastparser's pythoneparse AST to our PGCL AST.
// Copyright 2023 Andrzej Wąsowski
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
//   http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package probros

import _root_.{pythonparse => PP}

/** Parse a file containing a Python program to pythoneparse abstract syntax */
def parsePythonFile(path: String) = 
  import scala.io.Source
  fastparse
    .parse(Source.fromFile(path).mkString, pythonparse.Statements.file_input)
    .get
    .value

/** Parse a file containing a Python program to PGCL abstract syntax */
def pythonFile2pgcl(path: String): Stm = 
  python2pgcl(parsePythonFile(path))

/** Parse a string containing a Python program to pythoneparse syntax. */ 
def parsePython(s: String) = 
  import fastparse.~
  import fastparse.NoWhitespace.*
  def parseSuite[$: fastparse.P] = 
    pythonparse.Statements.file_input(fastparse.P.current) // ~ fastparse.End
  fastparse.parse(s, parseSuite)
    .get
    .value

/** Parse a string containing a Python program to pgcl syntax. */ 
def python2pgcl(s: String): Stm = 
  python2pgcl(parsePython(s))

/** Parse a string containing a Python expression. */
def pythonExpr2pgcl(s: String): Exp = 
  import fastparse.~
  import fastparse.NoWhitespace.*
  def parseExp[$: fastparse.P] = 
    pythonparse.Expressions.expr(fastparse.P.current) ~ fastparse.End
  val ast = fastparse.parse(s, parseExp)
    .get
    .value
  expr2pgcl(ast)

def python2pgcl(p: Seq[PP.Ast.stmt]): Stm =
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
              PP.Ast.identifier("random"),
              _
            ),
            PP.Ast.identifier(seed),
            _
          ),
          Seq(initial),
          Seq(),
          None,
          None
        )
      ) =>
    Nil // skip random number initialization (np.random.seed(...))

  case _: PP.Ast.stmt.Expr =>
    Nil // ignore expression statements as expressions are pure so far

  case _ =>
    throw NotImplementedError(s"Statement not supported $s")

def stmSeq2stm(ss: Seq[Stm]): Stm =
  ss.foldRight(Stm.Skip)(Stm.Seq.apply)

def stmtList2pgcl(ss: Seq[PP.Ast.stmt]): Stm =
  stmSeq2stm(ss.flatMap(stmt2pgcl))

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
  case n: Int        => NDist.CstI(n)
  case x: Double     => NDist.CstF(x)
  case x: BigDecimal => NDist.CstF(x.toDouble)
  case _ =>
    throw NotImplementedError(s"An unsupported literal type '${a.getClass}'")

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
            List(PP.Ast.expr.Num(bias)),
            Nil,
            None,
            None
          ),
          PP.Ast.identifier("rvs"),
          _
        ),
        Nil,
        Nil,
        None,
        None
      ) =>
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
