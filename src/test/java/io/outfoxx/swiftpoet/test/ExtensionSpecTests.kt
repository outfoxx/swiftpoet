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

import io.outfoxx.swiftpoet.CodeWriter
import io.outfoxx.swiftpoet.DeclaredTypeName.Companion.typeName
import io.outfoxx.swiftpoet.ExtensionSpec
import io.outfoxx.swiftpoet.STRING
import io.outfoxx.swiftpoet.TypeAliasSpec
import io.outfoxx.swiftpoet.TypeVariableName.Bound.Constraint.CONFORMS_TO
import io.outfoxx.swiftpoet.TypeVariableName.Companion.bound
import io.outfoxx.swiftpoet.TypeVariableName.Companion.typeVariable
import io.outfoxx.swiftpoet.tag
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.StringWriter

class ExtensionSpecTests {

  @Test
  @DisplayName("Tags on builders can be retrieved on builders and built specs")
  fun testTags() {
    val testExtBuilder = ExtensionSpec.builder(STRING)
      .tag(5)
    val testExt = testExtBuilder.build()

    assertThat(testExtBuilder.tags[Integer::class] as? Int, equalTo(5))
    assertThat(testExt.tag(), equalTo(5))
  }

  @Test
  @DisplayName("Generates documentation before extension definition")
  fun testGenDocs() {
    val testExt = ExtensionSpec.builder(typeName(".MyType"))
      .addDoc("this is a comment\n")
      .build()

    val out = StringWriter()
    testExt.emit(CodeWriter(out))

    assertThat(
      out.toString(),
      equalTo(
        """
            /**
             * this is a comment
             */
            extension MyType {
            }

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates inheritance clause")
  fun testGenInheritance() {
    val testExt = ExtensionSpec.builder(typeName(".MyType"))
      .addSuperType(typeName("Swift.Encodable"))
      .build()

    val out = StringWriter()
    testExt.emit(CodeWriter(out))

    assertThat(
      out.toString(),
      equalTo(
        """
            extension MyType : Swift.Encodable {
            }

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates conditional conformance")
  fun testGenConditionalConformance() {
    val testExt = ExtensionSpec.builder(typeName("Swift.Array"))
      .addSuperType(typeName("Swift.Encodable"))
      .addConditionalConstraint(typeVariable("Element", bound(CONFORMS_TO, "Swift.Encodable")))
      .build()

    val out = StringWriter()
    testExt.emit(CodeWriter(out))

    assertThat(
      out.toString(),
      equalTo(
        """
            extension Swift.Array : Swift.Encodable where Element : Swift.Encodable {
            }

        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Generates nested type alias")
  fun testNestedTypeAlias() {
    val testExt = ExtensionSpec.builder(typeName("Swift.Array"))
      .addType(TypeAliasSpec.builder("Keys", typeName("Other.Keys")).build())
      .build()

    val out = StringWriter()
    testExt.emit(CodeWriter(out))

    assertThat(
      out.toString(),
      equalTo(
        """
            extension Swift.Array {
            
              typealias Keys = Other.Keys
            
            }

        """.trimIndent()
      )
    )
  }
}
