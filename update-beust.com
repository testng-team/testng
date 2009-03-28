v=5.9
ev=5.9.0.0

set -x

scp testng-${v}.zip testng-eclipse-${ev}.zip ${U}@beust.com:w/testng
scp -r javadocs doc/*.html doc/*.css src/main/testng-1.0.dtd src/testng-1.0.dtd.html ${U}@beust.com:w/testng
scp doc/*.html doc/*.css ${U}@beust.com:w/testng/doc
scp -r javadocs ${U}@beust.com:w/testng
scp dtd/* ${U}@beust.com:w/dtd
#(cd ~/java/beust.com; scp -r . ${U}@beust.com:w/eclipse)


# scp testng-eclipse-${ev}.zip doc/download.html ${U}@beust.com:w/testng

