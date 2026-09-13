package org.testng.parameters.samples.resolver;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.testng.IParameterResolver;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;

/** Answers a fresh {@link CustomObject} every time, and keeps every answer it gave. */
public class CountingParameterResolver implements IParameterResolver {

  private final List<CustomObject> answers = Collections.synchronizedList(new ArrayList<>());

  @Override
  public boolean supportsParameter(
      Parameter parameter, ITestNGMethod method, ITestContext context) {
    return parameter.isAnnotationPresent(FromResolver.class);
  }

  @Override
  public Object resolveParameter(Parameter parameter, ITestNGMethod method, ITestContext context) {
    CustomObject answer = new CustomObject("resolution-" + (answers.size() + 1));
    answers.add(answer);
    return answer;
  }

  public List<CustomObject> answers() {
    return answers;
  }
}
