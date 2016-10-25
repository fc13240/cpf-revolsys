package com.revolsys.geometry.index.quadtree;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.revolsys.geometry.index.AbstractPointSpatialIndex;
import com.revolsys.geometry.index.IdObjectIndex;
import com.revolsys.geometry.index.PointSpatialIndex;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Point;

public abstract class AbstractIdObjectPointQuadTree<T> extends AbstractPointSpatialIndex<T>
  implements IdObjectIndex<T> {

  private final PointSpatialIndex<Integer> index = new PointQuadTree<>();

  public void add(final Collection<Integer> ids) {
    for (final Integer id : ids) {
      final T object = getObject(id);
      add(object);
    }
  }

  @Override
  public T add(final T object) {
    final Point point = getCoordinates(object);
    put(point, object);
    return object;
  }

  @Override
  public void forEach(final BoundingBox envelope, final Consumer<? super T> action) {
    this.index.forEach(envelope, (id) -> {
      final T object = getObject(id);
      final BoundingBox e = getEnvelope(object);
      if (e.intersects(envelope)) {
        action.accept(object);
      }
    });
  }

  @Override
  public void forEach(final BoundingBox boundingBox, final Predicate<? super T> filter,
    final Consumer<? super T> action) {
    this.index.forEach(boundingBox, (id) -> {
      final T object = getObject(id);
      final BoundingBox e = getEnvelope(object);
      if (e.intersects(boundingBox) && filter.test(object)) {
        action.accept(object);
      }
    });
  }

  @Override
  public void forEach(final Consumer<? super T> action) {
    this.index.forEach((id) -> {
      final T object = getObject(id);
      action.accept(object);
    });
  }

  public abstract Point getCoordinates(T object);

  @Override
  public void put(final Point point, final T object) {
    final int id = getId(object);
    this.index.put(point, id);
  }

  @Override
  public boolean remove(final Point point, final T object) {
    final int id = getId(object);
    return this.index.remove(point, id);
  }

  @Override
  public boolean remove(final T object) {
    final Point point = getCoordinates(object);
    return remove(point, object);
  }

  public void removeAll(final Collection<T> objects) {
    for (final T object : objects) {
      remove(object);
    }
  }

}