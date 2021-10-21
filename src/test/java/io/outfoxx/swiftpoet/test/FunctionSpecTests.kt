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

import io.outfoxx.swiftpoet.AttributeSpec.Companion.DISCARDABLE_RESULT
import io.outfoxx.swiftpoet.AttributeSpec.Companion.ESCAPING
import io.outfoxx.swiftpoet.CodeWriter
import io.outfoxx.swiftpoet.ComposedTypeName.Companion.composed
import io.outfoxx.swiftpoet.DeclaredTypeName.Companion.typeName
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.FunctionTypeName
import io.outfoxx.swiftpoet.INT
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.ParameterSpec
import io.outfoxx.swiftpoet.STRING
import io.outfoxx.swiftpoet.TypeAliasSpec
import io.outfoxx.swiftpoet.TypeName
import io.outfoxx.swiftpoet.TypeVariableName.Companion.bound
import io.outfoxx.swiftpoet.TypeVariableName.Companion.typeVariable
import io.outfoxx.swiftpoet.VOID
import io.outfoxx.swiftpoet.tag
import io.outfoxx.swiftpoet.toImmutableSet
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.hasItems
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.StringWriter

@DisplayName("FunctionSpec Tests")
class FunctionSpecTests {

  @Test
  @DisplayName("Generates correct closure parameters")
  fun testGenerateClosureParameters() {
    val closureTypeName = FunctionTypeName.get(listOf(ParameterSpec.unnamed(STRING), ParameterSpec.unnamed(INT)), STRING)
    val testFunc = FunctionSpec.builder("test")
      .addParameter(
        ParameterSpec.builder("closure", closureTypeName)
          .build()
      )
      .build()

    val out = StringWriter()
    testFunc.emit(CodeWriter(out), null, setOf())

    assertThat(
      out.toString(),
      equalTo(
        """
            func test(closure: (Swift.String, Swift.Int) -> Swift.String) {
            }

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates escaping closure parameters")
  fun testGenerateEscapingClosureParameters() {
    val closureTypeName = FunctionTypeName.get(listOf(ParameterSpec.unnamed(STRING), ParameterSpec.unnamed(INT)), STRING)
    val testFunc = FunctionSpec.builder("test")
      .addParameter(
        ParameterSpec.builder("closure", closureTypeName)
          .addAttribute(ESCAPING)
          .build()
      )
      .build()

    val out = StringWriter()
    testFunc.emit(CodeWriter(out), null, setOf())

    assertThat(
      out.toString(),
      equalTo(
        """
            func test(closure: @escaping (Swift.String, Swift.Int) -> Swift.String) {
            }

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Tags on builders can be retrieved on builders and built specs")
  fun testTags() {
    val testFuncBuilder = FunctionSpec.builder("Test")
      .tag(5)
    val testFunc = testFuncBuilder.build()

    assertThat(testFuncBuilder.tags[Integer::class] as? Int, equalTo(5))
    assertThat(testFunc.tag(), equalTo(5))
  }

  @Test
  @DisplayName("Generates local type definitions before code")
  fun testGenLocalTypes() {
    val testFunc = FunctionSpec.builder("test")
      .addLocalType(TypeAliasSpec.builder("LocalData", typeName("Swift.Data")).build())
      .addStatement("print(%S)", "local types")
      .build()

    val out = StringWriter()
    testFunc.emit(CodeWriter(out), null, setOf())

    assertThat(
      out.toString(),
      equalTo(
        """
            func test() {

              typealias LocalData = Swift.Data

              print("local types")
            }

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates documentation before function definition")
  fun testGenDoc() {
    val testFunc = FunctionSpec.builder("test")
      .addDoc("this is a comment\n")
      .build()

    val out = StringWriter()
    testFunc.emit(CodeWriter(out), null, setOf())

    assertThat(
      out.toString(),
      equalTo(
        """
            /**
             * this is a comment
             */
            func test() {
            }

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates decorators formatted")
  fun testGenDecorators() {
    val testFunc = FunctionSpec.builder("test")
      .addAttribute(DISCARDABLE_RESULT)
      .build()

    val out = StringWriter()
    testFunc.emit(CodeWriter(out), null, setOf())

    assertThat(
      out.toString(),
      equalTo(
        """
            @discardableResult
            func test() {
            }

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates modifiers in order")
  fun testGenModifiersInOrder() {
    val testClass = FunctionSpec.builder("test")
      .addModifiers(Modifier.PRIVATE, Modifier.MUTATING, Modifier.FINAL)
      .addCode("")
      .build()

    val out = StringWriter()
    testClass.emit(CodeWriter(out), null, setOf())

    assertThat(
      out.toString(),
      equalTo(
        """
            private mutating final func test() {
            }

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates type variables")
  fun testGenTypeVars() {
    val testClass = FunctionSpec.builder("test")
      .addTypeVariable(
        typeVariable("X", bound(".Test2"))
      )
      .addTypeVariable(
        typeVariable("Y", bound(composed(".Test3", ".Test4")))
      )
      .build()

    val out = StringWriter()
    testClass.emit(CodeWriter(out), null, setOf())

    assertThat(
      out.toString(),
      equalTo(
        """
            func test<X : Test2, Y : Test3 & Test4>() {
            }

        """.trimIndent()
      )
    )
  }

    @Test
    @DisplayName("Generates type variables with multiple bounds")
    fun testGenTypeVarsWithMultipleBounds() {
        val testClass = FunctionSpec.builder("test")
            .addTypeVariable(
                typeVariable("T", bound(".Test2"), bound(".Test3"))
            )
            .addTypeVariable(
                typeVariable("X", bound(".Test2"))
            )
            .addTypeVariable(
                typeVariable("Y", bound(composed(".Test3", ".Test4")))
            )
            .build()

        val out = StringWriter()
        testClass.emit(CodeWriter(out), null, setOf())

        assertThat(
            out.toString(),
            equalTo(
                """
            func test<T, X, Y>() where T : Test2, T : Test3, X : Test2, Y : Test3 & Test4 {
            }
    
        """.trimIndent()
            )
        )
    }


  @Test
  @DisplayName("Generates return type")
  fun testGenReturnType() {
    val testClass = FunctionSpec.builder("test")
      .returns(typeName(".Value"))
      .build()

    val out = StringWriter()
    testClass.emit(CodeWriter(out), null, setOf())

    assertThat(
      out.toString(),
      equalTo(
        """
            func test() -> Value {
            }

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates no return type when void")
  fun testGenNoReturnTypeForVoid() {
    val testClass = FunctionSpec.builder("test")
      .returns(VOID)
      .build()

    val out = StringWriter()
    testClass.emit(CodeWriter(out), null, setOf())

    assertThat(
      out.toString(),
      equalTo(
        """
            func test() {
            }

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates no return type when not set")
  fun testGenNoReturnType() {
    val testClass = FunctionSpec.builder("test")
      .build()

    val out = StringWriter()
    testClass.emit(CodeWriter(out), null, setOf())

    assertThat(
      out.toString(),
      equalTo(
        """
            func test() {
            }

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates parameters")
  fun testGenParameters() {
    val testClass = FunctionSpec.builder("test")
      .addParameter("withB", "b", STRING)
      .build()

    val out = StringWriter()
    testClass.emit(CodeWriter(out), null, setOf())

    assertThat(
      out.toString(),
      equalTo(
        """
            func test(withB b: Swift.String) {
            }

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates parameters with variadic parameter")
  fun testGenParametersRest() {
    val testClass = FunctionSpec.builder("test")
      .addParameter("b", STRING)
      .addParameter(
        ParameterSpec.builder("c", STRING)
          .variadic(true)
          .build()
      )
      .build()

    val out = StringWriter()
    testClass.emit(CodeWriter(out), null, setOf())

    assertThat(
      out.toString(),
      equalTo(
        """
            func test(b: Swift.String, c: Swift.String...) {
            }

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates parameters with default values")
  fun testGenParametersDefaults() {
    val testClass = FunctionSpec.builder("test")
      .addParameter(
        ParameterSpec.builder("withA", "a", INT)
          .defaultValue("10")
          .build()
      )
      .build()

    val out = StringWriter()
    testClass.emit(CodeWriter(out), null, setOf())

    assertThat(
      out.toString(),
      equalTo(
        """
            func test(withA a: Swift.Int = 10) {
            }

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates parameter modifiers")
  fun testGenParameterDecorators() {
    val testClass = FunctionSpec.builder("test")
      .addParameter(
        ParameterSpec.builder("a", INT)
          .addModifiers(Modifier.INOUT)
          .build()
      )
      .build()

    val out = StringWriter()
    testClass.emit(CodeWriter(out), null, setOf())

    assertThat(
      out.toString(),
      equalTo(
        """
            func test(a: inout Swift.Int) {
            }

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("toBuilder copies all fields")
  fun testToBuilder() {
    val testFuncBlder = FunctionSpec.builder("Test")
      .addDoc("this is a comment\n")
      .addAttribute(DISCARDABLE_RESULT)
      .addModifiers(Modifier.PUBLIC)
      .addTypeVariable(
        typeVariable("X", bound(".Test2"))
      )
      .addParameter("a", STRING)
      .returns(STRING)
      .addCode("val;\n")
      .build()
      .toBuilder()

    assertThat(testFuncBlder.doc.formatParts, hasItems("this is a comment\n"))
    assertThat(testFuncBlder.attributes, hasItems(DISCARDABLE_RESULT))
    assertThat(testFuncBlder.modifiers.toImmutableSet(), equalTo(setOf(Modifier.PUBLIC)))
    assertThat(testFuncBlder.typeVariables, hasItems(typeVariable("X", bound(".Test2"))))
    assertThat(testFuncBlder.returnType, equalTo<TypeName>(STRING))
    assertThat(testFuncBlder.parameters, hasItems(ParameterSpec.builder("a", STRING).build()))
    assertThat(testFuncBlder.body.formatParts, hasItems("val;\n"))
  }
}
