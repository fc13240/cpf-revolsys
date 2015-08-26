package com.revolsys.data.record.schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.PreDestroy;

import com.revolsys.data.record.io.RecordStoreExtension;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.PathName;
import com.revolsys.util.ExceptionUtil;
import com.revolsys.util.Property;

public class RecordStoreSchema extends AbstractRecordStoreSchemaElement {

  private final Map<PathName, RecordStoreSchemaElement> elementsByPath = new TreeMap<>();

  private boolean initialized = false;

  private final Map<PathName, RecordDefinition> recordDefinitionsByPath = new TreeMap<>();

  private AbstractRecordStore recordStore;

  private final Map<PathName, RecordStoreSchema> schemasByPath = new TreeMap<>();

  public RecordStoreSchema(final AbstractRecordStore recordStore) {
    this.recordStore = recordStore;
  }

  public RecordStoreSchema(final RecordStoreSchema schema, final PathName pathName) {
    super(schema, pathName);
  }

  public void addElement(final RecordStoreSchemaElement element) {
    refreshIfNeeded();
    final PathName path = getPathName();
    final PathName childPath = element.getPathName();
    if (path.isParentOf(childPath)) {
      this.elementsByPath.put(childPath, element);
      if (element instanceof RecordDefinition) {
        final RecordDefinition recordDefinition = (RecordDefinition)element;
        this.recordDefinitionsByPath.put(childPath, recordDefinition);
        final AbstractRecordStore recordStore = getRecordStore();
        recordStore.addRecordDefinitionProperties((RecordDefinitionImpl)recordDefinition);
      }
      if (element instanceof RecordStoreSchema) {
        final RecordStoreSchema schema = (RecordStoreSchema)element;
        this.schemasByPath.put(childPath, schema);
      }
    } else {
      throw new IllegalArgumentException(path + " is not a parent of " + childPath);
    }
  }

  @Override
  @PreDestroy
  public synchronized void close() {
    if (this.recordDefinitionsByPath != null) {
      for (final RecordDefinition recordDefinition : this.recordDefinitionsByPath.values()) {
        recordDefinition.destroy();
      }
    }
    this.recordStore = null;
    this.recordDefinitionsByPath.clear();
    this.elementsByPath.clear();
    this.schemasByPath.clear();
    super.close();
  }

  public RecordStoreSchema createSchema(final PathName path) {
    final RecordStoreSchemaElement element = getElement(path);
    if (element == null) {
      final RecordStoreSchema schema = new RecordStoreSchema(this, path);
      addElement(schema);
      return schema;
    } else if (element instanceof RecordStoreSchema) {
      final RecordStoreSchema schema = (RecordStoreSchema)element;
      return schema;
    } else {
      throw new IllegalArgumentException(
        "Non schema element with path " + path + " already exists");
    }
  }

  public synchronized RecordDefinition findRecordDefinition(final PathName path) {
    refreshIfNeeded();
    final RecordDefinition recordDefinition = this.recordDefinitionsByPath.get(path);
    return recordDefinition;
  }

  @SuppressWarnings("unchecked")
  public <V extends RecordStoreSchemaElement> V getElement(final PathName path) {
    RecordStoreSchemaElement childElement = this.elementsByPath.get(path);
    if (childElement == null) {
      if (path != null) {
        final PathName schemaPath = getPathName();
        if (schemaPath.equals(path)) {
          return (V)this;
        } else {
          if (schemaPath.isAncestorOf(path)) {
            childElement = this.elementsByPath.get(path);
            if (childElement == null) {
              synchronized (this) {
                refreshIfNeeded();
                childElement = this.elementsByPath.get(path);
                if (childElement == null || childElement instanceof NonExistingSchemaElement) {
                  return null;
                } else if (childElement.equalPath(path)) {
                  return (V)childElement;
                } else if (childElement instanceof RecordStoreSchema) {
                  final RecordStoreSchema childSchema = (RecordStoreSchema)childElement;
                  return childSchema.getElement(path);
                } else {
                  return null;
                }
              }
            } else if (childElement instanceof NonExistingSchemaElement) {
              return null;
            } else {
              return (V)childElement;
            }
          } else {
            final RecordStoreSchema parent = getSchema();
            if (parent != null) {
              return parent.getElement(path);
            }
          }
        }
      }
      if (this.recordStore == null) {
        return null;
      } else {
        return (V)this.recordStore.getRootSchema();
      }
    }
    return (V)childElement;
  }

  public List<RecordStoreSchemaElement> getElements() {
    refreshIfNeeded();
    final List<RecordStoreSchemaElement> elements = new ArrayList<>();
    elements.addAll(getSchemas());
    elements.addAll(getRecordDefinitions());
    return elements;
  }

  public GeometryFactory getGeometryFactory() {
    final GeometryFactory geometryFactory = getProperty("geometryFactory");
    if (geometryFactory == null) {
      final RecordStore recordStore = getRecordStore();
      if (recordStore == null) {
        return GeometryFactory.floating3();
      } else {
        return recordStore.getGeometryFactory();
      }
    } else {
      return geometryFactory;
    }
  }

