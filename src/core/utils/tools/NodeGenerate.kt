package core.utils.tools

fun main() {

    val nodes = arrayListOf(
            "Start", "Init", "InitName", "Name", "Input", "Output", "Final", "CloseBrace",
            "For", "ForInitHeader", "ForInit", "ForInitName", "ForInitInput", "ForInitOutput",
            "ForInput", "ForOutput",
            "ForSetHeader", "ForSetName", "ForSetInput", "ForSetExp", "ForGroupEnd", "ForEnd",
            "End"
    )

    val limits = arrayListOf(
            "Start", "End"
    )

    val variables = arrayListOf(
            "braceLevel = 0",
            "parenLevel = 0"
    )

    val links = arrayListOf(
            "Start: End EOF braceLevel==0 parenLevel==0, Init Var, Name Identifier, Input"
    )

}