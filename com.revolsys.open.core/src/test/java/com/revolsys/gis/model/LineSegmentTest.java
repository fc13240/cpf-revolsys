package com.revolsys.gis.model;

import org.junit.Test;

import com.revolsys.gis.jts.LineSegmentImpl;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.jts.geom.PointList;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineSegment;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.impl.PointDouble;
import com.revolsys.jts.util.Assert;

public class LineSegmentTest {
  private static final GeometryFactory GEOMETRY_FACTORY_2D = GeometryFactory.getFactory(
    3005, 2, 1000, 0);

  private static final GeometryFactory GEOMETRY_FACTORY_3D = GeometryFactory.getFactory(
    3005, 3, 1000, 1000);

  private final PointDouble c_0_0_0 = c(0, 0, 0);

  private final PointDouble c_50_0_5 = c(50, 0, 5);

  private final PointDouble c_50_0 = c(50, 0);

  private final PointDouble c_50_50_5 = c(50, 50, 5);

  private final PointDouble c_70_0_7 = c(70, 0, 7);

  private final PointDouble c_70_0 = c(70, 0);

  private final PointDouble c_100_0_10 = c(100, 0, 10);

  private final PointDouble c_100_100_10 = c(100, 100, 10);

  private final PointDouble c_0_100_1 = c(0, 100, 1);

  public void assertIntersection3d(final Point line1Start,
    final Point line1End, final Point line2Start,
    final Point line2End, final Point... expectedIntersection) {
    final LineSegment line1 = new LineSegmentImpl(GEOMETRY_FACTORY_3D, line1Start,
      line1End);
    final LineSegment line2 = new LineSegmentImpl(GEOMETRY_FACTORY_3D, line2Start,
      line2End);
    final PointList points = new DoubleCoordinatesList(3,
      expectedIntersection);

    final PointList intersection = line1.getIntersection(line2);
    Assert.equals(points, intersection);
  }

  public void assertLinearIntersection(final double l1x1, final double l1y1,
    final double l1x2, final double l1y2, final double l2x1, final double l2y1,
    final double l2x2, final double l2y2, final double lx1, final double ly1,
    final double lx2, final double ly2) {
    final LineSegment line1 = new LineSegmentImpl(GEOMETRY_FACTORY_2D, 2, l1x1,
      l1y1, l1x2, l1y2);
    final LineSegment line2 = new LineSegmentImpl(GEOMETRY_FACTORY_2D, 2, l2x1,
      l2y1, l2x2, l2y2);
    final LineSegment line = new LineSegmentImpl(GEOMETRY_FACTORY_2D, 2, lx1,
      ly1, lx2, ly2);

    final PointList intersection = line1.getIntersection(line2);
    Assert.equals(line, intersection);
  }

  public PointDouble c(final double... coordinates) {
    return new PointDouble(coordinates);
  }

  @Test
  public void linearIntersection() {
    // Equal
    assertIntersection3d(this.c_0_0_0, this.c_100_0_10, this.c_0_0_0,
      this.c_100_0_10, this.c_0_0_0, this.c_100_0_10);
    // First Start
    assertIntersection3d(this.c_0_0_0, this.c_50_0_5, this.c_0_0_0,
      this.c_100_0_10, this.c_0_0_0, this.c_50_0_5);
    // First End
    assertIntersection3d(this.c_50_0_5, this.c_100_0_10, this.c_0_0_0,
      this.c_100_0_10, this.c_50_0_5, this.c_100_0_10);
    // First Middle
    assertIntersection3d(this.c_50_0_5, this.c_70_0_7, this.c_0_0_0,
      this.c_100_0_10, this.c_50_0_5, this.c_70_0_7);
    // Second Start
    assertIntersection3d(this.c_0_0_0, this.c_100_0_10, this.c_0_0_0,
      this.c_50_0_5, this.c_0_0_0, this.c_50_0_5);
    // Second End
    assertIntersection3d(this.c_0_0_0, this.c_100_0_10, this.c_50_0_5,
      this.c_100_0_10, this.c_50_0_5, this.c_100_0_10);
    // Second Middle
    assertIntersection3d(this.c_0_0_0, this.c_100_0_10, this.c_50_0_5,
      this.c_70_0_7, this.c_50_0_5, this.c_70_0_7);
    // Reverse First Start
    assertIntersection3d(this.c_50_0_5, this.c_0_0_0, this.c_0_0_0,
      this.c_100_0_10, this.c_50_0_5, this.c_0_0_0);
    // Reverse First End
    assertIntersection3d(this.c_100_0_10, this.c_50_0_5, this.c_0_0_0,
      this.c_100_0_10, this.c_100_0_10, this.c_50_0_5);
    // Reverse First Middle
    assertIntersection3d(this.c_70_0_7, this.c_50_0_5, this.c_0_0_0,
      this.c_100_0_10, this.c_70_0_7, this.c_50_0_5);
    // Reverse Second Start
    assertIntersection3d(this.c_0_0_0, this.c_100_0_10, this.c_0_0_0,
      this.c_50_0_5, this.c_0_0_0, this.c_50_0_5);
    // Reverse Second End
    assertIntersection3d(this.c_0_0_0, this.c_100_0_10, this.c_70_0_7,
      this.c_50_0_5, this.c_50_0_5, this.c_70_0_7);

    // Reverse Second End No z on second line
    assertIntersection3d(this.c_0_0_0, this.c_100_0_10, this.c_70_0,
      this.c_50_0, this.c_50_0_5, this.c_70_0_7);

    // Middle Precision Model
    assertIntersection3d(this.c_0_0_0, c(100, 0.001, 10), c(50, 0.001),
      c(70, 0.001), c(50, 0.001, 5), c(70, 0.001, 7));
  }

  @Test
  public void pointIntersections() {
    // Cross
    assertIntersection3d(this.c_0_0_0, this.c_100_100_10, this.c_0_100_1,
      this.c_100_0_10, this.c_50_50_5);
    // Touch
    assertIntersection3d(this.c_0_0_0, this.c_100_0_10, this.c_50_0,
      this.c_100_100_10, this.c_50_0_5);
    // Touch approximate
    assertIntersection3d(this.c_0_0_0, c(100, 0.001, 10), c(50, 0.001, 5),
      this.c_100_100_10, c(50, 0.001, 5));

  }
}
