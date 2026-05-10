package com.craftinginterpreters.lox;

import java.util.HashMap;
import java.util.Map;

class Environment {
//当前环境的父节点

    final Environment enclosing;

    private final Map<String, Object> values = new HashMap<>();

    Environment() {
        enclosing = null;
    }

    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    //获取到已有的值
    Object get(Token name) {
        //先在当前的环境中寻找变量
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }
        //如果当前环境中没有找到该变量,并且当前环境不是终点环境的话
        if (enclosing != null) {
            //调用父节点的get来寻找变量
            return enclosing.get(name);
        }
        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");

    }

    void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
            return;
        }
        if (enclosing != null) {
            enclosing.assign(name, value);
            return;

        }
        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    //定义变量操作,讲一个名称与一个值进行绑定
    void define(String name, Object value) {
        values.put(name, value);
    }

}
