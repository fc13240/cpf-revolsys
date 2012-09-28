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
package com.revolsys.gis.model.geometry.operation.buffer;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesPrecisionModel;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateArrays;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.geomgraph.Position;
import com.vividsolutions.jts.operation.buffer.BufferParameters;

/**
 * Computes the raw offset curve for a single {@link Geometry} component (ring,
 * line or point). A raw offset curve line is not noded - it may contain
 * self-intersections (and usually will). The final buffer polygon is computed
 * by forming a topological graph of all the noded raw curves and tracing
 * outside contours. The points in the raw curve are rounded to a given
 * {@link PrecisionModel}.
 * 
 * @version 1.7
 */
public class OffsetCurveBuilder {
  private double distance = 0.0;

  private CoordinatesPrecisionModel precisionModel;

  private BufferParameters bufParams;

  public OffsetCurveBuilder(CoordinatesPrecisionModel precisionModel,
    BufferParameters bufParams) {
    this.precisionModel = precisionModel;
    this.bufParams = bufParams;
  }

  /**
   * Gets the buffer parameters being used to generate the curve.
   * 
   * @return the buffer parameters being used
   */
  public BufferParameters getBufferParameters() {
    return bufParams;
  }

  /**
   * This method handles single points as well as LineStrings. LineStrings are
   * assumed <b>not</b> to be closed (the function will not fail for closed
   * lines, but will generate superfluous line caps).
   * 
   * @param inputPts the vertices of the line to offset
   * @param distance the offset distance
   * @return a Coordinates array representing the curve
   * @return null if the curve is empty
   */
  public CoordinatesList getLineCurve(CoordinatesList inputPts, double distance) {
    this.distance = distance;

    // a zero or negative width buffer of a line/point is empty
    if (distance < 0.0 && !bufParams.isSingleSided())
      return null;
    if (distance == 0.0)
      return null;

    double posDistance = Math.abs(distance);
    OffsetSegmentGenerator segGen = getSegGen(posDistance);
    if (inputPts.size() <= 1) {
      computePointCurve(inputPts.get(0), segGen);
    } else {
      if (bufParams.isSingleSided()) {
        boolean isRightSide = distance < 0.0;
        computeSingleSidedBufferCurve(inputPts, isRightSide, segGen);
      } else
        computeLineBufferCurve(inputPts, segGen);
    }

    CoordinatesList lineCoord = segGen.getCoordinates();
    return lineCoord;
  }

  /**
   * This method handles the degenerate cases of single points and lines, as
   * well as rings.
   * 
   * @return a Coordinates array representing the curve
   * @return null if the curve is empty
   */
  public CoordinatesList getRingCurve(CoordinatesList inputPts, int side,
    double distance) {
    this.distance = distance;
    if (inputPts.size() <= 2)
      return getLineCurve(inputPts, distance);

    // optimize creating ring for for zero distance
    if (distance == 0.0) {
      return copyCoordinates(inputPts);
    }
    OffsetSegmentGenerator segGen = getSegGen(distance);
    computeRingBufferCurve(inputPts, side, segGen);
    return segGen.getCoordinates();
  }

  public CoordinatesList getOffsetCurve(CoordinatesList inputPts,
    double distance) {
    this.distance = distance;

    // a zero width offset curve is empty
    if (distance == 0.0)
      return null;

    boolean isRightSide = distance < 0.0;
    double posDistance = Math.abs(distance);
    OffsetSegmentGenerator segGen = getSegGen(posDistance);
    if (inputPts.size() <= 1) {
      computePointCurve(inputPts.get(0), segGen);
    } else {
      computeOffsetCurve(inputPts, isRightSide, segGen);
    }
    CoordinatesList curvePts = segGen.getCoordinates();
    // for right side line is traversed in reverse direction, so have to reverse
    // generated line
    if (isRightSide)
      curvePts = curvePts.reverse();
    return curvePts;
  }

