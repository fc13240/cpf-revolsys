package com.revolsys.converter.string;

public class ObjectStringConverter implements StringConverter<Object> {
  @Override
  public Class<Object> getConvertedClass() {
    return Object.class;
  }

  @Override
  public boolean requiresQuotes() {
    return true;
  }

  @Override
  public Object objectToObject(final Object value) {
    return value;
  }

  @Override
  public Object stringToObject(final String string) {
    return string;
  }

  @Override
  public String objectToString(final Object value) {
    if (value == null) {
      return null;
    } else {
      return value.toString();
    }
  }
}
