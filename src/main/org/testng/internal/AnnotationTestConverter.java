package org.testng.internal;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.testng.AnnotationConverter;
import org.testng.internal.annotations.AnnotationHelper;
import org.testng.internal.annotations.IAnnotation;
import org.testng.internal.annotations.JDK14TagFactory;

import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.model.AbstractJavaEntity;
import com.thoughtworks.qdox.model.DocletTag;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaSource;
import com.thoughtworks.qdox.parser.ParseException;

/**
 * Converts the set of java fiels passen into TestNG java annotation format
 * @author micheb10 12-Sep-2006
 * @since 5.3
 */
public class AnnotationTestConverter {

  // Files and their test methods
  private static File[] m_fileNames;
  private static Map<String, String> m_convertedTags= new HashMap<String, String>();

  /*private static File findFileName(ClassDoc cd) {
    for(File fn : m_fileNames) {
      if(fn.getAbsolutePath().endsWith(cd.name() + ".java")) {
        return fn;
      }
    }

    assert false : "COULDN'T FIND FILE " + cd.name();

    return null;
  }*/

  /**
   * @param tag
   * @return
   */
  private static boolean isTestNGTag(DocletTag tag) {
    return tag.getName().startsWith("testng.");
  }

  /**
   * @param tag
   * @param className
   * @return
   */
  private String lineForTag(DocletTag tag) {
    assert isTestNGTag(tag) : "We assume we only get @testng tags";
    String originalName= tag.getName().substring(7); // trim off "testng."

    StringBuilder builder= new StringBuilder("  @");
    builder.append(tagForOriginalName(originalName));

    Map<?, ?> parameters= tag.getNamedParameterMap();

    if(parameters.size() > 0) {
      builder.append("(");
      for(Iterator<?> keyIterator= parameters.keySet().iterator(); keyIterator.hasNext();) {
        String key= keyIterator.next().toString();
        Class<?> expectedValueType= expectedValueTypeForKey(tag, key);
        String value= tag.getNamedParameter(key);
        if(key.equals("value")) {
          insertArrayOfValues(builder, value, expectedValueType.getComponentType());
        }
        else if(expectedValueType.isArray()) {
          builder.append(key);
          builder.append("=");
          insertArrayOfValues(builder, value, expectedValueType.getComponentType());
        }
        else {
          builder.append(key);
          builder.append("=");
          builder.append(prefixForClass(expectedValueType));
          builder.append(value);
          builder.append(suffixForClass(expectedValueType));
        }

        if(keyIterator.hasNext()) {
          builder.append(", ");
        }

      }
      builder.append(")");
    }

    return builder.toString();
  }

  /**
   * @param tag
   * @param key
   * @return
   */
  @SuppressWarnings("deprecation")
  private Class<?> expectedValueTypeForKey(DocletTag tag, String key) {
    Class<IAnnotation> annotationClass= m_annotationMap.get(tag.getName());
    if(annotationClass == null) {
      ppp("Found unknown testng annotation " + tag.getName() + " in file "
          + tag.getContext().getSource().getFile().getAbsolutePath() + " at line "
          + tag.getLineNumber());

      // preserve the information - but it looks like an invalid tag
      return String.class;
    }
    String methodName= "get" + key.substring(0, 1).toUpperCase() + key.substring(1);
    Method method= null;
    try {
      method= annotationClass.getMethod(methodName, new Class[0]);
    }
    catch(Exception e) {
      ppp("Found unknown testng parameter " + key + " in annotation " + tag.getName() + " in file "
          + tag.getContext().getSource().getFile().getAbsolutePath() + " at line "
          + tag.getLineNumber());

      // preserve the information - but it looks like an invalid parameter
      return String.class;
    }

    return method.getReturnType();
  }

  /**
   * @param builder
   * @param value
   */
  private void insertArrayOfValues(StringBuilder builder, String value, Class<?> type) {
    String[] values= Utils.stringToArray(value);
    builder.append("{");
    for(int i= 0; i < values.length; i++) {
      if(i > 0) {
        builder.append(',');
      }
      builder.append(prefixForClass(type));
      builder.append(values[i]);
      builder.append(suffixForClass(type));
    }
    builder.append("}");
  }

  /**
   * @param type
   * @return
   */
  private String suffixForClass(Class<?> type) {
    if(String.class.isAssignableFrom(type)) {
      return "\"";
    }
    if(Class.class.isAssignableFrom(type)) {
      return ".class";
    }

    return "";
  }

  /**
   * @param type
   * @return
   */
  private Object prefixForClass(Class<?> type) {
    if(String.class.isAssignableFrom(type)) {
      return "\"";
    }
    if(Class.class.isAssignableFrom(type)) {
      return "";
    }

    return "";
  }

  /**
   * @param originalName
   * @return
   */
  private static String tagForOriginalName(String originalName) {

    // look in the cache
    String tag= m_convertedTags.get(originalName);
    if(tag != null) {
      return tag;
    }

    // construct from scratch by converting to CamelCase
    StringBuilder builder= new StringBuilder(originalName.length());
    boolean toCap= true;
    for(char c : originalName.toCharArray()) {
      if(c == '-') {
        toCap= true;
      }
      else if(toCap) {
        builder.append(String.valueOf(c).toUpperCase());
        toCap= false;
      }
      else {
        builder.append(c);
      }
    }
    tag= builder.toString();
    m_convertedTags.put(originalName, tag);

    return tag;
  }

  static private void ppp(String s) {
    if(AnnotationConverter.getLogLevel() >= 1) {
      System.out.println("[AnnotationTestConverter]" + s);
    }
  }

  private File m_outDir;
  private JavaDocBuilder m_qdox;
  private HashMap<String, Class<IAnnotation>> m_annotationMap;

