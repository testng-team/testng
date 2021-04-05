package org.testng.asserts;

/** Life cycle methods for the assertion class. */
public interface IAssertLifecycle {
  /**
   * Run the assert command in parameter.
   *
   * @param assertCommand The assertion
   */
  void executeAssert(IAssert<?> assertCommand);

  /**
   * Invoked when an assert succeeds.
   *
   * @param assertCommand The assertion
   */
  void onAssertSuccess(IAssert<?> assertCommand);

  /**
   * Invoked when an assert fails.
   *
   * @param assertCommand The assertion
   * @param ex The error
   */
  void onAssertFailure(IAssert<?> assertCommand, AssertionError ex);

  /**
   * Invoked before an assert is run.
   *
   * @param assertCommand The assertion
   */
  void onBeforeAssert(IAssert<?> assertCommand);

  /**
   * Invoked after an assert is run.
   *
   * @param assertCommand The assertion
   */
  void onAfterAssert(IAssert<?> assertCommand);
}
