/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 1.3.40
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.revolsys.gis.esri.gdb.file.swig;

public class Guid {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected Guid(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(Guid obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        EsriFileGdbJNI.delete_Guid(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public Guid() {
    this(EsriFileGdbJNI.new_Guid(), true);
  }

  public void SetNull() {
    EsriFileGdbJNI.Guid_SetNull(swigCPtr, this);
  }

  public void Create() {
    EsriFileGdbJNI.Guid_Create(swigCPtr, this);
  }

  public int FromString(String guidString) {
    return EsriFileGdbJNI.Guid_FromString(swigCPtr, this, guidString);
  }

  public boolean equal(Guid other) {
    return EsriFileGdbJNI.Guid_equal(swigCPtr, this, Guid.getCPtr(other), other);
  }

  public boolean notEqual(Guid other) {
    return EsriFileGdbJNI.Guid_notEqual(swigCPtr, this, Guid.getCPtr(other), other);
  }

  public void setData1(long value) {
    EsriFileGdbJNI.Guid_data1_set(swigCPtr, this, value);
  }

  public long getData1() {
    return EsriFileGdbJNI.Guid_data1_get(swigCPtr, this);
  }

  public void setData2(int value) {
    EsriFileGdbJNI.Guid_data2_set(swigCPtr, this, value);
  }

  public int getData2() {
    return EsriFileGdbJNI.Guid_data2_get(swigCPtr, this);
  }

  public void setData3(int value) {
    EsriFileGdbJNI.Guid_data3_set(swigCPtr, this, value);
  }

  public int getData3() {
    return EsriFileGdbJNI.Guid_data3_get(swigCPtr, this);
  }

  public void setData4(short[] value) {
    EsriFileGdbJNI.Guid_data4_set(swigCPtr, this, value);
  }

  public short[] getData4() {
    return EsriFileGdbJNI.Guid_data4_get(swigCPtr, this);
  }

  public String toString() {
    return EsriFileGdbJNI.Guid_toString(swigCPtr, this);
  }

}
