package org.testng;


import java.io.File;

import java.util.HashMap;
import java.util.Map;

import org.testng.internal.AnnotationDirectoryConverter;

/**
 * Converts between javadoc annotations and java 5 annotations
 * @author micheb10 12-Sep-2006
 * @since 5.3
 */
public class AnnotationConverter {
  private static final String SRC_DIR_OPT= "-srcdir";
  private static final String OUT_DIR_OPT= "-d";
  private static final String OVERWRITE_OPT= "-overwrite";
  private static final String QUIET= "-quiet";

  private static int m_logLevel= 1;

  /**
   * @param args
   */
  public static void main(String[] args) {
    if(args.length == 0) {
      usage();

      return;
    }

    Map params= extractOptions(args);

    if(null != params.get(QUIET)) {
      m_logLevel= 0;
    }
    if(null == params.get(SRC_DIR_OPT)) {
      TestNG.exitWithError("The source directory cannot be null");
    }

    String srcPath= (String) params.get(SRC_DIR_OPT);
    File src= new File(srcPath);

    if(!src.exists() || !src.isDirectory()) {
      TestNG.exitWithError("Invalid source directory: " + src.getAbsolutePath());
    }

    boolean overwrite= null != params.get(OVERWRITE_OPT);

    if((null != params.get(OUT_DIR_OPT)) && overwrite) {
      TestNG.exitWithError("Cannot use both -d and --overwrite options");
    }

    if((null == params.get(OUT_DIR_OPT)) && !overwrite) {
      TestNG.exitWithError("One of -d and --overwrite options is required");
    }

    File outFile= overwrite ? null : new File((String) params.get(OUT_DIR_OPT));
    AnnotationDirectoryConverter convertor= new AnnotationDirectoryConverter(new File(srcPath), outFile);
    int result= convertor.convert();

    switch(result) {
      case -1: {
        log("Generation failed. Consult messages.");

        break;
      }
      case 0: {
        log("No tests were generated");
      }
      default:
      {
        log(result + " tests were generated");
      }
    }
  }

  /**
   * Prints usage info to console.
   */
  private static void usage() {
    System.out.println("Converts Javadoc annotated test cases to Java 5 annotated cases");
    System.out.println("Usage: java -cp <> org.testng.AnnotationConverter "
                       + "-srcdir <source_dir> (-d <output_dir> | -overwrite)"
                       + " [-quiet]");
    System.out.println("");
    System.out.println("-srcdir\t Source directory containing original tests");
    System.out.println("-d\t\t Output directory for resulting TestNG tests");
    System.out.println("-overwrite\t Overwrite the original files with the new ones"
                       + "\n\t\t The flag cannot be used when -d is used.");
    System.out.println("-quiet\t Don't display any output");
  }

  /**
   * Extract command line options.
   */
  private static Map extractOptions(String[] args) {
    Map options= new HashMap();

    for(int i= 0; i < args.length; i++) {
      if(SRC_DIR_OPT.equals(args[i])) {
        if((i + 1) < args.length) {
          options.put(SRC_DIR_OPT, args[i + 1]);
          i++;
        }
      }
      else if(OUT_DIR_OPT.equals(args[i])) {
        if((i + 1) < args.length) {
          options.put(OUT_DIR_OPT, args[i + 1]);
          i++;
        }
      }
      else if(OVERWRITE_OPT.equals(args[i])) {
        options.put(OVERWRITE_OPT, Boolean.TRUE);
      }
      else if(QUIET.equals(args[i])) {
        options.put(QUIET, Boolean.TRUE);
      }
    }

    return options;
  }

  public static void log(String s) {
    if(getLogLevel() > 0) {
      System.out.println("[AnnotationConverter] " + s);
    }
  }

  public static int getLogLevel() {
    return m_logLevel;
  }

  public static void ppp(String s) {
    System.out.println("[AnnotationConverter] " + s);
  }

}
