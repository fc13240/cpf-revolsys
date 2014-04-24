package com.revolsys.gis.cs.projection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.revolsys.gis.jts.GeometryProperties;
import com.revolsys.gis.model.coordinates.CoordinatesListCoordinates;
import com.revolsys.gis.model.coordinates.CoordinatesPrecisionModel;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryCollection;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.MultiLineString;
import com.revolsys.jts.geom.MultiPoint;
import com.revolsys.jts.geom.MultiPolygon;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;

public class CoordinatesOperationGeometryOperation implements GeometryOperation {
  private final com.revolsys.jts.geom.GeometryFactory geometryFactory;

  private final CoordinatesOperation operation;

  public CoordinatesOperationGeometryOperation(
    final CoordinatesOperation operation,
    final com.revolsys.jts.geom.GeometryFactory geometryFactory) {
    this.operation = operation;
    this.geometryFactory = geometryFactory;
  }

  private void addUserData(final Geometry oldGeometry,
    final Geometry newGeometry) {
    final Object userData = oldGeometry.getUserData();
    if (userData != null) {
      if (userData instanceof Map) {
        GeometryProperties.copyUserData(oldGeometry, newGeometry);
      } else {
        newGeometry.setUserData(userData);
      }
    }
  }

  public CoordinatesList perform(final CoordinatesList coordinates) {
    if (coordinates == null) {
      return null;
    } else {
      final int size = coordinates.size();
      final CoordinatesList newCoordinates = new DoubleCoordinatesList(size,
        geometryFactory.getAxisCount());
      final CoordinatesListCoordinates sourceCoordinates = new CoordinatesListCoordinates(
        coordinates);
      final CoordinatesListCoordinates targetCoordinates = new CoordinatesListCoordinates(
        newCoordinates);
      final CoordinatesPrecisionModel precisionModel = geometryFactory.getCoordinatesPrecisionModel();
      for (int i = 0; i < size; i++) {
        sourceCoordinates.setIndex(i);
        targetCoordinates.setIndex(i);
        operation.perform(sourceCoordinates, targetCoordinates);
      }
      newCoordinates.makePrecise(precisionModel);
      return newCoordinates;
    }
  }

  public GeometryCollection perform(final GeometryCollection geometryCollection) {
    if (geometryCollection != null) {
      final Geometry[] newGeometries = new Geometry[geometryCollection.getGeometryCount()];
      for (int i = 0; i < geometryCollection.getGeometryCount(); i++) {
        final Geometry geometry = geometryCollection.getGeometry(i);
        final Geometry newGeometry = perform(geometry);
        addUserData(geometry, newGeometry);
        newGeometries[i] = newGeometry;
      }
      final GeometryCollection newGeometryCollection = geometryFactory.geometryCollection(newGeometries);
      addUserData(geometryCollection, newGeometryCollection);
      return newGeometryCollection;
    } else {
      return null;
    }
  }

  public LinearRing perform(final LinearRing ring) {
    if (ring != null) {
      final CoordinatesList newCoordinates = perform(CoordinatesListUtil.get(ring));
      final LinearRing newRing = geometryFactory.linearRing(newCoordinates);
      addUserData(ring, newRing);
      return newRing;
    } else {
      return null;
    }
  }

  public LineString perform(final LineString line) {
    if (line != null) {
      final CoordinatesList newCoordinates = perform(CoordinatesListUtil.get(line));
      final LineString newLine = geometryFactory.lineString(newCoordinates);
      addUserData(line, newLine);
      return newLine;
    } else {
      return null;
    }
  }

  public MultiLineString perform(final MultiLineString multiLineString) {
    if (multiLineString != null) {
      final LineString[] newLineStrings = new LineString[multiLineString.getGeometryCount()];
      for (int i = 0; i < multiLineString.getGeometryCount(); i++) {
        final LineString line = (LineString)multiLineString.getGeometry(i);
        final LineString newLineString = perform(line);
        addUserData(line, newLineString);
        newLineStrings[i] = newLineString;
      }
      final MultiLineString newMultiLineString = geometryFactory.multiLineString(newLineStrings);
      addUserData(multiLineString, newMultiLineString);
      return newMultiLineString;
    } else {
      return null;
    }
  }

