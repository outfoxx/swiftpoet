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

import io.outfoxx.swiftpoet.ComposedTypeName
import io.outfoxx.swiftpoet.DeclaredTypeName
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class ComposedTypeNameTests {

  @Test
  @DisplayName("Generates composited type names (Values)")
  fun composedTypeNameValues() {
    val typeA = DeclaredTypeName.typeName(".A")
    val typeB = DeclaredTypeName.typeName(".B")
    val typeC = DeclaredTypeName.typeName(".C")
    val composed = ComposedTypeName.composed(typeA, typeB, typeC)

    assertThat(composed.name, equalTo("A & B & C"))
  }

  @Test
  @DisplayName("Generates composited type names (Strings)")
  fun composedTypeNames() {
    val composed = ComposedTypeName.composed(".A", ".B", ".C")

    assertThat(composed.name, equalTo("A & B & C"))
  }

  @Test
  @DisplayName("Generates optional composited type names correctly")
  fun composedTypeNamesOptional() {
    val composed = ComposedTypeName.composed(".A", ".B", ".C").makeOptional()

    assertThat(composed.name, equalTo("(A & B & C)?"))
  }

  @Test
  @DisplayName("Generates implicit composited type names correctly")
  fun composedTypeNamesImplicit() {
    val composed = ComposedTypeName.composed(".A", ".B", ".C").makeImplicit()

    assertThat(composed.name, equalTo("(A & B & C)!"))
  }
}
