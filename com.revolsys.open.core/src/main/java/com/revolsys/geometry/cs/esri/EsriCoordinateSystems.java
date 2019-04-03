package com.revolsys.geometry.cs.esri;

import java.io.FileNotFoundException;
import java.nio.file.FileSystemException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.revolsys.geometry.cs.Authority;
import com.revolsys.geometry.cs.BaseAuthority;
import com.revolsys.geometry.cs.CompoundCoordinateSystem;
import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.cs.Ellipsoid;
import com.revolsys.geometry.cs.GeographicCoordinateSystem;
import com.revolsys.geometry.cs.ParameterName;
import com.revolsys.geometry.cs.ParameterValue;
import com.revolsys.geometry.cs.ParameterValueBigDecimal;
import com.revolsys.geometry.cs.PrimeMeridian;
import com.revolsys.geometry.cs.ProjectedCoordinateSystem;
import com.revolsys.geometry.cs.SingleParameterName;
import com.revolsys.geometry.cs.VerticalCoordinateSystem;
import com.revolsys.geometry.cs.WktCsParser;
import com.revolsys.geometry.cs.datum.GeodeticDatum;
import com.revolsys.geometry.cs.datum.VerticalDatum;
import com.revolsys.geometry.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.geometry.cs.unit.AngularUnit;
import com.revolsys.geometry.cs.unit.LinearUnit;
import com.revolsys.io.EndOfFileException;
import com.revolsys.io.channels.ChannelReader;
import com.revolsys.logging.Logs;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.WrappedException;

public class EsriCoordinateSystems {
  private static Map<Integer, CoordinateSystem> COORDINATE_SYSTEM_BY_ID = new HashMap<>();

  private static Map<ByteArray, List<Integer>> COORDINATE_SYSTEM_IDS_BY_DIGEST = new HashMap<>();

  private static final Map<String, AngularUnit> ANGULAR_UNITS_BY_NAME = new TreeMap<>();

  private static final Map<String, LinearUnit> LINEAR_UNITS_BY_NAME = new TreeMap<>();

  private static Map<ParameterName, ParameterValue> convertParameters(
    final Map<String, String> parameters) {
    final Map<ParameterName, ParameterValue> parameterValues = new LinkedHashMap<>();
    for (final Entry<String, String> parameter : parameters.entrySet()) {
      final String name = parameter.getKey();

      final String value = parameter.getValue();
      final ParameterName parameterName = new SingleParameterName(name);
      final ParameterValue parameterValue = new ParameterValueBigDecimal(value);
      parameterValues.put(parameterName, parameterValue);
    }
    return parameterValues;
  }

  @SuppressWarnings("unchecked")
  public static <C extends CoordinateSystem> C getCoordinateSystem(final int crsId) {
    CoordinateSystem coordinateSystem = COORDINATE_SYSTEM_BY_ID.get(crsId);
    if (coordinateSystem == null) {
      coordinateSystem = getGeographicCoordinateSystem(crsId);
      if (coordinateSystem == null) {
        coordinateSystem = getProjectedCoordinateSystem(crsId);
        if (coordinateSystem == null) {
          coordinateSystem = getVerticalCoordinateSystem(crsId);
        }
      }
    }
    return (C)coordinateSystem;
  }

  private static List<Integer> getCoordinateSystemIdsByDigest(
    final CoordinateSystem coordinateSystem, final ByteArray digest) {
    List<Integer> ids = COORDINATE_SYSTEM_IDS_BY_DIGEST.get(digest);
    if (ids == null) {
      final byte[] bytes = new byte[16];
      final ByteArray newDigest = new ByteArray(bytes);
      final String type = coordinateSystem.getCoordinateSystemType();
      if (coordinateSystem instanceof CompoundCoordinateSystem) {
        return Collections.emptyList();
      } else {
        try (
          ChannelReader reader = ChannelReader
            .newChannelReader("classpath:CoordinateSystems/esri/" + type + ".digest")) {
          while (true) {
            reader.getBytes(bytes);
            final short count = reader.getShort();
            if (digest.equals(newDigest)) {
              ids = new ArrayList<>();
              for (int i = 0; i < count; i++) {
                final int csId = reader.getInt();
                ids.add(csId);
              }
              COORDINATE_SYSTEM_IDS_BY_DIGEST.put(digest, ids);
              return ids;
            } else {
              for (int i = 0; i < count; i++) {
                reader.getInt();
              }
            }
          }
        } catch (final EndOfFileException e) {
          return Collections.emptyList();
        }
      }
    } else {
      return ids;
    }
  }

