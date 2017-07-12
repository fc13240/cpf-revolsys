package com.revolsys.geometry.test.old.operation;

import java.util.Collection;
import java.util.Iterator;

import com.revolsys.geometry.algorithm.match.AreaSimilarityMeasure;
import com.revolsys.geometry.algorithm.match.HausdorffSimilarityMeasure;
import com.revolsys.geometry.algorithm.match.SimilarityMeasureCombiner;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.operation.union.CascadedPolygonUnion;
import com.revolsys.geometry.wkb.WKTReader;

/**
 * Compares the results of CascadedPolygonUnion to Geometry.union()
 * using shape similarity measures.
 *
 * @author mbdavis
 *
 */
public class CascadedPolygonUnionTester {
  private static final GeometryFactory fact = GeometryFactory.floating(0, 2);;

  public static final double MIN_SIMILARITY_MEAURE = 0.999999;

  static WKTReader wktRdr = new WKTReader(fact);

  GeometryFactory geomFact = GeometryFactory.DEFAULT_3D;

  public CascadedPolygonUnionTester() {
  }

  public boolean test(final Collection geoms, final double minimumMeasure) {
    // System.out.println("Computing Iterated union");
    final Geometry union1 = unionIterated(geoms);
    // System.out.println("Computing Cascaded union");
    final Geometry union2 = unionCascaded(geoms);

    // System.out.println("Testing similarity with min measure = "
    // + minimumMeasure);

    final double areaMeasure = new AreaSimilarityMeasure().measure(union1, union2);
    final double hausMeasure = new HausdorffSimilarityMeasure().measure(union1, union2);
    final double overallMeasure = SimilarityMeasureCombiner.combine(areaMeasure, hausMeasure);

    // System.out.println("Area measure = " + areaMeasure
    // + " Hausdorff measure = " + hausMeasure + " Overall = "
    // + overallMeasure);

    return overallMeasure > minimumMeasure;
  }

  /*
   * private void OLDdoTest(String filename, double distanceTolerance) throws IOException,
   * ParseException { WKTFileReader fileRdr = new WKTFileReader(filename, wktRdr); List geoms =
   * fileRdr.read(); System.out.println("Computing Iterated union"); Geometry union1 =
   * unionIterated(geoms); System.out.println("Computing Cascaded union"); Geometry union2 =
   * unionCascaded(geoms); System.out.println( "Testing similarity with tolerance = " +
   * distanceTolerance); boolean isSameWithinTolerance = SimilarityValidator.isSimilar(union1,
   * union2, distanceTolerance); assertTrue(isSameWithinTolerance); }
   */

  public Geometry unionCascaded(final Collection geoms) {
    return CascadedPolygonUnion.union(geoms);
  }

  public Geometry unionIterated(final Collection geoms) {
    Geometry unionAll = null;
    int count = 0;
    for (final Iterator i = geoms.iterator(); i.hasNext();) {
      final Geometry geom = (Geometry)i.next();

      if (unionAll == null) {
        unionAll = geom.clone();
      } else {
        unionAll = unionAll.union(geom);
      }

      count++;
      if (count % 100 == 0) {
        System.out.print(".");
        // System.out.println("Adding geom #" + count);
      }
    }
    return unionAll;
  }

}
