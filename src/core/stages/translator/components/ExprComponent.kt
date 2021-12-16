package core.stages.translator.components

import core.models.Token
import core.stages.parser.Expr
import core.utils.MessageProvider

abstract class ExprComponent: Expr.Visitor<Any?> {

    protected lateinit var environment: Environment

    override fun visitBinary(expr: Expr.Binary): Any? {
        val left = accept(expr.left)
        val right = accept(expr.right)

        return when(expr.operator.type) {

            Token.Type.Minus -> when(left) {
                is Int -> when(right) {
                    is Int -> left + right
                    is Double -> left + right
                    else -> MessageProvider.critical("Could not cast to number", expr.operator.line)
                }
                is Double -> when(right) {
                    is Int -> left + right
                    is Double -> left + right
                    else -> MessageProvider.critical("Could not cast to number", expr.operator.line)
                }
                else -> MessageProvider.critical("Could not cast to number", expr.operator.line)
            }

            Token.Type.Percent -> when(left) {
                is Int -> when(right) {
                    is Int -> left % right
                    else -> MessageProvider.critical("Could not cast to int", expr.operator.line)
                }
                else -> MessageProvider.critical("Could not cast to int", expr.operator.line)
            }

            Token.Type.Slash -> when(left) {
                is Int -> when(right) {
                    is Int -> left / right
                    is Double -> left / right
                    else -> MessageProvider.critical("Could not cast to number", expr.operator.line)
                }
                is Double -> when(right) {
                    is Int -> left / right
                    is Double -> left / right
                    else -> MessageProvider.critical("Could not cast to number", expr.operator.line)
                }
                else -> MessageProvider.critical("Could not cast to number", expr.operator.line)
            }

            Token.Type.Star -> when(left) {
                is Int -> when(right) {
                    is Int -> left * right
                    is Double -> left * right
                    else -> MessageProvider.critical("Could not cast to number", expr.operator.line)
                }
                is Double -> when(right) {
                    is Int -> left * right
                    is Double -> left * right
                    else -> MessageProvider.critical("Could not cast to number", expr.operator.line)
                }
                else -> MessageProvider.critical("Could not cast to number", expr.operator.line)
            }

            Token.Type.Plus -> when(left) {
                is Int -> when(right) {
                    is Int -> left + right
                    is Double -> left + right
                    else -> MessageProvider.critical("Could not cast to number", expr.operator.line)
                }
                is Double -> when(right) {
                    is Int -> left + right
                    is Double -> left + right
                    else -> MessageProvider.critical("Could not cast to number", expr.operator.line)
                }
                is String -> when(right) {
                    is Int -> left + right.toString()
                    is Double -> left + right.toString()
                    is Boolean -> left + right.toString()
                    is String -> left + right
                    else -> MessageProvider.critical("Could not cast to string", expr.operator.line)
                }
                else -> MessageProvider.critical("Could not cast to number", expr.operator.line)
            }
            Token.Type.Greater -> when(left) {
                is Int -> when(right) {
                    is Int -> left > right
                    is Double -> left > right
                    else -> MessageProvider.critical("Could not cast to number", expr.operator.line)
                }
                is Double -> when(right) {
                    is Int -> left > right
                    is Double -> left > right
                    else -> MessageProvider.critical("Could not cast to number", expr.operator.line)
                }
                else -> MessageProvider.critical("Could not cast to number", expr.operator.line)
            }

            Token.Type.GreaterOrEqual -> when(left) {
                is Int -> when(right) {
                    is Int -> left >= right
                    is Double -> left >= right
                    else -> MessageProvider.critical("Could not cast to number", expr.operator.line)
                }
                is Double -> when(right) {
                    is Int -> left >= right
                    is Double -> left >= right
                    else -> MessageProvider.critical("Could not cast to number", expr.operator.line)
                }
                else -> MessageProvider.critical("Could not cast to number", expr.operator.line)
            }

            Token.Type.Less -> when(left) {
                is Int -> when(right) {
                    is Int -> left < right
                    is Double -> left < right
                    else -> MessageProvider.critical("Could not cast to number", expr.operator.line)
                }
                is Double -> when(right) {
                    is Int -> left < right
                    is Double -> left < right
                    else -> MessageProvider.critical("Could not cast to number", expr.operator.line)
                }
                else -> MessageProvider.critical("Could not cast to number", expr.operator.line)
            }

            Token.Type.LessOrEqual -> when(left) {
                is Int -> when(right) {
                    is Int -> left <= right
                    is Double -> left <= right
                    else -> MessageProvider.critical("Could not cast to number", expr.operator.line)
                }
                is Double -> when(right) {
                    is Int -> left <= right
                    is Double -> left <= right
                    else -> MessageProvider.critical("Could not cast to number", expr.operator.line)
                }
                else -> MessageProvider.critical("Could not cast to number", expr.operator.line)
            }

            Token.Type.EqualEqual -> when(left) {
                is Int -> when(right) {
                    is Int -> left == right
                    is Double -> left == right
                    else -> MessageProvider.critical("Could not cast to number", expr.operator.line)
                }
                is Double -> when(right) {
                    is Int -> left == right
                    is Double -> left == right
                    else -> MessageProvider.critical("Could not cast to number", expr.operator.line)
                }
                is String -> when(right) {
                    is String -> left == right
                    else -> MessageProvider.critical("Could not cast to string", expr.operator.line)
                }
                is Boolean -> when(right) {
                    is Boolean -> left == right
                    else -> MessageProvider.critical("Could not cast to boolean", expr.operator.line)
                }
                else -> MessageProvider.critical("Could not cast to number", expr.operator.line)
            }

            Token.Type.NotEqual -> when(left) {
                is Int -> when(right) {
                    is Int -> left != right
                    is Double -> left != right
                    else -> MessageProvider.critical("Could not cast to number", expr.operator.line)
                }
                is Double -> when(right) {
                    is Int -> left != right
                    is Double -> left != right
                    else -> MessageProvider.critical("Could not cast to number", expr.operator.line)
                }
                is String -> when(right) {
                    is String -> left != right
                    else -> MessageProvider.critical("Could not cast to string", expr.operator.line)
                }
                is Boolean -> when(right) {
                    is Boolean -> left != right
                    else -> MessageProvider.critical("Could not cast to boolean", expr.operator.line)
                }
                else -> MessageProvider.critical("Could not cast to number", expr.operator.line)
            }

            Token.Type.And -> when(left) {
                is Boolean -> when(right) {
                    is Boolean -> left && right
                    else -> MessageProvider.critical("Could not cast to boolean", expr.operator.line)
                }
                else -> MessageProvider.critical("Could not cast to number", expr.operator.line)
            }

            Token.Type.Or -> when(left) {
                is Boolean -> when(right) {
                    is Boolean -> left || right
                    else -> MessageProvider.critical("Could not cast to boolean", expr.operator.line)
                }
                else -> MessageProvider.critical("Could not cast to number", expr.operator.line)
            }

            else -> MessageProvider.critical("Could not recognize binary operator", expr.operator.line)
        }
    }

