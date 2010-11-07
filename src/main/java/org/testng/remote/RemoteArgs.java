package org.testng.remote;

import com.beust.jcommander.Parameter;

public class RemoteArgs {
  public static final String PORT = "-serport";
  @Parameter(names = PORT, description = "The port for the serialization protocol")
  public Integer serPort;
}
