package com.revolsys.gis.oracle.esri;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.io.FileUtil;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.MultiLineString;
import com.revolsys.jts.geom.MultiPoint;
import com.revolsys.jts.geom.MultiPolygon;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.PointList;
import com.revolsys.jts.geom.Polygon;

/**
 * Point        (x,y [,z] [,m])
 * Line         (x,y (,x,y)+) [,z (,z)+] [,m (,m)+]
 * Multi Point  x,y (,-1,0, x,y)* [,z (,0,z)*] [,m (,0,m)*]
 * Multi Line   (x,y (,x,y)+) (,-1,0, (x,y (,x,y)+))* [(,z (,z)+) (,0, (,z (,z)+))*] [(,m (,m)+) (,0, (,m (,m)+))*]
 *
 */
public class PackedCoordinateUtil {

  @SuppressWarnings("unused")
  private static PointList getCoordinatesList(final int vertexCount,
    final Double xOffset, final Double yOffset, final Double xyScale,
    final Double zOffset, final Double zScale, final Double mOffset,
    final Double mScale, final InputStream inputStream) {

    try {
      final PackedIntegerInputStream in = new PackedIntegerInputStream(
        inputStream);

      final long packedByteLength = in.readLong5();
      final long dimensionFlag = in.readLong();
      final int annotationDimension = in.read();
      final int shapeFlags = in.read();
      final boolean hasZ = (dimensionFlag & 0x01) == 0x01;
      final boolean hasM = (dimensionFlag & 0x02) == 0x02;

      int axisCount;
      if (hasM) {
        axisCount = 4;
      } else if (hasZ) {
        axisCount = 3;
      } else {
        axisCount = 2;
      }
      final double[] coordinates = new double[vertexCount * axisCount];

      double x = xOffset;
      double y = yOffset;

      for (int i = 0; i < vertexCount; i++) {
        x = readCoordinate(in, coordinates, i, axisCount, 0, x, xyScale);
        y = readCoordinate(in, coordinates, i, axisCount, 1, y, xyScale);
      }

      if (hasZ) {
        readCoordinates(in, coordinates, axisCount, vertexCount, 2, zOffset,
          zScale);
      }
      if (hasM) {
        readCoordinates(in, coordinates, axisCount, vertexCount, 3, mOffset,
          mScale);
      }

      return new DoubleCoordinatesList(axisCount, coordinates);
    } catch (final IOException e) {
      throw new RuntimeException("Error reading coordinates", e);
    } finally {
      FileUtil.closeSilent(inputStream);
    }
  }

  private static List<PointList> getCoordinatesLists(final int vertexCount,
    final Double xOffset, final Double yOffset, final Double xyScale,
    final Double zOffset, final Double zScale, final Double mOffset,
    final Double mScale, final InputStream inputStream) {

    try {
      final List<double[]> pointsList = new ArrayList<>();
      final PackedIntegerInputStream in = new PackedIntegerInputStream(
        inputStream);

      final long packedByteLength = in.readLong5();
      final long dimensionFlag = in.readLong();
      final int annotationDimension = in.read();
      final int shapeFlags = in.read();
      final boolean hasZ = (dimensionFlag & 0x01) == 0x01;
      final boolean hasM = (dimensionFlag & 0x02) == 0x02;

      int axisCount;
      if (hasM) {
        axisCount = 4;
      } else if (hasZ) {
        axisCount = 3;
      } else {
        axisCount = 2;
      }
      double[] coordinates = new double[vertexCount * axisCount];

      double x = xOffset;
      double y = yOffset;

      int j = 0;
      for (int i = 0; i < vertexCount; i++) {
        x = readCoordinate(in, coordinates, j, axisCount, 0, x, xyScale);
        y = readCoordinate(in, coordinates, j, axisCount, 1, y, xyScale);
        if (x == -1 && y == 0) {
          final double[] subCoordinates = new double[j * axisCount];
          System.arraycopy(coordinates, 0, subCoordinates, 0,
            subCoordinates.length);
          pointsList.add(subCoordinates);
          coordinates = new double[(vertexCount - j) * axisCount];
          j = 0;
        } else {
          j++;
        }
      }
      if (coordinates.length == axisCount * j) {
        pointsList.add(coordinates);
      } else {
        final double[] subCoordinates = new double[j * axisCount];
        System.arraycopy(coordinates, 0, subCoordinates, 0,
          subCoordinates.length);
        pointsList.add(subCoordinates);
      }

      if (hasZ) {
        readCoordinates(in, pointsList, axisCount, 2, zOffset, zScale);
      }
      if (hasM) {
        readCoordinates(in, pointsList, axisCount, 3, mOffset, mScale);
      }

      final List<PointList> lists = new ArrayList<>();
      for (final double[] partCoordinates : pointsList) {
        lists.add(new DoubleCoordinatesList(axisCount, partCoordinates));
      }
      return lists;
    } catch (final IOException e) {
      throw new RuntimeException("Error reading coordinates", e);
    } finally {
      FileUtil.closeSilent(inputStream);
    }
  }

