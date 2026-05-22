package com.craftinginterpreters.lox;

import java.util.List;

    
class LoxFunction implements LoxCallable{
    private final Stmt.Function declaration;
    private final Environmnet closure;
    LoxFunction(Stmt.Function declaration,Environment closure){
        this.declaration = declaration;
        this.closure = closure;
    }

    @Override
    public object call(Interpreter interpreter,
                        List<Object> arguments)
    {
        Environment environment = new Environment(closure);
        for(int i = 0;i < declaration.params.size(); i++)
        {
            environment.define(declaration.params.get(i).lexeme,arguments.get(i));
            
        }
        try{
            interpreter.executeBlock(declaration.body,environment);

        }catch(return returnValue)
        {
            return returnValue.value;
        }
        return null;
    }
    @Override
    public String toString()
    {
        return "<fn "+declaration.name.lexeme + ">";
    }
}
