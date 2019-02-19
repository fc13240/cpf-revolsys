package com.revolsys.swing.map.overlay.record;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.revolsys.awt.WebColors;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.editor.BoundingBoxEditor;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.io.BaseCloseable;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.record.Record;
import com.revolsys.swing.Icons;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.map.ImageViewport;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.renderer.AbstractRecordLayerRenderer;
import com.revolsys.swing.map.overlay.AbstractOverlay;
import com.revolsys.swing.map.overlay.BackgroundRefreshResource;
import com.revolsys.swing.map.overlay.CloseLocation;
import com.revolsys.swing.map.overlay.LayerRendererOverlay;
import com.revolsys.swing.map.overlay.VertexStyleRenderer;
import com.revolsys.swing.map.overlay.ZoomOverlay;
import com.revolsys.swing.map.view.ViewRenderer;
import com.revolsys.swing.map.view.graphics.Graphics2DViewRender;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.util.Cancellable;
import com.revolsys.util.Property;
import com.revolsys.value.ThreadBooleanValue;

public class SelectRecordsOverlay extends AbstractOverlay {
  public static final String ACTION_SELECT_RECORDS = "Select Records";

  protected static final BasicStroke BOX_STROKE = new BasicStroke(2, BasicStroke.CAP_SQUARE,
    BasicStroke.JOIN_MITER, 2, new float[] {
      6, 6
    }, 0f);

  private static final Color COLOR_BOX = WebColors.Green;

  private static final Color COLOR_BOX_TRANSPARENT = WebColors.newAlpha(COLOR_BOX, 127);

  private static final Cursor CURSOR_SELECT_BOX = Icons.getCursor("cursor_select_box", 8, 7);

  private static final Cursor CURSOR_SELECT_BOX_ADD = Icons.getCursor("cursor_select_box_add", 8,
    7);

  private static final Cursor CURSOR_SELECT_BOX_DELETE = Icons.getCursor("cursor_select_box_delete",
    8, 7);

  private static final Set<String> REDRAW_PROPERTY_NAMES = new HashSet<>(
    Arrays.asList("refresh", "viewBoundingBox", "unitsPerPixel", "scale"));

  private static final Set<String> REDRAW_REPAINT_PROPERTY_NAMES = new HashSet<>(Arrays.asList(
    "layers", "selectable", "visible", "editable", AbstractRecordLayer.RECORDS_CHANGED,
    "hasSelectedRecords", "hasHighlightedRecords", "minimumScale", "maximumScale",
    AbstractRecordLayer.RECORD_UPDATED, AbstractRecordLayer.RECORDS_DELETED));

  private static final VertexStyleRenderer CLOSE_VERTEX_STYLE_RENDERER = new VertexStyleRenderer(
    WebColors.Green);

  private static final long serialVersionUID = 1L;

  private final SelectedRecordsRenderer selectRenderer = new SelectedRecordsRenderer(WebColors.Lime,
    50);

  private final SelectedRecordsVertexRenderer selectVertexRenderer = new SelectedRecordsVertexRenderer(
    WebColors.Lime, false);

  private final SelectedRecordsRenderer highlightRenderer = new SelectedRecordsRenderer(
    WebColors.Yellow, 50);

  private final SelectedRecordsVertexRenderer highlightVertexRenderer = new SelectedRecordsVertexRenderer(
    WebColors.Yellow, false);

  private int selectBoxButton;

  private int selectBoxX1 = -1;

  private int selectBoxX2 = -1;

  private int selectBoxY1 = -1;

  private int selectBoxY2 = -1;

  private final BackgroundRefreshResource<GeoreferencedImage> imageSelected = new BackgroundRefreshResource<>(
    "Selected Records Overlay", this::refreshImageSelected);

  private final ThreadBooleanValue selectingRecords = new ThreadBooleanValue(false);

  public SelectRecordsOverlay(final MapPanel mapPanel) {
    super(mapPanel);
    addOverlayAction( //
      ACTION_SELECT_RECORDS, //
      CURSOR_SELECT_BOX, //
      ZoomOverlay.ACTION_PAN, //
      ZoomOverlay.ACTION_ZOOM //
    );
    this.imageSelected.addPropertyChangeListener(this);
  }

  public void addSelectedRecords(final BoundingBox boundingBox) {
    final LayerGroup project = getProject();
    addSelectedRecords(project, boundingBox);
    final LayerRendererOverlay overlay = getMap().getLayerOverlay();
    overlay.redraw();
  }