  public static GeographicCoordinateSystem getGeographicCoordinateSystem(final int id) {
    GeographicCoordinateSystem coordinateSystem = (GeographicCoordinateSystem)COORDINATE_SYSTEM_BY_ID
      .get(id);
    if (coordinateSystem == null) {
      try (
        final ChannelReader reader = ChannelReader
          .newChannelReader("classpath:CoordinateSystems/esri/Geographic.cs")) {
        while (true) {
          final int coordinateSystemId = reader.getInt();
          final String csName = reader.getStringUtf8ByteCount();
          final String datumName = reader.getStringUtf8ByteCount();
          final String spheroidName = reader.getStringUtf8ByteCount();
          final double semiMajorAxis = reader.getDouble();
          final double inverseFlattening = reader.getDouble();
          final String primeMeridianName = reader.getStringUtf8ByteCount();
          final double longitude = reader.getDouble();
          final String angularUnitName = reader.getStringUtf8ByteCount();
          final double conversionFactor = reader.getDouble();

          if (id == coordinateSystemId) {
            final Ellipsoid ellipsoid = new Ellipsoid(spheroidName, semiMajorAxis,
              inverseFlattening, null);
            final PrimeMeridian primeMeridian = new PrimeMeridian(primeMeridianName, longitude,
              null);
            final GeodeticDatum geodeticDatum = new GeodeticDatum(null, datumName, null, false,
              ellipsoid, primeMeridian);

            AngularUnit angularUnit = ANGULAR_UNITS_BY_NAME.get(angularUnitName);
            if (angularUnit == null) {
              angularUnit = new AngularUnit(angularUnitName, conversionFactor, null);
              ANGULAR_UNITS_BY_NAME.put(angularUnitName, angularUnit);
            }

            final Authority authority = new BaseAuthority("ESRI", coordinateSystemId);
            coordinateSystem = new GeographicCoordinateSystem(coordinateSystemId, csName,
              geodeticDatum, primeMeridian, angularUnit, null, authority);
            COORDINATE_SYSTEM_BY_ID.put(id, coordinateSystem);
            return coordinateSystem;
          }
        }
      } catch (final EndOfFileException e) {
        return null;
      }
    }
    return coordinateSystem;
  }

  public static int getIdUsingDigest(final CoordinateSystem coordinateSystem) {
    if (coordinateSystem == null) {
      return 0;
    } else {
      final ByteArray digest = new ByteArray(coordinateSystem.md5Digest());
      final List<Integer> ids = getCoordinateSystemIdsByDigest(coordinateSystem, digest);
      if (ids.isEmpty()) {
        return 0;
      } else if (ids.size() == 1) {
        return ids.get(0);
      } else {
        final List<CoordinateSystem> coordinateSystems = new ArrayList<>();
        for (final int coordinateSystemId : ids) {
          final CoordinateSystem coordinateSystem2 = getCoordinateSystem(coordinateSystemId);
          if (coordinateSystem2 != null) {
            if (coordinateSystem.getCoordinateSystemName()
              .equalsIgnoreCase(coordinateSystem2.getCoordinateSystemName())) {
              return coordinateSystemId;
            } else {
              coordinateSystems.add(coordinateSystem2);
            }
          }
        }
        for (final CoordinateSystem coordinateSystem2 : coordinateSystems) {
          if (coordinateSystem.isSame(coordinateSystem2)) {
            return coordinateSystem2.getCoordinateSystemId();
          }
        }
        // Match base on names etc
        return ids.get(0);
      }
    }
  }

  public static ProjectedCoordinateSystem getProjectedCoordinateSystem(final int id) {
    ProjectedCoordinateSystem coordinateSystem = (ProjectedCoordinateSystem)COORDINATE_SYSTEM_BY_ID
      .get(id);
    if (coordinateSystem == null) {
      try (
        final ChannelReader reader = ChannelReader
          .newChannelReader("classpath:CoordinateSystems/esri/Projected.cs")) {
        while (true) {
          final int coordinateSystemId = reader.getInt();
          final String csName = reader.getStringUtf8ByteCount();

          final int geographicCoordinateSystemId = reader.getInt();
          final String projectionName = reader.getStringUtf8ByteCount();
          final Map<String, String> parameters = readParameters(reader);
          final String unitName = reader.getStringUtf8ByteCount();
          final double conversionFactor = reader.getDouble();

          if (id == coordinateSystemId) {
            LinearUnit linearUnit = LINEAR_UNITS_BY_NAME.get(unitName);
            if (linearUnit == null) {
              linearUnit = new LinearUnit(unitName, conversionFactor);
              LINEAR_UNITS_BY_NAME.put(unitName, linearUnit);
            }
            final Authority authority = new BaseAuthority("ESRI", coordinateSystemId);
            final GeographicCoordinateSystem geographicCoordinateSystem = getGeographicCoordinateSystem(
              geographicCoordinateSystemId);
            final Map<ParameterName, ParameterValue> parameterValues = convertParameters(
              parameters);
            coordinateSystem = new ProjectedCoordinateSystem(coordinateSystemId, csName,
              geographicCoordinateSystem, projectionName, parameterValues, linearUnit, authority);
            COORDINATE_SYSTEM_BY_ID.put(id, coordinateSystem);
            return coordinateSystem;
          }
        }
      } catch (final EndOfFileException e) {
        return null;
      }
    }
    return coordinateSystem;
  }

