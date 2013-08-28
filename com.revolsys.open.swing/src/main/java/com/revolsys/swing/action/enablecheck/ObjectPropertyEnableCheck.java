package com.revolsys.swing.action.enablecheck;

import java.beans.PropertyChangeSupport;

import com.revolsys.beans.PropertyChangeSupportProxy;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.util.Property;

public class ObjectPropertyEnableCheck extends AbstractEnableCheck {
  private final Object object;

  private final String propertyName;

  private final Object value;

  private boolean inverse = false;

  public ObjectPropertyEnableCheck(final Object object,
    final String propertyName) {
    this(object, propertyName, true);
  }

  public ObjectPropertyEnableCheck(final Object object,
    final String propertyName, final Object value) {
    this(object, propertyName, value, false);
  }

  public ObjectPropertyEnableCheck(final Object object,
    final String propertyName, final Object value, final boolean inverse) {
    this.object = object;
    if (object instanceof PropertyChangeSupportProxy) {
      final PropertyChangeSupportProxy proxy = (PropertyChangeSupportProxy)object;
      final PropertyChangeSupport propertyChangeSupport = proxy.getPropertyChangeSupport();
      // TODO how to make this a weak reference
      if (propertyChangeSupport != null) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, this);
      }
    }
    this.propertyName = propertyName;
    this.value = value;
    this.inverse = inverse;
  }

  @Override
  public boolean isEnabled() {
    final Object value = Property.get(this.object, this.propertyName);
    boolean equal = EqualsRegistry.equal(value, this.value);
    if (equal == !this.inverse) {
      return enabled();
    } else {
      return disabled();
    }
  }

  @Override
  public String toString() {
    return this.object + "." + this.propertyName + "=" + this.value;
  }
}
