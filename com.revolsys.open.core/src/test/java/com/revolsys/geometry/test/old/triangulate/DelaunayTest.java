/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.revolsys.geometry.test.old.triangulate;

import org.junit.Assert;
import org.junit.Test;

import com.revolsys.elevation.tin.quadedge.QuadEdgeDelaunayTinBuilder;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.wkb.ParseException;

/**
 * Tests Delaunay Triangulatin classes
 *
 */
public class DelaunayTest {

  static final double COMPARISON_TOLERANCE = 1.0e-7;

  private final GeometryFactory geometryFactory = GeometryFactory.fixed(0, 1000.0, 1000.0, 1000.0);

  void runDelaunay(final String sitesWKT, final boolean computeTriangles, final String expectedWKT)
    throws ParseException {
    final Geometry sites = this.geometryFactory.geometry(sitesWKT);
    final QuadEdgeDelaunayTinBuilder builder = new QuadEdgeDelaunayTinBuilder(this.geometryFactory);
    builder.insertVertices(sites);

    Geometry result = null;
    if (computeTriangles) {
      result = builder.getTrianglesPolygonal();
    } else {
      result = builder.getEdges();
    }
    // System.out.println(result);

    Geometry expected = this.geometryFactory.geometry(expectedWKT);
    result = result.normalize();
    expected = expected.normalize();
    if (!expected.equalsExact(result, COMPARISON_TOLERANCE)) {
      System.err.println(expected);
      System.err.println(result);
      Assert.fail("Not equal");
    }
  }

  void runDelaunayEdges(final String sitesWKT, final String expectedWKT) throws ParseException {
    runDelaunay(sitesWKT, false, expectedWKT);
  }

  @Test
  public void testBoundary() throws ParseException {
    // final Point point = this.geometryFactory.point(0, 0, 0);
    // testBoundaryDo(point);

    final LineString line = this.geometryFactory.lineString(3, 2, 0.0, 0, 0, 10, 10, 0);
    testBoundaryDo(line);

    final Polygon polygon = this.geometryFactory.polygon(3, //
      0, 10, 2, //
      10, 10, 3, //
      10, 0, 4, //
      0.0, 0, 1, //
      0, 10, 2 //
    );
    testBoundaryDo(polygon);
  }

