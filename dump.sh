#!/bin/bash
# 诊断打印工具：展示 Token 表 → AST 语法树 → 语义分析
# 用法: ./dump.sh <脚本.lox>              → 输出到终端
#       ./dump.sh <脚本.lox> <输出文件>    → 同时保存到文件

set -e
javac -d out src/com/craftinginterpreters/lox/*.java

if [ $# -eq 2 ]; then
  echo "→ 输出保存到: $2"
  java -cp out com.craftinginterpreters.lox.LoxDump "$1" | tee "$2"
else
  java -cp out com.craftinginterpreters.lox.LoxDump "$1"
fi
