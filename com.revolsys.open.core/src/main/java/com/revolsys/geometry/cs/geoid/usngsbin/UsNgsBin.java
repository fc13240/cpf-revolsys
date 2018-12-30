package com.revolsys.geometry.cs.geoid.usngsbin;

import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.gridded.GriddedElevationModelReader;
import com.revolsys.elevation.gridded.GriddedElevationModelReaderFactory;
import com.revolsys.io.AbstractIoFactoryWithCoordinateSystem;
import com.revolsys.spring.resource.Resource;

public class UsNgsBin extends AbstractIoFactoryWithCoordinateSystem
  implements GriddedElevationModelReaderFactory {

  public static final String FILE_EXTENSION = "bin";

  public static final String FILE_EXTENSION_ZIP = FILE_EXTENSION + ".zip";

  public static final String FILE_EXTENSION_GZ = FILE_EXTENSION + ".gz";

  public UsNgsBin() {
    super("US NGS GEOID BIN");
    addMediaTypeAndFileExtension("image/x-us-ngs-bin", FILE_EXTENSION);
    addFileExtension(FILE_EXTENSION_ZIP);
    addFileExtension(FILE_EXTENSION_GZ);
  }

  @Override
  public GriddedElevationModelReader newGriddedElevationModelReader(final Resource resource,
    final MapEx properties) {
    return new UsNgsBinGeoidModelReader(resource, properties);
  }
}