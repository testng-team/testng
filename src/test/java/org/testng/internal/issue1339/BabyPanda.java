package org.testng.internal.issue1339;

public class BabyPanda extends LittlePanda {
  String name;

  @Override
  public String toString() {
    return "BabyPanda{" + "name='" + name + '\'' + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    BabyPanda babyPanda = (BabyPanda) o;

    return name != null ? name.equals(babyPanda.name) : babyPanda.name == null;
  }

  @Override
  public int hashCode() {
    return name != null ? name.hashCode() : 0;
  }
}
