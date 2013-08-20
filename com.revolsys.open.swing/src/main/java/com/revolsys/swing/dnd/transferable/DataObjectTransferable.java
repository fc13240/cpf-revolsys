package com.revolsys.swing.dnd.transferable;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.io.csv.CsvUtil;

public class DataObjectTransferable implements Transferable {

  public static final DataFlavor DATA_OBJECT_FLAVOR = new DataFlavor(
    DataObject.class, "Data Object");

  private final DataObject object;

  private static final DataFlavor[] DATA_FLAVORS = {
    DATA_OBJECT_FLAVOR, MapTransferable.MAP_FLAVOR, DataFlavor.stringFlavor
  };

  public DataObjectTransferable(final DataObject object) {
    this.object = object;
  }

  @Override
  public Object getTransferData(final DataFlavor flavor)
    throws UnsupportedFlavorException, IOException {
    if (this.object == null) {
      return null;
    } else if (DATA_OBJECT_FLAVOR.equals(flavor)
      || MapTransferable.MAP_FLAVOR.equals(flavor)) {
      return this.object;
    } else if (DataFlavor.stringFlavor.equals(flavor)) {
      final StringWriter out = new StringWriter();
      final Collection<String> attributeNames = this.object.getMetaData()
        .getAttributeNames();
      CsvUtil.writeColumns(out, attributeNames, '\t', '\n');
      final Collection<Object> values = this.object.values();
      CsvUtil.writeColumns(out, values, '\t', '\n');
      final String text = out.toString();
      return text;
    } else {
      throw new UnsupportedFlavorException(flavor);
    }
  }

  @Override
  public DataFlavor[] getTransferDataFlavors() {
    return DATA_FLAVORS;
  }

  @Override
  public boolean isDataFlavorSupported(final DataFlavor dataFlavor) {
    return Arrays.asList(DATA_FLAVORS).contains(dataFlavor);
  }

}
