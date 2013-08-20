package com.revolsys.swing.map.overlay;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.swing.map.MapPanel;
import com.vividsolutions.jts.geom.Point;

@SuppressWarnings("serial")
public class ZoomOverlay extends AbstractOverlay {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private static final Color TRANS_BG = new Color(0, 0, 0, 30);

  private java.awt.Point zoomBoxFirstPoint;

  private java.awt.Point panFirstPoint;

  private Rectangle2D zoomBox;

  private boolean panning;

  private BufferedImage panImage;

  public ZoomOverlay(final MapPanel map) {
    super(map);
  }

  @Override
  public void keyPressed(final KeyEvent e) {
    if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
      clearMapCursor();
      this.panning = false;
      this.panFirstPoint = null;
      this.panImage = null;
      this.zoomBox = null;
      this.zoomBoxFirstPoint = null;
      repaint();
    }
  }

  @Override
  public void keyReleased(final KeyEvent e) {
  }

  @Override
  public void keyTyped(final KeyEvent e) {
  }

  @Override
  public void mouseClicked(final MouseEvent event) {
    if (event.getClickCount() == 2) {
      final int x = event.getX();
      final int y = event.getY();
      int numSteps = 0;
      if (SwingUtilities.isLeftMouseButton(event)) {
        numSteps = -1;
      } else if (SwingUtilities.isRightMouseButton(event)) {
        numSteps = 1;
      }
      if (numSteps != 0) {
        final Point mapPoint = getViewport().toModelPoint(x, y);
        getMap().zoom(mapPoint, numSteps);
        event.consume();
      }
    }
  }

  @Override
  public void mouseDragged(final MouseEvent event) {
    if (this.zoomBoxFirstPoint != null) {
      zoomBoxDrag(event);
    } else if (this.panning) {
      panDrag(event);
    }
  }

  @Override
  public void mouseEntered(final MouseEvent e) {
  }

  @Override
  public void mouseExited(final MouseEvent e) {
  }

  @Override
  public void mouseMoved(final MouseEvent e) {
  }

  @Override
  public void mousePressed(final MouseEvent event) {
    final boolean shiftDown = event.isShiftDown();
    final int modifiers = event.getModifiers();
    if (shiftDown) {
      zoomBoxStart(event);
    } else if (modifiers == InputEvent.BUTTON1_MASK) {
      panStart(event);
    }
  }

  @Override
  public void mouseReleased(final MouseEvent event) {
    if (this.panning) {
      panFinish(event);
    } else if (this.zoomBox != null) {
      zoomBoxFinish(event);
    }
  }

  @Override
  public void mouseWheelMoved(final MouseWheelEvent event) {
    int numSteps = event.getWheelRotation();
    final int scrollType = event.getScrollType();
    if (scrollType == MouseWheelEvent.WHEEL_BLOCK_SCROLL) {
      numSteps *= 2;
    }
    final int x = event.getX();
    final int y = event.getY();
    final Point mapPoint = getViewport().toModelPoint(x, y);
    getMap().zoom(mapPoint, numSteps);
    event.consume();
  }

  @Override
  public void paintComponent(final Graphics graphics) {
    if (this.zoomBox != null) {
      final Graphics2D g = (Graphics2D)graphics;
      g.setColor(Color.DARK_GRAY);
      g.setStroke(new BasicStroke(2, BasicStroke.CAP_SQUARE,
        BasicStroke.JOIN_MITER, 2, new float[] {
          6, 6
        }, 0f));
      g.draw(this.zoomBox);
      g.setPaint(TRANS_BG);
      g.fill(this.zoomBox);
    }
  }

  public void panDrag(final MouseEvent event) {
    if (this.panFirstPoint == null) {
      panStart(event);
    }
    final java.awt.Point p = event.getPoint();
    final int dx = (int)(p.getX() - this.panFirstPoint.getX());
    final int dy = (int)(p.getY() - this.panFirstPoint.getY());

    final Container parent = getParent();
    final Graphics2D graphics = getGraphics();
    final int width = getViewport().getViewWidthPixels();
    final int height = getViewport().getViewHeightPixels();
    graphics.setColor(Color.WHITE);
    graphics.fillRect(0, 0, width, height);
    graphics.drawImage(this.panImage, dx, dy, parent);
    event.consume();
  }

  public void panFinish(final MouseEvent event) {
    final java.awt.Point point = event.getPoint();
    final Point fromPoint = getViewport().toModelPoint(this.panFirstPoint);
    final Point toPoint = getViewport().toModelPoint(point);

    final double deltaX = fromPoint.getX() - toPoint.getX();
    final double deltaY = fromPoint.getY() - toPoint.getY();

    final BoundingBox boundingBox = getViewport().getBoundingBox();
    final BoundingBox newBoundingBox = boundingBox.move(deltaX, deltaY);
    getMap().setBoundingBox(newBoundingBox);

    this.panFirstPoint = null;
    this.panning = false;
    clearMapCursor();
    this.panImage = null;
    getParent().repaint();
    event.consume();
  }

  public void panStart(final MouseEvent event) {
    final int width = getViewport().getViewWidthPixels();
    final int height = getViewport().getViewHeightPixels();
    final JComponent parent = (JComponent)getParent();
    this.panImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    final Graphics2D graphics = (Graphics2D)this.panImage.getGraphics();
    final Insets insets = parent.getInsets();
    graphics.translate(-insets.left, -insets.top);
    graphics.setColor(Color.WHITE);
    graphics.fillRect(insets.left, insets.top, width, height);
    parent.paintComponents(graphics);
    graphics.dispose();
    this.panning = true;
    setMapCursor(new Cursor(Cursor.HAND_CURSOR));
    this.panFirstPoint = event.getPoint();
    event.consume();
  }

  public void zoomBoxDrag(final MouseEvent event) {
    final int eventX = event.getX();
    final int eventY = event.getY();
    final double width = Math.abs(eventX - this.zoomBoxFirstPoint.getX());
    final double height = Math.abs(eventY - this.zoomBoxFirstPoint.getY());
    final java.awt.Point topLeft = new java.awt.Point(); // java.awt.Point
    if (this.zoomBoxFirstPoint.getX() < eventX) {
      topLeft.setLocation(this.zoomBoxFirstPoint.getX(), 0);
    } else {
      topLeft.setLocation(eventX, 0);
    }

    if (this.zoomBoxFirstPoint.getY() < eventY) {
      topLeft.setLocation(topLeft.getX(), this.zoomBoxFirstPoint.getY());
    } else {
      topLeft.setLocation(topLeft.getX(), eventY);
    }
    this.zoomBox.setRect(topLeft.getX(), topLeft.getY(), width, height);
    repaint();
    event.consume();
  }

  public void zoomBoxFinish(final MouseEvent event) {
    // Convert first point to envelope top left in map coords.
    final int minX = (int)this.zoomBox.getMinX();
    final int minY = (int)this.zoomBox.getMinY();
    final Point topLeft = getViewport().toModelPoint(minX, minY);

    // Convert second point to envelope bottom right in map coords.
    final int maxX = (int)this.zoomBox.getMaxX();
    final int maxY = (int)this.zoomBox.getMaxY();
    final Point bottomRight = getViewport().toModelPoint(maxX, maxY);

    final GeometryFactory geometryFactory = getMap().getGeometryFactory();
    final BoundingBox extent = new BoundingBox(geometryFactory, topLeft.getX(),
      topLeft.getY(), bottomRight.getX(), bottomRight.getY());
    getMap().setBoundingBox(extent);

    this.zoomBoxFirstPoint = null;
    this.zoomBox = null;
    clearMapCursor();
    event.consume();
  }

  public void zoomBoxStart(final MouseEvent event) {
    setMapCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
    this.zoomBoxFirstPoint = event.getPoint();
    this.zoomBox = new Rectangle2D.Double();
    event.consume();
  }
}
