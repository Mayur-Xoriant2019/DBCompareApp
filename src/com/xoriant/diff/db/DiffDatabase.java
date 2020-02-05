package com.xoriant.diff.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.diffkit.common.DKMapKeyValueComparator;
import org.diffkit.db.DKDBColumn;
import org.diffkit.db.DKDBPrimaryKey;
import org.diffkit.db.DKDBTable;
import org.diffkit.db.DKDBType;
import org.diffkit.util.DKMapUtil;
import org.diffkit.util.DKNumberUtil;
import org.diffkit.util.DKSqlUtil;
import org.diffkit.util.DKStringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiffDatabase {

	private static final String USERNAME_KEY = "user";
	private static final String PASSWORD_KEY = "password";
	private final DiffDBConnection _connectionInfo;
	private final boolean _caseSensitive;
	private final Logger _log = LoggerFactory.getLogger(this.getClass());
	private final boolean _isDebugEnabled = _log.isDebugEnabled();
	
	// uggg!! H2 calls it TABLE_CATALOG and DB2 calls it TABLE_CAT
	private static final String TABLE_CATALOG_KEY = "TABLE_CAT";
	// uggg!! H2 calls it TABLE_SCHEMA and DB2 calls it TABLE_SCHEM
	private static final String TABLE_SCHEMA_KEY = "TABLE_SCHEM";
	private static final String TABLE_NAME_KEY = "TABLE_NAME";

	public DiffDatabase(DiffDBConnection connectionInfo_) {
		_connectionInfo = connectionInfo_;
		/* DKValidate.notNull(_connectionInfo, _typeInfoDataAccess, _sqlGenerator,
         _tableDataAccess);*/
		_caseSensitive = _connectionInfo.getFlavor()._caseSensitive;
	}

	public Connection getConnection() throws SQLException {
		try {
			Class.forName(_connectionInfo.getDriverName());
		}
		catch (ClassNotFoundException e_) {
			throw new RuntimeException(e_);
		}
		String jdbcUrl = _connectionInfo.getJDBCUrl();
		_log.debug("jdbcUrl->{}", jdbcUrl);
		Properties properties = new Properties();
		properties.put(USERNAME_KEY, _connectionInfo.getUsername());
		properties.put(PASSWORD_KEY, _connectionInfo.getPassword());
		return DriverManager.getConnection(jdbcUrl, properties);
	}
	
	public List<DKDBTable> getTables(String catalog_, String schema_, String tableName_)
			throws SQLException {
		Connection connection = this.getConnection();
		DatabaseMetaData dbMeta = connection.getMetaData();
		List<Map<String, ?>> tableMaps = this.getTableMaps(catalog_, schema_, tableName_,
				dbMeta);
		if ((tableMaps == null) || (tableMaps.isEmpty()))
			return null;
		List<DKDBTable> tables = new ArrayList<DKDBTable>(tableMaps.size());
		for (Map<String, ?> tableMap : tableMaps) {
			_log.debug("tableMap->{}", tableMap);
			List<Map<String, ?>> columMaps = this.getColumnMaps(tableMap, dbMeta);
			_log.debug("columMaps->{}", columMaps);
			List<Map<String, ?>> pkMaps = this.getPKMaps(tableMap, dbMeta);
			_log.debug("pkMaps->{}", pkMaps);
			DKDBTable table = this.constructTable(tableMap, columMaps, pkMaps);
			_log.debug("table->{}", table);
			tables.add(table);
		}
		this.returnConnection(connection);
		return tables;
	}
	
	private DKDBTable constructTable(Map<String, ?> tableMap_,List<Map<String, ?>> columnMaps_,
			List<Map<String, ?>> pkMaps_) throws SQLException {
		String catalogName = (String) DKMapUtil.getValueForKeyPrefix(tableMap_,
				TABLE_CATALOG_KEY);
		String schemaName = (String) DKMapUtil.getValueForKeyPrefix(tableMap_,
				TABLE_SCHEMA_KEY);
		String tableName = (String) tableMap_.get(TABLE_NAME_KEY);
		_log.debug("catalogName->{}", catalogName);
		_log.debug("schemaName->{}", schemaName);
		_log.debug("tableName->{}", tableName);
		_log.debug("columnMaps_->{}", columnMaps_);
		_log.debug("pkMaps_->{}", pkMaps_);
		DKDBColumn[] columns = ((columnMaps_ == null) || (columnMaps_.isEmpty())) ? null
				: new DKDBColumn[columnMaps_.size()];
		for (int i = 0; i < columnMaps_.size(); i++) {
			columns[i] = this.constructColumn(columnMaps_.get(i));
			_log.debug("i->{} columns[i]->{}", i, columns[i]);
		}
		DKDBPrimaryKey primaryKey = this.constructPrimaryKey(pkMaps_, columns);

		return new DKDBTable(catalogName, schemaName, tableName, columns, primaryKey);
	}
	
	private DKDBPrimaryKey constructPrimaryKey(List<Map<String, ?>> pkMaps_,
			DKDBColumn[] columns_) {
		if (_log.isDebugEnabled()) {
			_log.debug("pkMaps_->{}", pkMaps_);
			_log.debug("columns_->{}", columns_ != null ? Arrays.toString(columns_) : null);
		}
		if ((pkMaps_ == null || (pkMaps_.isEmpty())))
			return null;
		List<Map> pkMaps = new ArrayList<Map>(pkMaps_);
		Comparator<Map> ordinalComparator = (Comparator<Map>) new DKMapKeyValueComparator(
				"KEY_SEQ");
		Collections.sort(pkMaps, ordinalComparator);
		String pkName = (String) pkMaps.get(0).get("PK_NAME");
		_log.debug("pkName->{}", pkName);
		String[] keyColumnNames = new String[pkMaps.size()];
		for (int i = 0; i < pkMaps.size(); i++) {
			Map pkMap = pkMaps.get(i);
			String mapName = (String) pkMap.get("PK_NAME");
			if (!mapName.equals(pkName))
				throw new RuntimeException(String.format("more than one pkName->%s, %s",
						pkName, mapName));
			keyColumnNames[i] = (String) pkMap.get("COLUMN_NAME");
		}
		return new DKDBPrimaryKey(pkName, keyColumnNames);
	}
	
	private DKDBColumn constructColumn(Map<String, ?> columnMap_) throws SQLException {
		_log.debug("columnMap_->{}", columnMap_);
		String tableName = (String) columnMap_.get("COLUMN_NAME");
		Number ordinalPosition = (Number) columnMap_.get("ORDINAL_POSITION");
		String dataTypeName = (String) columnMap_.get("TYPE_NAME");
		dataTypeName = DKDBType.getBaseTypeName(dataTypeName);
		Number columnSize = (Number) columnMap_.get("COLUMN_SIZE");
		Boolean isNullable = DKStringUtil.parseBoolean(
				(String) columnMap_.get("IS_NULLABLE"), Boolean.TRUE);
		return new DKDBColumn(tableName, DKNumberUtil.getInt(ordinalPosition, -1),
				dataTypeName, DKNumberUtil.getInt(columnSize, -1), isNullable);
	}
	
	private List<Map<String, ?>> getTableMaps(String catalog_, String schema_,
			String tableName_, DatabaseMetaData dbMeta_)
					throws SQLException {
		if (this.getFlavor() == DiffDBFlavor.HYPERSQL)
			return this.getTableMapsHyperSQL(catalog_, schema_, tableName_, dbMeta_);
		return this.getTableMapsStandard(catalog_, schema_, tableName_, dbMeta_);
	}
	
	/**
	 * default (normal) implementation of getTableMaps that relies on
	 */
	private List<Map<String, ?>> getTableMapsStandard(String catalog_, String schema_,
			String tableName_,
			DatabaseMetaData dbMeta_)
					throws SQLException {
		_log.debug("catalog_->{}", catalog_);
		_log.debug("schema_->{}", schema_);
		_log.debug("tableName_->{}", tableName_);
		ResultSet tablesRS = dbMeta_.getTables(catalog_, schema_, tableName_, null);
		_log.debug("tablesRS->{}", tablesRS);
		if (tablesRS == null) {
			_log.warn("no tablesRS for catalog_->{} schema_->{} tableName_->{}");
			return null;
		}
		List<Map<String, ?>> tableMaps = DKSqlUtil.readRows(tablesRS, true);
		_log.debug("tableMaps->{}", tableMaps);
		DKSqlUtil.close(tablesRS);
		return tableMaps;
	}
	   
	private List<Map<String, ?>> getColumnMaps(Map<String, ?> tableMap_,
			DatabaseMetaData dbMeta_)
					throws SQLException {
		String catalogName = (String) DKMapUtil.getValueForKeyPrefix(tableMap_,
				TABLE_CATALOG_KEY);
		String schemaName = (String) DKMapUtil.getValueForKeyPrefix(tableMap_,
				TABLE_SCHEMA_KEY);
		String tableName = (String) tableMap_.get(TABLE_NAME_KEY);
		_log.debug("catalogName->{}", catalogName);
		_log.debug("schemaName->{}", schemaName);
		_log.debug("tableName->{}", tableName);

		ResultSet columnsRS = dbMeta_.getColumns(catalogName, schemaName, tableName, null);
		List<Map<String, ?>> columnMaps = DKSqlUtil.readRows(columnsRS);
		_log.debug("columnMaps->{}", columnMaps);
		DKSqlUtil.close(columnsRS);
		return columnMaps;
	}

	private List<Map<String, ?>> getPKMaps(Map<String, ?> tableMap_,
			DatabaseMetaData dbMeta_) throws SQLException {
		String catalogName = (String) DKMapUtil.getValueForKeyPrefix(tableMap_,
				TABLE_CATALOG_KEY);
		String schemaName = (String) DKMapUtil.getValueForKeyPrefix(tableMap_,
				TABLE_SCHEMA_KEY);
		String tableName = (String) tableMap_.get(TABLE_NAME_KEY);
		_log.debug("catalogName->{}", catalogName);
		_log.debug("schemaName->{}", schemaName);
		_log.debug("tableName->{}", tableName);
		ResultSet primaryKeyRS = dbMeta_.getPrimaryKeys(catalogName, schemaName, tableName);
		_log.debug("primaryKeyRS->{}", primaryKeyRS);
		if (primaryKeyRS == null) {
			_log.warn("no primaryKeyRS for catalog_->{} schema_->{} tableName_->{}");
			return null;
		}
		List<Map<String, ?>> pkMaps = DKSqlUtil.readRows(primaryKeyRS, true);
		_log.debug("pkMaps->{}", pkMaps);
		DKSqlUtil.close(primaryKeyRS);
		return pkMaps;
	}
	
	private void returnConnection(Connection connection_) throws SQLException {
	      DKSqlUtil.close(connection_);
	   }
	
	public DiffDBConnection getConnectionInfo() {
		return _connectionInfo;
	}

	public boolean getCaseSensitive() {
		return _caseSensitive;
	}

	/**
	 * convenience that passes through to ConnectionInfo
	 */
	public DiffDBFlavor getFlavor() {
		return _connectionInfo.getFlavor();
	}

	public boolean canConnect() {
		try {
			Connection connection = this.getConnection();
			if (connection == null)
				return false;
			Map<String, ?> dbInfo = DKSqlUtil.getDatabaseInfo(connection);
			if (MapUtils.isEmpty(dbInfo))
				return false;
			if (dbInfo.get(DKSqlUtil.DATABASE_PRODUCT_VERSION_KEY) == null)
				return false;
			return true;
		}
		catch (Exception e_) {
			_log.debug(null, e_);
			return false;
		}
	}

	public boolean executeUpdate(String sql_) throws SQLException {
		Connection connection = this.getConnection();
		boolean update = DKSqlUtil.executeUpdate(sql_, connection);
		DKSqlUtil.close(connection);
		return update;
	}
	
	/**
	 * HyperSQL seems to have some problems with DatabaseMetaData.getTables(),
	 * when only a tableName is specified
	 */
	private List<Map<String, ?>> getTableMapsHyperSQL(String catalog_, String schema_,
			String tableName_,
			DatabaseMetaData dbMeta_)
					throws SQLException {
		_log.debug("catalog_->{}", catalog_);
		_log.debug("schema_->{}", schema_);
		_log.debug("tableName_->{}", tableName_);
		ResultSet tablesRS = dbMeta_.getTables(null, null, null, null);
		_log.debug("tablesRS->{}", tablesRS);
		if (tablesRS == null) {
			_log.warn("no tablesRS for catalog_->{} schema_->{} tableName_->{}");
			return null;
		}
		List<Map<String, ?>> allTableMaps = DKSqlUtil.readRows(tablesRS, true);
		_log.debug("allTableMaps->{}", allTableMaps);
		DKSqlUtil.close(tablesRS);
		List<Map<String, ?>> matchingTableMaps = new ArrayList<Map<String, ?>>();
		for (Map<String, ?> map : allTableMaps) {
			if (catalog_ != null) {
				String catalogName = (String) DKMapUtil.getValueForKeyPrefix(map,
						TABLE_CATALOG_KEY);
				if (!StringUtils.equalsIgnoreCase(catalog_, catalogName))
					continue;
			}
			if (schema_ != null) {
				String schemaName = (String) DKMapUtil.getValueForKeyPrefix(map,
						TABLE_SCHEMA_KEY);
				if (!StringUtils.equalsIgnoreCase(schema_, schemaName))
					continue;
			}
			if (tableName_ != null) {
				String tableName = (String) map.get(TABLE_NAME_KEY);
				if (!StringUtils.equalsIgnoreCase(tableName_, tableName))
					continue;
			}
			matchingTableMaps.add(map);
		}
		return matchingTableMaps;
	}
	   

	public String toString() {
		return _connectionInfo.toString();
	}
}
