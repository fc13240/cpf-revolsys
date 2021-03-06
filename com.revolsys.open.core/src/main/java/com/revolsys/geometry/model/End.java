package com.revolsys.geometry.model;

import java.util.List;

import com.revolsys.collection.list.Lists;
import com.revolsys.util.CaseConverter;

public enum End {
  FROM, TO;

  public static List<End> VALUES = Lists.newArray(FROM, TO);

  public static End getFrom(final Direction direction) {
    if (direction == null) {
      return null;
    } else if (direction.isForwards()) {
      return FROM;
    } else {
      return TO;
    }
  }

  public static boolean isFrom(final End end) {
    return end == FROM;
  }

  public static boolean isTo(final End end) {
    return end == TO;
  }

  public static End opposite(final End end) {
    if (end == FROM) {
      return TO;
    } else if (end == TO) {
      return FROM;
    } else {
      return null;
    }
  }

  private final String title;

  private End() {
    this.title = CaseConverter.captialize(name());
  }

  public String getTitle() {
    return this.title;
  }

  public boolean isFrom() {
    return this == FROM;
  }

  public boolean isOpposite(final End end) {
    if (end == null) {
      return false;
    } else {
      return isFrom() != end.isFrom();
    }
  }

  public boolean isTo() {
    return this == TO;
  }

  public End opposite() {
    if (isFrom()) {
      return TO;
    } else {
      return FROM;
    }
  }
}
