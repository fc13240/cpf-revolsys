package com.revolsys.swing.map.layer.dataobject.style.panel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import com.revolsys.swing.map.layer.dataobject.style.MarkerStyle;
import com.revolsys.swing.map.layer.dataobject.style.marker.Marker;
import com.revolsys.util.ExceptionUtil;

public class MarkerStylePreview extends JPanel {
  private static final long serialVersionUID = 1L;

  private final MarkerStyle markerStyle;

  public MarkerStylePreview(final MarkerStyle markerStyle) {
    final Dimension size = new Dimension(100, 100);
    setPreferredSize(size);
    setMinimumSize(size);
    setMaximumSize(size);
    setBackground(Color.WHITE);
    setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
    this.markerStyle = markerStyle;
  }

  @Override
  protected void paintComponent(final Graphics g) {
    super.paintComponent(g);
    final Graphics2D graphics = (Graphics2D)g;
    final Marker marker = this.markerStyle.getMarker();
    try {
      marker.render(null, graphics, this.markerStyle, 49, 49, 0);
    } catch (final Throwable e) {
      ExceptionUtil.log(getClass(), e);
    }
  }
}
