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
package com.revolsys.jts.testold.geom;

import junit.framework.TestCase;

import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.jts.geom.PointList;

/**
 * @version 1.7
 */
public class BasicCoordinateSequenceTest extends TestCase {
  public static void main(final String[] args) {
    junit.textui.TestRunner.run(BasicCoordinateSequenceTest.class);
  }

  public BasicCoordinateSequenceTest(final String name) {
    super(name);
  }

  public void testClone() {
    final PointList s1 = new DoubleCoordinatesList(2, 1.0, 2.0, 3.0, 4.0);
    final PointList s2 = s1.clone();
    assertTrue(s1.getCoordinate(0).equals(s2.getCoordinate(0)));
    assertTrue(s1.getCoordinate(0) != s2.getCoordinate(0));
  }
}
