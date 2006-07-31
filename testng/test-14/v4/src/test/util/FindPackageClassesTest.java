package test.util;

import java.io.IOException;
import java.util.Collections;

import org.testng.Assert;
import org.testng.internal.PackageUtils;

/**
 * Not very safe test for Utils.findClassesInPackage. It relies on some
 * internal directories/jar content.
 * 
 * @author <a href='mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
 */
public class FindPackageClassesTest {
  /**
   * @testng.test
   */
  public void findFilesInFolderNonRecursive() {
    try {
      String[] classes= PackageUtils.findClassesInPackage("test.confordering",
          Collections.EMPTY_LIST, Collections.EMPTY_LIST);
      

      Assert.assertEquals(classes.length, 1, "check if test/confordering contains more than 1 source");
    }
    catch(IOException ioe) {
      Assert.fail("cannot read directory", ioe);
    }
  }
  
  /**
   * @testng.test
   */
  public void findFilesInFolderRecursive() {
    try {
      String[] classes= PackageUtils.findClassesInPackage("test.confordering.*",
          Collections.EMPTY_LIST, Collections.EMPTY_LIST);
      
      Assert.assertEquals(classes.length, 5, "check if test/confordering and subdirs contains more than 5 sources");
    }
    catch(IOException ioe) {
      Assert.fail("cannot read directory", ioe);
    }
  }
  
  /**
   * @testng.test
   */
  public void findFilesInJarNonRecursive() {
    try {
      String[] classes= PackageUtils.findClassesInPackage("com.thoughtworks.qdox.parser",
          Collections.EMPTY_LIST, Collections.EMPTY_LIST);

      Assert.assertEquals(classes.length, 3, 
          "qdox-1.5.jar should contain only 3 classes in dir com.thoughtworks.qdox.parser");
    }
    catch(IOException ioe) {
      Assert.fail("cannot read jar [qdox-1.5.jar]", ioe);
    }
  }
  
  /**
   * @testng.test
   */
  public void findFilesInJarRecursive() {
    try {
      String[] classes= PackageUtils.findClassesInPackage("com.thoughtworks.qdox.parser.*",
          Collections.EMPTY_LIST, Collections.EMPTY_LIST);
      
      Assert.assertEquals(classes.length, 13, 
          "qdox-1.5.jar should contain only 3 classes in dir and subdirs com.thoughtworks.qdox.parser");
    }
    catch(IOException ioe) {
      Assert.fail("cannot read jar [qdox-1.5.jar]", ioe);
    }
  }
}
