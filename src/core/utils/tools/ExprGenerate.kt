package core.utils.tools

import java.io.File


fun main() {
    val file = File("Expr.kt")
    file.createNewFile()

    defineAst(file,"Expr", arrayOf(
            "Binary   : left: Expr, operator: Token, right: Expr",
            "Grouping : expression: Expr",
            "Literal  : value: Any?",
            "Unary    : operator: Token, right: Expr"
    ))

}

private fun defineAst(file: File, baseName: String, types: Array<String>) {
    file.writeText("package core.models\n\n")
    file.writeText("abstract class $baseName {\n")
    for (type in types) {
        val className = type.split(":").first()
        val fields = type.split(":").last()
        defineType(file, baseName, className, fields)
    }
    file.writeText("}\n")
}

private fun defineType(file: File, baseName: String, className: String, fieldsList: String) {

    file.writeText("class $className(")

    val fields = fieldsList.split(", ")
    for ((index, field) in fields.withIndex()) {
        file.writeText("val $field")
        if (index != fields.size - 1)
            file.writeText(", ")
    }
    file.writeText("): $baseName {")
}