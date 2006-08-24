package org.testng;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.testng.junit.JUnitDirectoryConverter;

/**
 * @author Andy.Glover
 *
 */
public class JUnitConverterTask extends Task {
  private File m_sourceDirectory;
  private File m_outputDirectory;
  private boolean m_useAnnotations;
  private String[] m_groups;
  
  public JUnitConverterTask() {
    super();
  }
  
  @Override
  public void execute() throws BuildException {
    this.validate();

    final JUnitDirectoryConverter convertor = new JUnitDirectoryConverter(
        m_sourceDirectory, m_outputDirectory, null, m_useAnnotations, m_groups);
    final int result = convertor.convert();

    if (result > -1) {
      this.log(result + " files generated");
    }
    else {
      //this could be better
      this.log("There were errors in file generation");
    }
  }
  
  private void validate() throws BuildException {
    if (this.m_outputDirectory == null || this.m_outputDirectory.equals("")) {
      throw new BuildException("OutputDir must have a value");
    }
    if (this.m_sourceDirectory == null || this.m_sourceDirectory.equals("")) {
      throw new BuildException("SourceDir must have a value");
    }
  }
  
  @Override
  public void init() throws BuildException {
    super.init();
    m_useAnnotations = true;
  }
  
  /**
   * Setter for report's output directory
   * @param outputDirectory
   */
  public void setOutputDir(final File outputDirectory) {
    m_outputDirectory = outputDirectory;
  }
  
  public void setGroups(String groups) {
    m_groups = groups.split("[ ,]");
  }
  
  /**
   * @param sourceDirectory
   */
  public void setSourceDir(final File sourceDirectory) {
    m_sourceDirectory = sourceDirectory;
  }
  
  /**
   * @param annotations
   */
  public void setAnnotations(final boolean annotations) {
    m_useAnnotations = annotations;
  }
}
