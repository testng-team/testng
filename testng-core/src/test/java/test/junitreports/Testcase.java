package test.junitreports;

import org.xml.sax.Attributes;

public class Testcase {
  private String name;
  private String classname;
  private String innerTagType;

  public String getName() {
    return name;
  }

  public String getClassname() {
    return classname;
  }

  public String getInnerTagType() {
    return innerTagType;
  }

  public void setInnerTagType(String innerTagType) {
    this.innerTagType = innerTagType;
  }

  public void init(Attributes attributes) {
    String value = attributes.getValue("name");
    if (value != null) {
      this.name = value;
    }
    value = attributes.getValue("classname");
    if (value != null) {
      this.classname = value;
    }
  }

  @Override
  public String toString() {
    return "Testcase{"
        + "name='"
        + name
        + '\''
        + ", classname='"
        + classname
        + '\''
        + ", innerTagType='"
        + innerTagType
        + '\''
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Testcase testcase = (Testcase) o;

    if (!name.equals(testcase.name)) {
      return false;
    }
    if (!classname.equals(testcase.classname)) {
      return false;
    }
    return innerTagType.equals(testcase.innerTagType);
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + classname.hashCode();
    result = 31 * result + innerTagType.hashCode();
    return result;
  }

  public static Testcase newInstance(String name, String classname, String innerTagType) {
    Testcase testcase = new Testcase();
    testcase.name = name;
    testcase.classname = classname;
    testcase.innerTagType = innerTagType;
    return testcase;
  }
}
