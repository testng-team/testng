package com.beust.jcommander;

import org.testng.collections.Lists;
import org.testng.collections.Maps;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class JCommander {
  private Map<String, ParameterDescription> m_descriptions;
  private Object m_object;

  public JCommander(Object object) {
    m_object = object;
  }

  public JCommander(Object object, String[] args) {
    m_object = object;
    parse(args);
  }

  public void parse(String[] args) {
    createDescriptions();
    parseValues(expandArgs(args));
  }

  /**
   * Expand the command line parameters to take @ parameters into account.
   * When @ is encountered, the content of the file that follows is inserted
   * in the command line
   * @param originalArgv the original command line parameters
   * @return the new and enriched command line parameters
   */
  private static String[] expandArgs(String[] originalArgv) {
    List<String> vResult = Lists.newArrayList();
    
    for (String arg : originalArgv) {

      if (arg.startsWith("@")) {
        String fileName = arg.substring(1);
        vResult.addAll(readFile(fileName));
      }
      else {
        vResult.add(arg);
      }
    }
    
    return vResult.toArray(new String[vResult.size()]);
  }

  /**
   * Reads the file specified by filename and returns the file content as a string.
   * End of lines are replaced by a space
   * 
   * @param fileName the command line filename
   * @return the file content as a string.
   */
  public static List<String> readFile(String fileName) {
    List<String> result = Lists.newArrayList();

    try {
      BufferedReader bufRead = new BufferedReader(new FileReader(fileName));

      String line;

      // Read through file one line at time. Print line # and line
      while ((line = bufRead.readLine()) != null) {
        result.add(line);
      }

      bufRead.close();
    }
    catch (IOException e) {
      throw new ParameterException("Could not read file " + fileName + ": " + e);
    }

    return result;
  }

  /**
   * @param string
   * @return
   */
  private static String trim(String string) {
    String result = string.trim();
    if (result.startsWith("\"")) {
      if (result.endsWith("\"")) {
          return result.substring(1, result.length() - 1);
      } else {
          return result.substring(1);
      }
    } else {
      return result;
    }
  }

  private void createDescriptions() {
    m_descriptions = Maps.newHashMap();
    Class<?> cls = m_object.getClass();
    for (Field f : cls.getDeclaredFields()) {
      p("Field:" + f.getName());
      f.setAccessible(true);
      Annotation annotation = f.getAnnotation(Parameter.class);
      if (annotation != null) {
        Parameter p = (Parameter) annotation;
        p("Adding description for " + p.name());
        ParameterDescription pd = new ParameterDescription(m_object, p, f);
        m_descriptions.put(p.name(), pd);
      }
    }
  }

  private void p(String string) {
    System.out.println("[JCommander] " + string);
  }

  private void parseValues(String[] args) {
    for (int i = 0; i < args.length; i++) {
      String a = trim(args[i]);
      if (a.startsWith("-")) {
        ParameterDescription pd = m_descriptions.get(a);
        if (pd != null) {
          Class<?> fieldType = pd.getField().getType();
          if (fieldType == boolean.class || fieldType == Boolean.class) {
            pd.addValue(Boolean.TRUE);
          } else if (i + 1 < args.length) {
            pd.addValue(args[i + 1]);
          } else {
            throw new ParameterException("Parameter expected after " + args[i]);
          }
          i++;
        } else {
          throw new ParameterException("Unknown option: " + a);
        }
      }
    }
  }

  public void usage() {
    System.out.println("Usage:");
    for (ParameterDescription pd : m_descriptions.values()) {
      System.out.println("\t" + pd.getName() + "\t" + pd.getDescription());
    }
  }
}
