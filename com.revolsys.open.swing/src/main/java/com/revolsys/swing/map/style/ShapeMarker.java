package com.revolsys.swing.map.style;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

import javax.measure.Measure;
import javax.measure.quantity.Length;

import com.revolsys.swing.map.Viewport2D;

public class ShapeMarker implements Marker {

  private static final Map<String, Shape> SHAPES = new HashMap<String, Shape>();

  static {
    SHAPES.put("square", square(1));
    SHAPES.put("circle", circle(1));
    SHAPES.put("triangle", triangle(1));
    SHAPES.put("star", star(1));
    SHAPES.put("cross", cross(1));
    SHAPES.put("x", x(1));
  }

  public static Shape circle(final double size) {
    return new Ellipse2D.Double(0, 0, size, size);
  }

  public static Shape cross(final double size) {
    final GeneralPath path = new GeneralPath();
    path.moveTo(size / 3, size);
    path.lineTo(size * 2 / 3, size);
    path.lineTo(size * 2 / 3, size * 2 / 3);
    path.lineTo(size, size * 2 / 3);
    path.lineTo(size, size / 3);
    path.lineTo(size * 2 / 3, size / 3);
    path.lineTo(size * 2 / 3, 0);
    path.lineTo(size / 3, 0);
    path.lineTo(size / 3, size / 3);
    path.lineTo(0, size / 3);
    path.lineTo(0, size * 2 / 3);
    path.lineTo(size / 3, size * 2 / 3);
    path.closePath();
    return path;
  }

  public static Shape square(final double size) {
    return new Rectangle2D.Double(0, 0, size, size);
  }

  public static Shape star(final double size) {
    final GeneralPath path = new GeneralPath();
    path.moveTo(size / 2, 0);
    path.lineTo(size * 0.64, size / 3);
    path.lineTo(size, size / 3);
    path.lineTo(size * .7, size * .57);
    path.lineTo(size * .8, size * .9);
    path.lineTo(size / 2, size * .683);
    path.lineTo(size * .2, size * .9);
    path.lineTo(size * .3, size * .57);
    path.lineTo(0, size / 3);
    path.lineTo(size * 0.36, size / 3);
    path.closePath();
    return path;
  }

  public static Shape triangle(final double size) {
    final GeneralPath path = new GeneralPath();
    path.moveTo(0, size);
    path.lineTo(size / 2, 0);
    path.lineTo(size, size);
    path.closePath();
    return path;
  }

  /**
   * Get an X shape for the size of the graphic.
   * 
   * @return The shape.
   */
  public static Shape x(final double size) {
    final GeneralPath path = new GeneralPath();
    path.moveTo(size * .25, size);
    path.lineTo(size * .5, size * .75);
    path.lineTo(size * .75, size);
    path.lineTo(size, size * .75);
    path.lineTo(size * .75, size * .5);
    path.lineTo(size, size * .25);
    path.lineTo(size * .75, 0);
    path.lineTo(size * .5, size * .25);
    path.lineTo(size * .25, 0);
    path.lineTo(0, size * .25);
    path.lineTo(size * .25, size * .5);
    path.lineTo(0, size * .75);
    path.closePath();
    return path;
  }

  private Shape shape;

  public ShapeMarker(Shape shape) {
    this.shape = shape;
    if (shape != null) {
      Rectangle bounds = shape.getBounds();
      double width = bounds.width;
      double height = bounds.height;
      double scale;
      if (width > height) {
        scale = 1 / width;
      } else {
        scale = 1 / height;
      }
      AffineTransform transform = AffineTransform.getTranslateInstance(
        -bounds.x, -bounds.y);
      transform.concatenate(AffineTransform.getScaleInstance(scale, scale));
      this.shape = new GeneralPath(shape).createTransformedShape(transform);
    }
  }

  public ShapeMarker(String name) {
    this(SHAPES.get(name));
    if (shape == null) {
      throw new IllegalArgumentException("Unknown shape " + name);
    }
  }

  public void render(final Viewport2D viewport, final Graphics2D graphics,
    final Style style, final double modelX, double modelY) {
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
      RenderingHints.VALUE_ANTIALIAS_ON);
    final AffineTransform savedTransform = graphics.getTransform();
    Measure<Length> markerWidth = style.getMarkerWidth();
    double mapWidth = viewport.toDisplayValue(markerWidth);
    Measure<Length> markerHeight = style.getMarkerHeight();
    double mapHeight = viewport.toDisplayValue(markerHeight);
    double x = modelX;
    double y = modelY;
    Measure<Length> deltaX = style.getMarkerDeltaX();
    x += viewport.toDisplayValue(deltaX);
    Measure<Length> deltaY = style.getMarkerDeltaX();
    y += viewport.toDisplayValue(deltaY);
    // final double rotation = graphic.getRotation().doubleValue(SI.RADIAN);
    // if (rotation != 0) {
    // graphics.rotate(rotation, x, y);
    // }
    graphics.translate(x, y);
    final Shape newShape = new GeneralPath(shape).createTransformedShape(AffineTransform.getScaleInstance(
      mapWidth, mapHeight));
    style.setFillStyle(viewport, graphics);
    graphics.fill(newShape);
    style.setLineStyle(viewport, graphics);
    graphics.draw(newShape);
    graphics.setTransform(savedTransform);
  }

}
