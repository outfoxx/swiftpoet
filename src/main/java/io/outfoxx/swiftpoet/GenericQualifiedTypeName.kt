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
 * Qualify a [TypeName] with a [GenericQualifier] (any or some).
 *
 * `DataProtocol` qualified with `any` is
 * ```
 *   any DataProtocol
 * ```
 */
class GenericQualifiedTypeName internal constructor(
  val type: TypeName,
  val qualifier: GenericQualifier,
) : TypeName() {

  override fun emit(out: CodeWriter): CodeWriter {
    out.emitCode("${qualifier.name.lowercase()} ")
    if (type is ComposedTypeName || type is GenericQualifiedTypeName) {
      out.emitCode("(%T)", type)
    } else {
      out.emitCode("%T", type)
    }
    return out
  }

  companion object {

    fun of(type: TypeName, qualifier: GenericQualifier): GenericQualifiedTypeName {
      return GenericQualifiedTypeName(type, qualifier)
    }

    fun any(type: TypeName): GenericQualifiedTypeName {
      return of(type, GenericQualifier.ANY)
    }

    fun some(type: TypeName): GenericQualifiedTypeName {
      return of(type, GenericQualifier.SOME)
    }
  }
}
