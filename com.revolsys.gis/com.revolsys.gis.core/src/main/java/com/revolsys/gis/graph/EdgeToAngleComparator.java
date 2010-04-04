package com.revolsys.gis.graph;

import java.util.Comparator;

import com.vividsolutions.jts.algorithm.Angle;

/**
 * The EdgeToAngleComparator class is used to return edges in a clockwise order.
 * 
 * @author Paul Austin
 * @param <T> The type of object stored on the edges in the graph.
 */
public class EdgeToAngleComparator<T> implements Comparator<Edge<T>> {
  @SuppressWarnings("unchecked")
  private static final EdgeToAngleComparator INSTANCE = new EdgeToAngleComparator();

  @SuppressWarnings("unchecked")
  public static <T> EdgeToAngleComparator<T> get() {
    return INSTANCE;
  }

  /**
   * Construct a new EdgeToAngleComparator.
   */
  public EdgeToAngleComparator() {
  }

  /**
   * Compare the to angle for two edges.
   * 
   * @param edge1 The first edge.
   * @param edge2 The second edge.
   * @see Angle#getTurn(double, double)
   */
  public int compare(
    final Edge<T> edge1,
    final Edge<T> edge2) {
    final double angle1 = edge1.getToAngle();
    final double angle2 = edge2.getToAngle();
    return Angle.getTurn(angle1, angle2);
  }
}
