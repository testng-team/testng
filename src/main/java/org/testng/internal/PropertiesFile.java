package org.testng.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * This class loads and abstracts remote.properties
 *
 * @author cbeust
 * @author Guy Korland
 * @since April 13, 2006
 */
public class PropertiesFile {

  private Properties m_properties = new Properties();

  /**
   * Loads a Properties file.
   *
   * @param fileName properties file path
   * @throws IOException if an error occurred when reading from the Properties file.
   */
  public PropertiesFile(String fileName) throws IOException
  {
	  FileInputStream fis = null;
	  //
	  // Parse the Properties file
	  //
	  try {
		  fis = new FileInputStream(new File(fileName));
		  m_properties.load(fis);
	  }
	  finally
	  {
		  if( fis != null) {
        fis.close();
      }
	  }
  }

  /**
   * Returns the properties loaded.
   * @return loaded properties.
   */
  public Properties getProperties()
  {
	  return m_properties;
  }
}
