# jlox 语法参考手册

本文档描述当前 jlox 实现支持的完整语法，基于《Crafting Interpreters》Lox 语言规范。

## 目录

- [词法结构](#词法结构)
- [数据类型](#数据类型)
- [表达式](#表达式)
- [语句](#语句)
- [声明](#声明)
- [标准库](#标准库)
- [EBNF 语法汇总](#ebnf-语法汇总)
- [当前不支持的特性](#当前不支持的特性)

---

## 词法结构

### 注释

仅支持单行注释，以 `//` 开头到行尾：

```
// 这是一行注释
var x = 1; // 行尾注释
```

### 标识符

由字母（`a-z`、`A-Z`）、数字（`0-9`）和下划线（`_`）组成，不能以数字开头。

```
foo   _bar   myVar123   PI
```

### 关键字

保留字，不能用作标识符：

```
and   class   else   false   for   fun   if   nil
or    print   return  super  this  true   var   while
```

> 注：`super` 已保留但尚未实现。

### 字面量

| 类型 | 示例 | 说明 |
|------|------|------|
| 数字 | `42`, `3.14`, `.5` | 全部为 64 位浮点数（Double），输出时 `.0` 自动省略 |
| 字符串 | `"hello"`, `"a\nb"` | 双引号包裹，支持换行符 |
| 布尔 | `true`, `false` | — |
| 空值 | `nil` | 表示"无值" |

---

## 数据类型

运行时只有 4 种数据类型：

| 类型 | Java 内部表示 | 说明 |
|------|-------------|------|
| **Number** | `java.lang.Double` | 所有数字均为浮点数 |
| **String** | `java.lang.String` | 不可变字符串 |
| **Boolean** | `java.lang.Boolean` | `true` 或 `false` |
| **Nil** | `null` | 未初始化的变量默认值 |

**真值规则**：仅 `false` 和 `nil` 为"假"，其余一切（包括 `0` 和 `""`）均为"真"。

---

## 表达式

表达式产生值。按优先级从低到高排列：

### 1. 赋值 `=`

```
identifier = expression
instance.property = expression
```

```lox
var a = 1;
a = 2;          // 变量赋值
obj.field = 3;  // 属性赋值
```

赋值表达式本身有值（即被赋予的值），因此支持链式赋值：
```lox
var a;
var b;
a = b = 10;  // a 和 b 都是 10
```

### 2. 逻辑或 `or`

```lox
expr1 or expr2
```

- 短路求值：若 `expr1` 为真，直接返回 `expr1`，不计算 `expr2`
- 返回的是**值本身**，而非布尔值

```lox
print "hello" or 42;  // "hello"
print nil or 42;      // 42
```

### 3. 逻辑与 `and`

```lox
expr1 and expr2
```

- 短路求值：若 `expr1` 为假，直接返回 `expr1`，不计算 `expr2`

```lox
print 42 and "ok";    // "ok"
print nil and "nope"; // nil
```

### 4. 相等与比较

```
==  !=  相等 / 不等
>   >=  大于 / 大于等于
<   <=  小于 / 小于等于
```

```lox
print 5 == 5;     // true
print "a" != "b"; // true
print 3.14 > 3;   // true
```

### 5. 算术运算

```
+  加 / 字符串拼接
-  减
*  乘
/  除
```

**`+` 运算符**：
- 两个数字 → 数值相加
- 任一操作数为字符串 → 将两者转字符串并拼接（自动转型）

```lox
print 3 + 4;           // 7
print "hi" + "!";      // "hi!"
print "val: " + 42;    // "val: 42"   ← 自动转型
```

### 6. 一元运算符

```
!   逻辑非
-   负数
```

```lox
print !true;   // false
print -5;      // -5
```

### 7. 函数调用 `()`

```lox
identifier(arg1, arg2, ...)
expression(arg1, arg2, ...)
```

```lox
print fib(10);
print counter();
print calc.add(3, 5);   // 链式调用
```

### 8. 属性访问 `.`

```lox
expression.propertyName
```

```lox
print obj.x;
print obj.add(3, 5);  // 先取方法属性，再调用
```

### 9. 基础表达式

| 形式 | 说明 |
|------|------|
| `identifier` | 变量引用 |
| `this` | 指向当前实例（仅类方法内可用） |
| `123`, `"str"`, `true`, `false`, `nil` | 字面量 |
| `( expression )` | 分组，提升优先级 |

---

## 语句

### 表达式语句

以分号结尾的表达式：

```lox
3 + 4;
fib(5);
```

### print 语句

计算表达式并打印到标准输出：

```lox
print "Hello World";
print 3 + 4;
```

### 代码块 `{}`

将多条语句组合成一个作用域：

```lox
{
  var a = 1;
  print a;
}
```

### if / else

```lox
if (condition) statement;
if (condition) statement; else statement;
```

```lox
if (x > 0) {
  print "positive";
} else {
  print "zero or negative";
}
```

### while 循环

```lox
while (condition) body;
```

```lox
var i = 0;
while (i < 10) {
  print i;
  i = i + 1;
}
```

### for 循环

`for` 是**语法糖**，解析器将其脱糖为 `while` + 代码块的组合：

```lox
for (initializer; condition; increment) body;
```

等价于：

```lox
{
  initializer;
  while (condition) {
    body;
    increment;
  }
}
```

示例：
```lox
for (var i = 0; i < 5; i = i + 1) {
  print i;
}
```

### return 语句

```lox
return;
return expression;
```

只能出现在函数体内，通过抛出异常来展开调用栈。顶层代码使用 `return` 会报错。

---

## 声明

### 变量声明

```lox
var name;
var name = expression;
```

- 未初始化时值为 `nil`
- 重复声明同名变量在同一作用域会报错
- 不能在初始化表达式中引用自身：`var a = a;` 会报错

```lox
var x;
var y = 42;
var name = "Lox";
```

### 函数声明

```lox
fun name(param1, param2, ...) { body }
```

- 支持递归：函数名在函数体内立即可用
- 参数上限 255 个
- 自动捕获定义时的环境（**闭包**）

```lox
fun add(a, b) {
  return a + b;
}

fun makeAdder(base) {
  fun adder(x) {
    return base + x;
  }
  return adder;
}

var add5 = makeAdder(5);
print add5(3);  // 8  ← 闭包捕获了 base=5
```

### 类声明

```lox
class Name {
  method1(param1, ...) { ... }
  method2(param1, ...) { ... }
}
```

- 使用 `ClassName()` 创建实例（无参构造）
- 方法内的 `this` 指向调用该方法的实例
- 实例属性使用 `.` 访问和赋值

```lox
class Person {
  sayHi() {
    return "Hi!";
  }
}

var p = Person();
print p.sayHi();  // "Hi!"

// 设置实例属性（运行时动态添加）
p.name = "Alice";
print p.name;     // "Alice"
```

---

## 标准库

当前仅有一个内置函数：

| 函数 | 说明 |
|------|------|
| `clock()` | 返回自 Unix 纪元以来的秒数（浮点），用于性能测量 |

```lox
var start = clock();
// ...一些计算...
print clock() - start;  // 耗时（秒）
```

---

## EBNF 语法汇总

```ebnf
program        → declaration* EOF ;

declaration    → classDecl
               | funDecl
               | varDecl
               | statement ;

classDecl      → "class" IDENTIFIER "{" function* "}" ;
funDecl        → "fun" function ;
varDecl        → "var" IDENTIFIER ( "=" expression )? ";" ;

statement      → exprStmt
               | forStmt
               | ifStmt
               | printStmt
               | returnStmt
               | whileStmt
               | block ;

exprStmt       → expression ";" ;
forStmt        → "for" "(" ( varDecl | exprStmt | ";" )
                           expression? ";"
                           expression? ")" statement ;
ifStmt         → "if" "(" expression ")" statement
                 ( "else" statement )? ;
printStmt      → "print" expression ";" ;
returnStmt     → "return" expression? ";" ;
whileStmt      → "while" "(" expression ")" statement ;
block          → "{" declaration* "}" ;

expression     → assignment ;
assignment     → ( call "." )? IDENTIFIER "=" assignment
               | logic_or ;
logic_or       → logic_and ( "or" logic_and )* ;
logic_and      → equality ( "and" equality )* ;
equality       → comparison ( ( "!=" | "==" ) comparison )* ;
comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
term           → factor ( ( "-" | "+" ) factor )* ;
factor         → unary ( ( "/" | "*" ) unary )* ;
unary          → ( "!" | "-" ) unary | call ;
call           → primary ( "(" arguments? ")" | "." IDENTIFIER )* ;
primary        → "true" | "false" | "nil" | "this"
               | NUMBER | STRING | IDENTIFIER
               | "(" expression ")" ;

function       → IDENTIFIER "(" parameters? ")" block ;
parameters     → IDENTIFIER ( "," IDENTIFIER )* ;
arguments      → expression ( "," expression )* ;
```

---

## 当前不支持的特性

以下特性尚未实现（在 `SUPER` token 已保留）：

| 特性 | 说明 |
|------|------|
| 构造函数 `init()` | 类无法定义构造函数，创建实例不能传参 |
| 继承 `class A < B` | 无类继承 |
| `super` 调用 | 已保留关键字但未实现 |
| 块注释 `/* */` | 仅支持 `//` 单行注释 |
| 数组 / 列表 | 无内建集合类型 |
