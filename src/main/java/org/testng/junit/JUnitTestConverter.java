package org.testng.junit;


import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.Doclet;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.SourcePosition;
import com.sun.tools.javadoc.Main;

import org.testng.JUnitConverter;
import org.testng.collections.Lists;
import org.testng.collections.Maps;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * This class converts a file to TestNG if it's a JUnit test class.
 * 
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 * @author <a href="mailto:the_mindstorm@evolva.ro">the_mindstorm</a>
 */
public class JUnitTestConverter extends Doclet {
  private static File[] m_fileNames = null;
  private static List<String> m_classNames = Lists.newArrayList();
  // Files and their test methods
  private static Map<File, List<MethodDoc>> m_files = Maps.newHashMap();
  private static Map<File, List<String>> m_fileLines = Maps.newHashMap();
  private static Map<File, String> m_packageNames = Maps.newHashMap();
  private static Map<File, Integer> m_typelines = Maps.newHashMap();
  private File m_outDir = null;
  private boolean m_useAnnotations;
  private boolean m_done;
  private String[] m_groups;

  /**
   * @param fileNames
   */
  public JUnitTestConverter(File[] fileNames, File outDir, String release,
      boolean useAnnotations, String[] groups) 
  {
    m_fileNames = fileNames;
    m_outDir = outDir;
    m_useAnnotations = useAnnotations;
    m_groups = groups;

    Collection<String> argv = Lists.newArrayList();

    argv.add("-quiet");
    if (null != release && !"".equals(release)) {
      argv.add("-source");
      argv.add(release);
    }

    argv.add("-doclet");
    argv.add("org.testng.junit.JUnitTestConverter");

    for (File fn : fileNames) {
      argv.add(fn.getAbsolutePath());
    }

    String[] newArgv = argv.toArray(new String[argv.size()]);

    m_done = 0 == Main.execute(newArgv);
  }

  private static File findFileName(ClassDoc cd) {
    for (File fn : m_fileNames) {
      if (fn.getAbsolutePath().endsWith(cd.name() + ".java")) {
        return fn;
      }
    }

    assert false : "COULDN'T FIND FILE " + cd.name();
    return null;
  }

  /**
   * This method is required for all doclets.
   * 
   * @return true on success.
   */
  public static boolean start(RootDoc root) {
    ClassDoc[] classes = root.classes();

    for (ClassDoc cd : classes) {
      if (!isJUnitTest(cd)) {
        continue;
      }

      if(!cd.isAbstract())
        m_classNames.add(cd.qualifiedTypeName());

      File file;
      if (null != cd.position().file()) {
        file = cd.position().file();
      } else {
        file = findFileName(cd);
      }

      String fqn = cd.qualifiedTypeName();
      int tn = fqn.indexOf(cd.typeName());
      if (tn > 0) {
        m_packageNames.put(file, fqn.substring(0, tn - 1));
      }
      m_typelines.put(file, new Integer(cd.position().line()));

      MethodDoc[] methods = cd.methods();
      List<MethodDoc> testMethods = Lists.newArrayList();

      for (MethodDoc md : methods) {
        if (isTest(md) || isSetUp(md) || isTearDown(md)) {
          // Add the lines backward, so we can insert them without messing
          // up their order later
          testMethods.add(0, md);
        }
      }

      m_files.put(file, testMethods);
    }

    return true;
  }

  /**
   * Query the superclasses in order to find out if the current classdoc is from
   * a real <CODE>TestCase</CODE>.
   */
  private static boolean isJUnitTest(ClassDoc clsDoc) {
    if (clsDoc.isInterface()) {
      return false;
    }

    ClassDoc superCls = clsDoc.superclass();

    while (null != superCls
        && !"java.lang.Object".equals(superCls.qualifiedTypeName())) {
      if ("junit.framework.TestCase".equals(superCls.qualifiedTypeName())
          || "TestCase".equals(superCls.typeName())) {
        return true;
      }

      superCls = superCls.superclass();
    }

    return false;
  }

  private static boolean isTest(MethodDoc md) {
    return md.name().startsWith("test");
  }

  private static boolean isSetUp(MethodDoc md) {
    return "setUp".equals(md.name());
  }

  private static boolean isTearDown(MethodDoc md) {
    return "tearDown".equals(md.name());
  }

  public int convert() {
    if (!m_done) {
      return -1;
    }

    int converted = 0;
    for (File file : m_files.keySet()) {
      try {
        List<String> lines = fileToLines(file);
        List<String> finalLines = m_useAnnotations ? insertAnnotations(m_files
            .get(file), lines) : insertJavadoc(file, m_files
            .get(file), lines);

        m_fileLines.put(file, finalLines);

        writeFile(file);
        converted++;
      }
      catch (IOException ioe) {
        ppp("failed to process " + file);
        ioe.printStackTrace();
      }
    }

    return converted;
  }

