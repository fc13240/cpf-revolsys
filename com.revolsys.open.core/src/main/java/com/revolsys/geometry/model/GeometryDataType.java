package com.revolsys.geometry.model;

import java.util.Collection;
import java.util.function.Function;

import com.revolsys.beans.Classes;
import com.revolsys.datatype.AbstractDataType;
import com.revolsys.geometry.model.editor.GeometryEditor;

public class GeometryDataType<G extends Geometry, GE extends GeometryEditor<?>>
  extends AbstractDataType {

  private final Function<Object, ? extends Geometry> toObjectFunction;

  private final Function<GeometryFactory, GE> newGeometryEditorFunction;

  public GeometryDataType(final Class<G> javaClass,
    final Function<Object, ? extends Geometry> toObjectFunction,
    final Function<GeometryFactory, GE> newGeometryEditorFunction) {
    super(Classes.className(javaClass), javaClass, true);
    this.toObjectFunction = toObjectFunction;
    this.newGeometryEditorFunction = newGeometryEditorFunction;
  }

  @Override
  protected boolean equalsNotNull(final Object value1, final Object value2) {
    return ((Geometry)value1).equalsExact((Geometry)value2);
  }

  @Override
  protected boolean equalsNotNull(final Object value1, final Object value2,
    final Collection<? extends CharSequence> excludeFieldNames) {
    return ((Geometry)value1).equalsExact((Geometry)value2);
  }

  public GE newGeometryEditor(final GeometryFactory geometryFactory) {
    return this.newGeometryEditorFunction.apply(geometryFactory);
  }

  @Override
  protected Object toObjectDo(final Object value) {
    return this.toObjectFunction.apply(value);
  }

}