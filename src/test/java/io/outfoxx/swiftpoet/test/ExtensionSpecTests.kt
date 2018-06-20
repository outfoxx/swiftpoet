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
