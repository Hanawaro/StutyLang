package core.stages.parser

import core.models.Token
import java.io.Serializable

abstract class Stmt: Serializable {

    interface Visitor<Type> {
        fun visitExpression(stmt: Expression): Type
        fun visitBlock(stmt: Block): Type
        fun visitPrint(stmt: Print): Type
        fun visitVar(stmt: Var): Type

        fun visitIf(stmt: If): Type
        fun visitFor(stmt: For): Type
    }

    class If(val condition: Expr, val thenBranch: Stmt, val elseBranch: Stmt?): Stmt() {
        override fun <Type> accept(visitor: Visitor<Type>): Type {
            return visitor.visitIf(this)
        }
    }

    class For(val init: Stmt?, val condition: Expr, val iterate: Stmt?, val body: Stmt): Stmt() {
        override fun <Type> accept(visitor: Visitor<Type>): Type {
            return visitor.visitFor(this)
        }
    }

    class Expression(val expr: Expr): Stmt() {
        override fun <Type> accept(visitor: Visitor<Type>): Type {
            return visitor.visitExpression(this)
        }
    }

    class Block(val stmts: ArrayList<Stmt>): Stmt() {
        override fun <Type> accept(visitor: Visitor<Type>): Type {
            return visitor.visitBlock(this)
        }
    }

    class Print(val expr: Expr, val withNewLine: Boolean): Stmt() {
        override fun <Type> accept(visitor: Visitor<Type>): Type {
            return visitor.visitPrint(this)
        }
    }

    class Var(val name: Token, val expr: Expr): Stmt() {
        override fun <Type> accept(visitor: Visitor<Type>): Type {
            return visitor.visitVar(this)
        }
    }

    abstract fun <Type> accept(visitor: Visitor<Type>): Type
}