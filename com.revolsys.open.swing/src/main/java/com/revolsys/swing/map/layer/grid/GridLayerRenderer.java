package com.revolsys.swing.map.layer.grid;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.util.List;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.grid.RectangularMapGrid;
import com.revolsys.gis.grid.RectangularMapTile;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.AbstractLayerRenderer;
import com.revolsys.swing.map.util.GeometryShapeUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class GridLayerRenderer extends  AbstractLayerRenderer<GridLayer> {

  public GridLayerRenderer(GridLayer layer) {
    super("grid", layer);
  }

  @Override
  public void render(final Viewport2D viewport, final Graphics2D graphics,
    final GridLayer layer) {
    final double scale = viewport.getScale();
    if (layer.isVisible(scale)) {
      viewport.setUseModelCoordinates(true, graphics);

      final BoundingBox boundingBox = viewport.getBoundingBox();
      final RectangularMapGrid grid = layer.getGrid();
      final List<RectangularMapTile> tiles = grid.getTiles(boundingBox);
      final Font font = graphics.getFont();
      for (final RectangularMapTile tile : tiles) {
        final Polygon polygon = tile.getPolygon(viewport.getGeometryFactory(),
          50);
        graphics.setColor(Color.LIGHT_GRAY);
        graphics.setStroke(new BasicStroke(
          (float)viewport.getModelUnitsPerViewUnit()));
        final Shape shape = GeometryShapeUtil.toShape(viewport, polygon);
        graphics.draw(shape);

        final Point centroid = polygon.getCentroid();
        final Coordinate coordinate = centroid.getCoordinate();

        viewport.setUseModelCoordinates(false, graphics);

        final Font newFont = new Font(font.getName(), font.getStyle(), 12);
        graphics.setFont(newFont);

        final FontMetrics metrics = graphics.getFontMetrics();
        final double[] coord = new double[2];
        viewport.getModelToScreenTransform().transform(new double[] {
          coordinate.x, coordinate.y
        }, 0, coord, 0, 1);
        final String tileName = tile.getName();
        final int x = (int)(coord[0] + metrics.stringWidth(tileName) / 2);
        final int y = (int)(coord[1] + metrics.getHeight() / 2);

        final Stroke savedStroke = graphics.getStroke();
        final Stroke outlineStroke = new BasicStroke(3, BasicStroke.CAP_BUTT,
          BasicStroke.JOIN_BEVEL);
        graphics.setColor(Color.WHITE);
        graphics.setStroke(outlineStroke);

        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
          RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        final TextLayout textLayout = new TextLayout(tileName, newFont,
          graphics.getFontRenderContext());

        graphics.draw(textLayout.getOutline(AffineTransform.getTranslateInstance(
          x, y)));

        graphics.setStroke(savedStroke);

        graphics.setColor(Color.BLACK);
        graphics.drawString(tileName, x, y);
        viewport.setUseModelCoordinates(true, graphics);
      }
      graphics.setFont(font);
    }
  }

}