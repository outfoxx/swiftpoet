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

import io.outfoxx.swiftpoet.AttributeSpec
import io.outfoxx.swiftpoet.BOOL
import io.outfoxx.swiftpoet.CodeWriter
import io.outfoxx.swiftpoet.ComposedTypeName.Companion.composed
import io.outfoxx.swiftpoet.DeclaredTypeName.Companion.typeName
import io.outfoxx.swiftpoet.FLOAT
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.INT
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.PropertySpec
import io.outfoxx.swiftpoet.STRING
import io.outfoxx.swiftpoet.TypeAliasSpec
import io.outfoxx.swiftpoet.TypeName
import io.outfoxx.swiftpoet.TypeSpec
import io.outfoxx.swiftpoet.TypeVariableName
import io.outfoxx.swiftpoet.TypeVariableName.Bound.Constraint.SAME_TYPE
import io.outfoxx.swiftpoet.tag
import io.outfoxx.swiftpoet.toImmutableSet
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.hasItems
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.StringWriter

@DisplayName("(actor) TypeSpec Tests")
class ActorSpecTests {

  @Test
  @DisplayName("Tags on builders can be retrieved on builders and built specs")
  fun testTags() {
    val testActorBuilder = TypeSpec.actorBuilder("Test")
      .tag(5)
    val testActor = testActorBuilder.build()

    assertThat(testActorBuilder.tags[Integer::class] as? Int, equalTo(5))
    assertThat(testActor.tag(), equalTo(5))
  }

