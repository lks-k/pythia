/*
  Expose the benchmark programs as Scala values
  Copyright 2023 Andrzej Wąsowski, IT University of Copenhagen
  
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  
    http://www.apache.org/licenses/LICENSE-2.0
  
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package probros
package benchmark

case class Benchmark(name: String): 
  lazy val text = this.load(this.name)
  lazy val python = parsePython(this.text)
  lazy val pgcl = stmtList2pgcl(this.python)
  lazy val unparsed = unparse(0)(this.pgcl)

  private def load (name: String): String =
    val r = this.getClass.getClassLoader.getResourceAsStream(name+".py")
    java.util.Scanner(r).useDelimiter("\\A").next

val flip = Benchmark("flip")

trait PyFiles[T]: 

  val benchmarks: List[Benchmark] = List(
    benchmark.flip
  )

  def flip: T = this.all(benchmark.flip.name)

  def all: Map[String, T]
  def apply(name: String): T = this.all(name)



object name extends PyFiles[String]:
  lazy val all = this.benchmarks.map { b => (b.name, b.name) }.toMap

object text extends PyFiles[String]:
  lazy val all = this.benchmarks.map { b => (b.name, b.text) }.toMap

object python extends PyFiles[Seq[pythonparse.Ast.stmt]]:
  lazy val all = this.benchmarks.map { b => (b.name, b.python) }.toMap

object pgcl extends PyFiles[Stm]: 
  lazy val all = this.benchmarks.map { b => (b.name, b.pgcl) }.toMap

object unparsed extends PyFiles[String]:
  lazy val all = this.benchmarks.map { b => (b.name, b.unparsed) }.toMap
