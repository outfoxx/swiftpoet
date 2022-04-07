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

import io.outfoxx.swiftpoet.AttributeSpec.Companion.available
import io.outfoxx.swiftpoet.CodeWriter
import io.outfoxx.swiftpoet.ComposedTypeName.Companion.composed
import io.outfoxx.swiftpoet.DeclaredTypeName.Companion.typeName
import io.outfoxx.swiftpoet.FunctionSpec.Companion.abstractBuilder
import io.outfoxx.swiftpoet.INT
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.PropertySpec
import io.outfoxx.swiftpoet.STRING
import io.outfoxx.swiftpoet.TypeAliasSpec
import io.outfoxx.swiftpoet.TypeName
import io.outfoxx.swiftpoet.TypeSpec
import io.outfoxx.swiftpoet.TypeVariableName.Companion.bound
import io.outfoxx.swiftpoet.TypeVariableName.Companion.typeVariable
import io.outfoxx.swiftpoet.tag
import io.outfoxx.swiftpoet.toImmutableSet
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.hasItems
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.StringWriter

@DisplayName("(protocol) TypeSpec Tests")
class ProtocolSpecTests {

  @Test
  @DisplayName("Tags on builders can be retrieved on builders and built specs")
  fun testTags() {
    val testProtoBuilder = TypeSpec.protocolBuilder("Test")
      .tag(5)
    val testProto = testProtoBuilder.build()

    assertThat(testProtoBuilder.tags[Integer::class] as? Int, equalTo(5))
    assertThat(testProto.tag(), equalTo(5))
  }

