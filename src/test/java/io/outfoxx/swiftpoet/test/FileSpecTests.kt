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
import io.outfoxx.swiftpoet.PropertySpec
import io.outfoxx.swiftpoet.STRING
import io.outfoxx.swiftpoet.TupleTypeName
import io.outfoxx.swiftpoet.TypeSpec
import io.outfoxx.swiftpoet.TypeVariableName
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
  @DisplayName("Generates correct imports for extension type names")
  fun testImportsForSameExtensionTypes() {

    val obs = typeName("RxSwift.Observable")
    val obsElement = typeName("RxSwift.Observable.Element")
    val obsElementSub = typeName("RxSwift.Observable.Element.SubSequence")

    val extension =
      ExtensionSpec.builder(obsElement.enclosingTypeName()!!)
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

            let a: Array
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
          "foundation_order",
          typeName("Foundation.SortOrder", alwaysQualify = true)
        ).build()
      )
      .addProperty(
        PropertySpec.varBuilder(
          "order",
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
            import some_other_module

            struct SomeType {

              var foundation_order: Foundation.SortOrder
              var order: SortOrder

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
        .build()

    val testFile = FileSpec.builder("Test", "Test")
      .addImport("Foundation")
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

            struct SomeType {

              var foundation_order: SortOrder
              var order: some_other_module.SortOrder

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
