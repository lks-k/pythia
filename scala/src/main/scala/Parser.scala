package probrogs

val TODO = 42
// import org.parboiled2.*
// import org.scalaparser
// import probrogs.Stm.*, probrogs.Dist.*, probrogs.NDist.*
// 
// class PyParse (val input: ParserInput)  
//   extends Parser, scalaparser.L3_Literals:
// 
//   def stmR = rule {
//     importR | seedR | ifR | assignR | whileR | sampleR
//   }
// 
//   private def importR = rule { "import" }
// 
//   def seedR = ???
// 
//   def ifR = ???
// 
//   def assignR = ???
// 
//   def whileR = ???
// 
//   def sampleR = ???
// 
//   // expressions
// 
//   def expR = rule { cstIR }
// 
//   lazy val cstIR = rule { Number ~> { CstI(_) } }
// 
//   // tokens
//   
//   // lazy val Double = rule {
//   //   // "(\+|-)?[0-9]+(\.[0-9]+((e|E)(-|\+)?[0-9]+)?)?" ~> { _.toDouble } 
//   // }
// 
//   // TODO: check if not supported by parboiled builtins
//   lazy val Number = rule { capture(Digits) ~> { _.toInt } }
// 
//   // TODO: check if not supported by parboiled builtins
//   lazy val Digits = rule { oneOrMore(CharPredicate.Digit) }
