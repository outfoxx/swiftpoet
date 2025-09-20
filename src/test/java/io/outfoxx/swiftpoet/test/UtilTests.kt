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

import io.outfoxx.swiftpoet.characterLiteralWithoutSingleQuotes
import io.outfoxx.swiftpoet.escapeIfNecessary
import io.outfoxx.swiftpoet.stringLiteralWithQuotes
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UtilTests {
  @Test fun characterLiteral() {
    assertEquals("a", characterLiteralWithoutSingleQuotes('a'))
    assertEquals("b", characterLiteralWithoutSingleQuotes('b'))
    assertEquals("c", characterLiteralWithoutSingleQuotes('c'))
    assertEquals("%", characterLiteralWithoutSingleQuotes('%'))
    // common escapes
    assertEquals("\\u{8}", characterLiteralWithoutSingleQuotes('\b'))
    assertEquals("\\t", characterLiteralWithoutSingleQuotes('\t'))
    assertEquals("\\n", characterLiteralWithoutSingleQuotes('\n'))
    assertEquals("\\u{c}", characterLiteralWithoutSingleQuotes('\u000c'))
    assertEquals("\\r", characterLiteralWithoutSingleQuotes('\r'))
    assertEquals("\\\"", characterLiteralWithoutSingleQuotes('"'))
    assertEquals("\\'", characterLiteralWithoutSingleQuotes('\''))
    assertEquals("\\\\", characterLiteralWithoutSingleQuotes('\\'))
    // octal escapes
    assertEquals("\\u{0}", characterLiteralWithoutSingleQuotes('\u0000'))
    assertEquals("\\u{7}", characterLiteralWithoutSingleQuotes('\u0007'))
    assertEquals("?", characterLiteralWithoutSingleQuotes('\u003f'))
    assertEquals("\\u{7f}", characterLiteralWithoutSingleQuotes('\u007f'))
    assertEquals("¿", characterLiteralWithoutSingleQuotes('\u00bf'))
    assertEquals("ÿ", characterLiteralWithoutSingleQuotes('\u00ff'))
    // unicode escapes
    assertEquals("\\u{0}", characterLiteralWithoutSingleQuotes('\u0000'))
    assertEquals("\\u{1}", characterLiteralWithoutSingleQuotes('\u0001'))
    assertEquals("\\u{2}", characterLiteralWithoutSingleQuotes('\u0002'))
    assertEquals("€", characterLiteralWithoutSingleQuotes('\u20AC'))
    assertEquals("☃", characterLiteralWithoutSingleQuotes('\u2603'))
    assertEquals("♠", characterLiteralWithoutSingleQuotes('\u2660'))
    assertEquals("♣", characterLiteralWithoutSingleQuotes('\u2663'))
    assertEquals("♥", characterLiteralWithoutSingleQuotes('\u2665'))
    assertEquals("♦", characterLiteralWithoutSingleQuotes('\u2666'))
    assertEquals("✵", characterLiteralWithoutSingleQuotes('\u2735'))
    assertEquals("✺", characterLiteralWithoutSingleQuotes('\u273A'))
    assertEquals("／", characterLiteralWithoutSingleQuotes('\uFF0F'))
  }

  @Test fun stringLiteral() {
    stringLiteral("abc")
    stringLiteral("♦♥♠♣")
    stringLiteral("€\\t@\\t$", "€\t@\t$")
    assertThat(stringLiteralWithQuotes("abc();\ndef();"), equalTo("\"\"\"\nabc();\ndef();\n\"\"\""))
    stringLiteral("This is \\\"quoted\\\"!", "This is \"quoted\"!")
    stringLiteral("😀", "😀")
    stringLiteral("e^{i\\\\pi}+1=0", "e^{i\\pi}+1=0")
    assertThat(
      stringLiteralWithQuotes("a \"\"\" b\nc"),
      equalTo("\"\"\"\n" +
              "a \\\"\"\" b\n" +
              "c\n" +
              "\"\"\"")
    )
    assertThat(stringLiteralWithQuotes("abc();\ndef();", isConstantContext = true), equalTo("\"abc();\\ndef();\""))
  }

  @Test fun escapeNonJavaIdentifiers() {
    assertThat(escapeIfNecessary("8startWithNumber"), equalTo("`8startWithNumber`"))
    assertThat(escapeIfNecessary("with-hyphen"), equalTo("`with-hyphen`"))
    assertThat(escapeIfNecessary("with space"), equalTo("`with space`"))
    assertThat(escapeIfNecessary("with_unicode_punctuation\\u2026"), equalTo("`with_unicode_punctuation\\u2026`"))
  }

  private fun stringLiteral(string: String) = stringLiteral(string, string)
  private fun stringLiteral(expected: String, value: String) =
    assertEquals("\"$expected\"", stringLiteralWithQuotes(value))
}
