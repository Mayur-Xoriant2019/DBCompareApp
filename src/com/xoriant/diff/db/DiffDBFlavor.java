package com.xoriant.diff.db;

import org.diffkit.common.DKValidate;

public enum DiffDBFlavor {

	H2("org.h2.Driver"), MYSQL("com.mysql.jdbc.Driver"), ORACLE(
		      "oracle.jdbc.driver.OracleDriver"), DB2("com.ibm.db2.jcc.DB2Driver"), SQLSERVER(
		      "net.sourceforge.jtds.jdbc.Driver"), POSTGRES("org.postgresql.Driver",
		      true, true), HYPERSQL("org.hsqldb.jdbc.JDBCDriver"), SYBASE("");

   public final String _driverName;
   public final boolean _ignoreUnrecognizedTypes;
   public final boolean _caseSensitive;

   private DiffDBFlavor(String driverName_) {
      this(driverName_, false, false);
   }

   private DiffDBFlavor(String driverName_, boolean ignoreUnrecognizedTypes_,
                      boolean caseSensitive_) {
      _driverName = driverName_;
      _ignoreUnrecognizedTypes = ignoreUnrecognizedTypes_;
      _caseSensitive = caseSensitive_;
      DKValidate.notNull(_driverName);
   }

   /**
    * will simply return null if argument is not recognized, instead of throwing
    */
   public static DiffDBFlavor forName(String name_) {
      if (name_ == null)
         return null;

      try {
         return Enum.valueOf(DiffDBFlavor.class, name_);
      }
      catch (Exception e_) {
         return null;
      }
   }
}
