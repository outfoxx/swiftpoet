/*
 * Copyright 2018 Outfox, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.outfoxx.swiftpoet.test

import io.outfoxx.swiftpoet.CodeBlock
import io.outfoxx.swiftpoet.CodeWriter
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.StringWriter

class CodeBlockTests {

  @Test
  @DisplayName("Generates 'if' control flow with indent")
  fun testGenIfControlFlowWithIndent() {

    val code = CodeBlock.builder()
       .beginControlFlow("if", "x == %L", 5)
       .addStatement("print(\"It's five!\")")
       .endControlFlow("if")
       .build()

    val out = StringWriter()
    CodeWriter(out).emitCode(code)

    MatcherAssert.assertThat(
       out.toString(),
       CoreMatchers.equalTo(
          """
            if x == 5 {
              print("It's five!")
            }
            
          """.trimIndent()
       )
    )
  }

  @Test
  @DisplayName("Generates 'switch' control flow without indent")
  fun testGenSwitchControlFlowWithoutIndent() {

    val code = CodeBlock.builder()
       .beginControlFlow("switch", "x")
       .addStatement("case %L: print(\"It's five!\")", 5)
       .endControlFlow("switch")
       .build()

    val out = StringWriter()
    CodeWriter(out).emitCode(code)

    MatcherAssert.assertThat(
       out.toString(),
       CoreMatchers.equalTo(
          """
            switch x {
            case 5: print("It's five!")
            }
            
          """.trimIndent()
       )
    )
  }

}
