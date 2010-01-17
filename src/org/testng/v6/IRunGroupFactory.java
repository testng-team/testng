package org.testng.v6;

public interface IRunGroupFactory {

  RunGroup getRunGroup(int type, String name);
  
  Integer findRunGroup(int type, String name);

}