  private void addSelectedRecords(final LayerGroup group, final BoundingBox boundingBox) {

    final double scale = getViewportScale();
    final List<Layer> layers = group.getLayers();
    Collections.reverse(layers);
    for (final Layer layer : layers) {
      if (layer instanceof LayerGroup) {
        final LayerGroup childGroup = (LayerGroup)layer;
        addSelectedRecords(childGroup, boundingBox);
      } else if (layer instanceof AbstractRecordLayer) {
        final AbstractRecordLayer recordLayer = (AbstractRecordLayer)layer;
        if (recordLayer.isSelectable(scale)) {
          recordLayer.addSelectedRecords(boundingBox);
        }
      }
    }
  }

  protected void cancel() {
    selectBoxClear();
    repaint();
  }

  protected void doSelectRecords(final InputEvent event, final BoundingBox boundingBox) {
    if (SwingUtil.isShiftDown(event)) {
      Invoke.background("Select records", () -> addSelectedRecords(boundingBox));
    } else if (SwingUtil.isAltDown(event)) {
      Invoke.background("Unselect records", () -> unSelectRecords(boundingBox));
    } else {
      Invoke.background("Select records", () -> selectRecords(boundingBox));
    }
  }

  @Override
  public void focusLost(final FocusEvent e) {
    cancel();
  }

  protected boolean isSelectable(final AbstractLayer recordLayer) {
    return recordLayer.isSelectable();
  }

  public boolean isSelectEvent(final MouseEvent event) {
    if (event.getButton() == MouseEvent.BUTTON1) {
      final boolean keyPress = SwingUtil.isControlOrMetaDown(event);
      return keyPress;
    }
    return false;
  }

  @Override
  public void keyPressed(final KeyEvent event) {
    final int keyCode = event.getKeyCode();
    if (keyCode == KeyEvent.VK_ESCAPE) {
      cancel();
    } else if (keyCode == KeyEvent.VK_CONTROL || keyCode == KeyEvent.VK_META) {
      if (isMouseInMap() && !hasOverlayAction()) {
        setSelectCursor(event);
      }
    } else if (isOverlayAction(ACTION_SELECT_RECORDS)) {
      if (keyCode == KeyEvent.VK_SHIFT) {
        setSelectCursor(event);
      } else if (keyCode == KeyEvent.VK_ALT) {
        setSelectCursor(event);
      }
    }
  }

  @Override
  public void keyReleased(final KeyEvent event) {
    final int keyCode = event.getKeyCode();
    if (isOverlayAction(ACTION_SELECT_RECORDS)) {
      if (keyCode == KeyEvent.VK_SHIFT) {
        setSelectCursor(event);
      } else if (keyCode == KeyEvent.VK_ALT) {
        setSelectCursor(event);
      } else if (keyCode == KeyEvent.VK_CONTROL || keyCode == KeyEvent.VK_META) {
        setSelectCursor(event);
      }
    }
  }

  @Override
  public void mouseDragged(final MouseEvent event) {
    if (selectBoxDrag(event)) {
    }
  }

  @Override
  public void mouseExited(final MouseEvent e) {
    if (this.selectBoxX1 == -1) {
      selectBoxClear();
    }
  }

  @Override
  public void mouseMoved(final MouseEvent event) {
    if (canOverrideOverlayAction(ACTION_SELECT_RECORDS)) {
      setSelectCursor(event);
    }
  }

  @Override
  public void mousePressed(final MouseEvent event) {
    if (selectBoxStart(event)) {
    }
  }

  @Override
  public void mouseReleased(final MouseEvent event) {
    if (selectBoxFinish(event)) {
    }
  }

  @Override
  public void paintComponent(final Graphics2DViewRender view, final Graphics2D graphics) {
    final GeoreferencedImage imageSelected = this.imageSelected.getResource();
    view.drawImage(imageSelected, false);
    final GeometryFactory viewportGeometryFactory = getViewportGeometryFactory2d();
    final MapPanel map = getMap();
    final List<LayerRecord> closeSelectedRecords = map.getCloseSelectedRecords();
    if (!closeSelectedRecords.isEmpty()) {
      for (final LayerRecord record : closeSelectedRecords) {
        final Geometry geometry = record.getGeometry();
        if (record.isHighlighted()) {
          this.highlightVertexRenderer.paintSelected(view, graphics, viewportGeometryFactory,
            geometry);
        } else {
          this.selectVertexRenderer.paintSelected(view, graphics, viewportGeometryFactory,
            geometry);
        }
      }
    }
    final List<CloseLocation> closeSelectedLocations = map.getCloseSelectedLocations();
    if (Property.hasValue(closeSelectedLocations)) {
      for (final CloseLocation location : closeSelectedLocations) {
        final Vertex vertex = location.getVertex();
        CLOSE_VERTEX_STYLE_RENDERER.paintSelected(view, graphics, viewportGeometryFactory, vertex);
      }
    }
    drawBox(graphics, this.selectBoxX1, this.selectBoxY1, this.selectBoxX2, this.selectBoxY2,
      COLOR_BOX, BOX_STROKE, COLOR_BOX_TRANSPARENT);
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    final Object source = event.getSource();
    final String propertyName = event.getPropertyName();
    if (this.selectingRecords != null && !this.selectingRecords.isTrue()) {
      if (source == this.imageSelected) {
        repaint();
      } else if (source instanceof Record || source instanceof LayerRenderer) {
        redraw();
      } else if (REDRAW_PROPERTY_NAMES.contains(propertyName)) {
        redraw();
      } else if (REDRAW_REPAINT_PROPERTY_NAMES.contains(propertyName)
        || source instanceof AbstractRecordLayer && propertyName.startsWith("record")) {
        redrawAndRepaint();
      }
    }
  }

