set ROOT=c:\java\TestNG
set JAR=%ROOT%\testng-4.5-jdk15.jar;%ROOT%\test\build
	rem set JAR=%ROOT%\testng-4.5-jdk14.jar;%ROOT%\test-14\build

java -ea -classpath %ROOT%\3rdparty\junit.jar;%JAVA_HOME%\lib\tools.jar;%JAR%;%CLASSPATH% org.testng.JUnitConverter -restore -overwrite -annotation -srcdir src

