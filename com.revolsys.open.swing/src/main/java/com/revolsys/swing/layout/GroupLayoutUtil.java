package com.revolsys.swing.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.LayoutManager;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;

public class GroupLayoutUtil {
  public static void makeColumns(final Container container, final int numColumns) {
    final LayoutManager layout = container.getLayout();
    if (layout instanceof GroupLayout) {
      final GroupLayout groupLayout = (GroupLayout)layout;

      final int componentCount = container.getComponentCount();
      final int numRows = (int)Math.ceil(componentCount / (double)numColumns);

      final SequentialGroup horizontalGroup = groupLayout.createSequentialGroup();
      groupLayout.setHorizontalGroup(horizontalGroup);
      for (int columnIndex = 0; columnIndex < numColumns; columnIndex++) {
        final ParallelGroup columnGroup = groupLayout.createParallelGroup(Alignment.LEADING);
        horizontalGroup.addGroup(columnGroup);
        for (int rowIndex = 0; rowIndex < numRows; rowIndex++) {
          final int componentIndex = rowIndex * numColumns + columnIndex;
          if (componentIndex < componentCount) {
            final Component component = container.getComponent(componentIndex);
            columnGroup.addComponent(component, GroupLayout.PREFERRED_SIZE,
              GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
          }
        }
      }

      final SequentialGroup verticalGroup = groupLayout.createSequentialGroup();
      groupLayout.setVerticalGroup(verticalGroup);
      for (int rowIndex = 0; rowIndex < numRows; rowIndex++) {
        final ParallelGroup rowGroup = groupLayout.createParallelGroup(Alignment.BASELINE);
        verticalGroup.addGroup(rowGroup);
        for (int columnIndex = 0; columnIndex < numColumns; columnIndex++) {
          final int componentIndex = rowIndex * numColumns + columnIndex;
          if (componentIndex < componentCount) {
            final Component component = container.getComponent(componentIndex);
            rowGroup.addComponent(component, GroupLayout.PREFERRED_SIZE,
              GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
          }
        }
      }
    } else {
      throw new IllegalArgumentException("Expecting a " + GroupLayout.class
        + " not " + layout.getClass());
    }
  }

}