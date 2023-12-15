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

import io.outfoxx.swiftpoet.TypeVariableName.Bound.Constraint.SAME_TYPE
import java.io.Closeable

/** Sentinel value that indicates that no user-provided package has been set.  */
private val NO_MODULE = String()

/**
 * Converts a [FileSpec] to a string suitable to both human- and swiftc-consumption. This honors
 * imports, indentation, and variable names.
 */
internal class CodeWriter(
  out: Appendable,
  private val indent: String = DEFAULT_INDENT,
  internal val importedTypes: Map<String, DeclaredTypeName> = emptyMap(),
  private val importedModules: Set<String> = emptySet()
) : Closeable {

  private val out = LineWrapper(out, indent, 100)
  private var indentLevel = 0

  private var doc = false
  private var comment = false
  private var moduleStack = mutableListOf(NO_MODULE)
  private val typeSpecStack = mutableListOf<AnyTypeSpec>()
  private val importableTypes = mutableMapOf<String, DeclaredTypeName>()
  private val referencedTypes = mutableMapOf<String, DeclaredTypeName>()
  private var trailingNewline = false

  /**
   * When emitting a statement, this is the line of the statement currently being written. The first
   * line of a statement is indented normally and subsequent wrapped lines are double-indented. This
   * is -1 when the currently-written line isn't part of a statement.
   */
  var statementLine = -1

  fun indent(levels: Int = 1) = apply {
    indentLevel += levels
  }

  fun unindent(levels: Int = 1) = apply {
    require(indentLevel - levels >= 0) { "cannot unindent $levels from $indentLevel" }
    indentLevel -= levels
  }

  val currentModule: String get() = this.moduleStack.last()

  fun pushModule(moduleName: String) = apply {
    this.moduleStack.add(moduleName)
  }

  fun popModule() = apply {
    val lastModuleName = this.moduleStack.removeLast()
    require(lastModuleName !== NO_MODULE) { "module stack imbalance" }
  }

  fun pushType(type: AnyTypeSpec) = apply {
    this.typeSpecStack.add(type)
  }

  fun popType() = apply {
    this.typeSpecStack.removeAt(typeSpecStack.size - 1)
  }

  fun emitComment(codeBlock: CodeBlock) {
    trailingNewline = true // Force the '//' prefix for the comment.
    comment = true
    try {
      emitCode(codeBlock)
      emit("\n")
    } finally {
      comment = false
    }
  }

  fun emitDoc(docCodeBlock: CodeBlock) {
    if (docCodeBlock.isEmpty()) return

    emit("/**\n")
    doc = true
    try {
      emitCode(docCodeBlock)
    } finally {
      doc = false
    }
    emit(" */\n")
  }

  /**
   * Emits `attributes` in declaration order.
   */
  fun emitAttributes(
    attributes: List<AttributeSpec>,
    separator: String = "\n",
    suffix: String = "\n"
  ) {
    if (attributes.isNotEmpty()) {
      var firstAttribute = true
      attributes.forEach {
        if (!firstAttribute) emit(separator)
        firstAttribute = false
        it.emit(this)
      }
      emit(suffix)
    }
  }

  /**
   * Emits `modifiers` in the standard order. Modifiers in `implicitModifiers` will not
   * be emitted.
   */
  fun emitModifiers(
    modifiers: Set<Modifier>,
    implicitModifiers: Set<Modifier> = emptySet()
  ) {
    if (modifiers.isEmpty()) return
    for (modifier in modifiers.toEnumSet()) {
      if (implicitModifiers.contains(modifier)) continue
      emit(modifier.keyword)
      emit(" ")
    }
  }

  private fun requiresWhere(typeVariables: List<TypeVariableName>): Boolean =
    typeVariables.size > 2 ||
      typeVariables.any { tv -> tv.name.contains(".") || tv.bounds.size > 1 || tv.bounds.any { it.constraint == SAME_TYPE } }

  /**
   * Emit type variables declarations, possibly with their bounds.
   *
   * If there are too many type variables, or the type variable bounding information is too complex - call
   * [emitWhereBlock] with same input to produce an additional `where` block.
   *
   * @see emitWhereBlock
   *
   * @param allTypeVariables All possible type variables and constraints
   * @return Boolean determining if a where clause is required or not
   */
  fun emitTypeVariables(allTypeVariables: List<TypeVariableName>): Boolean {
    val requiresWhere = requiresWhere(allTypeVariables)
    val declaringTypeVariables = allTypeVariables.filterNot { it.name.contains(".") }

    if (declaringTypeVariables.isEmpty()) return requiresWhere

    emit("<")
    declaringTypeVariables.forEachIndexed { index, typeVariable ->
      if (index > 0) emit(", ")
      emitCode("%L", typeVariable.name)
      if (!requiresWhere && typeVariable.bounds.isNotEmpty()) {
        typeVariable.bounds[0].emit(this)
      }
    }
    emit(">")
    return requiresWhere
  }

  /**
   * Emit a `where` block containing complex type bounds and constraints.
   *
   * To be used with [emitTypeVariables], which will emit the accompanying type variable declarations
   * and simple bound clauses.
   *
   * @see emitTypeVariables
   */
  fun emitWhereBlock(typeVariables: List<TypeVariableName>, forceOutput: Boolean = false) {
    val requiresWhere = requiresWhere(typeVariables)
    if (typeVariables.isEmpty()) return

    var index = 0
    typeVariables.forEach { typeVariable ->
      if (forceOutput || requiresWhere) {
        for ((boundIndex, bound) in typeVariable.bounds.withIndex()) {
          if (index > 0 || boundIndex > 0) emitCode(",%W") else emitCode("%Wwhere ")
          emitCode("%T", typeVariable)
          bound.emit(this)
          ++index
        }
      }
    }
  }

  fun emitCode(s: String) = emitCode(CodeBlock.of(s))

  fun emitCode(format: String, vararg args: Any?) = emitCode(CodeBlock.of(format, *args))

  fun emitCode(
    codeBlock: CodeBlock,
    isConstantContext: Boolean = false,
  ) = apply {
    var a = 0
    val partIterator = codeBlock.formatParts.listIterator()
    while (partIterator.hasNext()) {
      when (val part = partIterator.next()) {
        "%L" -> emitLiteral(codeBlock.args[a++], isConstantContext)

        "%N" -> emit(codeBlock.args[a++] as String)

        "%S" -> {
          val string = codeBlock.args[a++] as String?
          // Emit null as a literal null: no quotes.
          emit(
            if (string != null)
              stringLiteralWithQuotes(
                string,
                isInsideRawString = false,
                isConstantContext = isConstantContext,
              ) else
              "null"
          )
        }

        "%T" -> {
          val typeName = codeBlock.args[a++] as TypeName
          typeName.emit(this)
        }

        "%%" -> emit("%")

        "%>" -> indent()

        "%<" -> unindent()

        "%[" -> {
          check(statementLine == -1) { "statement enter %[ followed by statement enter %[" }
          statementLine = 0
        }

        "%]" -> {
          check(statementLine != -1) { "statement exit %] has no matching statement enter %[" }
          if (statementLine > 0) {
            unindent(2) // End a multi-line statement. Decrease the indentation level.
          }
          statementLine = -1
        }

        "%W" -> out.wrappingSpace(indentLevel + 2)

        else -> emit(part)
      }
    }
  }

  fun emitWrappingSpace() = apply {
    out.wrappingSpace(indentLevel + 2)
  }

  private fun emitLiteral(o: Any?, isConstantContext: Boolean) {
    when (o) {
      is AnyTypeSpec -> o.emit(this)
      is PropertySpec -> o.emit(this, emptySet())
      is CodeBlock -> emitCode(o, isConstantContext = isConstantContext)
      else -> emit(o.toString())
    }
  }

  /**
   * Returns the best name to identify `typeName` with in the current context. This uses the
   * available imports and the current scope to find the shortest name available. It does not honor
   * names visible due to inheritance.
   */
  fun lookupName(typeName: DeclaredTypeName): String {

    // Track all referenced type names, Swift needs to import the module for each type
    referencedTypes[typeName.canonicalName] = typeName

    // Find the shortest suffix of typeName that resolves to typeName. This uses both local type
    // names (so `Entry` in `Map` refers to `Map.Entry`). Also uses imports.
    var currentTypeName: DeclaredTypeName? = typeName
    val currentNestedSimpleNames = mutableListOf<String>()
    while (currentTypeName != null) {
      val simpleName = currentTypeName.simpleName
      val resolved = resolve(simpleName)?.nestedType(currentNestedSimpleNames)

      if (resolved == typeName.unwrapOptional()) {
        // If the type is the same as the type we're resolving for, we must use at least that name.
        if (currentNestedSimpleNames.isEmpty()) {
          return simpleName
        }
        // Otherwise, we need to use all the nested names that didn't match
        return currentNestedSimpleNames.joinToString(".")
      }

      currentNestedSimpleNames.add(0, simpleName)
      currentTypeName = currentTypeName.enclosingTypeName()
    }

    // If the type is in the same module, we're done.
    if (moduleStack.last() == typeName.moduleName) {
      return typeName.simpleNames.joinToString(".")
    }

    // If the type is in a manually imported module and doesn't clash, use an unqualified type
    if (importedModules.contains(typeName.moduleName) && !importedTypes.containsKey(typeName.simpleName)) {
      return typeName.simpleName
    }

    // We'll have to use the fully-qualified name. Mark the type as importable for a future pass.
    if (!doc) {
      importableType(typeName)
    }

    return resolveImport(typeName)
  }

  private fun importableType(typeName: DeclaredTypeName) {
    if (typeName.moduleName.isEmpty()) {
      return
    }
    val topLevelTypeName = typeName.topLevelTypeName()
    val simpleName = topLevelTypeName.simpleName
    importableTypes.putIfAbsent(simpleName, topLevelTypeName)
  }

  /**
   * Returns the type referenced by `simpleName`, using the current nesting context.
   */
  private fun resolve(simpleName: String): DeclaredTypeName? {
    // Match a child of the current (potentially nested) type.
    for (i in typeSpecStack.indices.reversed()) {
      val typeSpec = typeSpecStack[i]
      if (typeSpec is ExternalTypeSpec) {
        if (typeSpec.name == simpleName) {
          return stackTypeName(i)
        }
      }
      for (visibleChild in typeSpec.typeSpecs) {
        if (visibleChild.name == simpleName) {
          return stackTypeName(i).nestedType(simpleName)
        }
      }
    }

    // Match the top-level type.
    if (typeSpecStack.size > 0 && typeSpecStack[0].name == simpleName) {
      return DeclaredTypeName(moduleStack.last(), simpleName)
    }

    // No match.
    return null
  }

  /**
   * Looks up `typeName` in the imports and returns the shortest name possible for that type name.
   */
  private fun resolveImport(typeName: DeclaredTypeName): String {
    val topLevelTypeName = typeName.topLevelTypeName()
    return if (importedTypes.values.any { it == topLevelTypeName }) {
      typeName.simpleNames.joinToString(".")
    } else {
      typeName.canonicalName
    }
  }

  /** Returns the type named `simpleName` when nested in the type at `stackDepth`.  */
  private fun stackTypeName(stackDepth: Int): DeclaredTypeName {
    var typeName = DeclaredTypeName(moduleStack.last(), typeSpecStack[0].name)
    for (i in 1..stackDepth) {
      typeName = typeName.nestedType(typeSpecStack[i].name)
    }
    return typeName
  }

  /**
   * Emits `s` with indentation as required. It's important that all code that writes to
   * [CodeWriter.out] does it through here, since we emit indentation lazily in order to avoid
   * unnecessary trailing whitespace.
   */
  fun emit(s: String) = apply {
    var first = true
    for (line in s.split('\n')) {
      // Emit a newline character. Make sure blank lines in doc & comments look good.
      if (!first) {
        if ((doc || comment) && trailingNewline) {
          emitIndentation()
          out.append(if (doc) " *" else "//")
        }
        out.append("\n")
        trailingNewline = true
        if (statementLine != -1) {
          if (statementLine == 0) {
            indent(2) // Begin multiple-line statement. Increase the indentation level.
          }
          statementLine++
        }
      }

      first = false
      if (line.isEmpty()) continue // Don't indent empty lines.

      // Emit indentation and comment prefix if necessary.
      if (trailingNewline) {
        emitIndentation()
        if (doc) {
          out.append(" * ")
        } else if (comment) {
          out.append("// ")
        }
      }

      out.append(line)
      trailingNewline = false
    }
  }

  private fun emitIndentation() {
    for (j in 0 until indentLevel) {
      out.append(indent)
    }
  }

  override fun close() {
    out.close()
  }

  /**
   * Returns the non-colliding importable types and module names for all referenced types.
   */
  private fun generateImports(): Pair<Map<String, DeclaredTypeName>, Set<String>> {
    return importableTypes to referencedTypes.values.map { it.moduleName }.toSet()
  }

  companion object {

    /**
     * Collect imports by executing [emitStep], and returns the non-colliding imported types
     * and referenced modules.
     */
    fun collectImports(
      indent: String,
      emitStep: (importsCollector: CodeWriter) -> Unit,
    ): Pair<Map<String, DeclaredTypeName>, Set<String>> =
      CodeWriter(NullAppendable, indent)
        .use { importsCollector ->

          emitStep(importsCollector)

          importsCollector.generateImports()
        }
  }
}
