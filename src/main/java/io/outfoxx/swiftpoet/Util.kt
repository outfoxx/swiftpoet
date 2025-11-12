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

package io.outfoxx.swiftpoet

import java.util.Collections

internal object NullAppendable : Appendable {
  override fun append(charSequence: CharSequence) = this
  override fun append(charSequence: CharSequence, start: Int, end: Int) = this
  override fun append(c: Char) = this
}

internal fun <K, V> Map<K, V>.toImmutableMap(): Map<K, V> =
  Collections.unmodifiableMap(LinkedHashMap(this))

internal fun <T> Collection<T>.toImmutableList(): List<T> =
  Collections.unmodifiableList(ArrayList(this))

internal fun <T> Collection<T>.toImmutableSet(): Set<T> =
  Collections.unmodifiableSet(LinkedHashSet(this))

internal inline fun <reified T : Enum<T>> Collection<T>.toEnumSet(): Set<T> =
  enumValues<T>().filterTo(mutableSetOf(), this::contains)

internal fun requireExactlyOneOf(modifiers: Set<Modifier>, vararg mutuallyExclusive: Modifier) {
  val count = mutuallyExclusive.count(modifiers::contains)
  require(count == 1) {
    "modifiers $modifiers must contain one of ${mutuallyExclusive.contentToString()}"
  }
}

internal fun requireNoneOrOneOf(modifiers: Set<Modifier>, vararg mutuallyExclusive: Modifier) {
  val count = mutuallyExclusive.count(modifiers::contains)
  require(count <= 1) {
    "modifiers $modifiers must contain none or only one of ${mutuallyExclusive.contentToString()}"
  }
}

internal fun requireNoneOf(modifiers: Set<Modifier>, vararg forbidden: Modifier) {
  require(forbidden.none(modifiers::contains)) {
    "modifiers $modifiers must contain none of ${forbidden.contentToString()}"
  }
}

internal fun <T> T.isOneOf(t1: T, t2: T, t3: T? = null, t4: T? = null, t5: T? = null, t6: T? = null) =
  this == t1 || this == t2 || this == t3 || this == t4 || this == t5 || this == t6

internal fun <T> Collection<T>.containsAnyOf(vararg t: T) = t.any(this::contains)

// see https://docs.oracle.com/javase/specs/jls/se7/html/jls-3.html#jls-3.10.6
internal fun characterLiteralWithoutSingleQuotes(c: Char) = when {
  c == '\b' -> "\\u{8}" // \u{0008}: backspace (BS)
  c == '\t' -> "\\t" // \u{0009}: horizontal tab (HT)
  c == '\n' -> "\\n" // \u{000a}: linefeed (LF)
  c == '\r' -> "\\r" // \u{000d}: carriage return (CR)
  c == '\"' -> "\\\"" // \u{0022}: double quote (")
  c == '\'' -> "\\'" // \u{0027}: single quote (')
  c == '\\' -> "\\\\" // \u{005c}: backslash (\)
  c.isIsoControl -> String.format("\\u{%x}", c.code)
  else -> Character.toString(c)
}

private val Char.isIsoControl: Boolean
  get() {
    return this in '\u0000'..'\u001F' || this in '\u007F'..'\u009F'
  }

/**
 * Returns the Swift string literal for `value`, choosing the simplest valid form:
 * standard single-line, raw single-line (when escapes would be needed), standard multiline,
 * or raw multiline (when triple-quotes/backslashes appear). When emitting inside an existing
 * raw string, falls back to an embedded multiline literal. Constant contexts keep multiline
 * values in standard form to avoid invalid raw usage.
 */
internal fun stringLiteralWithQuotes(
  value: String,
  isInsideRawString: Boolean = false,
  isConstantContext: Boolean = false,
): String {
  if (!isConstantContext && '\n' in value) {
    return if (shouldUseRawMultiline(value)) {
      buildRawMultilineLiteral(value)
    } else {
      buildStandardMultilineLiteral(value)
    }
  }

  if (isInsideRawString) {
    return buildEmbeddedLiteral(value)
  }

  val standard = buildStandardSingleLineLiteral(value)
  return if (shouldUseRawSingleLine(value)) {
    buildRawSingleLineLiteral(value) ?: standard
  } else {
    standard
  }
}

private fun buildStandardSingleLineLiteral(value: String): String {
  val result = StringBuilder(value.length + 32)
  result.append('\"')
  for (c in value) {
    result.append(characterLiteralWithoutSingleQuotes(c))
  }
  result.append('\"')
  return result.toString()
}

private fun buildRawSingleLineLiteral(value: String): String? {
  // Raw single-line literals can’t span lines; we also skip them when control chars are present
  // so we emit a standard escaped literal instead.
  if (value.any(Char::isIsoControl)) return null
  val hashes = "#".repeat(requiredHashCountForRaw(value, multiline = false))
  return buildString(value.length + hashes.length * 2 + 2) {
    append(hashes)
    append('\"')
    append(value)
    append('\"')
    append(hashes)
  }
}

