package com.revolsys.gis.model.coordinates.list;

import java.util.Iterator;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

public class CoordinatesListUtil {
  public static int append(
    final CoordinatesList src,
    final CoordinatesList dest,
    final int startIndex) {
    int coordIndex = startIndex;
    final int srcDimension = src.getDimension();
    final int destDimension = dest.getDimension();
    final int dimension = Math.min(srcDimension, destDimension);
    final int srcSize = src.size();
    final int destSize = dest.size();
    double previousX;
    double previousY;
    if (startIndex == 0) {
      previousX = Double.NaN;
      previousY = Double.NaN;
    } else {
      previousX = dest.getValue(startIndex - 1, 0);
      previousY = dest.getValue(startIndex - 1, 1);
    }
    for (int i = 0; i < srcSize && coordIndex < destSize; i++) {
      final double x = src.getValue(i, 0);
      final double y = src.getValue(i, 1);
      if (x != previousX && y != previousY) {
        dest.setValue(coordIndex, 0, x);
        dest.setValue(coordIndex, 1, y);
        for (int d = 2; d < dimension; d++) {
          final double ordinate = src.getValue(i, d);
          dest.setValue(coordIndex, d, ordinate);
        }
        coordIndex++;
      }
      previousX = x;
      previousY = y;
    }
    return coordIndex;
  }

  public static int appendReversed(
    final CoordinatesList src,
    final CoordinatesList dest,
    final int startIndex) {
    int coordIndex = startIndex;
    final int srcDimension = src.getDimension();
    final int destDimension = dest.getDimension();
    final int dimension = Math.min(srcDimension, destDimension);
    final int srcSize = src.size();
    final int destSize = dest.size();
    double previousX;
    double previousY;
    if (startIndex == 0) {
      previousX = Double.NaN;
      previousY = Double.NaN;
    } else {
      previousX = dest.getValue(startIndex - 1, 0);
      previousY = dest.getValue(startIndex - 1, 1);
    }
    for (int i = srcSize - 1; i > -1 && coordIndex < destSize; i--) {
      final double x = src.getValue(i, 0);
      final double y = src.getValue(i, 1);
      if (x != previousX && y != previousY) {
        dest.setValue(coordIndex, 0, x);
        dest.setValue(coordIndex, 1, y);
        for (int d = 2; d < dimension; d++) {
          final double ordinate = src.getValue(i, d);
          dest.setValue(coordIndex, d, ordinate);
        }
        coordIndex++;
      }
      previousX = x;
      previousY = y;
    }
    return coordIndex;
  }

  public static final CoordinatesList get(
    final CoordinateSequence coordinateSequence) {
    if (coordinateSequence instanceof CoordinatesList) {
      return (CoordinatesList)coordinateSequence;
    } else {
      return new CoordinateSequenceCoordinateList(coordinateSequence);
    }

  }

  public static CoordinatesList merge(
    final CoordinatesList coordinates1,
    final CoordinatesList coordinates2) {
    final int dimension = Math.max(coordinates1.getDimension(),
      coordinates2.getDimension());
    final int maxSize = coordinates1.size() + coordinates2.size();
    final CoordinatesList coordinates = new DoubleCoordinatesList(maxSize,
      dimension);

    int numCoords = 0;
    final Coordinate coordinates1Start = coordinates1.getCoordinate(0);
    final Coordinate coordinates1End = coordinates1.getCoordinate(coordinates1.size() - 1);
    final Coordinate coordinates2Start = coordinates2.getCoordinate(0);
    final Coordinate coordinates2End = coordinates2.getCoordinate(coordinates2.size() - 1);
    if (coordinates1Start.equals2D(coordinates2End)) {
      numCoords = append(coordinates2, coordinates, numCoords);
      numCoords = append(coordinates1, coordinates, numCoords);
    } else if (coordinates2Start.equals2D(coordinates1End)) {
      numCoords = append(coordinates1, coordinates, numCoords);
      numCoords = append(coordinates2, coordinates, numCoords);
    } else if (coordinates1Start.equals2D(coordinates2Start)) {
      numCoords = appendReversed(coordinates2, coordinates, numCoords);
      numCoords = append(coordinates1, coordinates, numCoords);
    } else if (coordinates1End.equals2D(coordinates2End)) {
      numCoords = append(coordinates1, coordinates, numCoords);
      numCoords = appendReversed(coordinates2, coordinates, numCoords);
    } else {
      throw new IllegalArgumentException("lines don't touch");

    }
    return trim(coordinates, numCoords);
  }

  public static CoordinatesList merge(
    final List<CoordinatesList> coordinatesList) {
    final Iterator<CoordinatesList> iterator = coordinatesList.iterator();
    if (!iterator.hasNext()) {
      return null;
    } else {
      CoordinatesList coordinates = iterator.next();
      while (iterator.hasNext()) {
        final CoordinatesList nextCoordinates = iterator.next();
        coordinates = merge(coordinates, nextCoordinates);
      }
      return coordinates;
    }
  }

  public static CoordinatesList trim(
    final CoordinatesList coordinates,
    final int length) {
    if (length == coordinates.size()) {
      return coordinates;
    } else {
      return coordinates.subList(0, length);
    }
  }

  public static CoordinatesList get(
    LineString line) {
    return get(line.getCoordinateSequence());
  }

  public static CoordinatesList get(
    Point point) {
    return get(point.getCoordinateSequence());
  }

}
