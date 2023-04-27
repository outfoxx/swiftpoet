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

import io.outfoxx.swiftpoet.FunctionSpec.Companion.isAccessor
import io.outfoxx.swiftpoet.FunctionSpec.Companion.isObserver

class FunctionSignatureSpec(builder: Builder) {

  val typeVariables = builder.typeVariables.toImmutableList()
  val returnType = builder.returnType
  val parameters = builder.parameters.toImmutableList()
  val throws = builder.throws
  val async = builder.async
  val failable = builder.failable

  internal fun emit(codeWriter: CodeWriter, name: String, includeEmptyParameters: Boolean = true) {
    codeWriter.emit(name)
    if (failable) {
      codeWriter.emit("?")
    }

    if (typeVariables.isNotEmpty()) {
      codeWriter.emitTypeVariables(typeVariables)
    }

    if (parameters.isNotEmpty() || includeEmptyParameters) {
      parameters.emit(codeWriter) { param ->
        param.emit(codeWriter, includeType = !name.isAccessor && !name.isObserver)
      }
    }

    val modifiers = mutableListOf<String>()

    if (async) {
      modifiers.add("async")
    }
    if (throws) {
      modifiers.add("throws")
    }

    if (modifiers.isNotEmpty()) {
      codeWriter.emit(modifiers.joinToString(separator = " ", prefix = " "))
    }

    if (returnType != null && returnType != VOID) {
      codeWriter.emitCode(" -> %T", returnType)
    }

    codeWriter.emitWhereBlock(typeVariables)
  }

  class Builder internal constructor() : AttributedSpec.Builder<Builder>() {
    internal val typeVariables = mutableListOf<TypeVariableName>()
    internal var returnType: TypeName? = null
    internal val parameters = mutableListOf<ParameterSpec>()
    internal var throws = false
    internal var async = false
    internal var failable = false
    internal val body: CodeBlock.Builder = CodeBlock.builder()

    fun addTypeVariables(typeVariables: Iterable<TypeVariableName>) = apply {
      this.typeVariables += typeVariables
    }

    fun addTypeVariable(typeVariable: TypeVariableName) = apply {
      typeVariables += typeVariable
    }

    fun returns(returnType: TypeName) = apply {
      this.returnType = returnType
    }

    fun addParameters(parameterSpecs: Iterable<ParameterSpec>) = apply {
      for (parameterSpec in parameterSpecs) {
        addParameter(parameterSpec)
      }
    }

    fun addParameter(parameterSpec: ParameterSpec) = apply {
      parameters += parameterSpec
    }

    fun addParameter(name: String, type: TypeName, vararg modifiers: Modifier) =
      addParameter(ParameterSpec.builder(name, type, *modifiers).build())

    fun addParameter(label: String, name: String, type: TypeName, vararg modifiers: Modifier) =
      addParameter(ParameterSpec.builder(label, name, type, *modifiers).build())

    fun addCode(format: String, vararg args: Any) = apply {
      body.add(format, *args)
    }

    fun failable(value: Boolean) = apply {
      failable = value
    }

    fun throws(value: Boolean) = apply {
      throws = value
    }

    fun async(value: Boolean) = apply {
      async = value
    }

    fun build() = FunctionSignatureSpec(this)
  }

  fun toBuilder(): Builder {
    val builder = Builder()
    builder.typeVariables += typeVariables
    builder.returnType = returnType
    builder.parameters += parameters
    builder.throws = throws
    builder.async = async
    builder.failable = failable
    return builder
  }

  companion object {

    @JvmStatic
    fun builder() = Builder()
  }
}
