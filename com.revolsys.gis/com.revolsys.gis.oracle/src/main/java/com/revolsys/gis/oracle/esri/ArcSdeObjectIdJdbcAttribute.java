package com.revolsys.gis.oracle.esri;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.jdbc.attribute.JdbcAttribute;
import com.revolsys.jdbc.JdbcUtils;

public class ArcSdeObjectIdJdbcAttribute extends JdbcAttribute {
  private static final Logger LOG = LoggerFactory.getLogger(ArcSdeOracleStGeometryJdbcAttribute.class);

  public static ArcSdeObjectIdJdbcAttribute getInstance(
    final JdbcAttribute attribute,
    final Connection connection,
    final QName name) {
    return getInstance(attribute, connection, name.getNamespaceURI(),
      name.getLocalPart());
  }

  public static ArcSdeObjectIdJdbcAttribute getInstance(
    final JdbcAttribute attribute,
    final Connection connection,
    final String schemaName,
    final String tableName) {
    try {
      final long registrationId = JdbcUtils.selectLong(
        connection,
        "SELECT registration_id FROM sde.table_registry WHERE owner = ? AND table_name = ?",
        schemaName, tableName);
      final String name = attribute.getName();
      final DataType type = attribute.getType();
      final int length = attribute.getLength();
      final int scale = attribute.getScale();
      final boolean required = attribute.isRequired();
      final Map<QName, Object> properties = attribute.getProperties();
      return new ArcSdeObjectIdJdbcAttribute(name, type, length, scale,
        required, properties, schemaName, registrationId);
    } catch (final IllegalArgumentException e) {
      LOG.error("Cannot get sde.table_registry values for " + schemaName + "."
        + tableName);
      return null;
    } catch (final SQLException e) {
      LOG.error("Cannot get sde.table_registry values for " + schemaName + "."
        + tableName, e);
      return null;
    }
  }

  /** The SDE.TABLE_REGISTRY REGISTRATION_ID for the table. */
  private final long registrationId;

  /** The name of the database schema the table owned by. */
  private final String schemaName;

  public ArcSdeObjectIdJdbcAttribute(
    final String name,
    final DataType type,
    final int length,
    final int scale,
    final boolean required,
    final Map<QName, Object> properties,
    final String schemaName,
    final long registrationId) {
    super(name, type, -1, length, scale, required, properties);
    this.schemaName = schemaName;
    this.registrationId = registrationId;
  }

  @Override
  protected ArcSdeObjectIdJdbcAttribute clone() {
    return new ArcSdeObjectIdJdbcAttribute(getName(), getType(), getLength(), getScale(), isRequired(), getProperties(), schemaName, registrationId);
  }
  
  @Override
  public void addInsertStatementPlaceHolder(
    final StringBuffer sql,
    final boolean generateKeys) {
    sql.append("NVL(?, sde.version_user_ddl.next_row_id('");
    sql.append(schemaName);
    sql.append("', ");
    sql.append(registrationId);
    sql.append("))");
  }
}