  public RecordDefinition getRecordDefinition(final PathName path) {
    final RecordStoreSchemaElement element = getElement(path);
    if (element instanceof RecordDefinition) {
      return (RecordDefinition)element;
    } else {
      return null;
    }
  }

  public List<RecordDefinition> getRecordDefinitions() {
    refreshIfNeeded();
    return new ArrayList<>(this.recordDefinitionsByPath.values());
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends RecordStore> V getRecordStore() {
    final RecordStoreSchema schema = getSchema();
    if (schema == null) {
      return (V)this.recordStore;
    } else {
      return schema.getRecordStore();
    }
  }

  public RecordStoreSchema getSchema(final PathName path) {
    final RecordStoreSchemaElement element = getElement(path);
    if (element instanceof RecordStoreSchema) {
      return (RecordStoreSchema)element;
    } else {
      return null;
    }
  }

  public List<PathName> getSchemaPaths() {
    refreshIfNeeded();
    return new ArrayList<>(this.schemasByPath.keySet());
  }

  public List<RecordStoreSchema> getSchemas() {
    refreshIfNeeded();
    return new ArrayList<>(this.schemasByPath.values());
  }

  public List<String> getTypeNames() {
    refreshIfNeeded();
    final List<String> names = new ArrayList<>();
    for (final PathName typeName : getTypePaths()) {
      names.add(typeName.getParentPath());
    }
    return names;
  }

  public List<PathName> getTypePaths() {
    refreshIfNeeded();
    return new ArrayList<>(this.recordDefinitionsByPath.keySet());
  }

  private boolean isEqual(final RecordStoreSchemaElement oldElement,
    final RecordStoreSchemaElement newElement) {
    if (oldElement == newElement) {
      return true;
    } else if (oldElement instanceof RecordStoreSchema) {
      if (newElement instanceof RecordStoreSchema) {
        return true;
      }
    } else if (oldElement instanceof RecordDefinition) {
      final RecordDefinition oldRecordDefinition = (RecordDefinition)oldElement;
      if (newElement instanceof RecordDefinition) {
        final RecordDefinition newRecordDefinition = (RecordDefinition)newElement;
        if (Property.equals(newRecordDefinition, oldRecordDefinition, "idFieldNames")) {
          if (Property.equals(newRecordDefinition, oldRecordDefinition, "idFieldIndexes")) {
            if (Property.equals(newRecordDefinition, oldRecordDefinition, "geometryFieldNames")) {
              if (Property.equals(newRecordDefinition, oldRecordDefinition,
                "geometryFieldIndexes")) {
                if (Property.equals(newRecordDefinition, oldRecordDefinition, "fields")) {
                  return true;
                }
              }
            }
          }
        }
      }
    }
    return false;
  }

  public boolean isInitialized() {
    return this.initialized;
  }

  public synchronized void refresh() {
    this.initialized = true;
    final AbstractRecordStore recordStore = getRecordStore();
    if (recordStore != null) {
      final Collection<RecordStoreExtension> extensions = recordStore.getRecordStoreExtensions();
      for (final RecordStoreExtension extension : extensions) {
        try {
          if (extension.isEnabled(recordStore)) {
            extension.preProcess(this);
          }
        } catch (final Throwable e) {
          ExceptionUtil.log(extension.getClass(), "Unable to pre-process schema " + this, e);
        }
      }

      final Map<PathName, ? extends RecordStoreSchemaElement> elementsByPath = recordStore
        .refreshSchemaElements(this);

      final Set<PathName> removedPaths = new HashSet<>(this.elementsByPath.keySet());
      for (final Entry<PathName, ? extends RecordStoreSchemaElement> entry : elementsByPath
        .entrySet()) {
        final PathName path = entry.getKey();
        removedPaths.remove(path);
        final RecordStoreSchemaElement newElement = entry.getValue();
        final RecordStoreSchemaElement oldElement = this.elementsByPath.get(path);
        if (oldElement == null) {
          addElement(newElement);
        } else {
          replaceElement(path, oldElement, newElement);
        }
      }
      for (final PathName removedPath : removedPaths) {
        removeElement(removedPath);
      }

      for (final RecordStoreExtension extension : extensions) {
        try {
          if (extension.isEnabled(recordStore)) {
            extension.postProcess(this);
          }
        } catch (final Throwable e) {
          ExceptionUtil.log(extension.getClass(), "Unable to post-process schema " + this, e);
        }
      }
    }
  }

  protected void refreshIfNeeded() {
    final RecordStore recordStore = getRecordStore();
    if (!isInitialized() && recordStore.isLoadFullSchema()) {
      refresh();
    }
  }

  private void removeElement(final PathName pathName) {
    this.elementsByPath.remove(pathName);
    this.recordDefinitionsByPath.remove(pathName);
    this.schemasByPath.remove(pathName);
  }

  private void replaceElement(final PathName pathName, final RecordStoreSchemaElement oldElement,
    final RecordStoreSchemaElement newElement) {
    if (!isEqual(oldElement, newElement)) {
      removeElement(pathName);
      addElement(newElement);
    }
  }
}
