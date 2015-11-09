package com.revolsys.gis.converter.string;

import com.revolsys.converter.string.StringConverter;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.record.io.format.wkt.EWktWriter;
import com.revolsys.record.io.format.wkt.WktParser;

public class GeometryStringConverter implements StringConverter<Geometry> {
  @Override
  public Class<Geometry> getConvertedClass() {
    return Geometry.class;
  }

  @Override
  public boolean requiresQuotes() {
    return true;
  }

  @Override
  public Geometry objectToObject(final Object value) {
    if (value instanceof Geometry) {
      final Geometry geometry = (Geometry)value;
      return geometry;
    } else if (value == null) {
      return null;
    } else {
      return stringToObject(value.toString());
    }
  }

  @Override
  public Geometry stringToObject(final String string) {
    return new WktParser().parseGeometry(string, false);
  }

  @Override
  public String objectToString(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Geometry) {
      final Geometry geometry = (Geometry)value;
      return EWktWriter.toString(geometry, true);
    } else {
      return value.toString();
    }
  }
}