  public static Geometry getGeometry(final byte[] data,
    final GeometryFactory geometryFactory, final int entity,
    final int numPoints, final Double xOffset, final Double yOffset,
    final Double xyScale, final Double zOffset, final Double zScale,
    final Double mOffset, final Double mScale) {
    final InputStream in = new ByteArrayInputStream(data);
    return getGeometry(in, geometryFactory, entity, numPoints, xOffset,
      yOffset, xyScale, zOffset, zScale, mOffset, mScale);
  }

  public static Geometry getGeometry(final InputStream pointsIn,
    final GeometryFactory geometryFactory, final int geometryType,
    final int numPoints, final Double xOffset, final Double yOffset,
    final Double xyScale, final Double zOffset, final Double zScale,
    final Double mOffset, final Double mScale) {
    switch (geometryType) {
      case ArcSdeConstants.ST_GEOMETRY_POINT:
        return getPoint(pointsIn, geometryFactory, numPoints, xOffset, yOffset,
          xyScale, zOffset, zScale, mOffset, mScale);
      case ArcSdeConstants.ST_GEOMETRY_MULTI_POINT:
        return getMultiPoint(pointsIn, geometryFactory, numPoints, xOffset,
          yOffset, xyScale, zOffset, zScale, mOffset, mScale);
      case ArcSdeConstants.ST_GEOMETRY_LINESTRING:
        return getLineString(pointsIn, geometryFactory, numPoints, xOffset,
          yOffset, xyScale, zOffset, zScale, mOffset, mScale);
      case ArcSdeConstants.ST_GEOMETRY_MULTI_LINESTRING:
        return getMultiLineString(pointsIn, geometryFactory, numPoints,
          xOffset, yOffset, xyScale, zOffset, zScale, mOffset, mScale);
      case ArcSdeConstants.ST_GEOMETRY_POLYGON:
        return getPolygon(pointsIn, geometryFactory, numPoints, xOffset,
          yOffset, xyScale, zOffset, zScale, mOffset, mScale);
      case ArcSdeConstants.ST_GEOMETRY_MULTI_POLYGON:
        return getMultiPolygon(pointsIn, geometryFactory, numPoints, xOffset,
          yOffset, xyScale, zOffset, zScale, mOffset, mScale);
      default:
        throw new IllegalArgumentException("Unknown ST_GEOMETRY entity type: "
          + geometryType);
    }
  }

  private static LineString getLineString(final InputStream pointsIn,
    final GeometryFactory geometryFactory, final int numPoints,
    final Double xOffset, final Double yOffset, final Double xyScale,
    final Double zOffset, final Double zScale, final Double mOffset,
    final Double mScale) {
    final PointList points = getCoordinatesList(numPoints, xOffset, yOffset,
      xyScale, zOffset, zScale, mOffset, mScale, pointsIn);
    return geometryFactory.lineString(points);
  }

  private static MultiLineString getMultiLineString(final InputStream pointsIn,
    final GeometryFactory geometryFactory, final int numPoints,
    final Double xOffset, final Double yOffset, final Double xyScale,
    final Double zOffset, final Double zScale, final Double mOffset,
    final Double mScale) {
    final List<PointList> parts = getCoordinatesLists(numPoints, xOffset,
      yOffset, xyScale, zOffset, zScale, mOffset, mScale, pointsIn);
    return geometryFactory.multiLineString(parts);
  }

