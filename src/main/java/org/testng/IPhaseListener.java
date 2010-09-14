package org.testng;

import org.testng.phase.PhaseClassEvent;
import org.testng.phase.PhaseGroupEvent;
import org.testng.phase.PhaseMethodEvent;
import org.testng.phase.PhaseSuiteEvent;
import org.testng.phase.PhaseTestEvent;

public interface IPhaseListener extends ITestNGListener {

  void onSuiteEvent(PhaseSuiteEvent event);

  void onTestEvent(PhaseTestEvent event);

  void onGroupEvent(PhaseGroupEvent event);

  void onClassEvent(PhaseClassEvent event);

  void onMethodEvent(PhaseMethodEvent event);
}
