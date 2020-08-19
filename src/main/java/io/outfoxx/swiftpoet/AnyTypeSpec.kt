package io.outfoxx.swiftpoet

abstract class AnyTypeSpec(
  val name: String,
  attributes: List<AttributeSpec> = listOf()
) : AttributedSpec(attributes.toImmutableList()) {

  internal abstract fun emit(codeWriter: CodeWriter)

}
