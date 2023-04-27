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

import io.outfoxx.swiftpoet.CodeBlock.Companion.ABSTRACT
import io.outfoxx.swiftpoet.FunctionSpec.Companion.DID_SET
import io.outfoxx.swiftpoet.FunctionSpec.Companion.GETTER
import io.outfoxx.swiftpoet.FunctionSpec.Companion.SETTER
import io.outfoxx.swiftpoet.FunctionSpec.Companion.WILL_SET
import io.outfoxx.swiftpoet.Modifier.FILEPRIVATE
import io.outfoxx.swiftpoet.Modifier.INTERNAL
import io.outfoxx.swiftpoet.Modifier.OPEN
import io.outfoxx.swiftpoet.Modifier.PRIVATE
import io.outfoxx.swiftpoet.Modifier.PUBLIC

/** A generated property declaration.  */
class PropertySpec private constructor(
  builder: Builder
) : AttributedSpec(builder.attributes.toImmutableList(), builder.tags) {

  val mutable = builder.mutable
  val mutableVisibility = builder.mutableVisibility
  val simpleSpec = builder.simpleSpec
  val subscriptSpec = builder.subscriptSpec
  val doc = builder.doc.build()
  val modifiers = builder.modifiers.toImmutableSet()
  val initializer = builder.initializer
  val getter = builder.getter
  val setter = builder.setter
  val willSet = builder.willSet
  val didSet = builder.didSet
  val name get() = simpleSpec?.first ?: "subscript"
  val type get() = simpleSpec?.second ?: subscriptSpec?.returnType ?: VOID

  init {
    require(simpleSpec != null || subscriptSpec != null)
    if (subscriptSpec != null) {
      require(subscriptSpec.parameters.isNotEmpty()) { "subscripts require at least one parameter" }
    }
  }

  internal fun emit(
    codeWriter: CodeWriter,
    implicitModifiers: Set<Modifier>,
    withInitializer: Boolean = true
  ) {
    codeWriter.emitDoc(doc)
    codeWriter.emitAttributes(attributes)
    codeWriter.emitModifiers(modifiers, implicitModifiers)

    if (subscriptSpec != null) {
      subscriptSpec.emit(codeWriter, "subscript")
    } else if (simpleSpec != null) {

      if (mutable && mutableVisibility != null) {
        codeWriter.emitCode("%L(set) ", mutableVisibility.keyword)
      }

      val (name, type) = simpleSpec
      codeWriter.emit(if (mutable || getter != null || setter != null) "var " else "let ")
      codeWriter.emitCode("%L: %T", escapeIfNecessary(name), type)
      if (withInitializer && initializer != null) {
        codeWriter.emitCode(" = %[%L%]", initializer)
      }
    }

    if (willSet != null || didSet != null) {
      codeWriter.emit(" {\n")
      if (willSet != null) {
        codeWriter.emitCode("%>")
        willSet.emit(codeWriter, implicitModifiers, setter == null)
        codeWriter.emitCode("%<")
      }
      if (didSet != null) {
        codeWriter.emitCode("%>")
        didSet.emit(codeWriter, implicitModifiers)
        codeWriter.emitCode("%<")
      }

      codeWriter.emit("}")
    } else if (getter != null || setter != null) {

      // Support concise syntax (e.g. "{ get set }") for protocol property declarations
      if ((getter == null || getter.body == ABSTRACT) &&
        (setter == null || setter.body == ABSTRACT)
      ) {
        codeWriter.emit(" { ")
        if (getter != null) codeWriter.emit("${getter.name} ")
        if (setter != null) codeWriter.emit("${setter.name} ")
        codeWriter.emit("}")
        return
      }

      codeWriter.emit(" {\n")
      if (getter != null) {
        codeWriter.emitCode("%>")
        getter.emit(codeWriter, implicitModifiers, setter == null)
        codeWriter.emitCode("%<")
      }
      if (setter != null) {
        codeWriter.emitCode("%>")
        setter.emit(codeWriter, implicitModifiers)
        codeWriter.emitCode("%<")
      }

      codeWriter.emit("}")
    }
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null) return false
    if (javaClass != other.javaClass) return false
    return toString() == other.toString()
  }

  override fun hashCode() = toString().hashCode()

  override fun toString() = buildString { emit(CodeWriter(this), emptySet()) }

  fun toBuilder(): Builder {
    val builder = Builder()
    builder.simpleSpec = simpleSpec
    builder.mutable = mutable
    builder.doc.add(doc)
    builder.modifiers += modifiers
    builder.initializer = initializer
    builder.setter = setter
    builder.getter = getter
    return builder
  }

  class Builder internal constructor() : AttributedSpec.Builder<Builder>() {
    internal var simpleSpec: Pair<String, TypeName>? = null
    internal var subscriptSpec: FunctionSignatureSpec? = null
    internal var mutable = false
    internal var mutableVisibility: Modifier? = null
    internal val doc = CodeBlock.builder()
    internal val modifiers = mutableListOf<Modifier>()
    internal var initializer: CodeBlock? = null
    internal var getter: FunctionSpec? = null
    internal var setter: FunctionSpec? = null
    internal var willSet: FunctionSpec? = null
    internal var didSet: FunctionSpec? = null

    internal constructor(name: String, type: TypeName) : this() {
      this.simpleSpec = name to type
    }

    internal constructor(subscriptSpec: FunctionSignatureSpec) : this() {
      this.subscriptSpec = subscriptSpec
    }

    fun mutable(mutable: Boolean) = apply {
      check(subscriptSpec == null) { "subscripts cannot be mutable" }
      this.mutable = mutable
    }

    fun mutableVisibility(modifier: Modifier) = apply {
      check(modifier.isOneOf(OPEN, PUBLIC, INTERNAL, FILEPRIVATE, PRIVATE)) { "mutable visibility must be open, public, internal, or private" }
      this.mutableVisibility = modifier
    }

    fun addDoc(format: String, vararg args: Any) = apply {
      doc.add(format, *args)
    }

    fun addDoc(block: CodeBlock) = apply {
      doc.add(block)
    }

    fun addModifiers(vararg modifiers: Modifier) = apply {
      modifiers.forEach { it.checkTarget(Modifier.Target.PROPERTY) }
      this.modifiers += modifiers
    }

    fun initializer(format: String, vararg args: Any?) = initializer(CodeBlock.of(format, *args))

    fun initializer(codeBlock: CodeBlock) = apply {
      check(this.initializer == null) { "initializer was already set" }
      check(subscriptSpec == null) { "subscripts cannot have an initializer" }
      this.initializer = codeBlock
    }

    fun getter(getter: FunctionSpec) = apply {
      require(getter.name == GETTER) { "${getter.name} is not a getter" }
      require(willSet == null && didSet == null) { "accessors cannot be added to a property with observers" }
      check(this.getter == null) { "getter was already set" }
      this.getter = getter
    }

    fun setter(setter: FunctionSpec) = apply {
      require(setter.name == SETTER) { "${setter.name} is not a setter" }
      require(willSet == null && didSet == null) { "accessors cannot be added to a property with observers" }
      check(this.setter == null) { "setter was already set" }
      this.setter = setter
    }

    fun willSet(willSet: FunctionSpec) = apply {
      require(willSet.name == WILL_SET) { "${willSet.name} is not a willSet" }
      require(setter == null && getter == null) { "observers cannot be added to computed property" }
      check(this.willSet == null) { "willSet was already set" }
      this.willSet = willSet
    }

    fun didSet(didSet: FunctionSpec) = apply {
      require(didSet.name == DID_SET) { "${didSet.name} is not a didSet" }
      require(setter == null && getter == null) { "observers cannot be added to computed property" }
      check(this.didSet == null) { "didSet was already set" }
      this.didSet = didSet
    }

    fun abstractGetter() = apply {
      this.getter = FunctionSpec.getterBuilder().abstract(true).build()
    }

    fun abstractSetter() = apply {
      this.setter = FunctionSpec.setterBuilder().abstract(true).build()
    }

    fun build() = PropertySpec(this)
  }

  companion object {
    @JvmStatic fun builder(name: String, type: TypeName, vararg modifiers: Modifier): Builder {
      return Builder(name, type)
        .addModifiers(*modifiers)
    }

    @JvmStatic fun varBuilder(name: String, type: TypeName, vararg modifiers: Modifier): Builder {
      return Builder(name, type)
        .mutable(true)
        .addModifiers(*modifiers)
    }

    @JvmStatic fun abstractBuilder(name: String, type: TypeName, vararg modifiers: Modifier): Builder {
      return Builder(name, type)
        .mutable(true)
        .addModifiers(*modifiers)
    }

    @JvmStatic fun subscriptBuilder(signature: FunctionSignatureSpec, vararg modifiers: Modifier): Builder {
      return Builder(signature)
        .addModifiers(*modifiers)
    }
  }
}
