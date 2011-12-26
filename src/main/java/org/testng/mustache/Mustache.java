package org.testng.mustache;

import org.testng.collections.Lists;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Mustache {

  private static class IterableInfo {
    public String variable;
    public int indexStart;
    public Iterable iterable;
  }

  private static class ScopedInfo {
    public String variable;
    public int indexStart;
    public Object object;
  }

  public String run(String template, Map<String, Object> m) throws IOException {
    return run(template, new Model(m));
  }

  String run(String template, Model model) throws IOException {
    int lineNumber = 0;
    Stack<String> ignoreStack = new Stack<String>();
    Stack<IterableInfo> iterableStack = new Stack<IterableInfo>();
    Stack<ScopedInfo> scopedStack = new Stack<ScopedInfo>();

    List<BaseChunk> chunks = Lists.newArrayList();
    for (int ti = 0; ti < template.length(); ti++) {
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
           String conditionalVariable = variable.substring(1);
           Value value = model.resolveValue(conditionalVariable);
           Object v = value.get();
           if (v == null) {
             ignoreStack.push(conditionalVariable);
           } else if (v instanceof Iterable) {
             IterableInfo ii = new IterableInfo();
             ii.variable = conditionalVariable;
             ii.indexStart = ti + variable.length() + 4;
             ii.iterable = ((Iterable) v);
             iterableStack.push(ii);
//               chunks.add(new IterableChunk(model, template, (Iterable) v, conditionalVariable));
             } else {
               ScopedInfo si = new ScopedInfo();
               si.variable = conditionalVariable;
               si.indexStart = ti + variable.length() + 4;
               si.object = v;
               scopedStack.push(si);
            }
          } else if (variable.startsWith("/")) {
            String conditionalVariable = variable.substring(1);
            if (! ignoreStack.isEmpty() && ignoreStack.peek().equals(conditionalVariable)) {
              String poppedVariable = ignoreStack.pop();
              if (!poppedVariable.equals(conditionalVariable)) {
                throw new RuntimeException("Closing " + conditionalVariable
                    + " but expected " + poppedVariable);
              } else {
                // nothing, resume normal appending
              }
            } else if (!scopedStack.isEmpty()
                && scopedStack.peek().variable.equals(conditionalVariable)) {
              ScopedInfo si = scopedStack.pop();
              String subTemplate = template.substring(si.indexStart, ti);
              chunks.add(new ScopedChunk(model, subTemplate, si.object, conditionalVariable));
            } else if (!iterableStack.isEmpty()
                && iterableStack.peek().variable.equals(conditionalVariable)) {
              IterableInfo ii = iterableStack.pop();
              String subTemplate = template.substring(ii.indexStart, ti);
              chunks.add(new IterableChunk(model, subTemplate, ii.iterable, conditionalVariable));
            } else {
              throw new RuntimeException("Closing unknown tag: " + variable);
            }
          } else if (scopedStack.isEmpty()) {
            chunks.add(new VariableChunk(model, variable));
          }
          ti = end + 1;
        } else {
          throw new RuntimeException("Unclosed variable at line " + lineNumber);
        }
      } else {
        if (ignoreStack.isEmpty() && iterableStack.isEmpty()) {
          chunks.add(new StringChunk(model, "" + template.charAt(ti)));
        }
      }
    }
    p("Chunks: " + chunks);
    StringBuilder result = new StringBuilder();
    for (BaseChunk bc : chunks) {
      String c = bc.compose();
      result.append(c);
    }
    return result.toString();
  }

  private void p(String string) {
    if (false) {
      System.out.println(string);
    }
  }

  public static void main(String[] args) throws IOException {
  }
}
