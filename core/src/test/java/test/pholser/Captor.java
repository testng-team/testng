package test.pholser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:pholser@thoughtworks.com">Paul Holser</a>
 * @version $Id: Captor.java,v 1.3 2004/08/26 22:25:22 cedric Exp $
 */
public class Captor {
  private static Captor instance = null;
  private List<String> captives;

  public static Captor instance() {
    if (null == instance) {
      instance = new Captor();
    }
    return instance;
  }

  public static void reset() {
//    System.out.println("@@PHOLSER RESETTING CAPTOR");
    instance().captives = new ArrayList<>();
  }

  public void capture( String aString ) {
//    System.out.println("@@PHOLSER CAPTURING " + aString);
    captives.add( aString );
  }

  public List<String> captives() {
    return Collections.unmodifiableList( captives );
  }
}