  @Test
  @DisplayName("Generates documentation at before actor definition")
  fun testGenDoc() {
    val testActor = TypeSpec.actorBuilder("Test")
      .addDoc("this is a comment\n")
      .build()

    val out = StringWriter()
    testActor.emit(CodeWriter(out))

    assertThat(
      out.toString(),
      equalTo(
        """
            /**
             * this is a comment
             */
            actor Test {
            }

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Escapes reserved type names")
  fun testGenReservedActorName() {
    val testActor = TypeSpec.actorBuilder("Type").build()

    val out = StringWriter()
    testActor.emit(CodeWriter(out))

    assertThat(
      out.toString(),
      equalTo(
        """
        actor `Type` {
        }

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates modifiers in order")
  fun testGenModifiersInOrder() {
    val testActor = TypeSpec.actorBuilder("Test")
      .addModifiers(Modifier.PUBLIC)
      .build()

    val out = StringWriter()
    testActor.emit(CodeWriter(out))

    assertThat(
      out.toString(),
      equalTo(
        """
            public actor Test {
            }

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates type variables")
  fun testGenTypeVars() {
    val testActor = TypeSpec.actorBuilder("Test")
      .addTypeVariable(
        TypeVariableName.typeVariable("T", TypeVariableName.Bound(".Test6"), TypeVariableName.Bound(".Test7"))
      )
      .addTypeVariable(
        TypeVariableName.typeVariable("X", TypeVariableName.Bound(".Test2"))
      )
      .addTypeVariable(
        TypeVariableName.typeVariable("Y", TypeVariableName.Bound(composed(".Test3", ".Test4")))
      )
      .addTypeVariable(
        TypeVariableName.typeVariable("Z", TypeVariableName.Bound(SAME_TYPE, ".Test5"))
      )
      .build()

    val out = StringWriter()
    testActor.emit(CodeWriter(out))

    assertThat(
      out.toString(),
      equalTo(
        """
            actor Test<T, X, Y, Z> where T : Test6, T : Test7, X : Test2, Y : Test3 & Test4, Z == Test5 {
            }

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates attributes")
  fun testGenAttributes() {
    val testActor = TypeSpec.actorBuilder("Test")
      .addAttribute("available", "iOS 9", "*")
      .addAttribute("dynamicMemberLookup")
      .build()

    val out = StringWriter()
    testActor.emit(CodeWriter(out))

    assertThat(
      out.toString(),
      equalTo(
        """
            @available(iOS 9, *)
            @dynamicMemberLookup
            actor Test {
            }

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates attributes using type name")
  fun testGenAttributesViaTypeName() {
    val testActor = TypeSpec.actorBuilder("Test")
      .addType(
        TypeSpec.structBuilder("Bar").build()
      )
      .addProperty(
        PropertySpec.builder("a", STRING)
          .addAttribute(
            AttributeSpec.builder(typeName("Library.Bar"))
              .build()
          )
          .build()
      )
      .build()

    val out = StringWriter()
    testActor.emit(CodeWriter(out))

    assertThat(
      out.toString(),
      equalTo(
        """
            actor Test {
            
              @Library.Bar
              let a: Swift.String

              struct Bar {
              }
            
            }

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates type variables (concise)")
  fun testGenTypeVarsConcise() {
    val testActor = TypeSpec.actorBuilder("Test")
      .addTypeVariable(
        TypeVariableName.typeVariable("X", TypeVariableName.Bound(".Test2"))
      )
      .addTypeVariable(
        TypeVariableName.typeVariable("Z", TypeVariableName.Bound(".Test5"))
      )
      .build()

    val out = StringWriter()
    testActor.emit(CodeWriter(out))

    assertThat(
      out.toString(),
      equalTo(
        """
            actor Test<X : Test2, Z : Test5> {
            }

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates type variables (same type triggers where clause)")
  fun testGenTypeVarsConciseUnlessSameType() {
    val testActor = TypeSpec.actorBuilder("Test")
      .addTypeVariable(
        TypeVariableName.typeVariable("X", TypeVariableName.Bound(SAME_TYPE, ".Test2"))
      )
      .build()

    val out = StringWriter()
    testActor.emit(CodeWriter(out))

    assertThat(
      out.toString(),
      equalTo(
        """
            actor Test<X> where X == Test2 {
            }

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates type variables (boundless)")
  fun testGenTypeVarsBoundless() {
    val testClass = TypeSpec.actorBuilder("Test")
      .addTypeVariable(
        TypeVariableName.typeVariable("X")
      )
      .build()

    val out = StringWriter()
    testClass.emit(CodeWriter(out))

    assertThat(
      out.toString(),
      equalTo(
        """
            actor Test<X> {
            }

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates super types")
  fun testGenSuperActor() {
    val testActor = TypeSpec.actorBuilder("Test")
      .addSuperType(typeName(".Test2"))
      .build()

    val out = StringWriter()
    testActor.emit(CodeWriter(out))

    assertThat(
      out.toString(),
      equalTo(
        """
            actor Test : Test2 {
            }

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates constructor")
  fun testGenConstructor() {
    val testActor = TypeSpec.actorBuilder("Test")
      .addFunction(
        FunctionSpec.constructorBuilder()
          .addModifiers(Modifier.REQUIRED)
          .addParameter("value", INT)
          .build()
      )
      .addFunction(
        FunctionSpec.constructorBuilder()
          .addParameter("value", STRING)
          .build()
      )
      .build()

    val out = StringWriter()
    testActor.emit(CodeWriter(out))

    assertThat(
      out.toString(),
      equalTo(
        """
            actor Test {

              required init(value: Swift.Int) {
              }

              init(value: Swift.String) {
              }

            }

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates deinitializer")
  fun testGenDeinitializer() {
    val testActor = TypeSpec.actorBuilder("Test")
      .addFunction(
        FunctionSpec.deinitializerBuilder()
          .build()
      )
      .build()

    val out = StringWriter()
    testActor.emit(CodeWriter(out))

    assertThat(
      out.toString(),
      equalTo(
        """
            actor Test {

              deinit {
              }

            }

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates property declarations")
  fun testGenProperties() {
    val testActor = TypeSpec.actorBuilder("Test")
      .addProperty("value", INT, Modifier.PRIVATE)
      .addMutableProperty("value2", STRING, Modifier.PUBLIC)
      .addProperty(
        PropertySpec.varBuilder("value3", BOOL, Modifier.INTERNAL)
          .initializer("true")
          .build()
      )
      .addProperty(
        PropertySpec.builder("value4", INT)
          .build()
      )
      .addProperty(
        PropertySpec.builder("valueBy5", INT)
          .getter(FunctionSpec.getterBuilder().addCode("%[return value * 5\n%]").build())
          .setter(FunctionSpec.setterBuilder().addParameter("newVal", INT).addCode("%[value2 = newVal / 5\n%]").build())
          .build()
      )
      .addProperty(
        PropertySpec.builder("valueBy6", INT)
          .getter(FunctionSpec.getterBuilder().addCode("%[return value * 6\n%]").build())
          .setter(FunctionSpec.setterBuilder().addCode("%[value2 = newValue / 6\n%]").build())
          .build()
      )
      .addProperty(
        PropertySpec.builder("getterBy7", INT)
          .getter(FunctionSpec.getterBuilder().addCode("%[return value * 7\n%]").build())
          .build()
      )
      .build()

    val out = StringWriter()
    testActor.emit(CodeWriter(out))

    assertThat(
      out.toString(),
      equalTo(
        """
            actor Test {

              private let value: Swift.Int
              public var value2: Swift.String
              var value3: Swift.Bool = true
              let value4: Swift.Int
              var valueBy5: Swift.Int {
                get {
                  return value * 5
                }
                set(newVal) {
                  value2 = newVal / 5
                }
              }
              var valueBy6: Swift.Int {
                get {
                  return value * 6
                }
                set {
                  value2 = newValue / 6
                }
              }
              var getterBy7: Swift.Int {
                return value * 7
              }

            }

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates method definitions")
  fun testGenMethods() {
    val testActor = TypeSpec.actorBuilder("Test")
      .addFunction(
        FunctionSpec.builder("test1")
          .addCode("")
          .build()
      )
      .addFunction(
        FunctionSpec.builder("test2")
          .addAttribute(AttributeSpec.DISCARDABLE_RESULT)
          .addCode("")
          .build()
      )
      .build()

    val out = StringWriter()
    testActor.emit(CodeWriter(out))

    assertThat(
      out.toString(),
      equalTo(
        """
            actor Test {

              func test1() {
              }

              @discardableResult
              func test2() {
              }

            }

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("toBuilder copies all fields")
  fun testToBuilder() {
    val testActorBlder = TypeSpec.actorBuilder("Test")
      .addDoc("this is a comment\n")
      .addAttribute(AttributeSpec.DISCARDABLE_RESULT)
      .addModifiers(Modifier.PUBLIC)
      .addTypeVariable(
        TypeVariableName.typeVariable("X", TypeVariableName.Bound(".Test2"))
      )
      .addSuperType(typeName(".Test2"))
      .addFunction(
        FunctionSpec.constructorBuilder()
          .addParameter("value", INT)
          .build()
      )
      .addProperty("value", FLOAT, Modifier.PRIVATE)
      .addProperty("value2", STRING, Modifier.PUBLIC)
      .addFunction(
        FunctionSpec.builder("test1")
          .addCode("")
          .build()
      )
      .build()
      .toBuilder()

    assertThat(testActorBlder.doc.formatParts, hasItems("this is a comment\n"))
    assertThat(testActorBlder.attributes, hasItems(AttributeSpec.DISCARDABLE_RESULT))
    assertThat(testActorBlder.kind.modifiers.toImmutableSet(), equalTo(setOf(Modifier.PUBLIC)))
    assertThat(testActorBlder.typeVariables, hasItems(TypeVariableName.typeVariable("X", TypeVariableName.Bound(".Test2"))))
    assertThat(testActorBlder.superTypes, hasItems<TypeName>(typeName(".Test2")))
    assertThat(testActorBlder.propertySpecs.map { it.name }, hasItems("value", "value2"))
    assertThat(testActorBlder.functionSpecs.map { it.name }, hasItems("test1"))
  }

  @Test
  @DisplayName("Generates nested type alias")
  fun testNestedTypeAlias() {
    val testExt = TypeSpec.actorBuilder("MyActor")
      .addType(TypeAliasSpec.builder("Keys", typeName("Other.Keys")).build())
      .build()

    val out = StringWriter()
    testExt.emit(CodeWriter(out))

    assertThat(
      out.toString(),
      equalTo(
        """
            actor MyActor {

              typealias Keys = Other.Keys

            }

        """.trimIndent()
      )
    )
  }
}
