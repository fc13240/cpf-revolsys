package com.revolsys.swing.map.layer.dataobject.style;

import java.util.LinkedHashMap;
import java.util.Map;

import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.util.CaseConverter;

public enum GeometryStyleProperty {
  /** */
  image_filters("image-filters"),
  /** */
  comp_op("comp-op"),
  /** */
  opacity("opacity"),
  /** */
  background_color("background-color", DataTypes.COLOR),
  /** */
  background_image("background-image"),
  /** */
  srs("srs"),
  /** */
  buffer_size("buffer-size", DataTypes.DOUBLE),
  /** */
  base("base"),
  /** */
  font_directory("font-directory"),
  /** */
  polygon("polygon"),
  /** */
  polygon_fill("polygon-fill", DataTypes.COLOR),
  /** */
  polygon_opacity("polygon-opacity"),
  /** */
  polygon_gamma("polygon-gamma"),
  /** */
  polygon_gamma_method("polygon-gamma-method"),
  /** */
  polygon_clip("polygon-clip"),
  /** */
  polygon_smooth("polygon-smooth"),
  /** */
  polygon_comp_op("polygon-comp-op"),
  /** */
  line_color("line-color", DataTypes.COLOR),
  /** */
  line_width("line-width", DataTypes.DOUBLE),
  /** */
  line_opacity("line-opacity"),
  /** */
  line_join("line-join"),
  /** */
  line_cap("line-cap"),
  /** */
  line_gamma("line-gamma"),
  /** */
  line_gamma_method("line-gamma-method"),
  /** */
  line_dasharray("line-dasharray"),
  /** */
  line_dash_offset("line-dash-offset"),
  /** */
  line_miterlimit("line-miterlimit"),
  /** */
  line_clip("line-clip"),
  /** */
  line_smooth("line-smooth"),
  /** */
  line_offset("line-offset"),
  /** */
  line_rasterizer("line-rasterizer"),
  /** */
  line_comp_op("line-comp-op"),

  /** */
  shield("shield"),
  /** */
  shield_name("shield-name"),
  /** */
  shield_file("shield-file"),
  /** */
  shield_face_name("shield-face-name"),
  /** */
  shield_size("shield-size", DataTypes.DOUBLE),
  /** */
  shield_fill("shield-fill", DataTypes.COLOR),
  /** */
  shield_placement("shield-placement"),
  /** */
  shield_avoid_edges("shield-avoid-edges"),
  /** */
  shield_allow_overlap("shield-allow-overlap"),
  /** */
  shield_min_distance("shield-min-distance"),
  /** */
  shield_spacing("shield-spacing"),
  /** */
  shield_min_padding("shield-min-padding"),
  /** */
  shield_wrap_width("shield-wrap-width", DataTypes.DOUBLE),
  /** */
  shield_wrap_before("shield-wrap-before"),
  /** */
  shield_wrap_character("shield-wrap-character"),
  /** */
  shield_halo_fill("shield-halo-fill", DataTypes.COLOR),
  /** */
  shield_halo_radius("shield-halo-radius"),
  /** */
  shield_character_spacing("shield-character-spacing"),
  /** */
  shield_line_spacing("shield-line-spacing"),
  /** */
  shield_text_dx("shield-text-dx", DataTypes.DOUBLE),
  /** */
  shield_text_dy("shield-text-dy", DataTypes.DOUBLE),
  /** */
  shield_dx("shield-dx", DataTypes.DOUBLE),
  /** */
  shield_dy("shield-dy", DataTypes.DOUBLE),
  /** */
  shield_opacity("shield-opacity"),
  /** */
  shield_text_opacity("shield-text-opacity"),
  /** */
  shield_horizontal_alignment("shield-horizontal-alignment"),
  /** */
  shield_vertical_alignment("shield-vertical-alignment"),
  /** */
  shield_text_transform("shield-text-transform"),
  /** */
  shield_justify_alignment("shield-justify-alignment"),
  /** */
  shield_clip("shield-clip"),
  /** */
  shield_comp_op("shield-comp-op"),
  /** */
  line_pattern("line-pattern"),
  /** */
  line_pattern_file("line-pattern-file"),
  /** */
  line_pattern_clip("line-pattern-clip"),
  /** */
  line_pattern_smooth("line-pattern-smooth"),
  /** */
  line_pattern_comp_op("line-pattern-comp-op"),
  /** */
  polygon_pattern("polygon-pattern"),
  /** */
  polygon_pattern_file("polygon-pattern-file"),
  /** */
  polygon_pattern_alignment("polygon-pattern-alignment"),
  /** */
  polygon_pattern_gamma("polygon-pattern-gamma"),
  /** */
  polygon_pattern_opacity("polygon-pattern-opacity"),
  /** */
  polygon_pattern_clip("polygon-pattern-clip"),
  /** */
  polygon_pattern_smooth("polygon-pattern-smooth"),
  /** */
  polygon_pattern_comp_op("polygon-pattern-comp-op"),
  /** */
  raster("raster"),
  /** */
  raster_opacity("raster-opacity"),
  /** */
  raster_filter_factor("raster-filter-factor"),
  /** */
  raster_scaling("raster-scaling"),
  /** */
  raster_mesh_size("raster-mesh-size", DataTypes.DOUBLE),
  /** */
  raster_comp_op("raster-comp-op"),
  /** */
  point("point"),
  /** */
  point_file("point-file"),
  /** */
  point_allow_overlap("point-allow-overlap"),
  /** */
  point_ignore_placement("point-ignore-placement"),
  /** */
  point_opacity("point-opacity"),
  /** */
  point_placement("point-placement"),
  /** */
  point_transform("point-transform"),
  /** */
  point_comp_op("point-comp-op"),
  /** */
  building_fill("building-fill", DataTypes.COLOR),
  /** */
  building_fill_opacity("building-fill-opacity"),
  /** */
  building_height("building-height", DataTypes.DOUBLE);

  private static final Map<String, GeometryStyleProperty> propertiesByLabel = new LinkedHashMap<String, GeometryStyleProperty>();

  static {
    for (final GeometryStyleProperty property : GeometryStyleProperty.values()) {
      final String label = property.getLabel();
      propertiesByLabel.put(label, property);
    }
  }

  public static GeometryStyleProperty getProperty(final String label) {
    return propertiesByLabel.get(label);
  }

  private String label;

  private DataType dataType;

  private String propertyName;

  private GeometryStyleProperty(final String label) {
    this(label, DataTypes.STRING);
  }

  private GeometryStyleProperty(final String label, final DataType dataType) {
    this.label = label;
    this.dataType = dataType;
    this.propertyName = CaseConverter.toLowerCamelCase(name());
  }

  public DataType getDataType() {
    return dataType;
  }

  public String getLabel() {
    return label;
  }

  public String getPropertyName() {
    return propertyName;
  }

  @Override
  public String toString() {
    return label;
  }
}