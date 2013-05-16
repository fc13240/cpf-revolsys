package com.revolsys.swing.action.enablecheck;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public abstract class MultiEnableCheck extends AbstractEnableCheck implements
  Iterable<EnableCheck> {

  private List<EnableCheck> enableChecks = new ArrayList<EnableCheck>();

  public MultiEnableCheck() {
  }

  public MultiEnableCheck(final Collection<EnableCheck> enableChecks) {
    setEnableChecks(enableChecks);
  }

  public List<EnableCheck> getEnableChecks() {
    return enableChecks;
  }

  @Override
  public Iterator<EnableCheck> iterator() {
    return enableChecks.iterator();
  }

  public void setEnableChecks(final Collection<EnableCheck> enableChecks) {
    this.enableChecks = new ArrayList<EnableCheck>(enableChecks);
    isEnabled();
  }

  @Override
  public String toString() {
    return enableChecks.toString();
  }
}