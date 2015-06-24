package org.testng.xml;

import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
  private static final List<ISuiteParser> PARSERS = Lists.newArrayList(DEFAULT_FILE_PARSER);
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
    init(fileName, null, null);
  }

  /**
   * Creates a parser that will try to find the DEFAULT_FILENAME from the jar.
   * @throws FileNotFoundException if the DEFAULT_FILENAME resource is not
   * found in the classpath.
   */
  public Parser() throws FileNotFoundException {
    init(null, null, null);
  }

  public Parser(InputStream is) {
    init(null, is, null);
  }

  private void init(String fileName, InputStream is, IFileParser fp) {
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

  /**
   * Returns an input stream on the resource named DEFAULT_FILENAME.
   *
   * @return an input stream on the resource named DEFAULT_FILENAME.
   * @throws FileNotFoundException if the DEFAULT_FILENAME resource is not
   * found in the classpath.
   */
//  private static InputStream getInputStream(String fileName) throws FileNotFoundException {
//    // Try to look for the DEFAULT_FILENAME from the jar
//    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
//    InputStream in;
//    // TODO CQ is this OK? should we fall back to the default classloader if the
//    // context classloader fails.
//    if (classLoader != null) {
//      in = classLoader.getResourceAsStream(fileName);
//    }
//    else {
//      in = Parser.class.getResourceAsStream(fileName);
//    }
//    if (in == null) {
//      throw new FileNotFoundException(fileName);
//    }
//    return in;
//  }

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
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws IOException if an I/O error occurs while parsing the test suite file or
   * if the default testng.xml file is not found.
   */
  public Collection<XmlSuite> parse()
    throws ParserConfigurationException, SAXException, IOException
  {
    // Each suite found is put in this list, using their canonical
    // path to make sure we don't add a same file twice
    // (e.g. "testng.xml" and "./testng.xml")
    List<String> processedSuites = Lists.newArrayList();
    XmlSuite resultSuite = null;

    List<String> toBeParsed = Lists.newArrayList();
    List<String> toBeAdded = Lists.newArrayList();
    List<String> toBeRemoved = Lists.newArrayList();

    if (m_fileName != null) {
      File mainFile = new File(m_fileName);
      toBeParsed.add(mainFile.getCanonicalPath());
    }

    /*
     * Keeps a track of parent XmlSuite for each child suite
     */
    Map<String, XmlSuite> childToParentMap = Maps.newHashMap();
    while (toBeParsed.size() > 0) {

      for (String currentFile : toBeParsed) {
        File currFile = new File(currentFile);
        File parentFile = currFile.getParentFile();
        InputStream inputStream = m_inputStream != null
            ? m_inputStream
            : new FileInputStream(currentFile);

        IFileParser<XmlSuite> fileParser = getParser(currentFile);
        XmlSuite currentXmlSuite = fileParser.parse(currentFile, inputStream, m_loadClasses);
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
        if (suiteFiles.size() > 0) {
          for (String path : suiteFiles) {
            String canonicalPath;
            if (parentFile != null && new File(parentFile, path).exists()) {
              canonicalPath = new File(parentFile, path).getCanonicalPath();
            } else {
              canonicalPath = new File(path).getCanonicalPath();
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
      for (String s : toBeRemoved) {
        toBeParsed.remove(s);
      }
      toBeRemoved = Lists.newArrayList();

      for (String s : toBeAdded) {
        toBeParsed.add(s);
      }
      toBeAdded = Lists.newArrayList();

    }

    //returning a list of single suite to keep changes minimum
    List<XmlSuite> resultList = Lists.newArrayList();
    resultList.add(resultSuite);

    boolean postProcess = true;

    if (postProcess && m_postProcessor != null) {
      return m_postProcessor.process(resultList);
    } else {
      return resultList;
    }

  }

  public List<XmlSuite> parseToList()
    throws ParserConfigurationException, SAXException, IOException
  {
    List<XmlSuite> result = Lists.newArrayList();
    Collection<XmlSuite> suites = parse();
    for (XmlSuite suite : suites) {
      result.add(suite);
    }

    return result;
  }



}

