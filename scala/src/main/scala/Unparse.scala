// A pretty printer from pgcl AST to Python concrete syntax
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

import scala.annotation.targetName

val HSKIP = 4

extension (s: String) 
  def indent(n: Int): String = 
    val spaces = " " * n
    spaces + s.stripMargin.replaceAll("\n", "\n" + spaces)

def unparse(indentation: Int)(s: Stm): String = s match
  case Stm.Skip =>
    "skip".indent(indentation)

  case Stm.Assign(id, value) =>
    s"${id} = ${unparse(value)}".indent(indentation)

  case Stm.Sample(id, distribution) =>
    s"${id} = ${unparse(distribution)}".indent(indentation)

  case Stm.If(cond, body, orelse) =>
    s"""if ${unparse(cond)}:
       |${unparse(HSKIP)(body)}
       |else:
       |${unparse(HSKIP)(orelse)}""".indent(indentation)

  case Stm.While(cond, body) =>
    s"""while ${unparse(cond)}:
       |${unparse(HSKIP)(body)}""".indent(indentation) 

  case Stm.Seq(pred, succ) =>
    s"""${unparse(0)(pred)}
    |${unparse(0)(succ)}""".indent(indentation) 

  case Stm.Comment(str) =>
    s"'''$str'''".indent(indentation)

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
