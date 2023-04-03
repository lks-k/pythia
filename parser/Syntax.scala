package probrogs

opaque type Probability = Double
opaque type Id = String 

enum ROp: 
  case Eq, LEq, Lt, GEq, Gt

enum UOp: 
  case Not, Minus

enum Stm:
  case Skip
  case If(cond: Exp, satisifed: Stm, violated: Stm)
  case Assign(variable: Id,  value: Exp)
  case While(cond: Exp, body: Stm)
  case Seq(fst: Stm, snd: Stm)
  case Sample(variable: Id, distribution: Dist)

enum Dist:
  case Bernoulli(bias: Probability)
  case Uniform(left: Exp, right: Exp)
  
enum NDist:
  case CstI(value: Int)
  case CstF(value: Double)
  case BOperator(left: Exp, ope: ROp, right: Exp)
  case UOperator(ope: UOp, right: Exp)
  case Call(name: Id, args: List[Exp])

type Exp = Dist | NDist
