package com.revolsys.gis.algorithm.index;

import java.util.LinkedList;

import com.revolsys.collection.Visitor;
import com.revolsys.filter.Filter;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.vividsolutions.jts.geom.Envelope;

public abstract class RTreeNode<T> extends Envelope {

  public RTreeNode() {
  }

  public abstract boolean visit(Envelope envelope, Visitor<T> visitor);

  public abstract boolean visit(Visitor<T> visitor);

  public abstract boolean visit(Envelope envelope, Filter<T> filter,
    Visitor<T> visitor);

  public abstract boolean remove(LinkedList<RTreeNode<T>> path,
    Envelope envelope, T object);

  protected abstract void updateEnvelope();

  @Override
  public String toString() {
    return new BoundingBox(new GeometryFactory(), this).toPolygon(1).toString();
  }
}
