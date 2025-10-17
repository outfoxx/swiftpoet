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

import io.outfoxx.swiftpoet.ComposedTypeName.Companion.composed
import io.outfoxx.swiftpoet.DATA
import io.outfoxx.swiftpoet.DeclaredTypeName.Companion.qualifiedTypeName
import io.outfoxx.swiftpoet.DeclaredTypeName.Companion.typeName
import io.outfoxx.swiftpoet.ExtensionSpec
import io.outfoxx.swiftpoet.FileMemberSpec
import io.outfoxx.swiftpoet.FileSpec
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.INT
import io.outfoxx.swiftpoet.ImportSpec
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.Modifier.PUBLIC
import io.outfoxx.swiftpoet.PropertySpec
import io.outfoxx.swiftpoet.STRING
import io.outfoxx.swiftpoet.TupleTypeName
import io.outfoxx.swiftpoet.TypeSpec
import io.outfoxx.swiftpoet.TypeVariableName
import io.outfoxx.swiftpoet.parameterizedBy
import io.outfoxx.swiftpoet.tag
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.StringWriter

@DisplayName("FileSpec Tests")
class FileSpecTests {

  @Test
  @DisplayName("Tags on builders can be retrieved on builders and built specs")
  fun testTags() {
    val testFileBuilder = FileSpec.builder("Test")
      .tag(5)
    val testFile = testFileBuilder.build()

    assertThat(testFileBuilder.tags[Integer::class] as? Int, equalTo(5))
    assertThat(testFile.tag(), equalTo(5))
  }

