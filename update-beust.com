v=6.4
ev=5.13.0.3
DEST=testng

set -x
RSYNC="rsync --verbose --progress --stats"

#scp testng-${v}.jar testng-${v}-bundle.jar testng-${v}.zip testng-eclipse-${ev}.zip ${U}@beust.com:${DEST}
${RSYNC} testng-${v}.jar testng-${v}-bundle.jar testng-${v}.zip testng-eclipse-${ev}.zip ${U}@beust.com:${DEST}

#scp -r javadocs doc/*.html doc/*.css src/main/resources/testng-1.0.dtd testng-1.0.dtd.html ${U}@beust.com:${DEST}
${RSYNC} -r javadocs doc/*.html doc/*.css src/main/resources/testng-1.0.dtd testng-1.0.dtd.html ${U}@beust.com:${DEST}
#scp dtd/* ${U}@beust.com:w/dtd
#(cd ~/java/beust.com; scp -r . ${U}@beust.com:w/eclipse)


# scp testng-eclipse-${ev}.zip doc/download.html ${U}@beust.com:${DEST}

