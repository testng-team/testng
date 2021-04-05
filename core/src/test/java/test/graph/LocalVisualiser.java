package test.graph;

import org.testng.IExecutionVisualiser;
import org.testng.collections.Lists;

import java.util.List;

public class LocalVisualiser implements IExecutionVisualiser {
  private List<String> definitions = Lists.newArrayList();
  private static LocalVisualiser instance;

  public LocalVisualiser() {
    setInstance(this);
  }

  private static void setInstance(LocalVisualiser visualiser) {
    instance = visualiser;
  }

  public static LocalVisualiser getInstance() {
    return instance;
  }

  @Override
  public void consumeDotDefinition(String dotDefinition) {
    definitions.add(dotDefinition.replaceAll("\\n", ""));
  }

  public List<String> getDefinitions() {
    return definitions;
  }
}
