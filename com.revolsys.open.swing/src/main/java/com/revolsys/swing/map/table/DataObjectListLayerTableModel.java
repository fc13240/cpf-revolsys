package com.revolsys.swing.map.table;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PreDestroy;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.swing.listener.InvokeMethodPropertyChangeListener;
import com.revolsys.swing.map.layer.dataobject.DataObjectListLayer;
import com.revolsys.swing.table.dataobject.row.DataObjectRowTable;
import com.vividsolutions.jts.geom.Geometry;

public class DataObjectListLayerTableModel extends DataObjectLayerTableModel
  implements PropertyChangeListener {
  private static final long serialVersionUID = 1L;

  public static final LayerTablePanelFactory FACTORY = new InvokeMethodLayerTablePanelFactory(
    DataObjectListLayer.class, DataObjectListLayerTableModel.class,
    "createPanel");

  public static DataObjectLayerTablePanel createPanel(
    final DataObjectListLayer layer) {
    final JTable table = createTable(layer);
    return new DataObjectLayerTablePanel(layer, table);
  }

  public static DataObjectRowTable createTable(final DataObjectListLayer layer) {
    final DataObjectListLayerTableModel model = new DataObjectListLayerTableModel(
      layer);
    final DataObjectRowTable table = new DataObjectRowTable(model);

    final TableCellRenderer cellRenderer = new DataObjectLayerTableCellRenderer(
      model);
    final TableColumnModel columnModel = table.getColumnModel();
    for (int i = 0; i < columnModel.getColumnCount(); i++) {
      final TableColumn column = columnModel.getColumn(i);

      column.setCellRenderer(cellRenderer);
    }

    table.setSelectionModel(new DataObjectLayerListSelectionModel(model));
    layer.addPropertyChangeListener("selected",
      new InvokeMethodPropertyChangeListener(table, "repaint"));

    return table;
  }

  private DataObjectListLayer layer;

  private final Set<PropertyChangeListener> propertyChangeListeners = new LinkedHashSet<PropertyChangeListener>();

  public DataObjectListLayerTableModel(final DataObjectListLayer layer) {
    this(layer, layer.getMetaData().getAttributeNames());
  }

  public DataObjectListLayerTableModel(final DataObjectListLayer layer,
    final List<String> columnNames) {
    super(layer, columnNames);
    this.layer = layer;
    layer.addPropertyChangeListener("objects", this);
    setEditable(false);
  }

  public void addPropertyChangeListener(
    final PropertyChangeListener propertyChangeListener) {
    this.propertyChangeListeners.add(propertyChangeListener);
  }

  @Override
  @PreDestroy
  public void dispose() {
    super.dispose();
    layer = null;
  }

  private void firePropertyChange(final DataObject object, final String name,
    final Object oldValue, final Object newValue) {
    final PropertyChangeEvent event = new PropertyChangeEvent(object, name,
      oldValue, newValue);
    for (final PropertyChangeListener listener : propertyChangeListeners) {
      listener.propertyChange(event);
    }
  }

  @Override
  public DataObject getObject(final int index) {
    return layer.getObject(index);
  }

  public List<DataObject> getObjects() {
    return layer.getObjects();
  }

  public Set<PropertyChangeListener> getPropertyChangeListeners() {
    return Collections.unmodifiableSet(propertyChangeListeners);
  }

  @Override
  public int getRowCount() {
    return layer.getRowCount();
  }

  @Override
  public boolean isCellEditable(final int rowIndex, final int columnIndex) {
    if (isEditable()) {
      final String columnName = getColumnName(columnIndex);
      final DataObjectMetaData metaData = getMetaData();
      final DataType dataType = metaData.getAttributeType(columnName);
      if (Geometry.class.isAssignableFrom(dataType.getJavaClass())) {
        return false;
      } else {
        return true;
      }
    } else {
      return false;
    }
  }

  @Override
  public void propertyChange(final PropertyChangeEvent evt) {
    fireTableDataChanged();
  }

  public void removePropertyChangeListener(
    final PropertyChangeListener propertyChangeListener) {
    this.propertyChangeListeners.remove(propertyChangeListener);
  }

  @Override
  public void setValueAt(final Object value, final int rowIndex,
    final int columnIndex) {
    final DataObject object = getObject(rowIndex);
    if (object != null) {
      final String name = getColumnName(columnIndex);
      final Object oldValue = object.getValueByPath(name);
      object.setValue(name, value);
      firePropertyChange(object, name, oldValue, value);
    }
  }

  /*
   * TODO @Override public SortOrder setSortOrder(int column) { SortOrder
   * sortOrder = super.setSortOrder(column); final String attributeName =
   * getAttributeName(column); final Comparator<DataObject> comparitor = new
   * DataObjectAttributeComparator( sortOrder == SortOrder.ASCENDING,
   * attributeName); Collections.sort(objects, comparitor);
   * fireTableDataChanged(); return sortOrder; }
   */

}