package io.outfoxx.swiftpoet

class FileMemberSpec internal constructor(builder: Builder) {
  val doc = builder.doc.build()
  val member = builder.member
  val guardTest = builder.guardTest.build()

  internal fun emit(out: CodeWriter): CodeWriter {

    out.emitDoc(doc)

    if (guardTest.isNotEmpty()) {
      out.emit("#if ")
      out.emitCode(guardTest)
      out.emit("\n")
    }

    when (member) {
      is TypeSpec -> member.emit(out)
      is FunctionSpec -> member.emit(out, null, setOf(Modifier.PUBLIC))
      is PropertySpec -> member.emit(out, setOf(Modifier.PUBLIC))
      is TypeAliasSpec -> member.emit(out)
      is ExtensionSpec -> member.emit(out)
      else -> throw AssertionError()
    }

    if (guardTest.isNotEmpty()) {
      out.emit("#endif")
      out.emit("\n")
    }

    return out
  }

  class Builder internal constructor(internal val member: Any) {
    internal val doc = CodeBlock.builder()
    internal val guardTest = CodeBlock.builder()

    fun addDoc(format: String, vararg args: Any) = apply {
      doc.add(format, *args)
    }

    fun addDoc(block: CodeBlock) = apply {
      doc.add(block)
    }

    fun addGuard(test: CodeBlock) = apply {
      guardTest.add(test)
    }

    fun addGuard(format: String, vararg args: Any) = apply {
      addGuard(CodeBlock.of(format, args))
    }

    fun build(): FileMemberSpec {
      return FileMemberSpec(this)
    }
  }

  companion object {
    @JvmStatic fun builder(member: TypeSpec) = Builder(member)

    @JvmStatic fun builder(member: FunctionSpec) = Builder(member)

    @JvmStatic fun builder(member: PropertySpec) = Builder(member)

    @JvmStatic fun builder(member: TypeAliasSpec) = Builder(member)

    @JvmStatic fun builder(member: ExtensionSpec) = Builder(member)
  }

}
