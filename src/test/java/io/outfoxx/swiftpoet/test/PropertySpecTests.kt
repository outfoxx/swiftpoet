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
import io.outfoxx.swiftpoet.Modifier.UNOWNED
import io.outfoxx.swiftpoet.Modifier.WEAK
import io.outfoxx.swiftpoet.PropertySpec
import io.outfoxx.swiftpoet.STRING
import io.outfoxx.swiftpoet.tag
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.StringWriter

@DisplayName("PropertySpec tests")
class PropertySpecTests {

  @Test
  @DisplayName("Tags on builders can be retrieved on builders and built specs")
  fun testTags() {
    val testPropertyBuilder = PropertySpec.builder("Test", STRING)
      .tag(5)
    val testProperty = testPropertyBuilder.build()

    assertThat(testPropertyBuilder.tags[Integer::class] as? Int, equalTo(5))
    assertThat(testProperty.tag(), equalTo(5))
  }

  @Test
  @DisplayName("Escapes names which are keywords")
  fun escapeName() {
    val property = PropertySpec.builder("extension", STRING).build()
    assertThat(property.toString(), equalTo("let `extension`: Swift.String"))
  }

  @Test
  @DisplayName("Escapes types which are keywords")
  fun escapeType() {
    val property = PropertySpec.builder("type", DeclaredTypeName(listOf("Foo", "Type"))).build()
    assertThat(property.toString(), equalTo("let type: Foo.`Type`"))
  }

  @Test
  @DisplayName("Adds weak modifier")
  fun weakReferences() {
    val testProperty =
      PropertySpec.varBuilder("test", STRING, WEAK)
        .build()

    val out = StringWriter()
    testProperty.emit(CodeWriter(out), setOf())

    assertThat(
      out.toString(),
      equalTo(
        """
          weak var test: Swift.String
        """.trimIndent()
      )
    )
  }

  @Test
  @DisplayName("Adds unowned modifier")
  fun unownedReferences() {
    val testProperty =
      PropertySpec.varBuilder("test", STRING, UNOWNED)
        .build()

    val out = StringWriter()
    testProperty.emit(CodeWriter(out), setOf())

    assertThat(
      out.toString(),
      equalTo(
        """
          unowned var test: Swift.String
        """.trimIndent()
      )
    )
  }
}
