package probrogs

import org.parboiled2.*
import probrogs.Stm.*, probrogs.Dist.*, probrogs.NDist.*

class PyParse (val input: ParserInput)  extends Parser:

  def stmR = rule {
    importR | seedR | ifR | assignR | whileR | sampleR
  }

  private def importR = rule { "import" }

  def seedR = ???

  def ifR = ???

  def assignR = ???

  def whileR = ???

  def sampleR = ???

  // expressions

  def expR = rule { cstIR }

  lazy val cstIR = rule { Number ~> { CstI(_) } }

  // tokens

  // TODO: check if not supported by parboiled builtins
  lazy val Number = rule { capture(Digits) ~> { _.toInt } }

  // TODO: check if not supported by parboiled builtins
  lazy val Digits = rule { oneOrMore(CharPredicate.Digit) }
