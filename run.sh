export CLASSPATH=$CLASSPATH:$(pwd)
export CLASSPATH=$CLASSPATH:$(pwd)/bin/
export CLASSPATH=$CLASSPATH:$(pwd)/lib/*

make clean


find . -name "*.java" > sources.txt 
javac -d bin @sources.txt

rm sources.txt

if [ "$#" -eq 0 ]; then
   java CodeGen.Main test2.g
else
   java CodeGen.Main $1
fi
