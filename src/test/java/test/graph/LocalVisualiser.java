package test.graph;

import java.util.List;
import org.testng.IExecutionVisualiser;
import org.testng.collections.Lists;

public class LocalVisualiser implements IExecutionVisualiser {

  private static LocalVisualiser instance;
  private List<String> definitions = Lists.newArrayList();

  public LocalVisualiser() {
    setInstance(this);
  }

  public static LocalVisualiser getInstance() {
    return instance;
  }

  private static void setInstance(LocalVisualiser visualiser) {
    instance = visualiser;
  }

  @Override
  public void consumeDotDefinition(String dotDefinition) {
    definitions.add(dotDefinition.replaceAll("\\n", ""));
  }

  public List<String> getDefinitions() {
    return definitions;
  }
}