  public void testBoundaryDo(final Geometry expected) {
    final QuadEdgeDelaunayTinBuilder tinBuilder = new QuadEdgeDelaunayTinBuilder(
      this.geometryFactory);
    tinBuilder.insertVertices(expected);
    final Geometry actual = tinBuilder.getBoundary();
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void testCircle() throws ParseException {
    final String wkt = "POLYGON ((42 30, 41.96 29.61, 41.85 29.23, 41.66 28.89, 41.41 28.59, 41.11 28.34, 40.77 28.15, 40.39 28.04, 40 28, 39.61 28.04, 39.23 28.15, 38.89 28.34, 38.59 28.59, 38.34 28.89, 38.15 29.23, 38.04 29.61, 38 30, 38.04 30.39, 38.15 30.77, 38.34 31.11, 38.59 31.41, 38.89 31.66, 39.23 31.85, 39.61 31.96, 40 32, 40.39 31.96, 40.77 31.85, 41.11 31.66, 41.41 31.41, 41.66 31.11, 41.85 30.77, 41.96 30.39, 42 30))";
    final String expected = "MULTILINESTRING ((41.66 31.11, 41.85 30.77), (41.41 31.41, 41.66 31.11), (41.11 31.66, 41.41 31.41), (40.77 31.85, 41.11 31.66), (40.39 31.96, 40.77 31.85), (40 32, 40.39 31.96), (39.61 31.96, 40 32), (39.23 31.85, 39.61 31.96), (38.89 31.66, 39.23 31.85), (38.59 31.41, 38.89 31.66), (38.34 31.11, 38.59 31.41), (38.15 30.77, 38.34 31.11), (38.04 30.39, 38.15 30.77), (38 30, 38.04 30.39), (38 30, 38.04 29.61), (38.04 29.61, 38.15 29.23), (38.15 29.23, 38.34 28.89), (38.34 28.89, 38.59 28.59), (38.59 28.59, 38.89 28.34), (38.89 28.34, 39.23 28.15), (39.23 28.15, 39.61 28.04), (39.61 28.04, 40 28), (40 28, 40.39 28.04), (40.39 28.04, 40.77 28.15), (40.77 28.15, 41.11 28.34), (41.11 28.34, 41.41 28.59), (41.41 28.59, 41.66 28.89), (41.66 28.89, 41.85 29.23), (41.85 29.23, 41.96 29.61), (41.96 29.61, 42 30), (41.96 30.39, 42 30), (41.85 30.77, 41.96 30.39), (41.66 31.11, 41.96 30.39), (41.41 31.41, 41.96 30.39), (41.41 28.59, 41.96 30.39), (41.41 28.59, 41.41 31.41), (38.59 28.59, 41.41 28.59), (38.59 28.59, 41.41 31.41), (38.59 28.59, 38.59 31.41), (38.59 31.41, 41.41 31.41), (38.59 31.41, 39.61 31.96), (39.61 31.96, 41.41 31.41), (39.61 31.96, 40.39 31.96), (40.39 31.96, 41.41 31.41), (40.39 31.96, 41.11 31.66), (38.04 30.39, 38.59 28.59), (38.04 30.39, 38.59 31.41), (38.04 30.39, 38.34 31.11), (38.04 29.61, 38.59 28.59), (38.04 29.61, 38.04 30.39), (39.61 28.04, 41.41 28.59), (38.59 28.59, 39.61 28.04), (38.89 28.34, 39.61 28.04), (40.39 28.04, 41.41 28.59), (39.61 28.04, 40.39 28.04), (41.96 29.61, 41.96 30.39), (41.41 28.59, 41.96 29.61), (41.66 28.89, 41.96 29.61), (40.39 28.04, 41.11 28.34), (38.04 29.61, 38.34 28.89), (38.89 31.66, 39.61 31.96))";
    runDelaunayEdges(wkt, expected);
  }

  @Test
  public void testGrid() throws ParseException {
    final String wkt = "MULTIPOINT ((10 10), (10 20), (20 20), (20 10), (20 0), (10 0), (0 0), (0 10), (0 20))";
    final String expected = "MULTILINESTRING ((10 20, 20 20), (0 20, 10 20), (0 10, 0 20), (0 0, 0 10), (0 0, 10 0), (10 0, 20 0), (20 0, 20 10), (20 10, 20 20), (10 20, 20 10), (10 10, 20 10), (10 10, 10 20), (10 10, 0 20), (10 10, 0 10), (10 0, 10 10), (0 10, 10 0), (10 10, 20 0))";
    runDelaunayEdges(wkt, expected);
    final String expectedTri = "GEOMETRYCOLLECTION (POLYGON ((0 20, 0 10, 10 10, 0 20)), POLYGON ((0 20, 10 10, 10 20, 0 20)), POLYGON ((10 20, 10 10, 20 10, 10 20)), POLYGON ((10 20, 20 10, 20 20, 10 20)), POLYGON ((10 0, 20 0, 10 10, 10 0)), POLYGON ((10 0, 10 10, 0 10, 10 0)), POLYGON ((10 0, 0 10, 0 0, 10 0)), POLYGON ((10 10, 20 0, 20 10, 10 10)))";
    runDelaunay(wkt, true, expectedTri);
  }

  @Test
  public void testPolygonWithChevronHoles() throws ParseException {
    final String wkt = "POLYGON ((0 0, 0 200, 180 200, 180 0, 0 0), (20 180, 160 180, 160 20, 152.625 146.75, 20 180), (30 160, 150 30, 70 90, 30 160))";
    final String expected = "MULTILINESTRING ((0 200, 180 200), (0 0, 0 200), (0 0, 180 0), (180 200, 180 0), (152.625 146.75, 180 0), (152.625 146.75, 180 200), (152.625 146.75, 160 180), (160 180, 180 200), (0 200, 160 180), (20 180, 160 180), (0 200, 20 180), (20 180, 30 160), (30 160, 0 200), (0 0, 30 160), (30 160, 70 90), (0 0, 70 90), (70 90, 150 30), (150 30, 0 0), (150 30, 160 20), (0 0, 160 20), (160 20, 180 0), (152.625 146.75, 160 20), (150 30, 152.625 146.75), (70 90, 152.625 146.75), (30 160, 152.625 146.75), (30 160, 160 180))";
    runDelaunayEdges(wkt, expected);
  }

  @Test
  public void testRandom() throws ParseException {
    final String wkt = "MULTIPOINT ((50 40), (140 70), (80 100), (130 140), (30 150), (70 180), (190 110), (120 20))";
    final String expected = "MULTILINESTRING ((70 180, 190 110), (30 150, 70 180), (30 150, 50 40), (50 40, 120 20), (190 110, 120 20), (120 20, 140 70), (190 110, 140 70), (130 140, 140 70), (130 140, 190 110), (70 180, 130 140), (80 100, 130 140), (70 180, 80 100), (30 150, 80 100), (50 40, 80 100), (80 100, 120 20), (80 100, 140 70))";
    runDelaunayEdges(wkt, expected);
    final String expectedTri = "GEOMETRYCOLLECTION (POLYGON ((30 150, 50 40, 80 100, 30 150)), POLYGON ((30 150, 80 100, 70 180, 30 150)), POLYGON ((70 180, 80 100, 130 140, 70 180)), POLYGON ((70 180, 130 140, 190 110, 70 180)), POLYGON ((190 110, 130 140, 140 70, 190 110)), POLYGON ((190 110, 140 70, 120 20, 190 110)), POLYGON ((120 20, 140 70, 80 100, 120 20)), POLYGON ((120 20, 80 100, 50 40, 120 20)), POLYGON ((80 100, 140 70, 130 140, 80 100)))";
    runDelaunay(wkt, true, expectedTri);
  }

  @Test
  public void testTriangle() throws ParseException {
    final String wkt = "MULTIPOINT ((10 10 1), (10 20 2), (20 20 3))";
    final String expected = "MULTILINESTRING ((10 20, 20 20), (10 10, 10 20), (10 10, 20 20))";
    runDelaunayEdges(wkt, expected);
    final String expectedTri = "POLYGON ((10 20, 10 10, 20 20, 10 20))";
    runDelaunay(wkt, true, expectedTri);
  }
}
