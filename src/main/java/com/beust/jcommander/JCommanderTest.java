package com.beust.jcommander;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import java.util.List;


public class JCommanderTest {

  @Parameter
  private List<String> m_rest;

  @Parameter(names = { "-src", "--sources" }, description = "The source directory")
  private String m_src;

  @Parameter(names = "-debug", description = "Debug mode")
  private boolean m_debug = false;

  @Parameter(names = "-port", description = "The port number")
  private Integer m_port = 0;

  @Parameter(names = "-testclass", description = "List of classes")
  private List<String> m_classes;

  public static void main(String[] a) {
    JCommanderTest m = new JCommanderTest();
    String[] args = new String[] {
//      "-src",
//        "@/tmp/b",
        "-testclass", "ClassB",
        "--sources", "/tmp",
        "-testclass", "ClassA",
        "-debug",
//        "-src", "/t",
//        "-port", "1234",
        "a", "b", "c",
    };
    JCommander jc = new JCommander(m);
    jc.parse(args);
//    jc.usage();
    System.out.println("Source:" + m.m_src);
    System.out.println("Debug:" + m.m_debug);
    System.out.println("Port:" + m.m_port);
    System.out.println("Classes:" + m.m_classes);
    System.out.println("Rest:" + m.m_rest);
  }

}
