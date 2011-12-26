package test.mustache;

import com.google.inject.internal.Maps;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.mustache.Mustache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Test
public class MustacheTest {

  public static class Person {
    public String name;
    public Person(String n) {
      name = n;
    }
  }

  private static final List<Person> PEOPLE = new ArrayList<Person>(
      Arrays.asList(new Person("Carl"), new Person("Christopher")));

  @DataProvider
  public Object[][] dp() {
    return new Object[][] {
        new Object[] {
            create("one", "ello", "two", "orld"),
            "H{{one}} W{{two}}",
            "Hello World"
        },
        new Object[] {
            Collections.emptyMap(),
            "E{{#foo}}xxx{{/foo}}lephant",
            "Elephant"
        },
        new Object[] {
            Collections.emptyMap(),
            "Hello\n{{#foo}}@\n{{/foo}}World",
            "Hello\nWorld"
        },
        new Object[] {
            create("person", new Person("John"), "day", "Monday"),
            "Hello {{#person}}{{name}}{{/person}}, {{day}}",
            "Hello John, Monday"
        },
        // Test scopes
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
        new Object[] {
            create("people", PEOPLE),
            "People:@{{#people}}{{name}}@{{/people}}!",
            "People:@Carl@Christopher@!",
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
