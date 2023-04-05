package probrogs

opaque type Probability = Double
opaque type Id = String

extension (s: String) def toId: Id = s
extension (id: Id) def toString: String = id

enum ROp:
  case Eq, NEq, LEq, Lt, GEq, Gt

enum AOp:
  case Plus, Minus, Mult, Div, Mod, Pow, LShift, RShift, BitOr, BitXor, BitAnd,
    FloorDiv

enum BOp:
  case And, Or

type Op = ROp | AOp | BOp

enum UOp:
  case Not, Minus, Plus, BitNot

enum Stm:
  case Skip
  case Assign(variable: Id, value: Exp)
  case Sample(variable: Id, distribution: Dist)
  case If(cond: Exp, body: Stm, orelse: Stm)
  case While(cond: Exp, body: Stm)
  case Seq(fst: Stm, snd: Stm)
  case Comment(
      msg: String
  ) // for debugging purposes, and preservation of some comments

enum Dist:
  case Bernoulli(bias: Exp)
  case Uniform(left: Exp, right: Exp)

enum NDist:
  case CstI(value: Int)
  case CstF(value: Double)
  case BOperator(left: Exp, ope: Op, right: Exp)
  case UOperator(ope: UOp, right: Exp)
  case Call(name: Id, args: List[Exp])
  case Var(name: Id)

type Exp = Dist | NDist
