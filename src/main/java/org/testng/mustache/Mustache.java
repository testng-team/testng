package org.testng.mustache;

import org.testng.collections.Lists;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Mustache {

  public String run(String template, Map<String, Object> m) throws IOException {
    return run(template, new Model(m));
  }

  String run(String template, Model model) throws IOException {
    int lineNumber = 0;

    List<BaseChunk> chunks = Lists.newArrayList();
    int ti = 0;
    while (ti < template.length()) {
      int start;
      int end;
      if (template.charAt(ti) == '\n') lineNumber++;

      if (template.charAt(ti) == '{' && ti + 1 < template.length()
          && template.charAt(ti + 1) == '{') {
        int index = ti + 2;
        start = index;
        boolean foundEnd = false;
        while (index < template.length() && ! foundEnd) {
          index++;
          foundEnd = template.charAt(index) == '}' && index + 1 < template.length()
              && template.charAt(index + 1) == '}';
        }

        if (foundEnd) {
          end = index;
          String variable = template.substring(start, end);
          p("Found variable:" + variable);
          if (variable.startsWith("#")) {
            // Complex variable {{#foo}}.
            String conditionalVariable = variable.substring(1);
            Value value = model.resolveValue(conditionalVariable);
            int endIndex = findClosingIndex(template, ti, conditionalVariable);
            Object v = value.get();
            if (v == null) {
              // Null condition, do nothing
            } else if (v instanceof Iterable) {
              // Iterable, call a sub Mustache to process this chunk in a loop
              // after pushing a new submodel
              Iterable it = (Iterable) v;
              String subTemplate = template.substring(ti + variable.length() + 4, endIndex);
              for (Object o : it) {
                model.push(conditionalVariable, o);
                String r = new Mustache().run(subTemplate, model);
                model.popSubModel();
                chunks.add(new StringChunk(model, r));
              }
            } else {
              // Scope, call a sub Mustache to process this chunk
              // after pushing a new submodel
              String subTemplate = template.substring(ti + variable.length() + 4, endIndex);
              model.push(conditionalVariable, v);
              String r = new Mustache().run(subTemplate, model);
              model.popSubModel();
              chunks.add(new StringChunk(model, r));
            }
            ti = endIndex + variable.length() + 4;
          } else {
            // Regular variable {{foo}}
            chunks.add(new VariableChunk(model, variable));
            ti += variable.length() + 4;
          }
        } else {
          throw new RuntimeException("Unclosed variable at line " + lineNumber);
        }
      } else {
        chunks.add(new StringChunk(model, "" + template.charAt(ti)));
        ti++;
      }
    } // while

    p("******************** Final composition, chunks:");
    StringBuilder result = new StringBuilder();
    p("*** Template:" + template);
    for (BaseChunk bc : chunks) {
      p("***  " + bc);
    }

    for (BaseChunk bc : chunks) {
      String c = bc.compose();
      result.append(c);
    }
    p("*** Final result:" + result);
    return result.toString();
  }

  private int findClosingIndex(String template, int ti,
      String conditionalVariable) {
    int result = template.lastIndexOf("{{/" + conditionalVariable);
    return result;
  }

  private void p(String string) {
    if (false) {
      System.out.println(string);
    }
  }

  public static void main(String[] args) throws IOException {
  }
}
