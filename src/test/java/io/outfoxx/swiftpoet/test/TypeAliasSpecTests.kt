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
import io.outfoxx.swiftpoet.DeclaredTypeName.Companion.typeName
import io.outfoxx.swiftpoet.TypeVariableName.Companion.bound
import io.outfoxx.swiftpoet.TypeVariableName.Companion.typeVariable
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.hasItems
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.StringWriter


@DisplayName("TypeAliasSpec Tests")
class TypeAliasSpecTests {

  @Test
  @DisplayName("Generates JavaDoc at before class definition")
  fun testGenJavaDoc() {
    val testAlias = TypeAliasSpec.builder("MyNumber", INT)
       .addKdoc("this is a comment\n")
       .build()

    val out = StringWriter()
    testAlias.emit(CodeWriter(out))

    assertThat(
       out.toString(),
       equalTo(
          """
            /**
             * this is a comment
             */
            typealias MyNumber = Swift.Int

          """.trimIndent()
       )
    )
  }

  @Test
  @DisplayName("Generates modifiers in order")
  fun testGenModifiersInOrder() {
    val testAlias = TypeAliasSpec.builder("MyNumber", INT)
       .addModifiers(Modifier.PUBLIC)
       .build()

    val out = StringWriter()
    testAlias.emit(CodeWriter(out))

    assertThat(
       out.toString(),
       equalTo(
          """
            public typealias MyNumber = Swift.Int

          """.trimIndent()
       )
    )
  }


  @Test
  @DisplayName("Generates simple alias")
  fun testSimpleAlias() {
    val testAlias = TypeAliasSpec.builder("MyNumber", INT)
       .build()

    val out = StringWriter()
    testAlias.emit(CodeWriter(out))

    assertThat(
       out.toString(),
       equalTo(
          """
            typealias MyNumber = Swift.Int

          """.trimIndent()
       )
    )
  }

  @Test
  @DisplayName("Generates generic alias")
  fun testGenericAlias() {
    val typeVar = typeVariable("A", bound(typeName(".Test")))
    val testAlias = TypeAliasSpec.builder("StringSet", SET.parameterizedBy(STRING, typeVar))
       .addTypeVariable(typeVar)
       .build()

    val out = StringWriter()
    testAlias.emit(CodeWriter(out))

    assertThat(
       out.toString(),
       equalTo(
          """
            typealias StringSet<A : Test> = Swift.Set<Swift.String, A>

          """.trimIndent()
       )
    )
  }

  @Test
  @DisplayName("toBuilder copies all fields")
  fun testToBuilder() {
    val testAliasBldr = TypeAliasSpec.builder("Test", INT)
       .addKdoc("this is a comment\n")
       .addModifiers(Modifier.PUBLIC)
       .addTypeVariable(typeVariable("A", bound(typeName(".Test"))))
       .build()
       .toBuilder()

    assertThat(testAliasBldr.name, equalTo("Test"))
    assertThat(testAliasBldr.type, equalTo<TypeName>(INT))
    assertThat(testAliasBldr.kdoc.formatParts, hasItems("this is a comment\n"))
    assertThat(testAliasBldr.modifiers, hasItems(Modifier.PUBLIC))
    assertThat(testAliasBldr.typeVariables.map { it.name }, hasItems("A"))
  }

}
