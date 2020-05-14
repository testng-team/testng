package test.mustache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.collections.Maps;
import org.testng.mustache.Mustache;

@Test
public class MustacheTest {

  public static class Person {
    public String name;
    public Person(String n) {
      name = n;
    }
  }

  public static class Age {
    public int age;
    public Age(int a) {
      this.age = a;
    }
  }

  private static final List<Person> PEOPLE = new ArrayList<>(
          Arrays.asList(new Person("Carl"), new Person("Christopher")));

  private static final List<Age> AGES = new ArrayList<>(
          Arrays.asList(new Age(42), new Age(43)));

  @DataProvider
  public Object[][] dp() {
    return new Object[][] {
        // Simple
        new Object[] {
            create("one", "ello", "two", "orld"),
            "H{{one}} W{{two}}",
            "Hello World"
        },
        // Null condition
        new Object[] {
            Collections.emptyMap(),
            "E{{#foo}}xxx{{/foo}}lephant",
            "Elephant"
        },
        // Null condition with new line
        new Object[] {
            Collections.emptyMap(),
            "Hello\n{{#foo}}@\n{{/foo}}World",
            "Hello\nWorld"
        },
        // Simple scope
        new Object[] {
            create("person", new Person("John"), "day", "Monday"),
            "Hello {{#person}}{{name}}{{/person}}, {{day}}",
            "Hello John, Monday"
        },
        // Scope with shadowing
        new Object[] {
            create("person", new Person("John"), "name", "Carl"),
            "Hello {{#person}}{{name}}{{/person}}, {{name}}",
            "Hello John, Carl"
        },
        // Test iteration
        new Object[] {
            create("people", PEOPLE),
            "People:@{{#people}}-{{/people}}!",
            "People:@--!",
        },
        // Nested scopes
        new Object[] {
            create("people", PEOPLE, "ages", AGES),
            ":@{{#people}}{{name}}{{#ages}}{{age}}{{/ages}}@{{/people}}!_",
            ":@Carl4243@Christopher4243@!_",
        },
    };
  }

  private Map<String, Object> create(Object... objects) {
    Map<String, Object> result = Maps.newHashMap();
    for (int i = 0; i < objects.length; i += 2) {
      result.put((String) objects[i], objects[i + 1]);
    }
    return result;
  }

  @Test(dataProvider = "dp")
  public void runTest(Map<String, Object> model, String template, String expected)
      throws IOException {
//    InputStream is = new StringInputStream(template);
    Assert.assertEquals(new Mustache().run(template, model), expected);
  }

}