    override fun visitUnary(expr: Expr.Unary): Any? {
        val value = accept(expr.value)

        return when(expr.operator.type) {
            Token.Type.Minus -> when(value) {
                is Int -> - value
                is Double -> - value
                else -> MessageProvider.critical("Could not cast to number", expr.operator.line)
            }
            Token.Type.Inverse -> when(value) {
                is Boolean -> !value
                else -> MessageProvider.critical("Could not cast to boolean", expr.operator.line)
            }
            else -> MessageProvider.critical("Could not recognize unary operator", expr.operator.line)
        }
    }

    override fun visitVariable(expr: Expr.Variable): Any? {
        return environment.get(expr.name)
    }

    override fun visitGroup(expr: Expr.Group): Any? {
        return accept(expr.value)
    }

    override fun visitLiteral(expr: Expr.Literal): Any? {
        return expr.value
    }

    protected fun accept(expr: Expr) = expr.accept(this)

    protected fun getLine(expression: Expr): Int? {
        return when (expression) {
            is Expr.Literal -> expression.line
            is Expr.Variable -> expression.name.line
            is Expr.Assign -> expression.name.line
            is Expr.Group -> getLine(expression.value)
            is Expr.Unary -> expression.operator.line
            is Expr.Binary -> expression.operator.line
            else -> null
        }
    }
}
