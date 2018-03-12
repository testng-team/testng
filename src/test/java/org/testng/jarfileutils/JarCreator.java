package org.testng.jarfileutils;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.testng.jarfileutils.org.testng.SampleTest1;
import org.testng.jarfileutils.org.testng.SampleTest2;
import org.testng.jarfileutils.org.testng.SampleTest3;
import org.testng.jarfileutils.org.testng.SampleTest4;
import org.testng.jarfileutils.org.testng.SampleTest5;

import java.io.File;
import java.io.IOException;

public class JarCreator {
    public static File generateJar() throws IOException {
        File jarFile = File.createTempFile("testng", ".jar");
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "testng-tests.jar")
                .addClasses(getTestClasses())
                .addAsResource("jarfileutils/testng-tests.xml")
                .addAsResource("jarfileutils/child.xml")
                .addAsResource("jarfileutils/child/child.xml")
                .addAsResource("jarfileutils/child/childofchild/childofchild.xml")
                .addAsResource("jarfileutils/childofchild/childofchild.xml");

        archive.as(ZipExporter.class).exportTo(jarFile, true);
        return jarFile;
    }

    private static Class<?>[] getTestClasses() {
        return new Class<?>[] {
                SampleTest1.class,
                SampleTest2.class,
                SampleTest3.class,
                SampleTest4.class,
                SampleTest5.class
        };
    }
}