  public void redraw() {
    this.imageSelected.refresh();
  }

  public void redrawAndRepaint() {
    redraw();
    repaint();
  }

  private void refreshImageRenderer(final ViewRenderer view, final LayerGroup layerGroup) {
    for (final Layer layer : layerGroup.getLayers()) {
      if (layer instanceof LayerGroup) {
        final LayerGroup childGroup = (LayerGroup)layer;
        refreshImageRenderer(view, childGroup);
      } else if (layer instanceof AbstractRecordLayer) {
        final AbstractRecordLayer recordLayer = (AbstractRecordLayer)layer;
        final AbstractRecordLayerRenderer layerRenderer = layer.getRenderer();
        if (recordLayer.isSelectable()) {
          final List<LayerRecord> selectedRecords = recordLayer.getSelectedRecords();
          for (final LayerRecord record : selectedRecords) {
            if (record != null && recordLayer.isVisible(record)) {
              if (!recordLayer.isDeleted(record)) {
                layerRenderer.renderSelectedRecord(view, recordLayer, record);
              }
            }
          }
        }
      }
    }
  }

  private GeoreferencedImage refreshImageSelected(final Cancellable cancellable) {
    final Viewport2D viewport = getViewport();
    if (viewport != null) {
      final int width = viewport.getViewWidthPixels();
      final int height = viewport.getViewHeightPixels();
      if (width > 0 && height > 0) {
        try (
          final ImageViewport imageViewport = new ImageViewport(viewport,
            BufferedImage.TYPE_INT_ARGB_PRE);) {
          final ViewRenderer view = imageViewport.newViewRenderer();
          final Project project = getProject();
          refreshImageRenderer(view, project);
          refreshImageSelectedAndHighlighted(view, project);
          return imageViewport.getGeoreferencedImage();
        }
      }
    }
    return null;
  }

  private void refreshImageSelectedAndHighlighted(final ViewRenderer view,
    final LayerGroup layerGroup) {
    final GeometryFactory viewportGeometryFactory = getViewportGeometryFactory2d();
    final List<Geometry> highlightedGeometries = new ArrayList<>();
    for (final Layer layer : layerGroup.getLayers()) {
      if (layer instanceof LayerGroup) {
        final LayerGroup childGroup = (LayerGroup)layer;
        refreshImageSelectedAndHighlighted(view, childGroup);
      } else if (layer instanceof AbstractRecordLayer) {
        final AbstractRecordLayer recordLayer = (AbstractRecordLayer)layer;
        if (recordLayer.isSelectable()) {
          final List<LayerRecord> selectedRecords = recordLayer.getSelectedRecords();
          for (final LayerRecord record : selectedRecords) {
            if (record != null && recordLayer.isVisible(record)) {
              if (!recordLayer.isDeleted(record)) {
                final Geometry geometry = record.getGeometry();
                if (recordLayer.isHighlighted(record)) {
                  highlightedGeometries.add(geometry);
                } else {
                  this.selectRenderer.paintSelected(view, viewportGeometryFactory, geometry);
                }
              }
            }
          }
        }
      }
    }
    for (final Geometry geometry : highlightedGeometries) {
      this.highlightRenderer.paintSelected(view, viewportGeometryFactory, geometry);
    }
  }

  private void selectBoxClear() {
    clearOverlayAction(ACTION_SELECT_RECORDS);
    this.selectBoxX1 = -1;
    this.selectBoxY1 = -1;
    this.selectBoxX2 = -1;
    this.selectBoxY2 = -1;
  }

  private boolean selectBoxDrag(final MouseEvent event) {
    if (isOverlayAction(ACTION_SELECT_RECORDS) && this.selectBoxX1 != -1) {
      this.selectBoxX2 = event.getX();
      this.selectBoxY2 = event.getY();

      setSelectCursor(event);
      repaint();
      return true;
    }
    return false;
  }

