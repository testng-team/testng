package org.testng.internal.thread.graph;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import org.jspecify.annotations.Nullable;
import org.testng.annotations.Test;
import org.testng.thread.IWorker;

public class TestNGFutureTaskTest {

  @Test(description = "GITHUB-3238")
  public void ensureCallbackReceivesTheWorkerWhenItCompletesExceptionally() {
    RuntimeException failure = new RuntimeException("Failed on purpose");
    IWorker<String> worker =
        new SampleWorker(
            () -> {
              throw failure;
            });
    RecordingCallback callback = new RecordingCallback();

    new TestNGFutureTask<>(worker, callback).run();

    assertThat(callback.reported)
        .withFailMessage(
            "the orchestrator cannot update the graph unless it is told which worker finished")
        .isSameAs(worker);
    assertThat(callback.error)
        .withFailMessage("the failure that ended the worker must not be discarded")
        .isInstanceOf(ExecutionException.class)
        .hasCause(failure);
  }

  @Test(description = "GITHUB-3238")
  public void ensureCallbackReceivesTheWorkerWhenItCompletesNormally() {
    IWorker<String> worker = new SampleWorker(() -> {});
    RecordingCallback callback = new RecordingCallback();

    new TestNGFutureTask<>(worker, callback).run();

    assertThat(callback.reported).isSameAs(worker);
    assertThat(callback.error).isNull();
  }

  /** The task runs on the calling thread, so plain fields are enough to capture the callback. */
  private static class RecordingCallback
      implements BiConsumer<IWorker<String>, @Nullable Throwable> {

    // Both stay null until the callback fires, and error stays null on a clean completion.
    private @Nullable IWorker<String> reported;
    private @Nullable Throwable error;

    @Override
    public void accept(IWorker<String> worker, @Nullable Throwable throwable) {
      this.reported = worker;
      this.error = throwable;
    }
  }

  private static class SampleWorker implements IWorker<String> {

    private final Runnable body;

    SampleWorker(Runnable body) {
      this.body = body;
    }

    @Override
    public void run() {
      body.run();
    }

    @Override
    public List<String> getTasks() {
      return List.of("task");
    }

    @Override
    public long getTimeOut() {
      return 0;
    }

    @Override
    public int getPriority() {
      return 0;
    }

    @Override
    public int compareTo(IWorker<String> other) {
      return 0;
    }
  }
}
