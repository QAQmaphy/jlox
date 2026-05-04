package com.craftinginterpreters.lox;

import java.util.HashMap;
import java.util.Map;

class Environment {

    private final Map<String, Object> values = new HashMap<>();

    //获取到已有的值
    Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }
        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");

    }

    //定义变量操作,讲一个名称与一个值进行绑定
    void define(String name, Object value) {
        values.put(name, value);
    }

}
