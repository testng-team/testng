package org.testng.internal.objects;

/**
 * Supports Object instantiation taking into account Dependency Injection.
 */
public final class Dispenser {

  private Dispenser() {
    //Defeat instantiation
  }

  /**
   * @return - An {@link IObjectDispenser} that backed by the chain of responsibilities.
   */
  public static IObjectDispenser newInstance() {
    GuiceBasedObjectDispenser dispenser = new GuiceBasedObjectDispenser();
    dispenser.setNextDispenser(new SimpleObjectDispenser());
    return dispenser;
  }
}
