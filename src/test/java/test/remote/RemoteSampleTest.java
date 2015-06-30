package test.remote;

import org.testng.annotations.Test;

import java.io.Serializable;

/**
 * Used by RemoteTest to test RemoteTestNG.
 *
 * @author Cedric Beust <cedric@beust.com>
 *
 */
public class RemoteSampleTest implements Serializable {

  private static final long serialVersionUID = -3179613214871596418L;

  @Test
  public void f1() {}

  @Test
  public void f2() {}
}
