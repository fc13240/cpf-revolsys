package com.revolsys.swing.map.layer.record;

import java.util.Collection;
import java.util.function.Consumer;

import com.revolsys.record.Record;
import com.revolsys.util.ExitLoopException;

public class RecordCacheCollection extends AbstractRecordCache<AbstractRecordLayer> {

  private final Collection<LayerRecord> records;

  public RecordCacheCollection(final String cacheId, final AbstractRecordLayer layer) {
    super(layer, cacheId);
    this.records = layer.newRecordCacheCollection();
  }

  @Override
  public boolean addRecordDo(final LayerRecord record) {
    synchronized (this.records) {
      return this.records.add(record);
    }
  }

  @Override
  public void clearRecords() {
    synchronized (this.records) {
      this.records.clear();
    }
  }

  @Override
  public boolean containsRecordDo(final LayerRecord record) {
    synchronized (this.records) {
      return record.contains(this.records);
    }
  }

  @Override
  public <R extends Record> void forEachRecordDo(final Consumer<R> action) {
    try {
      final AbstractRecordLayer layer = this.layer;
      for (final LayerRecord record : this.records) {
        @SuppressWarnings("unchecked")
        final R proxyRecord = (R)layer.newProxyLayerRecord(record);
        action.accept(proxyRecord);
      }
    } catch (final ExitLoopException e) {
    }
  }

  @Override
  public int getSize() {
    return this.records.size();
  }

  @Override
  public boolean removeRecordDo(final LayerRecord record) {
    synchronized (this.records) {
      record.removeFrom(this.records);
    }
    return true;
  }

  @Override
  public String toString() {
    return super.toString() + "\t" + getSize() + "\t";
  }
}