  @Test
  @DisplayName("Generates documentation at before protocol definition")
  fun testGenDoc() {
    val testProto = TypeSpec.protocolBuilder("Test")
      .addDoc("this is a comment\n")
      .build()

    val out = StringWriter()
    testProto.emit(CodeWriter(out))

    assertThat(
      out.toString(),
      equalTo(
        """
            /**
             * this is a comment
             */
            protocol Test {
            }

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates modifiers in order")
  fun testGenModifiersInOrder() {
    val testProto = TypeSpec.protocolBuilder("Test")
      .addModifiers(Modifier.PUBLIC)
      .build()

    val out = StringWriter()
    testProto.emit(CodeWriter(out))

    assertThat(
      out.toString(),
      equalTo(
        """
            public protocol Test {
            }

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates type variables")
  fun testGenTypeVars() {
    val testProto = TypeSpec.protocolBuilder("Test")
      .addTypeVariable(
        typeVariable("X", bound(".Test2"))
      )
      .addTypeVariable(
        typeVariable("Y", bound(".Test3"))
      )
      .build()

    val out = StringWriter()
    testProto.emit(CodeWriter(out))

    assertThat(
      out.toString(),
      equalTo(
        """
            protocol Test<X : Test2, Y : Test3> {
            }

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates associated types")
  fun testGenAssociatedTypes() {
    val testProto = TypeSpec.protocolBuilder("Test")
      .addAssociatedType(typeVariable("Element", bound(".Collection")))
      .build()

    val out = StringWriter()
    testProto.emit(CodeWriter(out))

    assertThat(
      out.toString(),
      equalTo(
        """
            protocol Test {

              associatedtype Element : Collection

            }

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates super types")
  fun testGenSuperTypes() {
    val testProto = TypeSpec.protocolBuilder("Test")
      .addSuperType(typeName(".Test2"))
      .addSuperType(typeName(".Test3"))
      .build()

    val out = StringWriter()
    testProto.emit(CodeWriter(out))

    assertThat(
      out.toString(),
      equalTo(
        """
            protocol Test : Test2, Test3 {
            }

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates super types, constrained to class")
  fun testGenSuperTypesWithClassFirst() {
    val testProto = TypeSpec.protocolBuilder("Test")
      .addSuperType(typeName(".Test2"))
      .addSuperType(typeName(".Test3"))
      .constrainToClass()
      .build()

    val out = StringWriter()
    testProto.emit(CodeWriter(out))

    assertThat(
      out.toString(),
      equalTo(
        """
            protocol Test : class, Test2, Test3 {
            }

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates type vars & super interfaces properly formatted")
  fun testGenTypeVarsAndSuperInterfacesFormatted() {
    val testProto = TypeSpec.protocolBuilder("Test")
      .addTypeVariable(
        typeVariable("Y", bound(composed(".Test3", ".Test4")))
      )
      .addSuperType(typeName(".Test2"))
      .addSuperType(typeName(".Test3"))
      .addSuperType(typeName(".Test4"))
      .build()

    val out = StringWriter()
    testProto.emit(CodeWriter(out))

    assertThat(
      out.toString(),
      equalTo(
        """
            protocol Test<Y : Test3 & Test4> : Test2, Test3, Test4 {
            }

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates type vars with multiple bounds & super interfaces properly formatted")
  fun testGenTypeVarsWithMultipleBoundAndSuperInterfacesFormatted() {
    val testProto = TypeSpec.protocolBuilder("Test")
      .addTypeVariable(
        typeVariable("X", bound(".Test4"), bound(".Test5"))
      )
      .addTypeVariable(
        typeVariable("Y", bound(composed(".Test3", ".Test4")))
      )
      .addSuperType(typeName(".Test2"))
      .addSuperType(typeName(".Test3"))
      .addSuperType(typeName(".Test4"))
      .build()

    val out = StringWriter()
    testProto.emit(CodeWriter(out))

    assertThat(
      out.toString(),
      equalTo(
        """
            protocol Test<X, Y> : Test2, Test3, Test4 where X : Test4, X : Test5, Y : Test3 & Test4 {
            }

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates property declarations")
  fun testGenProperties() {
    val testProto = TypeSpec.protocolBuilder("Test")
      .addProperty(
        PropertySpec.abstractBuilder("value", INT, Modifier.PRIVATE)
          .abstractGetter()
          .build()
      )
      .addProperty(
        PropertySpec.abstractBuilder("value2", INT, Modifier.PUBLIC)
          .abstractGetter()
          .abstractSetter()
          .build()
      )
      .build()

    val out = StringWriter()
    testProto.emit(CodeWriter(out))

    assertThat(
      out.toString(),
      equalTo(
        """
            protocol Test {

              private var value: Swift.Int { get }
              public var value2: Swift.Int { get set }

            }

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates method declarations")
  fun testGenMethods() {
    val testProto = TypeSpec.protocolBuilder("Test")
      .addFunction(
        abstractBuilder("test1")
          .addModifiers(Modifier.PRIVATE)
          .build()
      )
      .addFunction(
        abstractBuilder("test2")
          .addModifiers(Modifier.PUBLIC)
          .build()
      )
      .build()

    val out = StringWriter()
    testProto.emit(CodeWriter(out))

    assertThat(
      out.toString(),
      equalTo(
        """
            protocol Test {

              private func test1()
              public func test2()

            }

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("toBuilder copies all fields")
  fun testToBuilder() {
    val testProtoBldr = TypeSpec.protocolBuilder("Test")
      .addDoc("this is a comment\n")
      .addAttribute(available("iOS" to "9"))
      .addModifiers(Modifier.PUBLIC)
      .addTypeVariable(
        typeVariable("X", bound(".Test2"))
      )
      .addSuperType(typeName(".Test3"))
      .addProperty("value2", STRING, Modifier.PUBLIC)
      .addFunction(
        abstractBuilder("test1")
          .build()
      )
      .addAssociatedType(typeVariable("Element"))
      .build()
      .toBuilder()

    assertThat(testProtoBldr.doc.formatParts, hasItems("this is a comment\n"))
    assertThat(testProtoBldr.attributes.map { it.identifier.toString() }, hasItems("available"))
    assertThat(testProtoBldr.kind.modifiers.toImmutableSet(), equalTo(setOf(Modifier.PUBLIC)))
    assertThat(testProtoBldr.typeVariables.size, equalTo(1))
    assertThat(testProtoBldr.superTypes, hasItems<TypeName>(typeName(".Test3")))
    assertThat(testProtoBldr.propertySpecs.map { it.name }, hasItems("value2"))
    assertThat(testProtoBldr.functionSpecs.map { it.name }, hasItems("test1"))
    assertThat(testProtoBldr.associatedTypes.map { it.name }, hasItems("Element"))
  }

  @Test
  @DisplayName("Protocols cannot contain nested types")
  fun testAddNestedTypeToProtocol() {
    val exception = assertThrows<IllegalStateException> {
      TypeSpec.protocolBuilder("Test")
        .addType(TypeSpec.protocolBuilder("Nested").build())
        .build()
    }
    assertThat(exception.message, equalTo("Test is a protocol, it can only contain type aliases as nested types"))
  }

  @Test
  @DisplayName("Protocols cannot be added as nested types")
  fun testAddProtocolAsNestedType() {
    val exception = assertThrows<IllegalStateException> {
      TypeSpec.structBuilder("Test")
        .addType(TypeSpec.protocolBuilder("Nested").build())
        .build()
    }
    assertThat(exception.message, equalTo("Nested is a protocol, it cannot be added as a nested type"))
  }

  @Test
  @DisplayName("Type aliases can be added to protocols")
  fun testAddTypeAliasToProtocol() {
    val testProto = TypeSpec.protocolBuilder("Test")
      .addType(TypeAliasSpec.builder("NestedAlias", STRING).build())
      .build()

    val out = StringWriter()
    testProto.emit(CodeWriter(out))

    assertThat(
      out.toString(),
      equalTo(
        """
            protocol Test {

              typealias NestedAlias = Swift.String

            }

        """.trimIndent()
      )
    )
  }
}
