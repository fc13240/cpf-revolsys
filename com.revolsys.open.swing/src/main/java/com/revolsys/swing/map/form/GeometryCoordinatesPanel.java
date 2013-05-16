package com.revolsys.swing.map.form;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import com.revolsys.swing.table.BaseJxTable;
import com.revolsys.swing.table.geometry.GeometryCoordinatesTableModel;
import com.vividsolutions.jts.geom.Geometry;

@SuppressWarnings("serial")
public class GeometryCoordinatesPanel extends JPanel implements
  TableModelListener {
  private final GeometryCoordinatesTableModel model = new GeometryCoordinatesTableModel();

  private final BaseJxTable table;

  public GeometryCoordinatesPanel() {
    super(new BorderLayout());

    model.addTableModelListener(this);
    table = new BaseJxTable(model);
    table.setAutoResizeMode(BaseJxTable.AUTO_RESIZE_OFF);

    final JScrollPane scrollPane = new JScrollPane(table);
    add(scrollPane, BorderLayout.WEST);
  }

  public Geometry getGeometry() {
    return model.getGeometry();
  }

  public BaseJxTable getTable() {
    return table;
  }

  public void setGeometry(final Geometry geometry) {
    model.setGeometry(geometry);
  }

  @Override
  public void tableChanged(TableModelEvent e) {
    for (int i = 0; i < model.getColumnCount(); i++) {
      if (i < model.getNumIndexItems()) {
        table.setColumnWidth(i, (model.getRowCount() / 10 + 1) * 20);
      } else {
        table.setColumnWidth(i, 120);
      }
    }
  }
}