package core.stages.parser

import core.models.Token
import java.io.Serializable

abstract class Expr: Serializable {

    interface Visitor<Type> {
        fun visitBinary(expr: Binary): Type
        fun visitUnary(expr: Unary): Type
        fun visitGroup(expr: Group): Type
        fun visitLiteral(expr: Literal): Type
        fun visitVariable(expr: Variable): Type
        fun visitAssign(expr: Assign): Type
    }

    class Assign(val name: Token, val value: Expr): Expr() {
        override fun <Type> accept(visitor: Visitor<Type>): Type {
            return visitor.visitAssign(this)
        }
    }

    class Variable(val name: Token): Expr() {
        override fun <Type> accept(visitor: Visitor<Type>): Type {
            return visitor.visitVariable(this)
        }
    }

    class Binary(val left: Expr, val operator: Token, val right: Expr): Expr() {
        override fun <Type> accept(visitor: Visitor<Type>): Type {
            return visitor.visitBinary(this)
        }
    }

    class Unary(val operator: Token, val value: Expr): Expr() {
        override fun <Type> accept(visitor: Visitor<Type>): Type {
            return visitor.visitUnary(this)
        }
    }

    class Group(val value: Expr): Expr() {
        override fun <Type> accept(visitor: Visitor<Type>): Type {
            return visitor.visitGroup(this)
        }
    }

    class Literal(val value: Any, val line: Int): Expr() {
        override fun <Type> accept(visitor: Visitor<Type>): Type {
            return visitor.visitLiteral(this)
        }
    }

    abstract fun <Type> accept(visitor: Visitor<Type>): Type
}