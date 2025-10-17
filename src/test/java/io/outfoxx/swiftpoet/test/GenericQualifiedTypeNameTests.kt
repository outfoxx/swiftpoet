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
import io.outfoxx.swiftpoet.GenericQualifiedTypeName
import io.outfoxx.swiftpoet.GenericQualifier
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class GenericQualifiedTypeNameTests {

  @Test
  @DisplayName("Generates generic qualified using explicit builders")
  fun genericQualifiedTypeNameExplicit() {
    val type = DeclaredTypeName.typeName(".GenericType")

    assertThat(GenericQualifiedTypeName.any(type).name, equalTo("any GenericType"))
    assertThat(GenericQualifiedTypeName.some(type).name, equalTo("some GenericType"))
  }

  @Test
  @DisplayName("Generates generic qualified declared type names")
  fun genericQualifiedDeclaredTypeName() {
    val type = DeclaredTypeName.typeName(".GenericType")

    assertThat(type.qualify(GenericQualifier.ANY).name, equalTo("any GenericType"))
    assertThat(type.qualify(GenericQualifier.SOME).name, equalTo("some GenericType"))
  }

  @Test
  @DisplayName("Generates generic qualified composite type names")
  fun genericQualifiedCompositeTypeName() {
    val typeA = DeclaredTypeName.typeName(".A")
    val typeB = DeclaredTypeName.typeName(".B")
    val typeC = DeclaredTypeName.typeName(".C")
    val composed = ComposedTypeName.composed(typeA, typeB, typeC)

    assertThat(composed.qualify(GenericQualifier.ANY).name, equalTo("any (A & B & C)"))
    assertThat(composed.qualify(GenericQualifier.SOME).name, equalTo("some (A & B & C)"))
  }

  @Test
  @DisplayName("Generates optional generic qualified declared type names")
  fun optionalGenericQualifiedDeclaredTypeName() {
    val type = DeclaredTypeName.typeName(".GenericType")

    assertThat(type.qualify(GenericQualifier.ANY).makeOptional().name, equalTo("(any GenericType)?"))
    assertThat(type.qualify(GenericQualifier.SOME).makeOptional().name, equalTo("(some GenericType)?"))
  }
}
