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

import io.outfoxx.swiftpoet.*
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.hasItems
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.StringWriter


@DisplayName("(enum) TypeSpec Tests")
class EnumSpecTests {

  @Test
  @DisplayName("Escapes names which are keywords")
  fun escapeName() {
    val testClass = TypeSpec.enumBuilder("Test")
            .addEnumCase("extension")
            .build()
    val out = StringWriter()
    testClass.emit(CodeWriter(out))

    assertThat(
            out.toString(),
            equalTo(
                    """
            enum Test {

              case `extension`

            }

          """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates JavaDoc at before class definition")
  fun testGenJavaDoc() {
    val testClass = TypeSpec.enumBuilder("Test")
       .addKdoc("this is a comment\n")
       .addEnumCase("a")
       .build()

    val out = StringWriter()
    testClass.emit(CodeWriter(out))

    assertThat(
       out.toString(),
       equalTo(
          """
            /**
             * this is a comment
             */
            enum Test {

              case a

            }

          """.trimIndent()
       )
    )
  }

  @Test
  @DisplayName("Generates modifiers in order")
  fun testGenModifiersInOrder() {
    val testClass = TypeSpec.enumBuilder("Test")
       .addModifiers(Modifier.PUBLIC)
       .addEnumCase("a")
       .build()

    val out = StringWriter()
    testClass.emit(CodeWriter(out))

    assertThat(
       out.toString(),
       equalTo(
          """
            public enum Test {

              case a

            }

          """.trimIndent()
       )
    )
  }

  @Test
  @DisplayName("Generates formatted constants")
  fun testGenConstants() {
    val testClass = TypeSpec.enumBuilder("Test")
       .addEnumCase("A", "10")
       .addEnumCase("B", "20")
       .addEnumCase("C", "30")
       .build()

    val out = StringWriter()
    testClass.emit(CodeWriter(out))

    assertThat(
       out.toString(),
       equalTo(
          """
            enum Test {

              case A = 10
              case B = 20
              case C = 30

            }

          """.trimIndent()
       )
    )
  }


  @Test
  @DisplayName("Generates raw & interface")
  fun testGenRawInterfaces() {
    val testClass = TypeSpec.enumBuilder("Test")
       .addSuperType(INT)
       .addSuperType(CASE_ITERABLE)
       .addEnumCase("A", "10")
       .build()

    val out = StringWriter()
    testClass.emit(CodeWriter(out))

    assertThat(
       out.toString(),
       equalTo(
          """
            enum Test : Swift.Int, Swift.CaseIterable {

              case A = 10

            }

          """.trimIndent()
       )
    )
  }


  @Test
  @DisplayName("Generates formatted associated values")
  fun testGenAssociatedValues() {
    val testClass = TypeSpec.enumBuilder("Test")
       .addEnumCase("A", TupleTypeName.of("value" to INT, "" to STRING))
       .addEnumCase("B", INT)
       .build()

    val out = StringWriter()
    testClass.emit(CodeWriter(out))

    assertThat(
       out.toString(),
       equalTo(
          """
            enum Test {

              case A(value: Swift.Int, Swift.String)
              case B(Swift.Int)

            }

          """.trimIndent()
       )
    )
  }
  @Test
  @DisplayName("toBuilder copies all fields")
  fun testToBuilder() {
    val testEnumBldr = TypeSpec.enumBuilder("Test")
       .addKdoc("this is a comment\n")
       .addModifiers(Modifier.PRIVATE)
       .addEnumCase("A", "10")
       .build()
       .toBuilder()

    assertThat(testEnumBldr.name, equalTo("Test"))
    assertThat(testEnumBldr.kdoc.formatParts, hasItems("this is a comment\n"))
    assertThat(testEnumBldr.kind.modifiers, hasItems(Modifier.PRIVATE))
    assertThat(testEnumBldr.enumCases.keys, hasItems("A"))
  }

  @Test
  @DisplayName("Allow enums with no cases")
  fun testNoCases() {
    val testClass = TypeSpec.enumBuilder("Test")
      .build()
    val out = StringWriter()
    testClass.emit(CodeWriter(out))

    assertThat(
      out.toString(),
      equalTo(
        """
            enum Test {
            }

          """.trimIndent()
      )
    )
  }

}