  public Geometry perform(final MultiPoint multiPoint) {
    if (multiPoint != null) {
      final Point[] newPoints = new Point[multiPoint.getGeometryCount()];
      for (int i = 0; i < multiPoint.getGeometryCount(); i++) {
        final Point point = (Point)multiPoint.getGeometry(i);
        final Point newPoint = perform(point);
        addUserData(point, newPoint);
        newPoints[i] = newPoint;
      }
      final MultiPoint newMultiPoint = geometryFactory.multiPoint(newPoints);
      addUserData(multiPoint, newMultiPoint);
      return newMultiPoint;
    } else {
      return null;
    }
  }

  public MultiPolygon perform(final MultiPolygon multiPolygon) {
    if (multiPolygon != null) {
      final Polygon[] newPolygons = new Polygon[multiPolygon.getGeometryCount()];
      for (int i = 0; i < multiPolygon.getGeometryCount(); i++) {
        final Polygon polygon = (Polygon)multiPolygon.getGeometry(i);
        final Polygon newPolygon = perform(polygon);
        addUserData(polygon, newPolygon);
        newPolygons[i] = newPolygon;
      }
      final MultiPolygon newMultiPolygon = geometryFactory.multiPolygon(newPolygons);
      addUserData(multiPolygon, newMultiPolygon);
      return newMultiPolygon;
    } else {
      return null;
    }
  }

  public Point perform(final Point point) {
    if (point != null) {
      final Point newPoint;
      if (point.isEmpty()) {
        newPoint = geometryFactory.point();
      } else {
        final int axisCount = point.getAxisCount();
        final double[] sourceCoordinates = new double[axisCount];
        final double[] targetCoordinates = new double[axisCount];
        for (int i = 0; i < axisCount; i++) {
          final double value = point.getValue(i);
          sourceCoordinates[i] = value;
          targetCoordinates[i] = value;
        }
        operation.perform(axisCount, sourceCoordinates, axisCount,
          targetCoordinates);
        newPoint = geometryFactory.point(targetCoordinates);
      }
      addUserData(point, newPoint);
      return newPoint;
    } else {
      return null;
    }
  }

  public Polygon perform(final Polygon polygon) {
    final List<LinearRing> rings = new ArrayList<>();
    for (final LinearRing ring : polygon.rings()) {
      final LinearRing newRing = perform(ring);
      rings.add(newRing);
    }
    final Polygon newPolygon = geometryFactory.polygon(rings);
    addUserData(newPolygon, polygon);
    return newPolygon;

  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends Geometry> T perform(final T geometry) {
    if (geometry instanceof Point) {
      final Point point = (Point)geometry;
      return (T)perform(point);
    } else if (geometry instanceof LineString) {
      final LineString line = (LineString)geometry;
      return (T)perform(line);
    } else if (geometry instanceof Polygon) {
      final Polygon polygon = (Polygon)geometry;
      return (T)perform(polygon);
    } else if (geometry instanceof MultiPoint) {
      final MultiPoint point = (MultiPoint)geometry;
      return (T)perform(point);
    } else if (geometry instanceof MultiLineString) {
      final MultiLineString line = (MultiLineString)geometry;
      return (T)perform(line);
    } else if (geometry instanceof MultiPolygon) {
      final MultiPolygon polygon = (MultiPolygon)geometry;
      return (T)perform(polygon);
    } else if (geometry instanceof GeometryCollection) {
      final GeometryCollection geometryCollection = (GeometryCollection)geometry;
      return (T)perform(geometryCollection);
    } else {
      return geometry;
    }
  }

}
