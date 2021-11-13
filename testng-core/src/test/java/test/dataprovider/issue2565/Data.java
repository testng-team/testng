package test.dataprovider.issue2565;

import java.util.ArrayList;
import java.util.List;

public enum Data {
  INSTANCE;

  private final List<String> data = new ArrayList<>();

  public void addDatum(String datum) {
    data.add(datum);
  }

  public List<String> getData() {
    return data;
  }

  public void clear() {
    data.clear();
  }
}
