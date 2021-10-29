package org.testng.internal;

public interface IClassHierarchyPriority {
  /**
   * Gets priority for class hierarchy sorting. There is no guaranty that methods will be sorted in
   * a class hierarchy ordering, if dependsOnMethods or Groups are used they will have higher
   * priority in the sort.
   *
   * @return class hierarchy priority
   */
  int getClassHierarchyPriority();

  void setClassHierarchyPriority(int priority);
}
