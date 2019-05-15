package com.revolsys.swing.map.layer.raster;

import java.io.File;

import org.jeometry.common.logging.Logs;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.swing.map.ImageViewport;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.view.graphics.Graphics2DViewRenderer;

public class SaveAsGeoreferencedImage {

  public static void save(final File file, final Project project) {
    try {

      final Viewport2D viewport = project.getViewport();
      final BoundingBox boundingBox = viewport.getBoundingBox();
      final int width = (int)Math.ceil(viewport.getViewWidthPixels());
      final int height = (int)Math.ceil(viewport.getViewHeightPixels());

      try (
        ImageViewport imageViewport = new ImageViewport(project, width, height, boundingBox)) {
        final Graphics2DViewRenderer view = imageViewport.newViewRenderer();
        view.renderLayer(project.getBaseMapLayers());
        view.renderLayer(project);

        final GeoreferencedImage image = imageViewport.getGeoreferencedImage();
        image.writeImage(file);
      }

    } catch (final Throwable e) {
      Logs.error(SaveAsGeoreferencedImage.class, "Unable to create PDF " + file, e);
    }
  }
}