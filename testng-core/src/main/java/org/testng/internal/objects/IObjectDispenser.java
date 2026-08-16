package org.testng.internal.objects;

import org.jspecify.annotations.Nullable;
import org.testng.internal.objects.pojo.CreationAttributes;

/**
 * Represents the capabilities of an implementation that is capable of dispensing new Objects for
 * TestNG
 */
public interface IObjectDispenser {

  String GUICE_HELPER = "testng.guice-helper";

  @Nullable
  Object dispense(CreationAttributes attributes);

  /** @param dispenser - The {@link IObjectDispenser} to dispense */
  void setNextDispenser(IObjectDispenser dispenser);
}