  private static MultiPoint getMultiPoint(final InputStream pointsIn,
    final GeometryFactory geometryFactory, final int numPoints,
    final Double xOffset, final Double yOffset, final Double xyScale,
    final Double zOffset, final Double zScale, final Double mOffset,
    final Double mScale) {
    final List<PointList> parts = getCoordinatesLists(numPoints, xOffset,
      yOffset, xyScale, zOffset, zScale, mOffset, mScale, pointsIn);
    return geometryFactory.multiPoint(parts);
  }

  private static MultiPolygon getMultiPolygon(final InputStream pointsIn,
    final GeometryFactory geometryFactory, final int numPoints,
    final Double xOffset, final Double yOffset, final Double xyScale,
    final Double zOffset, final Double zScale, final Double mOffset,
    final Double mScale) {
    final List<List<PointList>> pointsList = getMultiPolygonCoordinatesLists(
      numPoints, xOffset, yOffset, xyScale, zOffset, zScale, mOffset, mScale,
      pointsIn);
    try {
      return geometryFactory.multiPolygon(pointsList);
    } catch (final IllegalArgumentException e) {
      e.printStackTrace();
      LoggerFactory.getLogger(PackedCoordinateUtil.class).error(
        "Unable to load polygon", e);
      return null;
    }
  }

  private static List<List<PointList>> getMultiPolygonCoordinatesLists(
    final int vertexCount, final Double xOffset, final Double yOffset,
    final Double xyScale, final Double zOffset, final Double zScale,
    final Double mOffset, final Double mScale, final InputStream inputStream) {

    try {
      final List<List<double[]>> parts = new ArrayList<>();
      final PackedIntegerInputStream in = new PackedIntegerInputStream(
        inputStream);

      final long packedByteLength = in.readLong5();
      final long dimensionFlag = in.readLong();
      final int annotationDimension = in.read();
      final int shapeFlags = in.read();
      final boolean hasZ = (dimensionFlag & 0x01) == 0x01;
      final boolean hasM = (dimensionFlag & 0x02) == 0x02;

      int axisCount;
      if (hasM) {
        axisCount = 4;
      } else if (hasZ) {
        axisCount = 3;
      } else {
        axisCount = 2;
      }

      List<double[]> pointsList = new ArrayList<>();
      double[] coordinates = new double[vertexCount * axisCount];

      double x = xOffset;
      double y = yOffset;

      int j = 0;
      for (int i = 0; i < vertexCount; i++) {
        x = readCoordinate(in, coordinates, j, axisCount, 0, x, xyScale);
        y = readCoordinate(in, coordinates, j, axisCount, 1, y, xyScale);
        if (x == -1 && y == 0) {
          if (j > 2) {
            final double[] subCoordinates = new double[j * axisCount];
            System.arraycopy(coordinates, 0, subCoordinates, 0,
              subCoordinates.length);
            pointsList.add(subCoordinates);
          }
          coordinates = new double[(vertexCount - j) * axisCount];
          if (!pointsList.isEmpty()) {
            parts.add(pointsList);
          }
          pointsList = new ArrayList<>();
        } else if (j > 0 && i < vertexCount - 1) {
          if (coordinates[0] == coordinates[j * axisCount]
            && coordinates[1] == coordinates[j * axisCount + 1]) {
            if (j > 2) {
              final double[] subCoordinates = new double[j * axisCount];
              System.arraycopy(coordinates, 0, subCoordinates, 0,
                subCoordinates.length);
              pointsList.add(subCoordinates);
            }
            coordinates = new double[(vertexCount - j) * axisCount];
            j = 0;
          } else {
            j++;
          }
        } else {
          j++;
        }
      }
      if (j > 2) {
        if (coordinates.length == axisCount * j) {
          pointsList.add(coordinates);
        } else {
          final double[] subCoordinates = new double[j * axisCount];
          System.arraycopy(coordinates, 0, subCoordinates, 0,
            subCoordinates.length);
          pointsList.add(subCoordinates);
        }
      }
      if (!pointsList.isEmpty()) {
        parts.add(pointsList);
      }
      if (hasZ) {
        readMultiPolygonCoordinates(in, parts, axisCount, 2, zOffset, zScale);
      }
      if (hasM) {
        readMultiPolygonCoordinates(in, parts, axisCount, 3, mOffset, mScale);
      }

      final List<List<PointList>> lists = new ArrayList<>();
      for (final List<double[]> part : parts) {
        final List<PointList> list = new ArrayList<>();
        lists.add(list);
        for (final double[] partCoordinates : part) {
          list.add(new DoubleCoordinatesList(axisCount, partCoordinates));
        }
      }
      return lists;
    } catch (final IOException e) {
      throw new RuntimeException("Error reading coordinates", e);
    } finally {
      FileUtil.closeSilent(inputStream);
    }
  }

