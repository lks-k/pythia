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

trait PyFiles[T]: 
  val _flip = "flip"

  val names = List(_flip)

  def flip = this.all(_flip)

  def all: Map[String, T]

  def apply(name: String): T = 
    this.all(name)

  protected def load (name: String): String =
    val r = this.getClass.getClassLoader.getResourceAsStream(name)
    java.util.Scanner(r).useDelimiter("\\A").next

object files extends PyFiles[String]:
  lazy val all = names.map { n => (n, n + ".py") }.toMap

object text extends PyFiles[String]:
  lazy val all = files.all.view.mapValues(load).toMap

object python extends PyFiles[Seq[pythonparse.Ast.stmt]]:
  lazy val all = text.all.view.mapValues(parsePython).toMap

object pgcl extends PyFiles[Stm]: 
  lazy val all = python.all.view.mapValues(stmtList2pgcl).toMap

object unparsed extends PyFiles[String]:
  lazy val all = pgcl.all.view.mapValues(unparse(0)).toMap