  @Test
  @DisplayName("Generates fully qualified types for 'alwaysQualify' declared types")
  fun testDeclaredAlwaysQualified() {

    val explicitType = qualifiedTypeName("Special.Array")

    val testFile = FileSpec.builder("Test", "Test")
      .addProperty(
        PropertySpec.builder("value", explicitType)
          .build()
      )
      .build()

    val out = StringWriter()
    testFile.writeTo(out)

    assertThat(
      out.toString(),
      equalTo(
        """
            import Special

            let value: Special.Array
        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates correct imports when extending different type names")
  fun testImportsForDifferentExtensionTypes() {
    val parentElement = typeName("Foundation.Data")
    val obsElement = typeName("RxSwift.Observable.Element")

    val extension =
      ExtensionSpec.builder(parentElement)
        .addFunction(
          FunctionSpec.builder("test")
            .returns(obsElement)
            .build()
        )
        .build()

    val testFile = FileSpec.builder("Test", "Test")
      .addExtension(extension)
      .build()

    val out = StringWriter()
    testFile.writeTo(out)

    assertThat(
      out.toString(),
      equalTo(
        """
            import Foundation
            import RxSwift

            extension Data {

              func test() -> Observable.Element {
              }

            }

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates correct names for generated nested type names")
  fun testGeneratesCorrectNamesForGeneratedNestedTypeNames() {

    val typeSpec =
      TypeSpec.classBuilder("Root")
        .addType(
          TypeSpec.classBuilder("Node")
            .addType(
              TypeSpec.classBuilder("Leaf")
                .addType(
                  TypeSpec.classBuilder("Iterator")
                    .build()
                )
                .addFunction(
                  FunctionSpec.builder("test")
                    .addStatement("let iter = %T()", typeName(".Root.Node.Leaf.Iterator"))
                    .build()
                )
                .build()
            )
            .addFunction(
              FunctionSpec.builder("test")
                .addStatement("let leaf = %T()", typeName(".Root.Node.Leaf"))
                .addStatement("let leafIter = %T()", typeName(".Root.Node.Leaf.Iterator"))
                .build()
            )
            .build()
        )
        .addFunction(
          FunctionSpec.builder("test")
            .addStatement("let node = %T()", typeName(".Root.Node"))
            .addStatement("let leaf = %T()", typeName(".Root.Node.Leaf"))
            .addStatement("let leafIter = %T()", typeName(".Root.Node.Leaf.Iterator"))
            .build()
        )
        .build()

    val testFile = FileSpec.builder("", "Root")
      .addType(typeSpec)
      .build()

    val out = StringWriter()
    testFile.writeTo(out)

    assertThat(
      out.toString(),
      equalTo(
        """
            class Root {

              func test() {
                let node = Node()
                let leaf = Node.Leaf()
                let leafIter = Node.Leaf.Iterator()
              }

              class Node {

                func test() {
                  let leaf = Leaf()
                  let leafIter = Leaf.Iterator()
                }

                class Leaf {

                  func test() {
                    let iter = Iterator()
                  }

                  class Iterator {
                  }

                }

              }

            }

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates correct imports for extensions on declared type names")
  fun testImportsForSameExtensionDeclaredTypes() {

    val obs = typeName("RxSwift.Observable")
    val obsElement = typeName("RxSwift.Observable.Element")
    val obsElementSub = typeName("RxSwift.Observable.Element.SubSequence")

    val extension =
      ExtensionSpec.builder(obsElement.enclosingTypeName()!!)
        .addType(
          TypeSpec.classBuilder("Sub")
            .addFunction(
              FunctionSpec.builder("test")
                .addStatement("let obs = %T()", obs)
                .addStatement("let obsElement = %T()", obsElement)
                .addStatement("let obsElementSub = %T()", obsElementSub)
                .build()
            )
            .build()
        )
        .addFunction(
          FunctionSpec.builder("test")
            .returns(obs)
            .build()
        )
        .addFunction(
          FunctionSpec.builder("test2")
            .returns(obsElement)
            .build()
        )
        .addFunction(
          FunctionSpec.builder("test3")
            .returns(obsElementSub)
            .build()
        )
        .build()

    val testFile = FileSpec.builder("Test", "Test")
      .addExtension(extension)
      .build()

    val out = StringWriter()
    testFile.writeTo(out)

    assertThat(
      out.toString(),
      equalTo(
        """
            import RxSwift

            extension Observable {
            
              class Sub {

                func test() {
                  let obs = Observable()
                  let obsElement = Element()
                  let obsElementSub = Element.SubSequence()
                }

              }

              func test() -> Observable {
              }
            
              func test2() -> Element {
              }
            
              func test3() -> Element.SubSequence {
              }
            
            }

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Disambiguates type names with the same simple name but different modules")
  fun testCorreclyDisambiguatesTypeNamesThatMatchConext() {

    val dateTimeType = typeName("Test.DateTime")
    val commonDateTimeType = typeName("some_module.DateTime").makeOptional()

    val typeSpec =
      TypeSpec.structBuilder(dateTimeType)
        .addProperty(
          PropertySpec.builder("value", commonDateTimeType).build()
        )
        .build()

    val extensionSpec =
      ExtensionSpec.builder(dateTimeType)
        .addFunction(
          FunctionSpec.constructorBuilder()
            .addModifiers(PUBLIC)
            .throws(true)
            .addStatement("var %N: %T = %L", "field", commonDateTimeType, "nil")
            .build()
        )
        .build()

    val testFile = FileSpec.builder("Test", "DateTime")
      .addType(typeSpec)
      .addExtension(extensionSpec)
      .build()

    val out = StringWriter()
    testFile.writeTo(out)

    assertThat(
      out.toString(),
      equalTo(
        """
          import some_module

          struct DateTime {

            let value: some_module.DateTime?

          }

          extension DateTime {

            public init() throws {
              var field: some_module.DateTime? = nil
            }

          }

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates correct imports for extensions on type specs")
  fun testImportsForSameExtensionTypeSpecs() {

    val typeSpec =
      TypeSpec.classBuilder("Observable")
        .addType(
          TypeSpec.classBuilder("Element")
            .addType(
              TypeSpec.classBuilder("SubSequence")
                .build()
            )
            .build()
        )
        .build()

    val obs = typeName(".Observable")
    val obsElement = typeName(".Observable.Element")
    val obsElementSub = typeName(".Observable.Element.SubSequence")

    val extension =
      ExtensionSpec.builder(typeSpec)
        .addType(
          TypeSpec.classBuilder("Sub")
            .addFunction(
              FunctionSpec.builder("test")
                .addStatement("let obs = %T()", obs)
                .addStatement("let obsElement = %T()", obsElement)
                .addStatement("let obsElementSub = %T()", obsElementSub)
                .build()
            )
            .build()
        )
        .addFunction(
          FunctionSpec.builder("test")
            .returns(obs)
            .build()
        )
        .addFunction(
          FunctionSpec.builder("test2")
            .returns(obsElement)
            .build()
        )
        .addFunction(
          FunctionSpec.builder("test3")
            .returns(obsElementSub)
            .build()
        )
        .build()

    val testFile = FileSpec.builder("Test", "Test")
      .addExtension(extension)
      .build()

    val out = StringWriter()
    testFile.writeTo(out)

    assertThat(
      out.toString(),
      equalTo(
        """
            extension Observable {
            
              class Sub {

                func test() {
                  let obs = Observable()
                  let obsElement = Element()
                  let obsElementSub = Element.SubSequence()
                }

              }

              func test() -> Observable {
              }
            
              func test2() -> Element {
              }
            
              func test3() -> Element.SubSequence {
              }
            
            }

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates correct imports & shortens used types")
  fun testImportsAndShortensTypes() {

    val testClass = TypeSpec.classBuilder("Test")
      .addTypeVariable(
        TypeVariableName.typeVariable("X", TypeVariableName.Bound(INT))
      )
      .addTypeVariable(
        TypeVariableName.typeVariable("Y", TypeVariableName.Bound(composed("Foundation.Test3", "Swift.Test4")))
      )
      .addTypeVariable(
        TypeVariableName.typeVariable("Z", TypeVariableName.Bound(TypeVariableName.Bound.Constraint.SAME_TYPE, "Test.Test5"))
      )
      .build()

    val testFile = FileSpec.builder("Test", "Test")
      .addType(testClass)
      .build()

    val out = StringWriter()
    testFile.writeTo(out)

    assertThat(
      out.toString(),
      equalTo(
        """
            import Foundation

            class Test<X, Y, Z> where X : Int, Y : Test3 & Test4, Z == Test5 {
            }

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates correct imports & shortens used types with tuple types")
  fun testImportsAndShortensTupleTypes() {

    val testFile = FileSpec.builder("Test", "Test")
      .addProperty(
        PropertySpec.builder("test", TupleTypeName.of("" to DATA, "" to typeName("Combine.Publisher")))
          .build()
      )
      .build()

    val out = StringWriter()
    testFile.writeTo(out)

    assertThat(
      out.toString(),
      equalTo(
        """
            import Combine
            import Foundation

            let test: (Data, Publisher)
        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates correct imports with conflicts")
  fun testCorrectImportsWithConflicts() {

    val testClass = TypeSpec.classBuilder("Test")
      .addProperty("a", typeName("Swift.Array"))
      .addProperty("b", typeName("Foundation.Array"))
      .build()

    val testFile = FileSpec.builder("Test", "Test")
      .addType(testClass)
      .build()

    val out = StringWriter()
    testFile.writeTo(out)

    assertThat(
      out.toString(),
      equalTo(
        """
          import Foundation
          
          class Test {

            let a: Swift.Array
            let b: Foundation.Array

          }

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates all required imports with conflicts (alwaysQualify)")
  fun testGeneratesAllRequiredImportsWithConflictsUsingAlwaysQualify() {
    val type = TypeSpec.structBuilder("SomeType")
      .addProperty(
        PropertySpec.varBuilder(
          "yet_another_module_order",
          typeName("Swift.Array")
            .parameterizedBy(typeName("yet_another_module.SortOrder"))
        ).build()
      )
      .addProperty(
        PropertySpec.varBuilder(
          "order",
          typeName(".SortOrder")
        ).build()
      )
      .addProperty(
        PropertySpec.varBuilder(
          "foundation_order",
          typeName("Foundation.SortOrder", alwaysQualify = true)
        ).build()
      )
      .addProperty(
        PropertySpec.varBuilder(
          "some_module_order",
          typeName("some_module.SortOrder")
        ).build()
      )
      .addProperty(
        PropertySpec.varBuilder(
          "some_other_module_order",
          typeName("some_other_module.SortOrder")
        ).build()
      )
      .build()

    val testFile = FileSpec.builder("Test", "Test")
      .addType(type)
      .build()

    val out = StringWriter()
    testFile.writeTo(out)

    assertThat(
      out.toString(),
      equalTo(
        """
            import Foundation
            import some_module
            import some_other_module
            import yet_another_module

            struct SomeType {

              var yet_another_module_order: [yet_another_module.SortOrder]
              var order: SortOrder
              var foundation_order: Foundation.SortOrder
              var some_module_order: some_module.SortOrder
              var some_other_module_order: some_other_module.SortOrder

            }

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates all required imports without conflicts")
  fun testGeneratesAllRequiredImportsWithoutConflicts() {
    val type = TypeSpec.structBuilder("SomeType")
      .addProperty(
        PropertySpec.varBuilder(
          "yet_another_module_order",
          typeName("Swift.Array")
            .parameterizedBy(typeName("yet_another_module.SortOrder"))
        ).build()
      )
      .build()

    val testFile = FileSpec.builder("Test", "Test")
      .addType(type)
      .build()

    val out = StringWriter()
    testFile.writeTo(out)

    assertThat(
      out.toString(),
      equalTo(
        """
            import yet_another_module

            struct SomeType {

              var yet_another_module_order: [SortOrder]

            }

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates all required imports with same module conflict")
  fun testGeneratesAllRequiredImportsWithSameModuleConflict() {
    val type =
      TypeSpec.structBuilder("SomeType")
        .addProperty(
          PropertySpec.varBuilder(
            "order",
            typeName(".SortOrder")
          ).build()
        )
        .addProperty(
          PropertySpec.varBuilder(
            "yet_another_module_order",
            typeName("Swift.Array")
              .parameterizedBy(typeName("yet_another_module.SortOrder"))
          ).build()
        )
        .build()

    val testFile = FileSpec.builder("Test", "Test")
      .addType(type)
      .build()

    val out = StringWriter()
    testFile.writeTo(out)

    assertThat(
      out.toString(),
      equalTo(
        """
            import yet_another_module

            struct SomeType {

              var order: SortOrder
              var yet_another_module_order: [yet_another_module.SortOrder]

            }

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates all required imports with conflicts")
  fun testGeneratesAllRequiredImportsWithConflicts() {
    val type =
      TypeSpec.structBuilder("SomeType")
        .addProperty(
          PropertySpec.varBuilder(
            "foundation_order",
            typeName("Foundation.SortOrder")
          ).build()
        )
        .addProperty(
          PropertySpec.varBuilder(
            "order",
            typeName("some_other_module.SortOrder")
          ).build()
        )
        .addProperty(
          PropertySpec.varBuilder(
            "yet_another_module_order",
            typeName("Swift.Array")
              .parameterizedBy(typeName("yet_another_module.SortOrder"))
          ).build()
        )
        .build()

    val testFile = FileSpec.builder("Test", "Test")
      .addType(type)
      .build()

    val out = StringWriter()
    testFile.writeTo(out)

    assertThat(
      out.toString(),
      equalTo(
        """
            import Foundation
            import some_other_module
            import yet_another_module

            struct SomeType {

              var foundation_order: Foundation.SortOrder
              var order: some_other_module.SortOrder
              var yet_another_module_order: [yet_another_module.SortOrder]

            }

        """.trimIndent()
      )
    )
  }
  @Test
  @DisplayName("Emits local module types without import")
  fun testLocalTypesAreNotImported() {
    val type =
      TypeSpec.structBuilder("SomeType")
        .addProperty(
          PropertySpec.varBuilder(
            "myStuff",
            typeName(".MyStuff")
          ).build()
        )
        .build()

    val testFile = FileSpec.builder("Test", "Test")
      .addType(type)
      .build()

    val out = StringWriter()
    testFile.writeTo(out)

    assertThat(
      out.toString(),
      equalTo(
        """
            struct SomeType {

              var myStuff: MyStuff

            }

        """.trimIndent()
      )
    )
  }
  @Test
  @DisplayName("Generates guarded imports")
  fun testGenGuardedImports() {

    val testFile = FileSpec.builder("Test", "Test")
      .addImport(
        ImportSpec.builder("SomeKit")
          .addGuard("canImport(SomeKit)")
          .build()
      )
      .build()

    val out = StringWriter()
    testFile.writeTo(out)

    assertThat(
      out.toString(),
      equalTo(
        """ 
            #if canImport(SomeKit)
            import SomeKit
            #endif
            
            
        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates documented imports")
  fun testGenDocumentedImports() {

    val testFile = FileSpec.builder("Test", "Test")
      .addImport(
        ImportSpec.builder("SomeKit")
          .addDoc("this is a comment\n")
          .build()
      )
      .build()

    val out = StringWriter()
    testFile.writeTo(out)

    assertThat(
      out.toString(),
      equalTo(
        """ 
            /**
             * this is a comment
             */
            import SomeKit
            
            
        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates guarded members")
  fun testGenGuardedMembers() {

    val testFile = FileSpec.builder("Test", "Test")
      .addMember(
        FileMemberSpec.builder(TypeSpec.classBuilder("Test").build())
          .addGuard("DEBUG")
          .build()
      )
      .build()

    val out = StringWriter()
    testFile.writeTo(out)

    assertThat(
      out.toString(),
      equalTo(
        """
            #if DEBUG
            class Test {
            }
            #endif

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates documented members")
  fun testGenDocumentedMembers() {

    val testClass = TypeSpec.classBuilder("Test")
      .addDoc("this is a type comment\n")
      .build()

    val testFile = FileSpec.builder("Test", "Test")
      .addMember(
        FileMemberSpec.builder(testClass)
          .addDoc("this is a member comment\n")
          .build()
      )
      .build()

    val out = StringWriter()
    testFile.writeTo(out)

    assertThat(
      out.toString(),
      equalTo(
        """
            /**
             * this is a member comment
             */
            /**
             * this is a type comment
             */
            class Test {
            }
 
        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates global properties with open access")
  fun testGeneratesPropertiesWithOpenAccess() {

    val testFile = FileSpec.builder("Test", "Test")
      .addProperty(
        PropertySpec.builder("global", STRING, Modifier.OPEN)
          .build()
      )
      .build()

    val out = StringWriter()
    testFile.writeTo(out)

    assertThat(
      out.toString(),
      equalTo(
        """
          open let global: String
        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates global properties with public access")
  fun testGeneratesPropertiesWithPublicAccess() {

    val testFile = FileSpec.builder("Test", "Test")
      .addProperty(
        PropertySpec.builder("global", STRING, Modifier.PUBLIC)
          .build()
      )
      .build()

    val out = StringWriter()
    testFile.writeTo(out)

    assertThat(
      out.toString(),
      equalTo(
        """
          public let global: String
        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates global properties with internal access (as default)")
  fun testGeneratesPropertiesWithInternalAccess() {

    val testFile = FileSpec.builder("Test", "Test")
      .addProperty(
        PropertySpec.builder("global", STRING, Modifier.INTERNAL)
          .build()
      )
      .build()

    val out = StringWriter()
    testFile.writeTo(out)

    assertThat(
      out.toString(),
      equalTo(
        """
          let global: String
        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates global properties with fileprivate access")
  fun testGeneratesPropertiesWithFileprivateAccess() {

    val testFile = FileSpec.builder("Test", "Test")
      .addProperty(
        PropertySpec.builder("global", STRING, Modifier.FILEPRIVATE)
          .build()
      )
      .build()

    val out = StringWriter()
    testFile.writeTo(out)

    assertThat(
      out.toString(),
      equalTo(
        """
          fileprivate let global: String
        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates global properties with private access")
  fun testGeneratesPropertiesWithPrivateAccess() {

    val testFile = FileSpec.builder("Test", "Test")
      .addProperty(
        PropertySpec.builder("global", STRING, Modifier.PRIVATE)
          .build()
      )
      .build()

    val out = StringWriter()
    testFile.writeTo(out)

    assertThat(
      out.toString(),
      equalTo(
        """
          private let global: String
        """.trimIndent()
      )
    )
  }
}
