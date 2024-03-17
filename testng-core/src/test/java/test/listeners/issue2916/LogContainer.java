package test.listeners.issue2916;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.testng.internal.AutoCloseableLock;

public enum LogContainer {
  instance;

  private List<String> logs;

  private final AutoCloseableLock lock = new AutoCloseableLock();

  public void initialiseLogs() {
    try (AutoCloseableLock ignore = lock.lock()) {
      logs = new ArrayList<>();
    }
  }

  public void log(String line) {
    try (AutoCloseableLock ignore = lock.lock()) {
      Objects.requireNonNull(logs).add(line);
    }
  }

  public List<String> allLogs() {
    if (logs == null || logs.isEmpty()) {
      throw new IllegalStateException("Logs should have been initialised");
    }
    return Collections.synchronizedList(logs);
  }
}
