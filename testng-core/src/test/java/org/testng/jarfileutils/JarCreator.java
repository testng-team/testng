package org.testng.jarfileutils;

import java.io.File;
import java.io.IOException;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.testng.jarfileutils.org.testng.SampleTest1;
import org.testng.jarfileutils.org.testng.SampleTest2;
import org.testng.jarfileutils.org.testng.SampleTest3;
import org.testng.jarfileutils.org.testng.SampleTest4;
import org.testng.jarfileutils.org.testng.SampleTest5;

public class JarCreator {

  private static final String PREFIX = "testng";
  private static final String ARCHIVE_NAME = "testng-tests.jar";

  public static File generateJar() throws IOException {
    return generateJar(getTestClasses(), getResources(), PREFIX, ARCHIVE_NAME);
  }

  public static File generateJar(Class<?>[] classes) throws IOException {
    return generateJar(classes, new String[] {}, PREFIX, ARCHIVE_NAME);
  }

  public static File generateJar(
      Class<?>[] classes, String[] resources, String prefix, String archiveName)
      throws IOException {
    File jarFile = File.createTempFile(prefix, ".jar");
    JavaArchive archive = ShrinkWrap.create(JavaArchive.class, archiveName).addClasses(classes);
    for (String resource : resources) {
      archive = archive.addAsResource(resource);
    }
    archive.as(ZipExporter.class).exportTo(jarFile, true);
    return jarFile;
  }

  private static Class<?>[] getTestClasses() {
    return new Class<?>[] {
      SampleTest1.class, SampleTest2.class, SampleTest3.class, SampleTest4.class, SampleTest5.class
    };
  }

  private static String[] getResources() {
    return new String[] {
      "jarfileutils/testng-tests.xml",
      "jarfileutils/child.xml",
      "jarfileutils/child/child.xml",
      "jarfileutils/child/childofchild/childofchild.xml",
      "jarfileutils/childofchild/childofchild.xml"
    };
  }
}
