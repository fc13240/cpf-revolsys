/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 1.3.40
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.revolsys.gis.esri.gdb.file.swig;

public class MultiPointShapeBuffer extends ShapeBuffer {
  private long swigCPtr;

  protected MultiPointShapeBuffer(long cPtr, boolean cMemoryOwn) {
    super(EsriFileGdbJNI.SWIGMultiPointShapeBufferUpcast(cPtr), cMemoryOwn);
    swigCPtr = cPtr;
  }

  protected static long getCPtr(MultiPointShapeBuffer obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        EsriFileGdbJNI.delete_MultiPointShapeBuffer(swigCPtr);
      }
      swigCPtr = 0;
    }
    super.delete();
  }

  public int GetExtent(DoubleArrayValue extent) {
    return EsriFileGdbJNI.MultiPointShapeBuffer_GetExtent(swigCPtr, this, extent);
  }

  public int GetNumPoints(IntValue numPoints) {
    return EsriFileGdbJNI.MultiPointShapeBuffer_GetNumPoints(swigCPtr, this, numPoints);
  }

  public int GetPoints(PointArrayValue points) {
    return EsriFileGdbJNI.MultiPointShapeBuffer_GetPoints(swigCPtr, this, points);
  }

  public int GetZExtent(DoubleArrayValue zExtent) {
    return EsriFileGdbJNI.MultiPointShapeBuffer_GetZExtent(swigCPtr, this, zExtent);
  }

  public int GetZs(DoubleArrayValue zArray) {
    return EsriFileGdbJNI.MultiPointShapeBuffer_GetZs(swigCPtr, this, zArray);
  }

  public int GetMExtent(DoubleArrayValue mExtent) {
    return EsriFileGdbJNI.MultiPointShapeBuffer_GetMExtent(swigCPtr, this, mExtent);
  }

  public int GetMs(DoubleArrayValue mArray) {
    return EsriFileGdbJNI.MultiPointShapeBuffer_GetMs(swigCPtr, this, mArray);
  }

  public int GetIDs(IntArrayValue ids) {
    return EsriFileGdbJNI.MultiPointShapeBuffer_GetIDs(swigCPtr, this, ids);
  }

  public int Setup(ShapeType shapeType, int numPoints) {
    return EsriFileGdbJNI.MultiPointShapeBuffer_Setup(swigCPtr, this, shapeType.swigValue(), numPoints);
  }

  public int CalculateExtent() {
    return EsriFileGdbJNI.MultiPointShapeBuffer_CalculateExtent(swigCPtr, this);
  }

  public MultiPointShapeBuffer() {
    this(EsriFileGdbJNI.new_MultiPointShapeBuffer(), true);
  }

}
