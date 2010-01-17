package org.testng.internal;

import org.testng.ITestNGMethod;
import org.testng.xml.XmlTest;

import java.util.List;
import java.util.Set;

public interface IWorkerFactory {
  List<IMethodWorker> createWorkers(XmlTest xmlTest, Set<ITestNGMethod> methods);
}
