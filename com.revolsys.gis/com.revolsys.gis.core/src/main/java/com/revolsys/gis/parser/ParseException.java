/*
 * $URL:https://secure.revolsys.com/svn/open.revolsys.com/GIS/trunk/src/main/java/com/revolsys/gis/parser/ParseException.java $
 * $Author:paul.austin@revolsys.com $
 * $Date:2007-06-09 09:28:28 -0700 (Sat, 09 Jun 2007) $
 * $Revision:265 $

 * Copyright 2004-2005 Revolution Systems Inc.
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
package com.revolsys.gis.parser;

public class ParseException extends RuntimeException {
  /**
   * 
   */
  private static final long serialVersionUID = 422288785858881823L;

  private String location;

  public ParseException() {
  }

  public ParseException(
    final String message) {
    super(message);
  }

  public ParseException(
    final String location,
    final String message) {
    super(message);
    this.location = location;
  }

  public ParseException(
    final String location,
    final String message,
    final Throwable cause) {
    super(message, cause);
    this.location = location;
  }

  public ParseException(
    final String message,
    final Throwable cause) {
    super(message, cause);
  }

  public ParseException(
    final Throwable cause) {
    super(cause);
  }

  public String getLocation() {
    return location;
  }

  @Override
  public String toString() {
    if (location != null) {
      return super.toString() + " at '" + location + "'";
    } else {
      return super.toString();
    }
  }
}
