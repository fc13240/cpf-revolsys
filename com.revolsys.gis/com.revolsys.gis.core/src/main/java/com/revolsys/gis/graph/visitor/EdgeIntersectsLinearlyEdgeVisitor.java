package com.revolsys.gis.graph.visitor;

import java.util.List;

import com.revolsys.gis.data.visitor.CreateListVisitor;
import com.revolsys.gis.data.visitor.Visitor;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.EdgeQuadTree;
import com.revolsys.gis.graph.Graph;
import com.vividsolutions.jts.geom.Dimension;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.IntersectionMatrix;
import com.vividsolutions.jts.geom.LineString;

public class EdgeIntersectsLinearlyEdgeVisitor<T> implements Visitor<Edge<T>> {

  public static <T> List<Edge<T>> getEdges(
    final Graph<T> graph,
    final Edge<T> edge) {
    final CreateListVisitor<Edge<T>> results = new CreateListVisitor<Edge<T>>();
    final LineString line = edge.getLine();
    final Envelope env = line.getEnvelopeInternal();
    final EdgeQuadTree<T> index = graph.getEdgeIndex();
    index.query(env, new EdgeIntersectsLinearlyEdgeVisitor<T>(edge, results));
    return results.getList();

  }

  private final Edge<T> edge;

  private final Visitor<Edge<T>> matchVisitor;

  public EdgeIntersectsLinearlyEdgeVisitor(
    final Edge<T> edge,
    final Visitor<Edge<T>> matchVisitor) {
    this.edge = edge;
    this.matchVisitor = matchVisitor;
  }

  public boolean visit(
    final Edge<T> edge2) {
    if (edge2 != edge) {
      final LineString line1 = edge.getLine();
      final LineString line2 = edge2.getLine();
      final Envelope envelope1 = line1.getEnvelopeInternal();
      final Envelope envelope2 = line2.getEnvelopeInternal();
      if (envelope1.intersects(envelope2)) {
        final IntersectionMatrix relate = line1.relate(line2);
        if (relate.get(0, 0) == Dimension.L) {
          matchVisitor.visit(edge2);
        }
      }
    }
    return true;
  }

}