  public static int getNumPoints(final List<List<PointList>> parts) {
    int numPoints = 0;
    if (!parts.isEmpty()) {
      for (final List<PointList> part : parts) {
        for (final PointList points : part) {
          numPoints += points.getVertexCount();
        }
      }
      numPoints += parts.size() - 1;
    }
    return numPoints;
  }

  public static byte[] getPackedBytes(final Double xOffset,
    final Double yOffset, final Double xyScale, final boolean hasZ,
    final Double zOffset, final Double zScale, final boolean hasM,
    final Double mScale, final Double mOffset, final List<List<PointList>> parts) {

    final int packedByteLength = 0;
    byte dimensionFlag = 0;
    final byte annotationDimension = 0;
    final byte shapeFlags = 0;

    if (hasZ) {
      dimensionFlag |= 1;
    }
    if (hasM) {
      dimensionFlag |= 2;
    }

    final PackedIntegerOutputStream out = new PackedIntegerOutputStream();
    out.writeLong5(packedByteLength);
    out.writeLong(dimensionFlag);
    out.writeLong(annotationDimension);
    out.writeLong(shapeFlags);

    // Write x,y for all parts
    long previousX = Math.round(xOffset * xyScale);
    long previousY = Math.round(yOffset * xyScale);
    boolean first = true;
    for (final List<PointList> part : parts) {
      if (first) {
        first = false;
      } else {
        previousX = writeOrdinate(out, previousX, xyScale, -1);
        previousY = writeOrdinate(out, previousY, xyScale, 0);
      }
      for (final PointList points : part) {
        final int numPoints = points.getVertexCount();
        for (int i = 0; i < numPoints; i++) {
          previousX = writeOrdinate(out, points, previousX, xyScale, i, 0);
          previousY = writeOrdinate(out, points, previousY, xyScale, i, 1);
        }
      }
    }

    // Write z for all parts
    if (hasZ) {
      writeMultiCoordinates(out, parts, 2, zOffset, zScale);
    }

    // Write m for all parts
    if (hasM) {
      writeMultiCoordinates(out, parts, 3, mOffset, mScale);
    }
    return out.toByteArray();
  }

  private static Point getPoint(final InputStream pointsIn,
    final GeometryFactory geometryFactory, final int numPoints,
    final Double xOffset, final Double yOffset, final Double xyScale,
    final Double zOffset, final Double zScale, final Double mOffset,
    final Double mScale) {
    final PointList points = getCoordinatesList(numPoints, xOffset, yOffset,
      xyScale, zOffset, zScale, mOffset, mScale, pointsIn);
    return geometryFactory.point(points);
  }

  private static Polygon getPolygon(final InputStream pointsIn,
    final GeometryFactory geometryFactory, final int numPoints,
    final Double xOffset, final Double yOffset, final Double xyScale,
    final Double zOffset, final Double zScale, final Double mOffset,
    final Double mScale) {
    final List<LinearRing> pointsList = getPolygonRings(geometryFactory,
      numPoints, xOffset, yOffset, xyScale, zOffset, zScale, mOffset, mScale,
      pointsIn);
    try {
      return geometryFactory.polygon(pointsList);
    } catch (final IllegalArgumentException e) {
      e.printStackTrace();
      LoggerFactory.getLogger(PackedCoordinateUtil.class).error(
        "Unable to load polygon", e);
      return null;
    }
  }

