package test.assertion;

import com.google.common.base.Objects;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AssertionListContainsTest {

  private User userJack = new User("Jack", 22);
  private User userJohn = new User("John", 32);
  private List<User> users = List.of(userJack, userJohn);

  @Test
  public void assertListContainsObject() {
    Assert.assertListContainsObject(users, userJack, "user Jack");
  }

  @Test
  public void assertListNotContainsObject() {
    Assert.assertListNotContainsObject(users, new User("NoName", 34), "user NoName");
  }

  @Test
  public void testAssertListContainsByPredicate() {
    Assert.assertListContains(users, user -> user.age.equals(22), "user with age 22");
  }

  @Test
  public void testAssertListNotContainsByPredicate() {
    Assert.assertListNotContains(users, user -> user.age.equals(19), "user with age 19");
  }

  private class User {

    private String name;
    private Integer age;

    public User(String name, Integer age) {
      this.name = name;
      this.age = age;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      User user = (User) o;
      return Objects.equal(name, user.name) && Objects.equal(age, user.age);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(name, age);
    }

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder("User{");
      sb.append("name='").append(name).append('\'');
      sb.append(", age=").append(age);
      sb.append('}');
      return sb.toString();
    }
  }
}
