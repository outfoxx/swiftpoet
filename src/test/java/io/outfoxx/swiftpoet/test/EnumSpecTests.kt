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

import io.outfoxx.swiftpoet.CASE_ITERABLE
import io.outfoxx.swiftpoet.CodeBlock
import io.outfoxx.swiftpoet.CodeWriter
import io.outfoxx.swiftpoet.EnumerationCaseSpec
import io.outfoxx.swiftpoet.INT
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.STRING
import io.outfoxx.swiftpoet.TupleTypeName
import io.outfoxx.swiftpoet.TypeSpec
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.hasItems
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
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
  @DisplayName("Generates doc before enum definition")
  fun testGenDocs() {
    val testEnum = TypeSpec.enumBuilder("Test")
      .addDoc("this is a comment\n")
      .addEnumCase("a")
      .build()

    val out = StringWriter()
    testEnum.emit(CodeWriter(out))

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
  @DisplayName("Generates attributes before enum definition")
  fun testGenAttrs() {
    val testEnum = TypeSpec.enumBuilder("Test")
      .addAttribute("available", "swift 5.1")
      .addEnumCase("a")
      .build()

    val out = StringWriter()
    testEnum.emit(CodeWriter(out))

    assertThat(
      out.toString(),
      equalTo(
        """
            @available(swift 5.1)
            enum Test {

              case a

            }

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates doc before enum case definitions")
  fun testGenDocOnCases() {
    val testEnum = TypeSpec.enumBuilder("Test")
      .addEnumCase(
        EnumerationCaseSpec.builder("a")
          .addDoc("this is a comment\n")
          .build()
      )
      .build()

    val out = StringWriter()
    testEnum.emit(CodeWriter(out))

    assertThat(
      out.toString(),
      equalTo(
        """
            enum Test {

              /**
               * this is a comment
               */
              case a

            }

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates attributes before enum case definition")
  fun testGenAttrsOnCases() {
    val testEnum = TypeSpec.enumBuilder("Test")
      .addEnumCase(
        EnumerationCaseSpec.builder("a")
          .addAttribute("available", "swift 5.1")
          .build()
      )
      .build()

    val out = StringWriter()
    testEnum.emit(CodeWriter(out))

    assertThat(
      out.toString(),
      equalTo(
        """
            enum Test {

              @available(swift 5.1)
              case a

            }

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates modifiers in order")
  fun testGenModifiersInOrder() {
    val testEnum = TypeSpec.enumBuilder("Test")
      .addModifiers(Modifier.PUBLIC)
      .addEnumCase("a")
      .build()

    val out = StringWriter()
    testEnum.emit(CodeWriter(out))

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
    val testEnum = TypeSpec.enumBuilder("Test")
      .addEnumCase("A", 10)
      .addEnumCase("B", 20)
      .addEnumCase("C", 30)
      .build()

    val out = StringWriter()
    testEnum.emit(CodeWriter(out))

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
    val testEnum = TypeSpec.enumBuilder("Test")
      .addSuperType(INT)
      .addSuperType(CASE_ITERABLE)
      .addEnumCase("A", CodeBlock.of("10"))
      .build()

    val out = StringWriter()
    testEnum.emit(CodeWriter(out))

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
    val testEnum = TypeSpec.enumBuilder("Test")
      .addEnumCase("A", TupleTypeName.of("value" to INT, "" to STRING))
      .addEnumCase("B", INT)
      .build()

    val out = StringWriter()
    testEnum.emit(CodeWriter(out))

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
  @DisplayName("Disallows repeat enumeration case names")
  fun testDisallowRepeatCases() {
    assertThrows(IllegalArgumentException::class.java) {
      TypeSpec.enumBuilder("Test")
        .addEnumCase("A")
        .addEnumCase("A")
        .build()
    }
  }

  @Test
  @DisplayName("toBuilder copies all fields")
  fun testToBuilder() {
    val testEnumBldr = TypeSpec.enumBuilder("Test")
      .addDoc("this is a comment\n")
      .addModifiers(Modifier.PRIVATE)
      .addEnumCase("A", "10")
      .build()
      .toBuilder()

    assertThat(testEnumBldr.name, equalTo("Test"))
    assertThat(testEnumBldr.doc.formatParts, hasItems("this is a comment\n"))
    assertThat(testEnumBldr.kind.modifiers, hasItems(Modifier.PRIVATE))
    assertThat(testEnumBldr.enumCases.map { it.name }, hasItems("A"))
  }

  @Test
  @DisplayName("case toBuilder copies all fields")
  fun testCaseToBuilder() {
    val testEnumBldr = EnumerationCaseSpec.builder("A", "a-value")
      .addDoc("this is a comment\n")
      .addAttribute("available", "swift 5.1")
      .build()
      .toBuilder()

    assertThat(testEnumBldr.name, equalTo("A"))
    assertThat(testEnumBldr.doc.formatParts, hasItems("this is a comment\n"))
    assertThat(testEnumBldr.attributes.map { it.identifier.toString() }, hasItems("available"))
    assertThat(testEnumBldr.typeOrConstant as? CodeBlock, equalTo(CodeBlock.of("%S", "a-value")))
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
