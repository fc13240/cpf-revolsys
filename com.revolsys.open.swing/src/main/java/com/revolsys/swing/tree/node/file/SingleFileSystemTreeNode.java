package com.revolsys.swing.tree.node.file;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import com.revolsys.swing.tree.BaseTreeNode;
import com.revolsys.util.WrappedException;

public class SingleFileSystemTreeNode extends PathTreeNode {

  private FileSystem fileSystem;

  public SingleFileSystemTreeNode(final Path path) {
    super(path);
    try {
      this.fileSystem = FileSystems.newFileSystem(path, null);
    } catch (final IOException e) {
      throw new WrappedException(e);
    }
  }

  @Override
  public boolean isAllowsChildren() {
    return true;
  }

  @Override
  protected List<BaseTreeNode> loadChildrenDo() {
    for (final Path root : this.fileSystem.getRootDirectories()) {
      return PathTreeNode.getPathNodes(root);
    }
    return Collections.emptyList();
  }
}
