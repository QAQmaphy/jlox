#!/bin/bash
cd /home/maphy/workspace/jlox
javac -d out src/com/craftinginterpreters/lox/*.java
java -cp out com.craftinginterpreters.lox.Lox "$@"
