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
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.ExtensionSpec
import io.outfoxx.swiftpoet.TypeVariableName.Bound.Constraint.CONFORMS_TO
import io.outfoxx.swiftpoet.TypeVariableName.Companion.bound
import io.outfoxx.swiftpoet.TypeVariableName.Companion.typeVariable
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.StringWriter

class ExtensionSpecTests {

  @Test
  @DisplayName("Generates JavaDoc before extension definition")
  fun testGenDocs() {
    val testExt = ExtensionSpec.builder(DeclaredTypeName.typeName(".MyType"))
       .addKdoc("this is a comment\n")
       .build()

    val out = StringWriter()
    testExt.emit(CodeWriter(out))

    MatcherAssert.assertThat(
       out.toString(),
       CoreMatchers.equalTo(
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
    val testExt = ExtensionSpec.builder(DeclaredTypeName.typeName(".MyType"))
       .addSuperType(DeclaredTypeName.typeName("Swift.Encodable"))
       .build()

    val out = StringWriter()
    testExt.emit(CodeWriter(out))

    MatcherAssert.assertThat(
       out.toString(),
       CoreMatchers.equalTo(
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
    val testExt = ExtensionSpec.builder(DeclaredTypeName.typeName("Swift.Array"))
       .addSuperType(DeclaredTypeName.typeName("Swift.Encodable"))
       .addConditionalConstraint(typeVariable("Element", bound(CONFORMS_TO, "Swift.Encodable")))
       .build()

    val out = StringWriter()
    testExt.emit(CodeWriter(out))

    MatcherAssert.assertThat(
       out.toString(),
       CoreMatchers.equalTo(
          """
            extension Swift.Array : Swift.Encodable where Element : Swift.Encodable {
            }

          """.trimIndent()
       )
    )
  }

}