  private static CoordinatesList copyCoordinates(CoordinatesList pts) {
    CoordinatesList copy = new DoubleCoordinatesList(pts);
    return copy;
  }

  private OffsetSegmentGenerator getSegGen(double distance) {
    return new OffsetSegmentGenerator(precisionModel, bufParams, distance);
  }

  /**
   * Use a value which results in a potential distance error which is
   * significantly less than the error due to the quadrant segment
   * discretization. For QS = 8 a value of 100 is reasonable. This should
   * produce a maximum of 1% distance error.
   */
  private static final double SIMPLIFY_FACTOR = 100.0;

  /**
   * Computes the distance tolerance to use during input line simplification.
   * 
   * @param distance the buffer distance
   * @return the simplification tolerance
   */
  private static double simplifyTolerance(double bufDistance) {
    return bufDistance / SIMPLIFY_FACTOR;
  }

  private void computePointCurve(Coordinates pt, OffsetSegmentGenerator segGen) {
    switch (bufParams.getEndCapStyle()) {
      case BufferParameters.CAP_ROUND:
        segGen.createCircle(pt);
      break;
      case BufferParameters.CAP_SQUARE:
        segGen.createSquare(pt);
      break;
    // otherwise curve is empty (e.g. for a butt cap);
    }
  }

  private void computeLineBufferCurve(CoordinatesList inputPts,
    OffsetSegmentGenerator segGen) {
    double distTol = simplifyTolerance(distance);

    // --------- compute points for left side of line
    // Simplify the appropriate side of the line before generating
    CoordinatesList simp1 = BufferInputLineSimplifier.simplify(inputPts,
      distTol);
    // MD - used for testing only (to eliminate simplification)
    // CoordinatesList simp1 = inputPts;

    int n1 = simp1.size() - 1;
    segGen.initSideSegments(simp1.get(0), simp1.get(1), Position.LEFT);
    for (int i = 2; i <= n1; i++) {
      segGen.addNextSegment(simp1.get(i), true);
    }
    segGen.addLastSegment();
    // add line cap for end of line
    segGen.addLineEndCap(simp1.get(n1-1), simp1.get(n1));

    // ---------- compute points for right side of line
    // Simplify the appropriate side of the line before generating
    CoordinatesList simp2 = BufferInputLineSimplifier.simplify(inputPts,
      -distTol);
    // MD - used for testing only (to eliminate simplification)
    // CoordinatesList simp2 = inputPts;
    int n2 = simp2.size() - 1;

    // since we are traversing line in opposite order, offset position is still
    // LEFT
    segGen.initSideSegments(simp2.get(n2), simp2.get(n2-1), Position.LEFT);
    for (int i = n2 - 2; i >= 0; i--) {
      segGen.addNextSegment(simp2.get(i), true);
    }
    segGen.addLastSegment();
    // add line cap for start of line
    segGen.addLineEndCap(simp2.get(1), simp2.get(0));

    segGen.closeRing();
  }

  /*
   * private void OLDcomputeLineBufferCurve(CoordinatesList inputPts) { int n =
   * inputPts.size() - 1; // compute points for left side of line
   * initSideSegments(inputPts.get(0), inputPts.get(1), Position.LEFT); for (int i = 2;
   * i <= n; i++) { addNextSegment(inputPts.get(i), true); } addLastSegment(); //
   * add line cap for end of line addLineEndCap(inputPts.get(n - 1), inputPts[n]);
   * // compute points for right side of line initSideSegments(inputPts[n],
   * inputPts.get(n - 1), Position.LEFT); for (int i = n - 2; i >= 0; i--) {
   * addNextSegment(inputPts.get(i), true); } addLastSegment(); // add line cap for
   * start of line addLineEndCap(inputPts.get(1), inputPts.get(0));
   * vertexList.closeRing(); }
   */

