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

import io.outfoxx.swiftpoet.Modifier.INTERNAL

/** A generated class, protocol, or enum declaration.  */
class ExtensionSpec private constructor(builder: ExtensionSpec.Builder) {
  val doc = builder.doc.build()
  val extendedType = builder.extendedType
  val modifiers = builder.modifiers.toImmutableSet()
  val superTypes = builder.superTypes.toImmutableSet()
  val conditionalConstraints = builder.conditionalConstraints.toImmutableList()
  val propertySpecs = builder.propertySpecs.toImmutableList()
  val funSpecs = builder.functionSpecs.toImmutableList()
  val typeSpecs = builder.typeSpecs.toImmutableList()

  fun toBuilder(): Builder {
    val builder = Builder(extendedType)
    builder.doc.add(doc)
    builder.conditionalConstraints += conditionalConstraints
    builder.propertySpecs += propertySpecs
    builder.functionSpecs += funSpecs
    builder.typeSpecs += typeSpecs
    return builder
  }

  internal fun emit(codeWriter: CodeWriter) {
    // Nested classes interrupt wrapped line indentation. Stash the current wrapping state and put
    // it back afterwards when this type is complete.
    val previousStatementLine = codeWriter.statementLine
    codeWriter.statementLine = -1

    try {
      codeWriter.emitDoc(doc)
      codeWriter.emitModifiers(modifiers, setOf(INTERNAL))
      codeWriter.emit("extension")
      codeWriter.emitCode(" %T", extendedType)

      val superTypes = superTypes.map { type -> CodeBlock.of("%T", type) }

      if (superTypes.isNotEmpty()) {
        codeWriter.emitCode(superTypes.joinToCode(separator = ",%W", prefix = " : "))
      }

      codeWriter.emitWhereBlock(conditionalConstraints, true)
      codeWriter.emit(" {\n")

      codeWriter.pushType(extendedType)

      codeWriter.indent()
      var firstMember = true

      // Properties.
      for (propertySpec in propertySpecs) {
        if (!firstMember) codeWriter.emit("\n")
        propertySpec.emit(codeWriter, setOf(INTERNAL))
        firstMember = false
      }

      // Constructors.
      for (funSpec in funSpecs) {
        if (!funSpec.isConstructor) continue
        if (!firstMember) codeWriter.emit("\n")
        funSpec.emit(codeWriter, extendedType.name, setOf(INTERNAL))
        firstMember = false
      }

      // Functions.
      for (funSpec in funSpecs) {
        if (funSpec.isConstructor) continue
        if (!firstMember) codeWriter.emit("\n")
        funSpec.emit(codeWriter, extendedType.name, setOf(INTERNAL))
        firstMember = false
      }

      // Types.
      for (typeSpec in typeSpecs) {
        if (!firstMember) codeWriter.emit("\n")
        typeSpec.emit(codeWriter)
        firstMember = false
      }

      codeWriter.unindent()
      codeWriter.popType()

      codeWriter.emit("}\n")
    } finally {
      codeWriter.statementLine = previousStatementLine
    }
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null) return false
    if (javaClass != other.javaClass) return false
    return toString() == other.toString()
  }

  override fun hashCode() = toString().hashCode()

  override fun toString() = buildString { emit(CodeWriter(this)) }

  class Builder internal constructor(internal val extendedType: AnyTypeSpec) {
    internal val doc = CodeBlock.builder()
    internal val modifiers = mutableSetOf<Modifier>()
    internal val superTypes = mutableListOf<TypeName>()
    internal val conditionalConstraints = mutableListOf<TypeVariableName>()
    internal val propertySpecs = mutableListOf<PropertySpec>()
    internal val functionSpecs = mutableListOf<FunctionSpec>()
    internal val typeSpecs = mutableListOf<AnyTypeSpec>()

    fun addDoc(format: String, vararg args: Any) = apply {
      doc.add(format, *args)
    }

    fun addDoc(block: CodeBlock) = apply {
      doc.add(block)
    }

    fun addModifiers(vararg modifiers: Modifier) = apply {
      this.modifiers += modifiers
    }

    fun addSuperType(superType: TypeName) = apply {
      superTypes += superType
    }

    fun addConditionalConstraints(typeVariables: Iterable<TypeVariableName>) = apply {
      this.conditionalConstraints += typeVariables
    }

    fun addConditionalConstraint(typeVariable: TypeVariableName) = apply {
      conditionalConstraints += typeVariable
    }

    fun addProperties(propertySpecs: Iterable<PropertySpec>) = apply {
      propertySpecs.map(this::addProperty)
    }

    fun addProperty(propertySpec: PropertySpec) = apply {
      propertySpecs += propertySpec
    }

    fun addProperty(name: String, type: TypeName, vararg modifiers: Modifier) =
      addProperty(PropertySpec.builder(name, type, *modifiers).build())

    fun addFunctions(functionSpecs: Iterable<FunctionSpec>) = apply {
      functionSpecs.forEach { addFunction(it) }
    }

    fun addFunction(functionSpec: FunctionSpec) = apply {
      requireNoneOrOneOf(functionSpec.modifiers, Modifier.OPEN, Modifier.INTERNAL, Modifier.PUBLIC, Modifier.PRIVATE)
      functionSpecs += functionSpec
    }

    fun addTypes(typeSpecs: Iterable<AnyTypeSpec>) = apply {
      this.typeSpecs += typeSpecs
    }

    fun addType(typeSpec: AnyTypeSpec) = apply {
      typeSpecs += typeSpec
    }

    fun build(): ExtensionSpec {
      return ExtensionSpec(this)
    }
  }

  companion object {
    @JvmStatic fun builder(extendedType: AnyTypeSpec) = Builder(extendedType)
    @JvmStatic fun builder(extendedType: TypeName) = Builder(TypeSpec.classBuilder(extendedType.name).build())
  }
}