  @SuppressWarnings("unused")
  private static List<LinearRing> getPolygonRings(
    final GeometryFactory geometryFactory, final int vertexCount,
    final Double xOffset, final Double yOffset, final Double xyScale,
    final Double zOffset, final Double zScale, final Double mOffset,
    final Double mScale, final InputStream inputStream) {

    try {
      final List<double[]> pointsList = new ArrayList<>();
      final PackedIntegerInputStream in = new PackedIntegerInputStream(
        inputStream);

      final long packedByteLength = in.readLong5();
      final long dimensionFlag = in.readLong();
      final int annotationDimension = in.read();
      final int shapeFlags = in.read();
      final boolean hasZ = (dimensionFlag & 0x01) == 0x01;
      final boolean hasM = (dimensionFlag & 0x02) == 0x02;

      int axisCount;
      if (hasM) {
        axisCount = 4;
      } else if (hasZ) {
        axisCount = 3;
      } else {
        axisCount = 2;
      }
      double[] coordinates = new double[vertexCount * axisCount];

      double x = xOffset;
      double y = yOffset;

      int j = 0;
      for (int i = 0; i < vertexCount; i++) {
        x = readCoordinate(in, coordinates, j, axisCount, 0, x, xyScale);
        y = readCoordinate(in, coordinates, j, axisCount, 1, y, xyScale);
        if (j > 0 && i < vertexCount - 1) {
          final int numVertices = j * axisCount;
          if (coordinates[0] == coordinates[numVertices]
            && coordinates[1] == coordinates[numVertices + 1]) {
            if (j > 2) {
              final double[] subCoordinates = new double[numVertices];
              System.arraycopy(coordinates, 0, subCoordinates, 0,
                subCoordinates.length);
              pointsList.add(subCoordinates);
            }
            coordinates = new double[(vertexCount - j) * axisCount];
            j = 0;
          } else {
            j++;
          }
        } else {
          j++;
        }
      }
      if (j > 2) {
        final int numVertices = j * axisCount;
        if (numVertices == coordinates.length) {
          pointsList.add(coordinates);
        } else {
          final double[] subCoordinates = new double[numVertices];
          System.arraycopy(coordinates, 0, subCoordinates, 0,
            subCoordinates.length);
          pointsList.add(subCoordinates);
        }
      }
      if (hasZ) {
        readPolygonOordinates(in, pointsList, axisCount, 2, zOffset, zScale);
      }
      if (hasM) {
        readPolygonOordinates(in, pointsList, axisCount, 3, mOffset, mScale);
      }
      final List<LinearRing> rings = new ArrayList<>();
      for (final double[] partCoordinates : pointsList) {
        rings.add(geometryFactory.linearRing(axisCount, partCoordinates));
      }
      return rings;
    } catch (final IOException e) {
      throw new RuntimeException("Error reading coordinates", e);
    } finally {
      FileUtil.closeSilent(inputStream);
    }
  }

  private static double readCoordinate(final PackedIntegerInputStream in,
    final double previousValue, final double scale) throws IOException {
    final long deltaValueLong = in.readLong();
    final double deltaValue = deltaValueLong / scale;
    final double value = Math.round((previousValue + deltaValue) * scale)
      / scale;
    return value;
  }

  private static double readCoordinate(final PackedIntegerInputStream in,
    final double[] coordinates, final int vertexIndex, final int axisCount,
    final int axisIndex, final double previousValue, final double scale)
    throws IOException {
    final double value = readCoordinate(in, previousValue, scale);
    coordinates[vertexIndex * axisCount + axisIndex] = value;
    return value;
  }

  private static void readCoordinates(final PackedIntegerInputStream in,
    final double[] coordinates, final int vertexCount, final int axisCount,
    final int axisIndex, final double offset, final double scale)
    throws IOException {

    double previousValue = offset;
    for (int i = 0; i < vertexCount; i++) {
      previousValue = readCoordinate(in, coordinates, i, axisCount, axisIndex,
        previousValue, scale);
    }
  }