  public static VerticalCoordinateSystem getVerticalCoordinateSystem(final int id) {
    VerticalCoordinateSystem coordinateSystem = (VerticalCoordinateSystem)COORDINATE_SYSTEM_BY_ID
      .get(id);
    if (coordinateSystem == null) {
      try (
        final ChannelReader reader = ChannelReader
          .newChannelReader("classpath:CoordinateSystems/esri/Vertical.cs")) {
        while (true) {
          final int coordinateSystemId = reader.getInt();
          final String csName = reader.getStringUtf8ByteCount();
          final String datumName = reader.getStringUtf8ByteCount();
          final Map<String, String> parameters = readParameters(reader);
          final String linearUnitName = reader.getStringUtf8ByteCount();
          final double conversionFactor = reader.getDouble();

          if (id == coordinateSystemId) {
            final VerticalDatum verticalDatum = new VerticalDatum(null, datumName, 0);

            LinearUnit linearUnit = LINEAR_UNITS_BY_NAME.get(linearUnitName);
            if (linearUnit == null) {
              linearUnit = new LinearUnit(linearUnitName, conversionFactor, null);
              LINEAR_UNITS_BY_NAME.put(linearUnitName, linearUnit);
            }

            final Authority authority = new BaseAuthority("ESRI", coordinateSystemId);
            final Map<ParameterName, ParameterValue> parameterValues = convertParameters(
              parameters);

            coordinateSystem = new VerticalCoordinateSystem(authority, csName, verticalDatum,
              parameterValues, linearUnit, Collections.emptyList());
            COORDINATE_SYSTEM_BY_ID.put(id, coordinateSystem);
            return coordinateSystem;
          }
        }
      } catch (final EndOfFileException e) {
        return null;
      }
    }
    return coordinateSystem;
  }

  /**
   * Read the coordinate system from the resource. If it is a standard one then
   *  {@link EpsgCoordinateSystems#getCoordinateSystem(int)} will be used to return that
   *  coordinate system.
   */
  public static <C extends CoordinateSystem> C readCoordinateSystem(final Resource resource) {
    if (resource == null) {
      return null;
    } else {
      final Resource projResource = resource.newResourceChangeExtension("prj");
      if (projResource != null) {
        try {
          final String wkt = projResource.contentsAsString();
          return readCoordinateSystem(wkt);
        } catch (final WrappedException e) {
          final Throwable cause = e.getCause();
          if (cause instanceof FileNotFoundException) {
          } else if (cause instanceof FileSystemException) {
          } else {
            Logs.error(WktCsParser.class, "Unable to load projection from " + projResource, e);
          }
        } catch (final Exception e) {
          Logs.error(WktCsParser.class, "Unable to load projection from " + projResource, e);
        }
      }
    }
    return null;
  }

  /**
   * Parse the coordinate system from the WKT. If it is a standard one then
   *  {@link EpsgCoordinateSystems#getCoordinateSystem(int)} will be used to return that
   *  coordinate system.
   */
  @SuppressWarnings("unchecked")
  public static <C extends CoordinateSystem> C readCoordinateSystem(final String wkt) {
    final CoordinateSystem coordinateSystem = WktCsParser.read(wkt);
    return (C)readCoordinateSystemPost(coordinateSystem);
  }

  private static CoordinateSystem readCoordinateSystemPost(
    final CoordinateSystem coordinateSystem) {
    if (coordinateSystem == null) {
      return null;
    } else {
      int id = coordinateSystem.getCoordinateSystemId();
      if (id <= 0) {
        id = getIdUsingDigest(coordinateSystem);
      }
      if (id > 0) {
        final CoordinateSystem epsgCoordinateSystem = EpsgCoordinateSystems.getCoordinateSystem(id);
        if (epsgCoordinateSystem == null) {
          final CoordinateSystem esriCoordinateSystem = getCoordinateSystem(id);
          if (esriCoordinateSystem != null) {
            return esriCoordinateSystem;
          }
        } else {
          return epsgCoordinateSystem;
        }
      }
      return coordinateSystem;
    }
  }

  private static Map<String, String> readParameters(final ChannelReader reader) {
    final byte parameterCount = reader.getByte();
    final Map<String, String> parameters = new LinkedHashMap<>();
    for (int i = 0; i < parameterCount; i++) {
      final String name = reader.getStringUtf8ByteCount();
      final String value = reader.getStringUtf8ByteCount();
      parameters.put(name, value);
    }
    return parameters;
  }

}
