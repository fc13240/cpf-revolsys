package com.revolsys.swing.map.overlay;

import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.editor.GeometryEditor;
import com.revolsys.swing.undo.AbstractUndoableEdit;

class InsertVertexUndoEdit extends AbstractUndoableEdit {
  private static final long serialVersionUID = 1L;

  private final GeometryEditor<?> geometryEditor;

  private final int[] vertexId;

  private final Point newPoint;

  private final Point oldPoint;

  private final int vertexCount;

  InsertVertexUndoEdit(final GeometryEditor<?> geometryEditor, final int[] vertexId,
    final Point newPoint) {
    this.geometryEditor = geometryEditor;
    this.vertexId = vertexId;
    this.newPoint = newPoint;
    this.oldPoint = geometryEditor.getVertex(vertexId).newPoint();
    this.vertexCount = getCurrentVertexCount();
  }

  @Override
  public boolean canRedo() {
    if (super.canRedo()) {
      final int currentVertexCount = getCurrentVertexCount();
      if (this.vertexCount == currentVertexCount) {
        if (this.geometryEditor.equalsVertex(this.vertexId, this.oldPoint)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public boolean canUndo() {
    if (super.canUndo()) {
      final int currentVertexCount = getCurrentVertexCount();
      if (this.vertexCount + 1 == currentVertexCount) {
        if (this.geometryEditor.equalsVertex(this.vertexId, this.newPoint)) {
          return true;
        }
      }
    }
    return false;
  }

  private int getCurrentVertexCount() {
    return this.geometryEditor.getVertexCount(this.vertexId, this.vertexId.length - 1);
  }

  @Override
  protected void redoDo() {
    this.geometryEditor.insertVertex(this.vertexId, this.newPoint);
  }

  @Override
  protected void undoDo() {
    this.geometryEditor.deleteVertex(this.vertexId);
  }
}
