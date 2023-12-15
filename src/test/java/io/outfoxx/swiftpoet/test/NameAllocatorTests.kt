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

import io.outfoxx.swiftpoet.NameAllocator
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class NameAllocatorTests {
  @Test
  fun usage() {
    val nameAllocator = NameAllocator()
    assertThat(nameAllocator.newName("foo", 1), equalTo("foo"))
    assertThat(nameAllocator.newName("bar", 2), equalTo("bar"))
    assertThat(nameAllocator[1], equalTo("foo"))
    assertThat(nameAllocator[2], equalTo("bar"))
  }

  @Test fun nameCollision() {
    val nameAllocator = NameAllocator()
    assertThat(nameAllocator.newName("foo"), equalTo("foo"))
    assertThat(nameAllocator.newName("foo"), equalTo("foo_"))
    assertThat(nameAllocator.newName("foo"), equalTo("foo__"))
  }

  @Test fun nameCollisionWithTag() {
    val nameAllocator = NameAllocator()
    assertThat(nameAllocator.newName("foo", 1), equalTo("foo"))
    assertThat(nameAllocator.newName("foo", 2), equalTo("foo_"))
    assertThat(nameAllocator.newName("foo", 3), equalTo("foo__"))
    assertThat(nameAllocator[1], equalTo("foo"))
    assertThat(nameAllocator[2], equalTo("foo_"))
    assertThat(nameAllocator[3], equalTo("foo__"))
  }

  @Test fun characterMappingSubstitute() {
    val nameAllocator = NameAllocator()
    assertThat(nameAllocator.newName("a-b", 1), equalTo("a_b"))
  }

  @Test fun characterMappingSurrogate() {
    val nameAllocator = NameAllocator()
    assertThat(nameAllocator.newName("a\uD83C\uDF7Ab", 1), equalTo("a_b"))
  }

  @Test fun characterMappingInvalidStartButValidPart() {
    val nameAllocator = NameAllocator()
    assertThat(nameAllocator.newName("1ab", 1), equalTo("_1ab"))
  }

  @Test fun characterMappingInvalidStartIsInvalidPart() {
    val nameAllocator = NameAllocator()
    assertThat(nameAllocator.newName("&ab", 1), equalTo("_ab"))
  }

  @Test fun swiftKeyword() {
    val nameAllocator = NameAllocator()
    assertThat(nameAllocator.newName("associatedtype", 1), equalTo("associatedtype_"))
    assertThat(nameAllocator[1], equalTo("associatedtype_"))
  }

  @Test fun tagReuseForbidden() {
    val nameAllocator = NameAllocator()
    nameAllocator.newName("foo", 1)
    assertThrows(IllegalArgumentException::class.java) {
      nameAllocator.newName("bar", 1)
    }
  }

  @Test fun useBeforeAllocateForbidden() {
    val nameAllocator = NameAllocator()
    assertThrows(IllegalArgumentException::class.java) {
      nameAllocator[1]
    }
  }

  @Test fun cloneUsage() {
    val outerAllocator = NameAllocator()
    outerAllocator.newName("foo", 1)

    val innerAllocator1 = outerAllocator.copy()
    assertThat(innerAllocator1.newName("bar", 2), equalTo("bar"))
    assertThat(innerAllocator1.newName("foo", 3), equalTo("foo_"))

    val innerAllocator2 = outerAllocator.copy()
    assertThat(innerAllocator2.newName("foo", 2), equalTo("foo_"))
    assertThat(innerAllocator2.newName("bar", 3), equalTo("bar"))
  }
}