  private void computeSingleSidedBufferCurve(CoordinatesList inputPts,
    boolean isRightSide, OffsetSegmentGenerator segGen) {
    double distTol = simplifyTolerance(distance);

    if (isRightSide) {
      // add original line
      segGen.addSegments(inputPts, true);

      // ---------- compute points for right side of line
      // Simplify the appropriate side of the line before generating
      CoordinatesList simp2 = BufferInputLineSimplifier.simplify(inputPts,
        -distTol);
      // MD - used for testing only (to eliminate simplification)
      // CoordinatesList simp2 = inputPts;
      int n2 = simp2.size() - 1;

      // since we are traversing line in opposite order, offset position is
      // still LEFT
      segGen.initSideSegments(simp2.get(n2), simp2.get(n2-1), Position.LEFT);
      segGen.addFirstSegment();
      for (int i = n2 - 2; i >= 0; i--) {
        segGen.addNextSegment(simp2.get(i), true);
      }
    } else {
      // add original line
      segGen.addSegments(inputPts, false);

      // --------- compute points for left side of line
      // Simplify the appropriate side of the line before generating
      CoordinatesList simp1 = BufferInputLineSimplifier.simplify(inputPts,
        distTol);
      // MD - used for testing only (to eliminate simplification)
      // CoordinatesList simp1 = inputPts;

      int n1 = simp1.size() - 1;
      segGen.initSideSegments(simp1.get(0), simp1.get(1), Position.LEFT);
      segGen.addFirstSegment();
      for (int i = 2; i <= n1; i++) {
        segGen.addNextSegment(simp1.get(i), true);
      }
    }
    segGen.addLastSegment();
    segGen.closeRing();
  }

  private void computeOffsetCurve(CoordinatesList inputPts,
    boolean isRightSide, OffsetSegmentGenerator segGen) {
    double distTol = simplifyTolerance(distance);

    if (isRightSide) {
      // ---------- compute points for right side of line
      // Simplify the appropriate side of the line before generating
      CoordinatesList simp2 = BufferInputLineSimplifier.simplify(inputPts,
        -distTol);
      // MD - used for testing only (to eliminate simplification)
      // CoordinatesList simp2 = inputPts;
      int n2 = simp2.size() - 1;

      // since we are traversing line in opposite order, offset position is
      // still LEFT
      segGen.initSideSegments(simp2.get(n2), simp2.get(n2-1), Position.LEFT);
      segGen.addFirstSegment();
      for (int i = n2 - 2; i >= 0; i--) {
        segGen.addNextSegment(simp2.get(i), true);
      }
    } else {
      // --------- compute points for left side of line
      // Simplify the appropriate side of the line before generating
      CoordinatesList simp1 = BufferInputLineSimplifier.simplify(inputPts,
        distTol);
      // MD - used for testing only (to eliminate simplification)
      // CoordinatesList simp1 = inputPts;

      int n1 = simp1.size() - 1;
      segGen.initSideSegments(simp1.get(0), simp1.get(1), Position.LEFT);
      segGen.addFirstSegment();
      for (int i = 2; i <= n1; i++) {
        segGen.addNextSegment(simp1.get(i), true);
      }
    }
    segGen.addLastSegment();
  }

  private void computeRingBufferCurve(CoordinatesList inputPts, int side,
    OffsetSegmentGenerator segGen) {
    // simplify input line to improve performance
    double distTol = simplifyTolerance(distance);
    // ensure that correct side is simplified
    if (side == Position.RIGHT)
      distTol = -distTol;
    CoordinatesList simp = BufferInputLineSimplifier.simplify(inputPts, distTol);
    // CoordinatesList simp = inputPts;

    int n = simp.size() - 1;
    segGen.initSideSegments(simp.get(n - 1), simp.get(0), side);
    for (int i = 1; i <= n; i++) {
      boolean addStartPoint = i != 1;
      segGen.addNextSegment(simp.get(i), addStartPoint);
    }
    segGen.closeRing();
  }

}