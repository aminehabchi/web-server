#!/bin/bash

if ! command -v java &> /dev/null; then
    exit 1
fi

if ! command -v mvn &> /dev/null; then
    mkdir -p target/classes
    find src/main/java -name "*.java" > sources.txt
    javac -d target/classes @sources.txt
    rm sources.txt
    
    if [ $? -ne 0 ]; then
        exit 1
    fi
else
    mvn clean compile -q
    
    if [ $? -ne 0 ]; then
        exit 1
    fi
fi

java -cp target/classes com.http.Main