/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
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
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.revolsys.geometry.geomgraph.index;

/**
 * @version 1.7
 */
public class SweepLineEvent<O> implements Comparable<SweepLineEvent<O>> {
  private static final int DELETE = 2;

  private static final int INSERT = 1;

  private int deleteEventIndex;

  private final int eventType;

  private SweepLineEvent<O> insertEvent = null; // null if this is an INSERT
                                                // event

  private final Object label; // used for red-blue intersection detection

  private final O object;

  private final double xValue;

  /**
   * Creates a DELETE event.
   *
   * @param x the event location
   * @param insertEvent the corresponding INSERT event
   */
  public SweepLineEvent(final double x, final SweepLineEvent<O> insertEvent) {
    this.eventType = DELETE;
    this.label = null;
    this.xValue = x;
    this.insertEvent = insertEvent;
    this.object = null;
  }

  /**
   * Creates an INSERT event.
   *
   * @param label the edge set label for this object
   * @param x the event location
   * @param obj the object being inserted
   */
  public SweepLineEvent(final Object label, final double x, final O obj) {
    this.eventType = INSERT;
    this.label = label;
    this.xValue = x;
    this.object = obj;
  }

  /**
   * Events are ordered first by their x-value, and then by their eventType.
   * Insert events are sorted before Delete events, so that
   * items whose Insert and Delete events occur at the same x-value will be
   * correctly handled.
   */
  @Override
  public int compareTo(final SweepLineEvent<O> event) {
    final int compare = Double.compare(this.xValue, event.xValue);
    if (compare == 0) {
      return Integer.compare(this.eventType, event.eventType);
    }
    return compare;
  }

  public int getDeleteEventIndex() {
    return this.deleteEventIndex;
  }

  public SweepLineEvent<O> getInsertEvent() {
    return this.insertEvent;
  }

  public O getObject() {
    return this.object;
  }

  public boolean isDelete() {
    return this.eventType == DELETE;
  }

  public boolean isInsert() {
    return this.eventType == INSERT;
  }

  public boolean isSameLabel(final SweepLineEvent<O> ev) {
    // no label set indicates single group
    if (this.label == null) {
      return false;
    }
    return this.label == ev.label;
  }

  public void setDeleteEventIndex(final int deleteEventIndex) {
    this.deleteEventIndex = deleteEventIndex;
  }

}
