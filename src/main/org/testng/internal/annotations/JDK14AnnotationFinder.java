package org.testng.internal.annotations;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.testng.IAnnotationTransformer;
import org.testng.TestRunner;
import org.testng.annotations.IAnnotation;
import org.testng.annotations.ITest;
import org.testng.internal.Utils;
import org.testng.log4testng.Logger;

import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.directorywalker.DirectoryScanner;
import com.thoughtworks.qdox.directorywalker.FileVisitor;
import com.thoughtworks.qdox.directorywalker.SuffixFilter;
import com.thoughtworks.qdox.model.AbstractInheritableJavaEntity;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;

/**
 * This class implements IAnnotationFinder with QDox for JDK 1.4
 * 
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 * @author <a href='mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
 */
public class JDK14AnnotationFinder implements IAnnotationFinder {
  
  /** This class' loge4testng Logger. */
  private static final Logger LOGGER = Logger.getLogger(JDK14AnnotationFinder.class);
  
  private Map<String, List<File>> m_sourceFiles= new HashMap<String, List<File>>();
  
  private Map<String, String> m_parsedClasses= new HashMap<String, String>();
  private Map<String, String> m_parsedFiles= new HashMap<String, String>();
  
  private JDK14TagFactory m_tagFactory = new JDK14TagFactory();
  private JavaDocBuilder m_docBuilder;
  private String[] m_dirPaths;
  private IAnnotationTransformer m_transformer;

  public JDK14AnnotationFinder(IAnnotationTransformer transformer) {
    m_docBuilder = new JavaDocBuilder();
    m_transformer = transformer;
  }
  
  public void addSourceDirs(String[] dirPaths) {
    if(dirPaths == null) {
      Utils.log(getClass().getName(), 1, "[WARNING] Array of source directory paths is null");
      return;
    }

    m_dirPaths = dirPaths;
    
    for (int i = 0; i < m_dirPaths.length; i++) {
      File dir = new File(m_dirPaths[i]);
      if(!dir.exists() || !dir.isDirectory()) {
        Utils.log(getClass().getName(), 1, "[WARNING] Invalid source directory " + m_dirPaths[i] + " ignored");
        continue;
      }
      DirectoryScanner scanner = new DirectoryScanner(dir);
      scanner.addFilter(new SuffixFilter(".java"));
      scanner.scan(new FileVisitor() {
        public void visitFile(File currentFile) {
          registerSourceFile(currentFile);
        }
      });
    }
  }

  /**
   * Record in an internal map the existence of a source file.
   * @param sourcefile the source file
   */
  private void registerSourceFile(File sourcefile) {
    List<File> files= m_sourceFiles.get(sourcefile.getName());
    if(null == files) {
      files= new ArrayList<File>();
      m_sourceFiles.put(sourcefile.getName(), files);
    }
    files.add(sourcefile);
  }

  private boolean addSource(String filePath) {
    if(m_parsedFiles.containsKey(filePath)) {
      return true;
    }
    
    try {
      m_docBuilder.addSource(new FileReader(filePath));
      m_parsedFiles.put(filePath, filePath);

      return true;
    }
    catch(FileNotFoundException fnfe) {
      Utils.log(getClass().getName(), 1, "[WARNING] source file not found: " + filePath + "\n    " + fnfe.getMessage());
    }
    catch(Throwable t) {
      Utils.log(getClass().getName(), 1, "[WARNING] cannot parse source: " + filePath + "\n    " + t.getMessage());
    }

    return false;
  }

  /**
   * Must be synch to be assured that a file is not parsed twice
   */
  private synchronized JavaClass getClassByName(Class clazz) {
    if(m_parsedClasses.containsKey(clazz.getName())) {
      JavaClass jc= m_docBuilder.getClassByName(clazz.getName());
      return jc;
    }
    else {
      parseSource(clazz);

      return getClassByName(clazz);
    }
  }
  
