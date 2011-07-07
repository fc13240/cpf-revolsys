package com.revolsys.gis.esri.gdb.file.type;

import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.esri.gdb.file.swig.Row;

public class FloatAttribute extends AbstractEsriFileGeodatabaseAttribute {
  public FloatAttribute(String name, int length, boolean required) {
    super(name,DataTypes.FLOAT,length,required);
   }
 @Override
  public Object getValue(Row row, int index) {
    // TODO Auto-generated method stub
    return null;
  }

  public void setValue(Row row, Object object) {
  }

}
