ROOT=~/java/testng
VERSION=5.0
JAR14=$ROOT/testng-$VERSION-jdk14.jar
JAR15=$ROOT/testng-$VERSION-jdk15.jar

java -ea -classpath test/build:$ROOT/3rdparty/junit.jar:$JAVA_HOME/lib/tools.jar:$JAR15:$CLASSPATH org.testng.TestNG test/testng.xml

java -ea -classpath test/v4/build:$ROOT/3rdparty/junit.jar:$JAVA_HOME/lib/tools.jar:$JAR15:$CLASSPATH org.testng.TestNG test/v4/testng.xml

java -ea -classpath test-14/build:$ROOT/3rdparty/junit.jar:$JAVA_HOME/lib/tools.jar:$JAR14:$CLASSPATH org.testng.TestNG -sourcedir test-14/src test-14/testng.xml

java -ea -classpath test-14/v4/build:$ROOT/3rdparty/junit.jar:$JAVA_HOME/lib/tools.jar:$JAR14:$CLASSPATH org.testng.TestNG -sourcedir test-14/v4/src test-14/v4/testng.xml


