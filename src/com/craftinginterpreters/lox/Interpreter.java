package com.craftinginterpreters.lox;

import java.util.List;

class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    private Environment environment = new Environment();

    void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }


Most modern languages have a higher-level looping statement for iterating over arbitrary user-defined sequences. C# has foreach, Java has “enhanced for”, even C++ has range-based for statements now. Those offer cleaner syntax than C’s for statement by implicitly calling into an iteration protocol that the object being looped over supports. 大多数现代语言都有高级循环语句，用于迭代用户定义的任意序列。C# 有 foreach，Java 有 “enhanced for”，甚至 C++ 现在也有基于范围的 for 语句。这些语句通过隐式调用被循环对象所支持的迭代协议，提供了比 C 的 for 语句更简洁的语法。

I love those. For Lox, though, we’re limited by building up the interpreter a chapter at a time. We don’t have objects and methods yet, so we have no way of defining an iteration protocol that the for loop could use. So we’ll stick with the old school C for loop. Think of it as “vintage”. The fixie of control flow statements. 我喜欢这些语句。不过，对于 Lox 来说，我们只能一次一章地建立解释器。我们还没有对象和方法，因此无法定义 for 循环可以使用的迭代协议。因此，我们将坚持使用老式的 C for 循环。把它想象成 “老古董“。控制流语句的 “老古董“。

        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);
        switch (expr.operator.type) {
            case BANG -> {
                return !isTruthy(right);
            }
            case MINUS -> {
                checkNumberOperand(expr.operator, right);
                return -(double) right;
            }
        }
        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return environment.get(expr.name);
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {

            case GREATER -> {
                checkNumberOperand(expr.operator, left, right);
                return (double) left > (double) right;

            }

            case GREATER_EQUAL -> {
                checkNumberOperand(expr.operator, left, right);

                return (double) left >= (double) right;

            }
            case LESS -> {
                checkNumberOperand(expr.operator, left, right);

                return (double) left < (double) right;

            }
            case LESS_EQUAL -> {
                checkNumberOperand(expr.operator, left, right);
                return (double) left <= (double) right;

            }
            case BANG_EQUAL -> {
                return !isEqual(left, right);
            }
            case EQUAL_EQUAL -> {
                return isEqual(left, right);
            }
            case MINUS -> {
                checkNumberOperand(expr.operator, left, right);
                return (double) left - (double) right;
            }
            case PLUS -> {
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                }
                if (left instanceof String && right instanceof String) {
                    return (String) left + (String) right;
                }
                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings");

            }
            case SLASH -> {
                checkNumberOperand(expr.operator, left, right);
                return (double) left / (double) right;
            }
            case STAR -> {
                checkNumberOperand(expr.operator, left, right);
                return (double) left * (double) right;
            }

        }

        return null;
    }

    private void checkNumberOperand(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) {
            return;
        }
        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) {
            return;

        }
        throw new RuntimeError(operator, "operand must be a bunber.");
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null) {
            return false;
        }
        return a.equals(b);
    }

    private boolean isTruthy(Object object) {
        if (object == null) {
            return false;
        }
        if (object instanceof Boolean) {
            return (boolean) object;
        }
        return true;
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    void executeBlock(List<Stmt> statements, Environment environment) {
        //保存当前的环境
        Environment previous = this.environment;

        try {
            this.environment = environment;
            //更新当前块的环境
            for (Stmt statement : statements) {
                execute(statement);

            }
        } finally {
            this.environment = previous;
            //恢复环境
        }
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);

        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }
        //在环境中添加一个全局变量
        environment.define(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }

    private String stringify(Object object) {
        if (object == null) {
            return "nil";
        }
        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        return object.toString();
    }
}
