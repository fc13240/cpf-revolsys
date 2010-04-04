package com.revolsys.gis.data.model.filter;

import com.revolsys.filter.Filter;
import com.revolsys.gis.data.model.DataObject;

/**
 * Filter DataObjects by the the attribute having a null value.
 * 
 * @author Paul Austin
 */
public class AttributeValueNullFilter implements Filter<DataObject> {

  /** The property name, or path to match. */
  private final String attributeName;

  public AttributeValueNullFilter(
    final String attributeName) {
    this.attributeName = attributeName;
  }

  /**
   * Match the property on the data object with the required value.
   * 
   * @param object The object.
   * @return True if the object matched the filter, false otherwise.
   */
  public boolean accept(
    final DataObject object) {
    final Object propertyValue = object.getValue(attributeName);
    return propertyValue == null;
  }

  /**
   * @return the name
   */
  @Override
  public String toString() {
    return attributeName + " == null ";
  }
}