  private static void readCoordinates(final PackedIntegerInputStream in,
    final List<double[]> pointsList, final int axisCount, final int axisIndex,
    final double offset, final double scale) throws IOException {

    double previousValue = offset;
    int j = 0;
    for (final double[] coordinates : pointsList) {
      if (j > 0) {
        previousValue = readCoordinate(in, previousValue, scale);
      }
      final int vertexCount = coordinates.length / axisCount;
      for (int i = 0; i < vertexCount; i++) {
        previousValue = readCoordinate(in, coordinates, i, axisCount,
          axisIndex, previousValue, scale);
      }
      j++;
    }
  }

  private static void readMultiPolygonCoordinates(
    final PackedIntegerInputStream in, final List<List<double[]>> parts,
    final int axisCount, final int axisIndex, final double offset,
    final double scale) throws IOException {

    double previousValue = offset;
    boolean first = true;
    for (final List<double[]> part : parts) {
      if (first) {
        first = false;
      } else {
        previousValue = readCoordinate(in, previousValue, scale);
      }
      for (final double[] points : part) {
        final int numPoints = points.length / axisCount;
        for (int i = 0; i < numPoints; i++) {
          previousValue = readCoordinate(in, points, i, axisCount, axisIndex,
            previousValue, scale);
        }
      }
    }
  }

  private static void readPolygonOordinates(final PackedIntegerInputStream in,
    final List<double[]> pointsList, final int axisCount, final int axisIndex,
    final double offset, final double scale) throws IOException {

    double previousValue = offset;
    for (final double[] coordinates : pointsList) {
      final int numPoints = coordinates.length / axisCount;
      for (int i = 0; i < numPoints; i++) {
        previousValue = readCoordinate(in, coordinates, i, axisCount,
          axisIndex, previousValue, scale);
      }
    }
  }

  private static long writeCoordinates(final PackedIntegerOutputStream out,
    final PointList points, long previousValue, final double scale,
    final int ordinateIndex) {
    for (int i = 0; i < points.getVertexCount(); i++) {
      previousValue = writeOrdinate(out, points, previousValue, scale, i,
        ordinateIndex);
    }
    return previousValue;
  }

  private static void writeMultiCoordinates(
    final PackedIntegerOutputStream out, final List<List<PointList>> partsList,
    final int axisIndex, final double offset, final double scale) {
    long previous = Math.round(offset * scale);
    boolean firstPart = true;
    for (final List<PointList> part : partsList) {
      if (firstPart) {
        firstPart = false;
      } else {
        previous = writeOrdinate(out, previous, scale, 0);
      }
      for (final PointList points : part) {
        if (points.getAxisCount() > axisIndex) {
          previous = writeCoordinates(out, points, previous, scale, axisIndex);
        } else {
          previous = writeZeroCoordinates(out, points.getVertexCount(), scale, previous);
        }
      }
    }
  }

  private static long writeOrdinate(final PackedIntegerOutputStream out,
    final long previousValue, final double scale, final double value) {
    long longValue;
    if (Double.isNaN(value)) {
      longValue = 0;
    } else {
      longValue = Math.round(value * scale);
    }
    out.writeLong(longValue - previousValue);
    return longValue;
  }

  /**
   * Write the value of an ordinate from the coordinates which has the specified
   * coordinateIndex and ordinateIndex. The value written is the difference
   * between the current value and the previous value which are both multiplied
   * by the scale and rounded to longs before conversion.
   * 
   * @param out The stream to write the bytes to.
   * @param coordinates The coordinates.
   * @param previousValue The value of the previous coordinate, returned from
   *          this method.
   * @param scale The scale which defines the precision of the values.
   * @param coordinateIndex The coordinate index.
   * @param ordinateIndex The ordinate index.
   * @return The current ordinate value * scale rounded to a long value.
   */
  private static long writeOrdinate(final PackedIntegerOutputStream out,
    final PointList coordinates, final long previousValue, final double scale,
    final int coordinateIndex, final int ordinateIndex) {
    final double value = coordinates.getCoordinate(coordinateIndex, ordinateIndex);
    return writeOrdinate(out, previousValue, scale, value);
  }

  private static long writeZeroCoordinates(final PackedIntegerOutputStream out,
    final int numCoordinates, final double scale, long previousValue) {
    for (int i = 0; i < numCoordinates; i++) {
      previousValue = writeOrdinate(out, previousValue, scale, 0);
    }
    return previousValue;
  }
}
