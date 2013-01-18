package com.revolsys.swing.table.dataobject;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

import org.jdesktop.swingx.JXTable;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.swing.builder.DataObjectMetaDataUiBuilderRegistry;
import com.revolsys.swing.builder.ValueUiBuilder;
import com.revolsys.util.CollectionUtil;

public class DataObjectTableCellRenderer implements TableCellRenderer {
  private final JLabel valueComponent = new JLabel();

  private final JLabel labelComponent = new JLabel();

  private DataObjectMetaDataUiBuilderRegistry uiBuilderRegistry;

  public DataObjectTableCellRenderer() {
    this(DataObjectMetaDataUiBuilderRegistry.getInstance());
  }

  public DataObjectTableCellRenderer(
    final DataObjectMetaDataUiBuilderRegistry uiBuilderRegistry) {
    this.uiBuilderRegistry = uiBuilderRegistry;
    labelComponent.setBorder(new EmptyBorder(1, 2, 1, 2));
    labelComponent.setFont(labelComponent.getFont().deriveFont(Font.BOLD));
    labelComponent.setOpaque(true);

    valueComponent.setBorder(new EmptyBorder(1, 2, 1, 2));
    valueComponent.setOpaque(true);
  }

  @Override
  public Component getTableCellRendererComponent(final JTable table,
    final Object value, final boolean isSelected, final boolean hasFocus,
    final int row, final int column) {
    int attributeIndex;
    if (table instanceof JXTable) {
      JXTable jxTable = (JXTable)table;
      attributeIndex = jxTable.convertRowIndexToModel(row);
    } else {
      attributeIndex = row;
    }
    final AbstractDataObjectTableModel model = (AbstractDataObjectTableModel)table.getModel();
    final DataObjectMetaData metaData = model.getMetaData();
    final boolean required = metaData.isAttributeRequired(attributeIndex);

    Component component = null;
    String name = metaData.getAttributeName(attributeIndex);
    if (column == 0) {
      valueComponent.setText(String.valueOf(attributeIndex));
      component = valueComponent;
    } else if (column == 1) {
      labelComponent.setText(name);
      component = labelComponent;
    } else if (column == 2) {
      if (uiBuilderRegistry != null) {
        final ValueUiBuilder uiBuilder = uiBuilderRegistry.getValueUiBuilder(
          metaData, attributeIndex);
        if (uiBuilder != null) {
          component = uiBuilder.getRendererComponent(value);
        }
      }
      if (component == null) {
        String text;
        if (value == null) {
          text = "-";
        } else {
          final CodeTable codeTable = metaData.getCodeTableByColumn(name);
          if (codeTable == null) {
            text = StringConverterRegistry.toString(value);
          } else {
            List<Object> values = codeTable.getValues(value);
            if (values == null || values.isEmpty()) {
              text = "-";
            } else {
              text = CollectionUtil.toString(values);
            }
          }
        }
        valueComponent.setText(text);
        component = valueComponent;
      }
    }
    final int[] selectedRows = table.getSelectedRows();
    boolean selected = false;
    for (final int selectedRow : selectedRows) {
      if (row == selectedRow) {
        selected = true;
      }
    }
    if (required && model.getValue(attributeIndex) == null) {
      component.setBackground(new Color(255, 0, 0, 100));
      component.setForeground(table.getForeground());
    } else if (selected) {
      component.setBackground(table.getSelectionBackground());
      component.setForeground(table.getSelectionForeground());
    } else if (row % 2 == 0) {
      component.setBackground(Color.WHITE);
      component.setForeground(table.getForeground());
    } else {
      component.setBackground(Color.LIGHT_GRAY);
      component.setForeground(table.getForeground());
    }
    return component;
  }

  public void setUiBuilderRegistry(
    DataObjectMetaDataUiBuilderRegistry uiBuilderRegistry) {
    this.uiBuilderRegistry = uiBuilderRegistry;
  }
}