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

class AttributeSpec internal constructor(
  builder: Builder
) : Taggable(builder.tags.toImmutableMap()) {

  internal val identifier = builder.identifier
  internal val arguments = builder.arguments

  internal fun emit(out: CodeWriter): CodeWriter {
    out.emit("@")
    out.emitCode(identifier)
    if (arguments.isNotEmpty()) {
      out.emit("(")
      out.emitCode(
        codeBlock = arguments.joinToCode(),
        isConstantContext = true,
      )
      out.emit(")")
    }
    return out
  }

  class Builder internal constructor(
    val identifier: CodeBlock
  ) : Taggable.Builder<Builder>() {
    internal val arguments = mutableListOf<CodeBlock>()

    fun addArgument(code: String): Builder = apply {
      arguments += CodeBlock.of(code)
    }

    fun addArgument(code: CodeBlock): Builder = apply {
      arguments += code
    }

    fun addArguments(codes: List<String>): Builder = apply {
      arguments += codes.map(CodeBlock.Companion::of)
    }

    fun addArguments(vararg codes: String): Builder = apply {
      arguments += codes.map(CodeBlock.Companion::of)
    }

    fun build(): AttributeSpec =
      AttributeSpec(this)
  }

  companion object {
    fun builder(name: String): Builder {
      return Builder(CodeBlock.of("%L", name))
    }

    fun builder(typeName: DeclaredTypeName): Builder {
      return Builder(CodeBlock.of("%T", typeName))
    }

    fun available(vararg platforms: Pair<String, String>): AttributeSpec {
      return builder("available").addArguments(platforms.map { "${it.first} ${it.second}" }).build()
    }

    val DISCARDABLE_RESULT = builder("discardableResult").build()
    val ESCAPING = builder("escaping").build()
  }
}