  /**
   * @param files
   * @param destDir
   */
  @SuppressWarnings("unchecked")
  public AnnotationTestConverter(File[] files, File destDir) {
    m_fileNames= files;
    m_outDir= destDir;
    m_qdox= new JavaDocBuilder();
    Class<IAnnotation>[] annotations= AnnotationHelper.getAllAnnotations();
    m_annotationMap= new HashMap<String, Class<IAnnotation>>(annotations.length);
    JDK14TagFactory factory= new JDK14TagFactory();
    for(Class<IAnnotation> clazz : annotations) {
      m_annotationMap.put(factory.getTagName(clazz), clazz);
    }
  }

  /**
   * @return
   */
  @SuppressWarnings("deprecation")
  public int convert() {
    for(File f : m_fileNames) {
      try {
        m_qdox.addSource(f);
      }
      catch(ParseException e) {
        ppp("Cannot parse file " + f.getAbsolutePath() + " - skipping");
      }
      catch(IOException e) {
        e.printStackTrace();

        return -1;
      }
    }
    int converted= 0;

    JavaSource[] sources= m_qdox.getSources();

    for(JavaSource source : sources) {
      File file= source.getFile();
      try {

        List<String> lines= fileToLines(file);
        int lineCount= lines.size();

        List<String> finalLines= insertAnnotations(source, lines);

        if(finalLines.size() > lineCount) {
          ppp("Writing file " + file.getAbsolutePath());
          writeFile(file, source.getPackage(), finalLines);
          converted++;
        }
      }
      catch(IOException ioe) {
        ppp("failed to process " + file);
        ioe.printStackTrace();
      }
    }

    return converted;
  }

  /**
   * Convert a file into a list of its lines
   *
   * @throws IOException
   */
  private List<String> fileToLines(File file) throws IOException {
    List<String> result= new ArrayList<String>();
    BufferedReader br= new BufferedReader(new FileReader(file));

    String line= br.readLine();
    while(null != line) {
      result.add(line);
      line= br.readLine();
    }

    return result;
  }

  private File getPackageOutputDir(File outDir, String packageName) {
    if(packageName == null) {
      packageName= "";
    }

    return new File(outDir, packageName.replace('.', File.separatorChar));
  }

  private List<String> insertAnnotations(JavaSource source, List<String> lines) {

    int oldLinesLength= lines.size();

    // We need to add lines from the end, otherwise earlier line numbers will get messed up
    AbstractJavaEntity[] entities= new AbstractJavaEntity[lines.size()];

    JavaClass[] classes= source.getClasses();
    if(classes != null) {
      for(JavaClass cd : classes) {
        iterateClassesFromSource(cd, entities);
      }
    }

    for(int i= entities.length; --i >= 0;) {
      AbstractJavaEntity entity= entities[i];
      if(entity != null) {
        DocletTag[] tags= entity.getTags();

        for(DocletTag tag : tags) {
          if(isTestNGTag(tag)) {
            lines.add(i - 1, lineForTag(tag));
          }
        }
      }
    }

    // Don't write the import if we didn't insert any lines
    if(oldLinesLength == lines.size()) {
      return lines;
    }

    //
    // Add import declaration to the top for the annotations
    //
    int lineCount= 0;
    for(String line : lines) {
      lineCount++;
      line= line.trim();
      if(line.startsWith("import")) {
        lines.add(lineCount - 1, "import org.testng.annotations.*;");

        break;
      }
    }

    return lines;
  }

  /**
   * @param cd
   * @param source
   * @param entities
   */
  private void iterateClassesFromSource(JavaClass cd, AbstractJavaEntity[] entities) {
    int lineNumber= cd.getLineNumber();
    if(lineNumber == 0) {
      System.out.append("Found class " + cd.getFullyQualifiedName() + " at line 0 in source "
                        + cd.getParentSource().getURL().toExternalForm() + "\n");
    }
    else {
      assert (entities[lineNumber] == null) : "Can't have class and method declarations in the same place!";
      entities[lineNumber]= cd;
    }

    // Now look through nested classes
    JavaClass[] childClasses= cd.getNestedClasses();
    for(JavaClass nc : childClasses) {
      iterateClassesFromSource(nc, entities);
    }

    JavaMethod[] methods= cd.getMethods();
    if(methods != null) {
      for(JavaMethod md : methods) {
        int methodLineNumber= md.getLineNumber();
        if(methodLineNumber == 0) {
          System.out.append("Found method " + md.getName() + " at line 0 in class "
                            + cd.getFullyQualifiedName() + " in source "
                            + cd.getParentSource().getURL().toExternalForm() + "\n");
        }
        else {
          assert (entities[methodLineNumber] == null) : "Can't have class and method declarations in the same place!";
          entities[methodLineNumber]= md;
        }
      }
    }
  }

  private void writeFile(File filePath, String packageName, List<String> lines) {
    String fileName= filePath.getName();
    File file= (m_outDir == null) ? filePath
                                  : new File(getPackageOutputDir(m_outDir, packageName), fileName);
    File parentDir= file.getParentFile();

    parentDir.mkdirs();

    FileWriter fw= null;
    BufferedWriter bw= null;
    try {
      fw= new FileWriter(file);
      bw= new BufferedWriter(fw);

      assert null != lines : "NO LINES FOR " + filePath;

      for(String l : lines) {
        bw.write(l);
        bw.write('\n');
      }

      ppp("Wrote " + file.getAbsolutePath());
    }
    catch(IOException e) {
      e.printStackTrace();
    }
    finally {
      try {
        if(null != bw) {
          bw.close();
        }
      }
      catch(IOException ioe) {
      }
      try {
        if(null != fw) {
          fw.close();
        }
      }
      catch(IOException ioe) {
      }
    }
  }
}
