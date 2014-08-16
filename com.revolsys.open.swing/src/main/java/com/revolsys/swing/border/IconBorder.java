/*
 * $Id: IconBorder.java 2528 2007-12-14 13:49:37Z stolis $
 *
 * Copyright 2004 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.revolsys.swing.border;

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.io.Serializable;

import javax.swing.Icon;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import org.jdesktop.swingx.icon.EmptyIcon;

/**
 * {@code IconBorder} creates a border that places an {@code Icon} in the border
 * on the horizontal axis. The border does not add any additional insets other
 * than the inset required to produce the space for the icon. If additional
 * insets are required, users should create a
 * {@link javax.swing.border.CompoundBorder compund border}.
 * <p>
 * This border is useful when attempting to add {@code Icon}s to pre-existing
 * components without requiring specialty painting.
 *
 * @author Amy Fowler
 * @author Karl Schaefer
 *
 * @version 1.1
 */
public class IconBorder implements Border, Serializable {

  /**
   * An empty icon.
   */
  public static final Icon EMPTY_ICON = new EmptyIcon(16, 16);

  private int padding;

  private Icon icon;

  private int iconPosition;

  private final Rectangle iconBounds = new Rectangle();

  /**
   * Creates an {@code IconBorder} with an empty icon in a trailing position
   * with a padding of 4.
   *
   * @see #EMPTY_ICON
   */
  public IconBorder() {
    this(null);
  }

  /**
   * Creates an {@code IconBorder} with the specified icon in a trailing
   * position with a padding of 1.
   *
   * @param validIcon
   *            the icon to set. This may be {@code null} to represent an
   *            empty icon.
   * @see #EMPTY_ICON
   */
  public IconBorder(final Icon validIcon) {
    this(validIcon, SwingConstants.TRAILING);
  }

  /**
   * Creates an {@code IconBorder} with the specified constraints and a
   * padding of 1.
   *
   * @param validIcon
   *            the icon to set. This may be {@code null} to represent an
   *            empty icon.
   * @param iconPosition
   *            the position to place the icon relative to the component
   *            contents. This must be one of the following
   *            {@code SwingConstants}:
   *            <ul>
   *            <li>{@code LEADING}</li>
   *            <li>{@code TRAILING}</li>
   *            <li>{@code EAST}</li>
   *            <li>{@code WEST}</li>
   *            </ul>
   * @throws IllegalArgumentException
   *             if {@code iconPosition} is not a valid position.
   * @see #EMPTY_ICON
   */
  public IconBorder(final Icon validIcon, final int iconPosition) {
    this(validIcon, iconPosition, 1);
  }

  /**
   * Creates an {@code IconBorder} with the specified constraints. If
   * {@code validIcon} is {@code null}, {@code EMPTY_ICON} is used instead.
   * If {@code padding} is negative, then the border does not use padding.
   *
   * @param validIcon
   *            the icon to set. This may be {@code null} to represent an
   *            empty icon.
   * @param iconPosition
   *            the position to place the icon relative to the component
   *            contents. This must be one of the following
   *            {@code SwingConstants}:
   *            <ul>
   *            <li>{@code LEADING}</li>
   *            <li>{@code TRAILING}</li>
   *            <li>{@code EAST}</li>
   *            <li>{@code WEST}</li>
   *            </ul>
   * @param padding
   *            the padding to surround the icon with. All non-positive values
   *            set the padding to 0.
   * @throws IllegalArgumentException
   *             if {@code iconPosition} is not a valid position.
   * @see #EMPTY_ICON
   */
  public IconBorder(final Icon validIcon, final int iconPosition,
    final int padding) {
    setIcon(validIcon);
    setPadding(padding);
    setIconPosition(iconPosition);
  }

