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
package com.revolsys.gis.model.geometry.operation.geomgraph.index;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.vividsolutions.jts.algorithm.NotRepresentableException;

/**
 * Represents a homogeneous coordinate in a 2-D coordinate space. In JTS
 * {@link HCoordinates}s are used as a clean way of computing intersections
 * between line segments.
 * 
 * @author David Skea
 * @version 1.7
 */
public class HCoordinate {

  /**
   * Computes the (approximate) intersection point between two line segments
   * using homogeneous coordinates.
   * <p>
   * Note that this algorithm is not numerically stable; i.e. it can produce
   * intersection points which lie outside the envelope of the line segments
   * themselves. In order to increase the precision of the calculation input
   * points should be normalized before passing them to this routine.
   */
  public static Coordinates intersection(Coordinates p1, Coordinates p2,
    Coordinates q1, Coordinates q2) throws NotRepresentableException {
    // unrolled computation
    double px = p1.getY() - p2.getY();
    double py = p2.getX() - p1.getX();
    double pw = p1.getX() * p2.getY() - p2.getX() * p1.getY();

    double qx = q1.getY() - q2.getY();
    double qy = q2.getX() - q1.getX();
    double qw = q1.getX() * q2.getY() - q2.getX() * q1.getY();

    double x = py * qw - qy * pw;
    double y = qx * pw - px * qw;
    double w = px * qy - qx * py;

    double xInt = x / w;
    double yInt = y / w;

    if ((Double.isNaN(xInt)) || (Double.isInfinite(xInt) || Double.isNaN(yInt))
      || (Double.isInfinite(yInt))) {
      throw new NotRepresentableException();
    }

    return new DoubleCoordinates(xInt, yInt);
  }

  /*
   * public static Coordinates OLDintersection( Coordinates p1, Coordinates p2,
   * Coordinates q1, Coordinates q2) throws NotRepresentableException {
   * HCoordinates l1 = new HCoordinates(p1, p2); HCoordinates l2 = new
   * HCoordinates(q1, q2); HCoordinates intHCoord = new HCoordinates(l1, l2);
   * Coordinates intPt = intHCoord.getCoordinates(); return intPt; }
   */

  public double x, y, w;

  public HCoordinate() {
    x = 0.0;
    y = 0.0;
    w = 1.0;
  }

  public HCoordinate(double _x, double _y, double _w) {
    x = _x;
    y = _y;
    w = _w;
  }

  public HCoordinate(double _x, double _y) {
    x = _x;
    y = _y;
    w = 1.0;
  }

  public HCoordinate(Coordinates p) {
    x = p.getX();
    y = p.getY();
    w = 1.0;
  }

  public HCoordinate(HCoordinate p1, HCoordinate p2) {
    x = p1.y * p2.w - p2.y * p1.w;
    y = p2.x * p1.w - p1.x * p2.w;
    w = p1.x * p2.y - p2.x * p1.y;
  }

  /**
   * Constructs a homogeneous coordinate which is the intersection of the lines
   * define by the homogenous coordinates represented by two {@link Coordinates}
   * s.
   * 
   * @param p1
   * @param p2
   */
  public HCoordinate(Coordinates p1, Coordinates p2) {
    // optimization when it is known that w = 1
    x = p1.getY() - p2.getY();
    y = p2.getX() - p1.getX();
    w = p1.getX() * p2.getY() - p2.getX() * p1.getY();
  }

  public HCoordinate(Coordinates p1, Coordinates p2, Coordinates q1,
    Coordinates q2) {
    // unrolled computation
    double px = p1.getY() - p2.getY();
    double py = p2.getX() - p1.getX();
    double pw = p1.getX() * p2.getY() - p2.getX() * p1.getY();

    double qx = q1.getY() - q2.getY();
    double qy = q2.getX() - q1.getX();
    double qw = q1.getX() * q2.getY() - q2.getX() * q1.getY();

    x = py * qw - qy * pw;
    y = qx * pw - px * qw;
    w = px * qy - qx * py;
  }

  public double getX() throws NotRepresentableException {
    double a = x / w;
    if ((Double.isNaN(a)) || (Double.isInfinite(a))) {
      throw new NotRepresentableException();
    }
    return a;
  }

  public double getY() throws NotRepresentableException {
    double a = y / w;
    if ((Double.isNaN(a)) || (Double.isInfinite(a))) {
      throw new NotRepresentableException();
    }
    return a;
  }

  public Coordinates getCoordinates() throws NotRepresentableException {
    Coordinates p = new DoubleCoordinates(getX(), getY());
    return p;
  }
}