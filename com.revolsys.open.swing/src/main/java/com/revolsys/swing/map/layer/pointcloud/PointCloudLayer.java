package com.revolsys.swing.map.layer.pointcloud;

import java.util.Map;

import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.cloud.PointCloud;
import com.revolsys.elevation.cloud.PointCloudReaderFactory;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.logging.Logs;
import com.revolsys.spring.resource.Resource;
import com.revolsys.swing.Icons;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.component.BasePanel;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.layout.GroupLayouts;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.util.Property;

public class PointCloudLayer extends AbstractLayer {

  public static final String J_TYPE = "pointCloudLayer";

  private PointCloud<?> pointCloud;

  private Resource resource;

  private String url;

  public PointCloudLayer(final Map<String, ? extends Object> properties) {
    super(J_TYPE);
    setProperties(properties);
    setSelectSupported(false);
    setQuerySupported(false);
    setRenderer(new PointCloudLayerRenderer(this));
    setIcon(Icons.getIcon("point_cloud"));
  }

  public void cancelChanges() {
    if (this.pointCloud == null && this.resource != null) {
      PointCloud<?> pointCloud = null;
      final Resource pointCloudResource = Resource.getResource(this.url);
      if (pointCloudResource.exists()) {
        try {
          pointCloud = PointCloudReaderFactory.openPointCloud(pointCloudResource);
          if (pointCloud == null) {
            Logs.error(PointCloudLayer.class, "Cannot load: " + this.url);
          }
        } catch (final RuntimeException e) {
          Logs.error(PointCloudLayer.class, "Unable to load: " + this.url, e);
        }
      } else {
        Logs.error(PointCloudLayer.class, "URL does not exist: " + this.url);
      }
      setPointCloud(pointCloud);
    } else {
      // this.pointCloud.cancelChanges();
    }
    firePropertyChange("hasChanges", true, false);
  }

  @Override
  public BoundingBox getBoundingBox() {
    final PointCloud<?> pointCloud = getPointCloud();
    if (pointCloud == null) {
      return BoundingBox.empty();
    } else {
      final BoundingBox boundingBox = pointCloud.getBoundingBox();
      return boundingBox;
    }
  }

  @Override
  public BoundingBox getBoundingBox(final boolean visibleLayersOnly) {
    if (isExists() && (isVisible() || !visibleLayersOnly)) {
      return getBoundingBox();
    } else {
      return getGeometryFactory().newBoundingBoxEmpty();
    }
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    if (this.pointCloud == null) {
      return getBoundingBox().getGeometryFactory();
    } else {
      return this.pointCloud.getGeometryFactory();
    }
  }

  public PointCloud<?> getPointCloud() {
    return this.pointCloud;
  }

  @Override
  protected boolean initializeDo() {
    final String url = getProperty("url");
    if (Property.hasValue(url)) {
      this.url = url;
      this.resource = Resource.getResource(url);
      cancelChanges();
      return true;
    } else {
      Logs.error(this, "Layer definition does not contain a 'url' property");
      return false;
    }
  }

  @Override
  protected ValueField newPropertiesTabGeneralPanelSource(final BasePanel parent) {
    final ValueField panel = super.newPropertiesTabGeneralPanelSource(parent);

    if (this.url.startsWith("file:")) {
      final String fileName = this.url.replaceFirst("file:(//)?", "");
      SwingUtil.addLabelledReadOnlyTextField(panel, "File", fileName);
    } else {
      SwingUtil.addLabelledReadOnlyTextField(panel, "URL", this.url);
    }
    final String fileNameExtension = FileUtil.getFileNameExtension(this.url);
    if (Property.hasValue(fileNameExtension)) {
      SwingUtil.addLabelledReadOnlyTextField(panel, "File Extension", fileNameExtension);
      // final PointCloud factory = IoFactory
      // .factoryByFileExtension(PointCloudReadFactory.class, fileNameExtension);
      // if (factory != null) {
      // SwingUtil.addLabelledReadOnlyTextField(panel, "File Type", factory.getName());
      // }
    }
    GroupLayouts.makeColumns(panel, 2, true);
    return panel;
  }

  public void setPointCloud(final PointCloud pointCloud) {
    final PointCloud old = this.pointCloud;
    Property.removeListener(this.pointCloud, this);
    this.pointCloud = pointCloud;
    if (pointCloud == null) {
      setExists(false);
    } else {
      setExists(true);
      Property.addListener(pointCloud, this);
    }
    firePropertyChange("pointCloud", old, this.pointCloud);
  }

  @Override
  public MapEx toMap() {
    final MapEx map = super.toMap();
    addToMap(map, "url", this.url);
    return map;
  }

}