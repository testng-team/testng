v=5.12
ev=5.12.0.6

set -x

scp testng-${v}.jar testng-${v}-bundle.jar testng-${v}.zip testng-eclipse-${ev}.zip ${U}@beust.com:w/testng
scp -r javadocs doc/*.html doc/*.css src/main/testng-1.0.dtd testng-1.0.dtd.html ${U}@beust.com:w/testng
#scp dtd/* ${U}@beust.com:w/dtd
#(cd ~/java/beust.com; scp -r . ${U}@beust.com:w/eclipse)


# scp testng-eclipse-${ev}.zip doc/download.html ${U}@beust.com:w/testng

