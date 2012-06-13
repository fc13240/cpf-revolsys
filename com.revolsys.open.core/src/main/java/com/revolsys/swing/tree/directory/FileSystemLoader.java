package com.revolsys.swing.tree.directory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.revolsys.swing.tree.dynamic.DynamicNodeLoader;

public class FileSystemLoader implements DynamicNodeLoader {
  private static final DynamicNodeLoader childLoader = new DirectoryLoader();

  @Override
  public List loadNodes(final Object parentUserObject) {
    final List nodes = new ArrayList();
    final File[] fileSystems = File.listRoots();

    for (int i = 0; i < fileSystems.length; i++) {
      final DirectoryTreeNode node = new DirectoryTreeNode(fileSystems[i],
        childLoader);
      nodes.add(node);
    }
    return nodes;
  }
}