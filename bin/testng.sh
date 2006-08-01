ROOT=~/java/testng
VERSION=5.0
JAR14=$ROOT/testng-$VERSION-jdk14.jar
JAR15=$ROOT/testng-$VERSION-jdk15.jar

java -ea -classpath $ROOT/test/build:$ROOT/3rdparty/junit.jar:$JAVA_HOME/lib/tools.jar:$JAR15:$CLASSPATH org.testng.TestNG $*
