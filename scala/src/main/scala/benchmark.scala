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

package probrogs
package benchmark

object path:
  lazy val flip = "benchmark/flip.py"
  lazy val all = List(flip)

object python:
  lazy val flip = all.get(path.flip)
  lazy val all = path.all.zip(path.all.map(parsePythonFile)).toMap

object pgcl: 
  lazy val flip = all.get(path.flip)
  lazy val all = python.all.view.mapValues(stmtList2pgcl)

object unparsed:
  lazy val flip = all.get(path.flip)
  lazy val all = pgcl.all.view.mapValues(unparse(0))



