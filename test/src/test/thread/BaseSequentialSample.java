package test.thread;

import java.util.Map;

public class BaseSequentialSample {

  protected void addId(long id) {
    ppp("ADDING ID " + id);
    getMap().put(id, id);
  }

  Map getMap() {
    Map result = Helper.getMap(getClass().getName());
    ppp("RETURNING MAP " + result + " THIS:" + this);
    
    return result;
  }

  protected void ppp(String s) {
    if (false) {
      System.out.println("[" + getClass().getName() + "] " + s);
    }
  }
}
