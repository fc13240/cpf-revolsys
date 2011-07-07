/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 1.3.40
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.revolsys.gis.esri.gdb.file.swig;

public class FieldInfo {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected FieldInfo(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(FieldInfo obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        EsriFileGdbJNI.delete_FieldInfo(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public FieldInfo() {
    this(EsriFileGdbJNI.new_FieldInfo(), true);
  }

  public int getFieldCount() {
    return EsriFileGdbJNI.FieldInfo_getFieldCount(swigCPtr, this);
  }

  public String getFieldName(int i) {
    return EsriFileGdbJNI.FieldInfo_getFieldName(swigCPtr, this, i);
  }

  public int getFieldLength(int i) {
    return EsriFileGdbJNI.FieldInfo_getFieldLength(swigCPtr, this, i);
  }

  public boolean isNullable(int i) {
    return EsriFileGdbJNI.FieldInfo_isNullable(swigCPtr, this, i);
  }

  public FieldType getFieldType(int i) {
    return FieldType.swigToEnum(EsriFileGdbJNI.FieldInfo_getFieldType(swigCPtr, this, i));
  }

}