  private boolean selectBoxFinish(final MouseEvent event) {
    if (event.getButton() == this.selectBoxButton && this.selectBoxX1 != -1) {
      if (clearOverlayAction(ACTION_SELECT_RECORDS)) {
        final Viewport2D viewport = getViewport();

        final BoundingBoxEditor boundingBox = newBoundingBox(viewport, this.selectBoxX1,
          this.selectBoxY1, this.selectBoxX2, this.selectBoxY2);

        final double minSize = viewport.getModelUnitsPerViewUnit() * 10;
        final double width = boundingBox.getWidth();
        double deltaX = 0;
        if (width < minSize) {
          deltaX = (minSize - width) / 2;
          boundingBox.expandDeltaX(deltaX);
        }
        final double height = boundingBox.getWidth();
        double deltaY = 0;
        if (height < minSize) {
          deltaY = (minSize - height) / 2;
          boundingBox.expandDeltaY(deltaY);
        }
        if (!boundingBox.isEmpty()) {
          doSelectRecords(event, boundingBox.newBoundingBox());
        }
        selectBoxClear();
        if (isMouseInMap()) {
          setSelectCursor(event);
        }
        event.consume();
        repaint();
        return true;
      }
    }
    return false;
  }

  private boolean selectBoxStart(final MouseEvent event) {
    if (isOverlayAction(ACTION_SELECT_RECORDS) && SwingUtil.isLeftButtonOnly(event)) {
      this.selectBoxButton = event.getButton();
      this.selectBoxX1 = this.selectBoxX2 = event.getX();
      this.selectBoxY1 = this.selectBoxY2 = event.getY();
      return true;
    }
    return false;
  }

  public void selectRecords(final BoundingBox boundingBox) {
    try (
      BaseCloseable closeable = this.selectingRecords.closeable(true)) {
      final LayerGroup project = getProject();
      selectRecords(project, boundingBox);
      final LayerRendererOverlay overlay = getMap().getLayerOverlay();
      overlay.redraw();
      redrawAndRepaint();
    }
  }

  private void selectRecords(final LayerGroup group, final BoundingBox boundingBox) {
    final double scale = getViewportScale();
    final List<Layer> layers = group.getLayers();
    Collections.reverse(layers);
    for (final Layer layer : layers) {
      if (layer instanceof LayerGroup) {
        final LayerGroup childGroup = (LayerGroup)layer;
        selectRecords(childGroup, boundingBox);
      } else if (layer instanceof AbstractRecordLayer) {
        final AbstractRecordLayer recordLayer = (AbstractRecordLayer)layer;
        if (recordLayer.isSelectable(scale)) {
          recordLayer.setSelectedRecords(boundingBox);
        } else {
          recordLayer.clearSelectedRecords();
        }
      }
    }
  }

  public void setHighlightColors(final Color color) {
    this.highlightRenderer.setStyleColor(color);
    this.highlightVertexRenderer.setStyleColor(color);
  }

  public void setSelectColors(final Color color) {
    this.selectRenderer.setStyleColor(color);
    this.selectVertexRenderer.setStyleColor(color);
  }

  protected void setSelectCursor(final InputEvent event) {
    Cursor cursor = null;
    if (event != null) {
      final boolean selectBox = SwingUtil.isControlOrMetaDown(event) || this.selectBoxX1 != -1;
      if (SwingUtil.isShiftDown(event)) {
        if (selectBox) {
          cursor = CURSOR_SELECT_BOX_ADD;
        }
      } else if (SwingUtil.isAltDown(event)) {
        if (selectBox) {
          cursor = CURSOR_SELECT_BOX_DELETE;
        }
      } else if (SwingUtil.isControlOrMetaDown(event)) {
        if (selectBox || !hasOverlayAction()) {
          cursor = CURSOR_SELECT_BOX;
        }
      } else if (this.selectBoxX1 != -1) {
        cursor = CURSOR_SELECT_BOX;
      }
    }
    if (cursor == null) {
      clearOverlayAction(ACTION_SELECT_RECORDS);
    } else {
      setOverlayAction(ACTION_SELECT_RECORDS);
      setMapCursor(cursor);
    }
  }

  public void unSelectRecords(final BoundingBox boundingBox) {
    final LayerGroup project = getProject();
    unSelectRecords(project, boundingBox);
    final LayerRendererOverlay overlay = getMap().getLayerOverlay();
    overlay.redraw();
  }

  private void unSelectRecords(final LayerGroup group, final BoundingBox boundingBox) {

    final double scale = getViewportScale();
    final List<Layer> layers = group.getLayers();
    Collections.reverse(layers);
    for (final Layer layer : layers) {
      if (layer instanceof LayerGroup) {
        final LayerGroup childGroup = (LayerGroup)layer;
        unSelectRecords(childGroup, boundingBox);
      } else if (layer instanceof AbstractRecordLayer) {
        final AbstractRecordLayer recordLayer = (AbstractRecordLayer)layer;
        if (recordLayer.isSelectable(scale)) {
          recordLayer.unSelectRecords(boundingBox);
        }
      }
    }
  }
}