  /**
   * Returns EAST or WEST depending on the ComponentOrientation and
   * the given postion LEADING/TRAILING this method has no effect for other
   * position values
   */
  private int bidiDecodeLeadingTrailing(final ComponentOrientation c,
    final int position) {
    if (position == SwingConstants.TRAILING) {
      if (!c.isLeftToRight()) {
        return SwingConstants.WEST;
      }
      return SwingConstants.EAST;
    }
    if (position == SwingConstants.LEADING) {
      if (c.isLeftToRight()) {
        return SwingConstants.WEST;
      }
      return SwingConstants.EAST;
    }
    return position;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Insets getBorderInsets(final Component c) {
    final int horizontalInset = this.icon.getIconWidth() + 2 * this.padding;
    final int iconPosition = bidiDecodeLeadingTrailing(
      c.getComponentOrientation(), this.iconPosition);
    if (iconPosition == SwingConstants.EAST) {
      return new Insets(0, 0, 0, horizontalInset);
    }
    return new Insets(0, horizontalInset, 0, 0);
  }

  /**
   * Returns the position to place the icon (relative to the component contents).
   *
   * @return one of the following {@code SwingConstants}:
   *        <ul>
   *          <li>{@code LEADING}</li>
   *          <li>{@code TRAILING}</li>
   *          <li>{@code EAST}</li>
   *          <li>{@code WEST}</li>
   *        </ul>
   */
  public int getIconPosition() {
    return this.iconPosition;
  }

  /**
   * Gets the padding surrounding the icon.
   *
   * @return the padding for the icon. This value is guaranteed to be
   *         nonnegative.
   */
  public int getPadding() {
    return this.padding;
  }

  /**
   * This border is not opaque.
   *
   * @return always returns {@code false}
   */
  @Override
  public boolean isBorderOpaque() {
    return true;
  }

  private boolean isValidPosition(final int position) {
    boolean result = false;

    switch (position) {
      case SwingConstants.LEADING:
      case SwingConstants.TRAILING:
      case SwingConstants.EAST:
      case SwingConstants.WEST:
        result = true;
        break;
      default:
        result = false;
    }

    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void paintBorder(final Component c, final Graphics g, final int x,
    final int y, final int width, final int height) {
    final int iconPosition = bidiDecodeLeadingTrailing(
      c.getComponentOrientation(), this.iconPosition);
    if (iconPosition == SwingConstants.NORTH_EAST) {
      this.iconBounds.y = y + this.padding;
      this.iconBounds.x = x + width - this.padding - this.icon.getIconWidth();
    } else if (iconPosition == SwingConstants.EAST) { // EAST
      this.iconBounds.y = y + (height - this.icon.getIconHeight()) / 2;
      this.iconBounds.x = x + width - this.padding - this.icon.getIconWidth();
    } else if (iconPosition == SwingConstants.WEST) {
      this.iconBounds.y = y + (height - this.icon.getIconHeight()) / 2;
      this.iconBounds.x = x + this.padding;
    }
    this.iconBounds.width = this.icon.getIconWidth();
    this.iconBounds.height = this.icon.getIconHeight();
    this.icon.paintIcon(c, g, this.iconBounds.x, this.iconBounds.y);
  }

  /**
   * Sets the icon for this border.
   *
   * @param validIcon
   *            the icon to set.  This may be {@code null} to represent an
   *            empty icon.
   * @see #EMPTY_ICON
   */
  public void setIcon(final Icon validIcon) {
    this.icon = validIcon == null ? EMPTY_ICON : validIcon;
  }

  /**
   * Sets the position to place the icon (relative to the component contents).
   *
   * @param iconPosition must be one of the following {@code SwingConstants}:
   *        <ul>
   *          <li>{@code LEADING}</li>
   *          <li>{@code TRAILING}</li>
   *          <li>{@code EAST}</li>
   *          <li>{@code WEST}</li>
   *        </ul>
   * @throws IllegalArgumentException
   *             if {@code iconPosition} is not a valid position.
   */
  public void setIconPosition(final int iconPosition) {
    if (!isValidPosition(iconPosition)) {
      throw new IllegalArgumentException("Invalid icon position");
    }
    this.iconPosition = iconPosition;
  }

  /**
   * Sets the padding around the icon.
   *
   * @param padding
   *            the padding to set. If {@code padding < 0}, then
   *            {@code padding} will be set to {@code 0}.
   */
  public void setPadding(final int padding) {
    this.padding = padding < 0 ? 0 : padding;
  }

}
