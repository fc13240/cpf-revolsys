package com.revolsys.equals;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.record.Record;

public class EqualsRegistry implements Equals<Object> {

  private final Map<Class<?>, Equals<?>> classEqualsMap = new HashMap<Class<?>, Equals<?>>();

  public EqualsRegistry() {
    final ObjectEquals defaultEquals = new ObjectEquals();
    register(Object.class, defaultEquals);
    register(String.class, defaultEquals);

    register(Boolean.class, new BooleanEquals());
    final NumberEquals numberEquals = new NumberEquals();
    register(Number.class, numberEquals);
    register(BigDecimal.class, numberEquals);
    register(BigInteger.class, numberEquals);
    register(Long.class, numberEquals);
    register(Byte.class, numberEquals);
    register(Integer.class, numberEquals);
    register(Short.class, numberEquals);
    register(Geometry.class, new GeometryEqualsExact3d());
    register(Date.class, new DateTimeEquals());
    register(java.sql.Date.class, new DateEquals());
    register(Timestamp.class, new TimestampEquals());
    register(Map.class, new MapEquals());
    register(List.class, new ListEquals());
    register(Record.class, new RecordEquals());
  }

  public boolean equals(final Object object1, final Object object2) {
    final Set<String> exclude = Collections.emptySet();
    return equals(object1, object2, exclude);
  }

  @Override
  public boolean equals(final Object object1, final Object object2,
    final Collection<String> exclude) {
    if (object1 == null) {
      if (object2 == null) {
        return true;
      } else {
        return false;
      }
    } else if (object2 == null) {
      return false;
    } else {
      try {
        final Equals<Object> equals = getEquals(object1.getClass());
        return equals.equals(object1, object2, exclude);
      } catch (final ClassCastException e) {
        return false;
      }
    }
  }

  public Equals<Object> getEquals(final Class<?> clazz) {
    if (clazz == null) {
      return EqualsInstance.DEFAULT_EQUALS;
    } else {
      @SuppressWarnings("unchecked")
      Equals<Object> equals = (Equals<Object>)this.classEqualsMap.get(clazz);
      if (equals == null) {
        final Class<?>[] interfaces = clazz.getInterfaces();
        if (interfaces != null) {
          for (final Class<?> inter : interfaces) {
            equals = getEquals(inter);
            if (equals != null && equals != EqualsInstance.DEFAULT_EQUALS) {
              return equals;
            }
          }
        }
        return getEquals(clazz.getSuperclass());
      } else {
        return equals;
      }
    }
  }

  public void register(final Class<?> clazz, final Equals<?> equals) {
    this.classEqualsMap.put(clazz, equals);
    equals.setEqualsRegistry(this);
  }

  @Override
  public void setEqualsRegistry(final EqualsRegistry equalsRegistry) {
  }
}
