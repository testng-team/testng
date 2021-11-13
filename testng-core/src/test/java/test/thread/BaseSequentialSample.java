package test.thread;

import java.util.Map;
import org.testng.log4testng.Logger;

public class BaseSequentialSample {

  protected void addId(String method, long id) {
    debug(method + " ID:" + id);
    getMap().put(id, id);
  }

  Map getMap() {
    Map result = Helper.getMap(getClass().getName());
    debug("RETURNING MAP " + result + " THIS:" + this);

    return result;
  }

  protected void debug(String s) {
    Logger.getLogger(getClass())
        .debug("[" + getClass().getName() + " " + Thread.currentThread().getId() + " " + "] " + s);
  }
}
