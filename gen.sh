#!/bin/bash
cd /home/maphy/workspace/jlox
javac -d out src/com/craftinginterpreters/tool/GenerateAst.java
java -cp out com.craftinginterpreters.tool.GenerateAst src/com/craftinginterpreters/lox
