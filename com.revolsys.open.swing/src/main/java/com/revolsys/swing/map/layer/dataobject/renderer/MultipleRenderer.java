package com.revolsys.swing.map.layer.dataobject.renderer;

import java.awt.Graphics2D;
import java.util.List;
import java.util.Map;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.style.GeometryStyle;

/**
 * Use all the specified renderers to render the layer. All features are
 * rendered using the first renderer, then the second etc.
 */
public class MultipleRenderer extends AbstractMultipleRenderer {

  public MultipleRenderer(final DataObjectLayer layer, LayerRenderer<?> parent,
    final Map<String, Object> multipleStyle) {
    super("multipleStyle", layer, parent, multipleStyle);
  }

  public MultipleRenderer(DataObjectLayer layer) {
    super("multipleStyle", layer);
  }

  public void addStyle(final GeometryStyle style) {
    final GeometryStyleRenderer renderer = new GeometryStyleRenderer(
      getLayer(), this, style);
    addRenderer(renderer);
  }

  @Override
  protected void renderObjects(final Viewport2D viewport,
    final Graphics2D graphics, final DataObjectLayer layer,
    final List<DataObject> objects) {
    BoundingBox visibleArea = viewport.getBoundingBox();
    for (final AbstractDataObjectLayerRenderer renderer : getRenderers()) {
      long scale = (long)viewport.getScale();
      if (renderer.isVisible(scale)) {
        for (DataObject object : objects) {
          if (isVisible(object)) {
            renderer.renderObject(viewport, graphics, visibleArea, layer, object);
          }
        }
      }
    }
  }

  @Override
  // Needed for filter styles
  protected void renderObject(Viewport2D viewport, Graphics2D graphics,
    BoundingBox visibleArea, DataObjectLayer layer, DataObject object) {
    if (isVisible(object)) {
      for (final AbstractDataObjectLayerRenderer renderer : getRenderers()) {
        long scale = (long)viewport.getScale();
        if (renderer.isVisible(scale)) {
          renderer.renderObject(viewport, graphics, visibleArea, layer, object);
        }
      }
    }
  }
}