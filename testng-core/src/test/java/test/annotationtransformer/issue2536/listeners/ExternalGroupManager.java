package test.annotationtransformer.issue2536.listeners;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

public class ExternalGroupManager implements IAnnotationTransformer {

  private final Map<String, Set<String>> groupDefinitions = new HashMap<>();

  public Map<String, Set<String>> getGroupDefinitions() {
    return groupDefinitions;
  }

  @Override
  public void transform(
      ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {

    String fullName = null;

    if (testMethod != null) {
      fullName = testMethod.getDeclaringClass().getTypeName() + "." + testMethod.getName();
    } else if (testClass != null) {

      fullName = testClass.getTypeName();
    }

    if (fullName != null) {
      if (groupDefinitions.containsKey(fullName)) {
        Set<String> data = new HashSet<>(groupDefinitions.get(fullName));
        data.addAll(Arrays.asList(annotation.getGroups()));
        String[] fullGroup = data.toArray(new String[0]);
        annotation.setGroups(fullGroup);
      } else {
        Set<String> data = new HashSet<>(Arrays.asList(annotation.getGroups()));
        data.add("egm-notselected");
        String[] fullGroup = data.toArray(new String[0]);
        annotation.setGroups(fullGroup);
      }
    }
  }

  public void addGroupDefinition(String in_methodFullName, String in_groupName) {
    getGroupDefinitions()
        .computeIfAbsent(in_methodFullName, k -> new HashSet<>())
        .add(in_groupName);
  }
}
