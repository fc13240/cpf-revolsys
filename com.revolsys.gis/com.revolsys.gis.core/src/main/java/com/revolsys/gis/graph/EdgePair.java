package com.revolsys.gis.graph;

import java.util.HashMap;
import java.util.Map;

public class EdgePair<T> {
  private final Edge<T> edge1;

  private final Edge<T> edge2;

  private final Map<String, Object> properties1 = new HashMap<String, Object>();

  private final Map<String, Object> properties2 = new HashMap<String, Object>();

  public EdgePair(
    final Edge<T> edge1,
    final Edge<T> edge2) {
    this.edge1 = edge1;
    this.edge2 = edge2;
  }

  public Edge<T> getEdge1() {
    return edge1;
  }

  public Edge<T> getEdge2() {
    return edge2;
  }

  public <T> T getProperty1(
    final String name) {
    return (T)properties1.get(name);
  }

  public <T> T getProperty2(
    final String name) {
    return (T)properties2.get(name);
  }

  public void setProperty1(
    final String name,
    final Object value) {
    properties1.put(name, value);
  }

  public void setProperty2(
    final String name,
    final Object value) {
    properties2.put(name, value);
  }
}
