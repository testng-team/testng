package org.testng.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * This class abstracts hosts.properties
 * 
 * @author cbeust
 * @date Jan 1, 2006
 */
public class HostFile {
  /**
   * Properties allowed in hosts.properties
   */
  public static final String HOSTS = "testng.hosts";
  public static final String STRATEGY = "testng.strategy";
  public static final String VERBOSE = "testng.verbose";
  
  /**
   * Values allowed for STRATEGY
   */
  public static final String STRATEGY_TEST = "test";
  public static final String STRATEGY_SUITE = "suite";
  
  private Properties m_properties = new Properties();
  
  public HostFile(String fileName) {
    //
    // Parse the hosts file
    //
    try {
      FileInputStream fis = new FileInputStream(new File(fileName));
      m_properties.load(fis);
    }
    catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    catch (IOException e) {
      e.printStackTrace();
    }    
  }
  
  public String[] getHosts() {
    String hostLine = m_properties.getProperty(HOSTS);
    return hostLine.split(" ");
  }
  
  public boolean isStrategyTest() {
    String strategy = m_properties.getProperty(STRATEGY, STRATEGY_SUITE);
    boolean result = false;
    if (STRATEGY_TEST.equalsIgnoreCase(strategy)) {
      result = true;
    }    
    
    return result;
  }
  
  public int getVerbose() {
    String v = m_properties.getProperty(VERBOSE, "1");
    return Integer.parseInt(v);
  }
}
