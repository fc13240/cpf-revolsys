/*
 * $URL:$
 * $Author:$
 * $Date:$
 * $Revision:$

 * Copyright 2004-2007 Revolution Systems Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.revolsys.gis.jts.filter;

import com.revolsys.filter.Filter;
import com.vividsolutions.jts.geom.LineString;

public class LineCrossesFilter implements Filter<LineString> {

  /** The geometry to compare the data objects to to. */
  private final LineString geometry;

  /**
   * Construct a new LessThanDistanceFilter.
   * 
   * @param geometry The geometry to compare the data objects to to.
   * @param maxDistance
   */
  public LineCrossesFilter(
    final LineString geometry) {
    this.geometry = geometry;
  }

  public boolean accept(
    final LineString geometry) {
    return geometry.crosses(this.geometry);
  }

}
