package org.testng.xml;

import org.testng.collections.Lists;
import org.testng.collections.Maps;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * <code>Parser</code> is a parser for a TestNG XML test suite file.
 */
public class Parser {

  /** The name of the TestNG DTD. */
  public static final String TESTNG_DTD = "testng-1.0.dtd";

  /** The URL to the deprecated TestNG DTD. */
  public static final String DEPRECATED_TESTNG_DTD_URL = "http://beust.com/testng/" + TESTNG_DTD;

  /** The URL to the TestNG DTD. */
  public static final String TESTNG_DTD_URL = "http://testng.org/" + TESTNG_DTD;

  /** The default file name for the TestNG test suite if none is specified (testng.xml). */
  public static final String DEFAULT_FILENAME = "testng.xml";

  private static final ISuiteParser DEFAULT_FILE_PARSER = new SuiteXmlParser();
  private static final List<ISuiteParser> PARSERS = Lists.newArrayList();
  static {
    ServiceLoader<ISuiteParser> suiteParserLoader = ServiceLoader.load(ISuiteParser.class);
    for (ISuiteParser parser : suiteParserLoader) {
      PARSERS.add(parser);
    }
  }

  /** The file name of the xml suite being parsed. This may be null if the Parser
   * has not been initialized with a file name. TODO CQ This member is never used. */
  private String m_fileName;

  private InputStream m_inputStream;
  private IPostProcessor m_postProcessor;

  private boolean m_loadClasses = true;

  /**
   * Constructs a <code>Parser</code> to use the inputStream as the source of
   * the xml test suite to parse.
   * @param fileName the filename corresponding to the inputStream or null if
   * unknown.
   */
  public Parser(String fileName) {
    init(fileName, null);
  }

  /**
   * Creates a parser that will try to find the DEFAULT_FILENAME from the jar.
   * @throws FileNotFoundException if the DEFAULT_FILENAME resource is not
   * found in the classpath.
   */
  public Parser() throws FileNotFoundException {
    init(null, null);
  }

  public Parser(InputStream is) {
    init(null, is);
  }

  private void init(String fileName, InputStream is) {
    m_fileName = fileName != null ? fileName : DEFAULT_FILENAME;
    m_inputStream = is;
  }

  public void setPostProcessor(IPostProcessor processor) {
    m_postProcessor = processor;
  }

  /**
   * If false, don't try to load the classes during the parsing.
   */
  public void setLoadClasses(boolean loadClasses) {
    m_loadClasses = loadClasses;
  }

  private static IFileParser getParser(String fileName) {
    for (ISuiteParser parser : PARSERS) {
      if (parser.accept(fileName)) {
        return parser;
      }
    }

    return DEFAULT_FILE_PARSER;
  }