  /**
   * Must be synch to be assured that a file is not parsed twice
   */
  private synchronized void parseSource(Class clazz) {
    final String className= clazz.getName();
    int innerSignPos= className.indexOf('$');
    final String fileName =  innerSignPos == -1 
        ? className.substring(className.lastIndexOf('.') + 1) 
        : clazz.getName().substring(className.lastIndexOf('.') + 1, innerSignPos);
    
    List<File> sourcefiles= m_sourceFiles.get(fileName + ".java");
    if(null != sourcefiles) {
      for(File f: sourcefiles) {
        addSource(f.getAbsolutePath());
      }
    }
    
    m_parsedClasses.put(className, className);
    
    Class superClass= clazz.getSuperclass();
    if(null != superClass && !Object.class.equals(superClass)) {
      parseSource(superClass);
    }
  }
  
  public IAnnotation findAnnotation(Class cls, Class annotationClass) {
    if(Object.class.equals(cls)) {
      return null;
    }
    
    IAnnotation result = m_tagFactory.createTag(annotationClass, getClassByName(cls), m_transformer);

    transform(result, cls, null, null);

    return result;
  }

  public IAnnotation findAnnotation(Method m, Class annotationClass) {
    IAnnotation result = findMethodAnnotation(m.getName(), m.getParameterTypes(), 
        m.getDeclaringClass(), annotationClass, m_transformer);

    transform(result, null, null, m);
    
    return result;
  }
  
  public IAnnotation findAnnotation(Constructor m, Class annotationClass) {
    String name = stripPackage(m.getName());
    IAnnotation result = findMethodAnnotation(name, m.getParameterTypes(), m.getDeclaringClass(), 
        annotationClass, m_transformer);

    transform(result, null, m, null);
    
    return result;
  }
  
  private void transform (IAnnotation a, Class testClass, Constructor testConstructor, Method testMethod) {
    if (a instanceof ITest) {
      m_transformer.transform((ITest) a, testClass, testConstructor, testMethod);
    }
  }

  private String stripPackage(String name) {
    return name.substring(name.lastIndexOf('.') + 1);
//    String result = name;
//    int index = result.lastIndexOf(".");
//    if (index > 0) {
//      result = result.substring(index + 1);
//    }
//    
//    return result;
  }
  
  private IAnnotation findMethodAnnotation(String methodName, Class[] parameterTypes, 
      Class methodClass, Class annotationClass, IAnnotationTransformer transformer) 
  {
    if(Object.class.equals(methodClass)) {
      return null;
    }
    
    IAnnotation result = null;
    JavaClass jc = getClassByName(methodClass); 
    if (jc != null) {
      List<JavaMethod> methods = new ArrayList<JavaMethod>();
      JavaMethod[] allMethods = jc.getMethods();
      for (int i = 0; i < allMethods.length; i++) {
        JavaMethod jm = allMethods[i];
        if (methodsAreEqual(jm, methodName, parameterTypes)) {
          methods.add(jm);
        }
      }
      
      JavaMethod method = null;
//      if (methods.size() > 1) {
//        ppp("WARNING:  method " + methodName + " is overloaded, only considering the first one");
//      }
      
      if (methods.size() > 0) {
        method = methods.get(0);
        result = findTag(annotationClass, result, method, transformer);
      }
      
    }
    else {
      Utils.log(getClass().getName(), 1, "[WARNING] cannot resolve class: " + methodClass.getName());
    }
    
    return result;
  }

  private boolean methodsAreEqual(JavaMethod jm, String methodName, Class[] parameterTypes) {
    boolean result = jm.getName().equals(methodName) && 
      jm.getParameters().length == parameterTypes.length;
    
    return result;
  }

  private IAnnotation findTag(Class annotationClass, IAnnotation result, 
      AbstractInheritableJavaEntity entity, IAnnotationTransformer transformer)
  {
    return m_tagFactory.createTag(annotationClass, entity, transformer);
  }
  
  private static void ppp(String s) {
    System.out.println("[JDK14AnnotationFinder] " + s);
  }

  public boolean hasTestInstance(Method method, int i) {
    return false;
  }
  
  public String[] findOptionalValues(Method method) {
    return null;
  }
  
  public String[] findOptionalValues(Constructor method) {
    return null;
  }
}
