package org.testng.internal.thread.graph;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.testng.IDynamicGraph;
import org.testng.annotations.Test;
import org.testng.internal.DynamicGraph;
import org.testng.thread.IWorker;

public class GraphOrchestratorTest {

  /**
   * A worker that throws -- typically because a listener threw -- still has to be marked finished,
   * or the graph never reaches its final state and the run hangs (see GITHUB-3238). That leaves the
   * orchestrator as the only place that ever sees the throwable, and it used to discard it, so the
   * cause of the failure was reachable only under a debugger. See GITHUB-3243.
   */
  @Test(description = "GITHUB-3243")
  public void ensureTheThrowableThatEndedAWorkerIsKept() throws InterruptedException {
    RuntimeException failure = new RuntimeException("Failed on purpose");
    DynamicGraph<String> graph = new DynamicGraph<>();
    graph.addNode("a");
    ExecutorService service = Executors.newSingleThreadExecutor();

    GraphOrchestrator<String> orchestrator =
        new GraphOrchestrator<>(
            service,
            freeNodes ->
                List.of(
                    new SampleWorker(
                        freeNodes,
                        () -> {
                          throw failure;
                        })),
            graph,
            null);
    orchestrator.run();

    assertThat(orchestrator.awaitCompletion(10, TimeUnit.SECONDS))
        .withFailMessage("a worker that throws must not stop the graph from finishing")
        .isTrue();
    assertThat(graph.getNodeCountWithStatus(IDynamicGraph.Status.FINISHED))
        .withFailMessage("the node of a worker that threw must still be marked finished")
        .isEqualTo(1);
    assertThat(orchestrator.getFailures())
        .withFailMessage("the throwable that ended the worker is the only trace of what went wrong")
        .hasSize(1);
    assertThat(orchestrator.getFailures().get(0))
        .isInstanceOf(ExecutionException.class)
        .hasCause(failure);
  }

  @Test(description = "GITHUB-3243")
  public void ensureAGraphThatRanCleanlyReportsNoFailure() throws InterruptedException {
    DynamicGraph<String> graph = new DynamicGraph<>();
    graph.addNode("a");
    ExecutorService service = Executors.newSingleThreadExecutor();

    GraphOrchestrator<String> orchestrator =
        new GraphOrchestrator<>(
            service, freeNodes -> List.of(new SampleWorker(freeNodes, () -> {})), graph, null);
    orchestrator.run();

    assertThat(orchestrator.awaitCompletion(10, TimeUnit.SECONDS)).isTrue();
    assertThat(orchestrator.getFailures()).isEmpty();
  }

  private static class SampleWorker implements IWorker<String> {

    private final List<String> tasks;
    private final Runnable body;

    SampleWorker(List<String> tasks, Runnable body) {
      this.tasks = tasks;
      this.body = body;
    }

    @Override
    public void run() {
      body.run();
    }

    @Override
    public List<String> getTasks() {
      return tasks;
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