  private void writeFile(File filePath) {
    String fileName = filePath.getName();
    File file = new File(getPackageOutputDir(m_outDir, m_packageNames.get(filePath)), fileName);
    File parentDir = file.getParentFile();

    parentDir.mkdirs();

    FileWriter fw = null;
    BufferedWriter bw = null;
    try {
      fw = new FileWriter(file);
      bw = new BufferedWriter(fw);

      List<String> lines = m_fileLines.get(filePath);
      assert null != lines : "NO LINES FOR " + filePath;

      for (String l : lines) {
        bw.write(l);
        bw.write('\n');
      }

      ppp("Wrote " + file.getAbsolutePath());
    }
    catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        if (null != bw) {
          bw.close();
        }
      }
      catch (IOException ioe) {
      }
      try {
        if (null != fw) {
          fw.close();
        }
      }
      catch (IOException ioe) {
      }
    }
  }

  private List<String> insertAnnotations(List<MethodDoc> methodDocs,
      List<String> lines) {
    //
    // Add import
    //
    int lineCount = 0;
    for (String line : lines) {
      lineCount++;
      line = line.trim();
      if(line.startsWith("import")) {
        lines.add(lineCount - 1, "import org.testng.annotations.Test;");
        lines.add(lineCount - 1, "import org.testng.annotations.BeforeMethod;");
        lines.add(lineCount - 1, "import org.testng.annotations.AfterMethod;");
        break;
      }
    }
    
    String groupsLine = createGroupsLine(m_groups);

    //
    // Add annotations
    //
    for (MethodDoc md : methodDocs) {
      SourcePosition sp = md.position();
      int line = sp.line() + 2;
      if (isTest(md)) {
        lines.add(line, "  @Test" + groupsLine);
      } 
      else if (isSetUp(md)) {
        ppp("ADDING NEW BEFORE AT " + line);
        lines.add(line, "  @BeforeMethod" + groupsLine);
      } 
      else if (isTearDown(md)) {
        lines.add(line, "  @AfterMethod" + groupsLine);
      }
    }

    return lines;
  }

  private String createGroupsLine(String[] groups) {
    StringBuffer result = new StringBuffer();
    
    if (groups != null) {
      result.append("(groups = {");
      for (int i = 0; i < groups.length; i++) {
        if (i > 0) result.append(", ");
        result.append("\""+ groups[i] + "\"");
      }
      result.append("})");
    }
     
    return result.toString();
  }

  private List<String> insertJavadoc(File file,
      List<MethodDoc> methodDocs, List<String> lines) {
    for (int i = 0; i < methodDocs.size(); i++) {
      MethodDoc md = methodDocs.get(i);

      int insertLineNo;

      if (i + 1 < methodDocs.size()) {
        insertLineNo = findCommentLine(lines, md, methodDocs.get(i + 1)
            .position().line());
      } else {
        insertLineNo = findCommentLine(lines, md, m_typelines.get(file)
            .intValue());
      }

      int realInsert = insertLineNo == 0 ? md.position().line() - 1
          : insertLineNo;

      if (insertLineNo == 0) {
        lines.add(realInsert, "    */");
      }

      if (isTest(md)) {
        lines.add(realInsert, "    * @testng.test");
      } else if (isSetUp(md)) {
        lines.add(realInsert,
            "    * @testng.before-method");
      } else if (isTearDown(md)) {
        lines.add(realInsert,
            "    * @testng.after-method");
      }

      if (insertLineNo == 0) {
        lines.add(realInsert, "   /**");
      }
    }

    return lines;
  }

  private File getPackageOutputDir(File outDir, String packageName) {
    if (packageName == null) packageName = "";
    return new File(outDir, packageName.replace('.', File.separatorChar));
  }

  private int findCommentLine(List<String> lines, MethodDoc md, int minLine) {
    for (int i = md.position().line() - 1; i > minLine; i--) {
      String line = lines.get(i);
      if (line.indexOf("*/") != -1 && line.indexOf("/**") == -1) {
        return i;
      } else if (line.indexOf("*/") != -1 && line.indexOf("/**") != -1) { 
        // HINT /** and */ on same line: must split
        StringBuffer buf = new StringBuffer(line);
        int idx = buf.indexOf("*/");
        buf.deleteCharAt(idx).deleteCharAt(idx);
        lines.set(i, buf.toString());
        lines.add(i + 1, "    */");

        return i + 1;
      }
    }

    return 0;
  }

  /**
   * Convert a file into a list of its lines
   * 
   * @throws IOException
   */
  private List<String> fileToLines(File file) throws IOException {
    List<String> result = Lists.newArrayList();
    BufferedReader br = new BufferedReader(new FileReader(file));

    String line = br.readLine();
    while (null != line) {
      result.add(line);
      line = br.readLine();
    }

    return result;
  }

  static private void ppp(String s) {
    if (JUnitConverter.getLogLevel() >= 1) {
      System.out.println("[JUnitTestConverter]" + s);
    }
  }

  public String[] getClassNames() {
    return m_classNames.toArray(new String[m_classNames.size()]);
  }

}
