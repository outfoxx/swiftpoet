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
import io.outfoxx.swiftpoet.CodeWriter
import io.outfoxx.swiftpoet.ImportSpec
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.StringWriter

@DisplayName("ImportSpec Tests")
class ImportSpecTests {
  @Test
  @DisplayName("Generates simple import")
  fun testSimpleImport() {
    val testAttr =
      ImportSpec.builder("MyModule")
        .build()

    val out = StringWriter()
    testAttr.emit(CodeWriter(out))

    assertThat(
      out.toString(),
      equalTo("import MyModule")
    )
  }

  @Test
  @DisplayName("Generates import with attribute")
  fun testImportWithAttribute() {
    val testAttr =
      ImportSpec.builder("MyModule")
        .addAttribute(AttributeSpec.builder("testable").build())
        .build()

    val out = StringWriter()
    testAttr.emit(CodeWriter(out))

    assertThat(
      out.toString(),
      equalTo("@testable import MyModule")
    )
  }
}
