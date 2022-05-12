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
import io.outfoxx.swiftpoet.IMPLICIT
import io.outfoxx.swiftpoet.STRING
import io.outfoxx.swiftpoet.parameterizedBy
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.StringWriter

@DisplayName("Implicitly unwrapped optional tests")
class ImplicitTests {

  @DisplayName("Generates concise syntax (e.g. Type?)")
  @Test
  fun testGenConcise() {

    val type = IMPLICIT.parameterizedBy(STRING)

    val out = StringWriter()
    type.emit(CodeWriter(out))

    assertThat(out.toString(), equalTo("Swift.String!"))
  }

  @DisplayName("makeImplicit merges multiple depths")
  @Test
  fun testGenConciseMultiple() {

    val type = IMPLICIT.parameterizedBy(STRING).makeImplicit()

    val out = StringWriter()
    type.emit(CodeWriter(out))

    assertThat(out.toString(), equalTo("Swift.String!"))
  }

  @DisplayName("makeNonImplicit removes implicit")
  @Test
  fun testMakeNonImplicit() {

    val impImpString = IMPLICIT.parameterizedBy(STRING).makeNonImplicit()
    val processedType = impImpString.makeNonImplicit()

    assertThat(processedType, equalTo(STRING))
  }
}