  /**
   * Parses the TestNG test suite and returns the corresponding XmlSuite,
   * and possibly, other XmlSuite that are pointed to by <suite-files>
   * tags.
   *
   * @return the parsed TestNG test suite.
   *
   * @throws IOException if an I/O error occurs while parsing the test suite file or
   * if the default testng.xml file is not found.
   */
  public Collection<XmlSuite> parse() throws IOException {
    // Each suite found is put in this list, using their canonical
    // path to make sure we don't add a same file twice
    // (e.g. "testng.xml" and "./testng.xml")
    List<String> processedSuites = Lists.newArrayList();
    XmlSuite resultSuite = null;

    List<String> toBeParsed = Lists.newArrayList();
    List<String> toBeAdded = Lists.newArrayList();
    List<String> toBeRemoved = Lists.newArrayList();

    if (m_fileName != null) {
      URI uri = constructURI(m_fileName);
      if (uri == null || uri.getScheme() == null) {
        uri = new File(m_fileName).toURI();
      }
      if ("file".equalsIgnoreCase(uri.getScheme())) {
        File mainFile = new File(uri);
        toBeParsed.add(mainFile.getCanonicalPath());
      } else {
        toBeParsed.add(uri.toString());
      }
    }

    /*
     * Keeps a track of parent XmlSuite for each child suite
     */
    Map<String, XmlSuite> childToParentMap = Maps.newHashMap();
    while (!toBeParsed.isEmpty()) {

      for (String currentFile : toBeParsed) {
        File parentFile = null;
        InputStream inputStream = null;

        if (hasFileScheme(currentFile)) {
          File currFile = new File(currentFile);
          parentFile = currFile.getParentFile();
          inputStream = m_inputStream != null ? m_inputStream : new FileInputStream(currFile);
        }

        IFileParser<XmlSuite> fileParser = getParser(currentFile);
        XmlSuite currentXmlSuite = fileParser.parse(currentFile, inputStream, m_loadClasses);
        currentXmlSuite.setParsed(true);
        processedSuites.add(currentFile);
        toBeRemoved.add(currentFile);

        if (childToParentMap.containsKey(currentFile)) {
           XmlSuite parentSuite = childToParentMap.get(currentFile);
           //Set parent
           currentXmlSuite.setParentSuite(parentSuite);
           //append children
           parentSuite.getChildSuites().add(currentXmlSuite);
        }

        if (null == resultSuite) {
           resultSuite = currentXmlSuite;
        }

        List<String> suiteFiles = currentXmlSuite.getSuiteFiles();
        if (!suiteFiles.isEmpty()) {
          for (String path : suiteFiles) {
            String canonicalPath = path;
            if (hasFileScheme(path)) {
              if (parentFile != null && new File(parentFile, path).exists()) {
                canonicalPath = new File(parentFile, path).getCanonicalPath();
              } else {
                canonicalPath = new File(path).getCanonicalPath();
              }
            }
            if (!processedSuites.contains(canonicalPath)) {
              toBeAdded.add(canonicalPath);
              childToParentMap.put(canonicalPath, currentXmlSuite);
            }
          }
        }
      }

      //
      // Add and remove files from toBeParsed before we loop
      //
      toBeParsed.removeAll(toBeRemoved);
      toBeRemoved = Lists.newArrayList();

      toBeParsed.addAll(toBeAdded);
      toBeAdded = Lists.newArrayList();

    }

    //returning a list of single suite to keep changes minimum
    List<XmlSuite> resultList = Lists.newArrayList();
    resultList.add(resultSuite);

    if (m_postProcessor != null) {
      return m_postProcessor.process(resultList);
    } else {
      return resultList;
    }

  }

  /**
   *
   * @param uri - The uri to be verified.
   * @return - <code>true</code> if the uri has "file:" as its scheme.
   */
  public static boolean hasFileScheme(String uri) {
    URI constructedURI = constructURI(uri);
    if (constructedURI == null) {
      //There were difficulties in constructing the URI. Falling back to considering the URI as a file.
      return true;
    }
    String scheme = constructedURI.getScheme();
    //A URI is regarded as having a file scheme if it either has its scheme as "file"
    //(or) if the scheme is null (which is true when uri's represent local file system path.)
    return scheme == null || "file".equalsIgnoreCase(scheme);
  }

  public List<XmlSuite> parseToList() throws IOException {
    return Lists.newArrayList(parse());
  }

  public static Collection<XmlSuite> parse(String suite, IPostProcessor processor) throws IOException {
    return newParser(suite, processor).parse();
  }

  public static Collection<XmlSuite> parse(InputStream is, IPostProcessor processor) throws IOException {
    return newParser(is, processor).parse();
  }
  
  public static boolean canParse(String fileName) {
    for (ISuiteParser parser : PARSERS) {
      if (parser.accept(fileName)) {
        return true;
      }
    }
      
    return DEFAULT_FILE_PARSER.accept(fileName);
  }

  private static Parser newParser(String path, IPostProcessor processor) {
    Parser result = new Parser(path);
    result.setPostProcessor(processor);
    return result;
  }

  private static Parser newParser(InputStream is, IPostProcessor processor) {
    Parser result = new Parser(is);
    result.setPostProcessor(processor);
    return result;
  }

  private static URI constructURI(String text) {
    try {
      return URI.create(text);
    } catch (Exception e) {
      return null;
    }
  }

}

