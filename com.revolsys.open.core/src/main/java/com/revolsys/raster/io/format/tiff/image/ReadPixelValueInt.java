package com.revolsys.raster.io.format.tiff.image;

import java.io.IOException;

@FunctionalInterface
public interface ReadPixelValueInt {
  int getValue() throws IOException;
}
