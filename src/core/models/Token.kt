package core.models

import java.io.Serializable

data class Token(
        val line: Int,
        val data: Any?,
        val type: Type
): Serializable {
    enum class Type {
        LeftParen, RightParen, LeftBrace, RightBrace,
        Equal, Minus, Plus, Star, Slash, Percent, Semicolon, EOL,
        Less, LessOrEqual, Greater, GreaterOrEqual, EqualEqual, NotEqual,

        Inverse, And, Or,

        Identifier, String, Int, Decimal, True, False,

        Print, Println,

        Var, For, If, Else,

        EOF
    }

    companion object {
        val keyWords = mapOf(
                "var"  to Type.Var,
                "for"  to Type.For,
                "if"   to Type.If,
                "else" to Type.Else,

                "not" to Type.Inverse,
                "and" to Type.And,
                "or"  to Type.Or,

                "true"    to Type.True,
                "false"   to Type.False,
                "print"   to Type.Print,
                "println" to Type.Println
        )
    }

    override fun toString(): String {
        return if (data != null) "$data : ${type.name}" else type.name
    }
}