# jlox

Lox 编程语言的 Java 实现，参考 Robert Nystrom 的著作 [Crafting Interpreters](https://craftinginterpreters.com/)。

## 项目状态

正在实现扫描器（词法分析器），解释器部分已完成。

## 编译

```bash
javac -sourcepath src -d build src/com/craftinginterpreters/lox/*.java
```

## 运行

```bash
# 运行脚本文件
java -cp build com.craftinginterpreters.lox.Lox script.lox

# 交互式解释器
java -cp build com.craftinginterpreters.lox.Lox
```

## 项目结构

```
src/com/craftinginterpreters/lox/
├── Lox.java       # 主程序入口
├── Scanner.java   # 词法分析器
├── Token.java     # Token 表示
└── TokenType.java # Token 类型枚举
```

## 参考资料

- [Crafting Interpreters](https://craftinginterpreters.com/)
