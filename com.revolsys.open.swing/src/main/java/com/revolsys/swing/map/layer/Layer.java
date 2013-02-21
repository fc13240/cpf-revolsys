package com.revolsys.swing.map.layer;

import java.beans.PropertyChangeListener;

import com.revolsys.beans.PropertyChangeSupportProxy;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.io.ObjectWithProperties;

public interface Layer extends PropertyChangeSupportProxy, ObjectWithProperties, PropertyChangeListener,Comparable<Layer> {
  void addPropertyChangeListener(final PropertyChangeListener listener);

  void addPropertyChangeListener(final String propertyName,
    final PropertyChangeListener listener);

  BoundingBox getBoundingBox();

  BoundingBox getBoundingBox(boolean visibleLayersOnly);

  GeometryFactory getGeometryFactory();

  long getId();


  LayerGroup getLayerGroup();

  long getMaximumScale();

  long getMinimumScale();

  String getName();

  Project getProject();

  <L extends LayerRenderer<Layer>> L getRenderer();

  boolean isEditable();

  boolean isQueryable();

  boolean isQuerySupported();

  boolean isReadOnly();

  boolean isSelectable();

  boolean isSelectSupported();

  boolean isVisible();

  boolean isVisible(double scale);

  void refresh();

  void remove();

  void removePropertyChangeListener(final PropertyChangeListener listener);

  void removePropertyChangeListener(final String propertyName,
    final PropertyChangeListener listener);

  void setEditable(boolean editable);

  void setLayerGroup(LayerGroup layerGroup);

  /**
   * Set the maximum scale. This is the scale that if you zoom in to a more
   * detailed scale than the maximum scale the layer will not be visible. This
   * is inverse from the logical definition of maximum. If scale < maximumScale
   * it will not be shown.
   */
  void setMaximumScale(long maximumScale);

  /**
   * Set the minimum scale. This is the scale that if you zoom out to a less
   * detailed scale than the minimum scale the layer will not be visible. This
   * is inverse from the logical definition of minimum. If scale > minimumScale
   * it will not be shown.
   */
  void setMinimumScale(long minimumScale);

  void setName(String name);

  void setQueryable(boolean b);

  void setReadOnly(boolean readOnly);

  void setSelectable(boolean selectable);

  void setVisible(boolean visible);

  BoundingBox getSelectedBoundingBox();
}