private fun buildStandardMultilineLiteral(value: String): String {
  val result = StringBuilder(value.length + 32)
  result.append("\"\"\"\n")
  var index = 0
  while (index < value.length) {
    val remaining = value.length - index
    if (remaining >= 3 && value[index] == '"' && value[index + 1] == '"' && value[index + 2] == '"') {
      result.append("\\\"\"\"")
      index += 3
      continue
    }

    val c = value[index]
    if (c == '\\') {
      result.append("\\\\")
    } else {
      result.append(c)
    }
    index++
  }
  result.append("\n\"\"\"")
  return result.toString()
}

private fun buildRawMultilineLiteral(value: String): String {
  val hashes = "#".repeat(requiredHashCountForRaw(value, multiline = true))
  return buildString(value.length + hashes.length * 2 + 32) {
    append(hashes)
    append("\"\"\"\n")
    append(value)
    append("\n\"\"\"")
    append(hashes)
  }
}

private fun buildEmbeddedLiteral(value: String): String {
  val result = StringBuilder(value.length + 32)
  result.append("\"\"\"\n")
  result.append(value)
  result.append("\n\"\"\"")
  return result.toString()
}

private fun shouldUseRawMultiline(value: String): Boolean {
  // Prefer raw multiline when triple quotes appear or when backslashes would otherwise need escaping.
  if (value.contains("\"\"\"")) return true
  return value.contains('\\')
}

private fun shouldUseRawSingleLine(value: String): Boolean {
  // Avoid raw when empty or containing control chars; prefer it when quotes or backslashes exist.
  if (value.isEmpty()) return false
  if (value.any(Char::isIsoControl)) return false
  return value.any { it == '\"' || it == '\\' }
}

private fun requiredHashCountForRaw(value: String, multiline: Boolean): Int {
  val closingHashes = if (multiline) {
    maxHashesFollowing(value, "\"\"\"")
  } else {
    maxHashesFollowing(value, "\"")
  }
  val interpolationHashes = maxInterpolationHashes(value)
  // +1 guarantees the chosen delimiter is unique versus content and interpolation markers.
  return maxOf(closingHashes, interpolationHashes) + 1
}

private fun maxHashesFollowing(value: String, pattern: String): Int {
  var maxHashes = 0
  var searchIndex = value.indexOf(pattern)
  while (searchIndex != -1) {
    var current = 0
    var position = searchIndex + pattern.length
    while (position < value.length && value[position] == '#') {
      current++
      position++
    }
    if (current > maxHashes) {
      maxHashes = current
    }
    searchIndex = value.indexOf(pattern, searchIndex + 1)
  }
  return maxHashes
}

private fun maxInterpolationHashes(value: String): Int {
  var maxHashes = 0
  var index = value.indexOf('\\')
  while (index != -1) {
    var position = index + 1
    var current = 0
    while (position < value.length && value[position] == '#') {
      current++
      position++
    }
    if (position < value.length && value[position] == '(') {
      if (current > maxHashes) {
        maxHashes = current
      }
    }
    index = value.indexOf('\\', index + 1)
  }
  return maxHashes
}

internal fun escapeKeywords(canonicalName: String) =
  canonicalName.split('.').joinToString(".") { escapeIfKeyword(it) }

internal fun escapeIfKeyword(value: String) = if (value.isKeyword) "`$value`" else value

internal fun escapeIfNotJavaIdentifier(value: String) = if (value.isNotEmpty() && !Character.isJavaIdentifierStart(value.first()) || value.drop(1).any { !Character.isJavaIdentifierPart(it) }) "`$value`" else value

internal fun escapeIfNecessary(value: String) = escapeIfKeyword(escapeIfNotJavaIdentifier(value))

internal val String.isIdentifier get() = IDENTIFIER_REGEX.matches(this)

internal val String.isKeyword get() = KEYWORDS.contains(this)

internal val String.isName get() = split("\\.").none { it.isKeyword }

private val IDENTIFIER_REGEX =
  (
    "((\\p{gc=Lu}+|\\p{gc=Ll}+|\\p{gc=Lt}+|\\p{gc=Lm}+|\\p{gc=Lo}+|\\p{gc=Nl}+)+" +
      "\\d*" +
      "\\p{gc=Lu}*\\p{gc=Ll}*\\p{gc=Lt}*\\p{gc=Lm}*\\p{gc=Lo}*\\p{gc=Nl}*)" +
      "|" +
      "(`[^\n\r`]+`)"
    )
    .toRegex()

// https://github.com/JetBrains/kotlin/blob/master/core/descriptors/src/org/jetbrains/kotlin/renderer/KeywordStringsGenerated.java
private val KEYWORDS = setOf(
  "associatedtype",
  "class",
  "deinit",
  "enum",
  "extension",
  "fileprivate",
  "func",
  "import",
  "init",
  "inout",
  "internal",
  "let",
  "open",
  "operator",
  "private",
  "protocol",
  "public",
  "static",
  "struct",
  "subscript",
  "typealias",
  "var",

  "break",
  "case",
  "continue",
  "default",
  "defer",
  "do",
  "else",
  "fallthrough",
  "for",
  "guard",
  "if",
  "in",
  "repeat",
  "return",
  "switch",
  "where",
  "while",

  "as",
  "catch",
  "false",
  "is",
  "nil",
  "rethrows",
  "super",
  "self",
  "Self",
  "throw",
  "throws",
  "true",
  "try",

  "Type",
  "Self"
)
