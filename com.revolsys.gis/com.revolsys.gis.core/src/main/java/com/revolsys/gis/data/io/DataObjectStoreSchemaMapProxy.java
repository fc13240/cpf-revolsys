package com.revolsys.gis.data.io;

import java.util.Map;
import java.util.TreeMap;

public class DataObjectStoreSchemaMapProxy extends
  TreeMap<String, DataObjectStoreSchema> {

  private Map<String, DataObjectStoreSchema> map;

  private AbstractDataObjectStore dataObjectStore;

  public DataObjectStoreSchemaMapProxy(
    AbstractDataObjectStore dataObjectStore,
    Map<String, DataObjectStoreSchema> map) {
    this.dataObjectStore = dataObjectStore;
    this.map = map;
  }

  @Override
  public DataObjectStoreSchema get(
    Object key) {
    DataObjectStoreSchema schema = super.get(key);
    if (schema == null) {
      schema = map.get(key);
      if (schema != null) {
        final String name = schema.getName();
        schema = new DataObjectStoreSchemaProxy(dataObjectStore,
          name, schema);
        super.put(name, schema);
      }
    }
    return schema;
  }

  @Override
  public DataObjectStoreSchema put(
    String key,
    DataObjectStoreSchema schema) {
    final DataObjectStoreSchemaProxy schemaProxy = new DataObjectStoreSchemaProxy(dataObjectStore,
        key, schema);
    map.put(key, schema);
    return super.put(key, schemaProxy);
  }
}
