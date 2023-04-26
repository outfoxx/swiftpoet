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

/**
 * Compose a set of [TypeName]s (usually protocols) as a single [TypeName].
 *
 * Composition of types `A`, `B` and `C` is
 * ```
 *   A & B & C
 * ```
 */
class ComposedTypeName private constructor(
  val types: List<DeclaredTypeName>
) : TypeName() {

  override fun emit(out: CodeWriter): CodeWriter {
    types.forEachIndexed { index, type ->
      if (index > 0) out.emit(" & ")
      out.emitCode("%T", type)
    }
    return out
  }

  companion object {

    /**
     * Compose a type name from multiple type names.
     *
     * @param types Type names to include.
     * @return Composed type name consisting of all [types].
     */
    fun composed(types: List<DeclaredTypeName>): ComposedTypeName {
      return ComposedTypeName(types)
    }

    /**
     * Compose a type name from multiple type names.
     *
     * @param types Type names to include.
     * @return Composed type name consisting of all [types].
     */
    fun composed(vararg types: DeclaredTypeName): ComposedTypeName {
      return ComposedTypeName(types.toList())
    }

    /**
     * Compose a type name from multiple type names.
     *
     * @param types Type names, mapped to [DeclaredTypeName]s, to include.
     * @return Composed type name consisting of all [types].
     */
    fun composed(vararg types: String): ComposedTypeName {
      return ComposedTypeName(types.map { DeclaredTypeName.typeName(it) })
    }
  }
}
