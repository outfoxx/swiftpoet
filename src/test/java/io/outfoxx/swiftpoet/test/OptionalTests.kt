package io.outfoxx.swiftpoet.test

import io.outfoxx.swiftpoet.*
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.StringWriter

@DisplayName("Swift.Optional tests")
class OptionalTests {

  @DisplayName("Generates concise syntax (e.g. Type?)")
  @Test
  fun testGenConcise() {

    val type = OPTIONAL.parameterizedBy(STRING)

    val out = StringWriter()
    type.emit(CodeWriter(out))

    assertThat(out.toString(), equalTo("Swift.String?"))
  }

  @DisplayName("Generates concise syntax for multiple depths")
  @Test
  fun testGenConciseMultiple() {

    val type = OPTIONAL.parameterizedBy(STRING).wrapOptional()

    val out = StringWriter()
    type.emit(CodeWriter(out))

    assertThat(out.toString(), equalTo("Swift.String??"))
  }

  @DisplayName("wrapOptional/unwrapOptional adds multiple layers")
  @Test
  fun testWrapOptional() {

    val optString = OPTIONAL.parameterizedBy(STRING)
    val processedType = optString.wrapOptional().unwrapOptional()

    assertThat(processedType, equalTo<TypeName>(optString))
  }

  @DisplayName("makeOptional adds multiple layers")
  @Test
  fun testMakeOptional() {

    val optString = OPTIONAL.parameterizedBy(STRING)
    val processedType = optString.makeOptional()

    assertThat(processedType, equalTo<TypeName>(optString))
  }

  @DisplayName("makeNonOptional removes multiple layers")
  @Test
  fun testMakeNonOptional() {

    val optOptString = OPTIONAL.parameterizedBy(STRING).wrapOptional()
    val processedType = optOptString.makeNonOptional()

    assertThat(processedType, equalTo<TypeName>(STRING))
  }

}
