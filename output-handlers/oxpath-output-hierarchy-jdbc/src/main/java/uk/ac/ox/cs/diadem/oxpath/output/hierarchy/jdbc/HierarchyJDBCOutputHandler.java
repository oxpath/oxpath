/*
 * Copyright (c) 2017, OXPath Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the OXPath team nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL OXPath Team BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package uk.ac.ox.cs.diadem.oxpath.output.hierarchy.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import uk.ac.ox.cs.diadem.oxpath.output.ISerializable;
import uk.ac.ox.cs.diadem.oxpath.output.relation.RelationModel;
import uk.ac.ox.cs.diadem.oxpath.output.relation.RelationOutputHandler;
import uk.ac.ox.cs.diadem.oxpath.utils.OXPathRuntimeException;

/**
 * Serialisation of the tree of entities from the OXPath result tree into one denormalised relation into the database.
 * 
 * 
 * @author Ruslan Fayzrakhmanov
 * 18 Feb 2017
 */
public class HierarchyJDBCOutputHandler
	extends RelationOutputHandler
	implements ISerializable<RelationModel> {
	private static final Logger log = LoggerFactory.getLogger(HierarchyJDBCOutputHandler.class);
	
	public static final char PATH_SEPARATOR_DEFAULT = '_';
	public static final char VALUE_SEPARATOR_DEFAULT = '|';
	public static final char ESCAPE_CHAR_DEFAULT = '\\';
	
	private final String driver;
	private final String dbUrl;
	private final String dbUser;
	private final String dbPsw;
	private final String schemaName;
	private final String tableName;
	
	private final boolean override;
	private final int batchSize;
	
	private final char pathSeparator;
	private final char valueSeparator;
	private final char escapeChar;

	/**
	 * @param allowMultipleValuesPerAttribute true: multiple values are allowed per attribute
	 * @param entityRelPathList array of paths of entities to be identified in the extraction tree
	 * @param includeId true: ids corresponding to entities are incorporated into the model
	 * @param includeRecordLabel true: the name of the record nesting attributes and child entities is included into the path name.
	 * @param valFilter a function for filtering values (can be null)
	 * @param rowFilter a function for filtering records (can be null)
	 * @param driver JDBC driver (class)
	 * @param dbUrl JDBC database URL
	 * @param dbUser database user
	 * @param dbPsw database password
	 * @param schemaName schema name
	 * @param tableName table name
	 * @param override true: able will be re-created
	 * @param batchSize minimal number of queries to be committed at once
	 */
	public HierarchyJDBCOutputHandler(
			boolean allowMultipleValuesPerAttribute,
			String[][] entityRelPathList,
			boolean includeId,
			boolean includeRecordLabel,
			IFilter<String> valFilter,
			IFilter<List<List<String>>> rowFilter,
			
			String driver,
			String dbUrl,
			String dbUser,
			String dbPsw,
			String schemaName,
			String tableName,
			boolean override,
			int batchSize) {
		this(allowMultipleValuesPerAttribute,
				entityRelPathList, includeId, includeRecordLabel, valFilter, rowFilter,
				PATH_SEPARATOR_DEFAULT, VALUE_SEPARATOR_DEFAULT, ESCAPE_CHAR_DEFAULT,
				driver, dbUrl, dbUser, dbPsw, schemaName, tableName, override, batchSize);
	}
	
	/**
	 * @param allowMultipleValuesPerAttribute true: multiple values are allowed per attribute
	 * @param entityRelPathList array of paths of entities to be identified in the extraction tree
	 * @param includeId true: ids corresponding to entities are incorporated into the model
	 * @param includeRecordLabel true: the name of the record nesting attributes and child entities is included into the path name.
	 * @param valFilter a function for filtering values (can be null)
	 * @param rowFilter a function for filtering records (can be null)
	 * @param pathSeparator character to join components of the path denoting the entity's attribute
	 * @param valueSeparator character to join values of the entity's attribute
	 * @param escapeChar escape character used in string join operation
	 * @param driver JDBC driver (class)
	 * @param dbUrl JDBC database URL
	 * @param dbUser database user
	 * @param dbPsw database password
	 * @param schemaName schema name
	 * @param tableName table name
	 * @param override true: able will be re-created
	 * @param batchSize minimal number of queries to be committed at once
	 */
	public HierarchyJDBCOutputHandler(
			boolean allowMultipleValuesPerAttribute,
			String[][] entityRelPathList,
			boolean includeId,
			boolean includeRecordLabel,
			IFilter<String> valFilter,
			IFilter<List<List<String>>> rowFilter,
			char pathSeparator,
			char valueSeparator,
			char escapeChar,
			
			String driver,
			String dbUrl,
			String dbUser,
			String dbPsw,
			String schemaName,
			String tableName,
			boolean override,
			int batchSize) {
		super(allowMultipleValuesPerAttribute,
				entityRelPathList, includeId, includeRecordLabel, valFilter, rowFilter);
		this.driver = driver;
		this.dbUrl = dbUrl;
		this.dbUser = dbUser;
		this.dbPsw = dbPsw;
		this.schemaName = schemaName;
		this.tableName = tableName;
		
		this.override = override;
		this.batchSize = (batchSize<1)?1:batchSize;
		
		this.pathSeparator = pathSeparator;
		this.valueSeparator = valueSeparator;
		this.escapeChar = escapeChar;
	}
	
	/**
	 * @param wrapperId
	 * @param allowMultipleValuesPerAttribute true: multiple values are allowed per attribute
	 * @param entityRelPathList array of paths of entities to be identified in the extraction tree
	 * @param includeId true: ids corresponding to entities are incorporated into the model
	 * @param includeRecordLabel true: the name of the record nesting attributes and child entities is included into the path name.
	 * @param valFilter a function for filtering values (can be null)
	 * @param rowFilter a function for filtering records (can be null)
	 * @param pathSeparator character to join components of the path denoting the entity's attribute
	 * @param valueSeparator character to join values of the entity's attribute
	 * @param escapeChar escape character used in string join operation
	 * @param driver JDBC driver (class)
	 * @param dbUrl JDBC database URL
	 * @param dbUser database user
	 * @param dbPsw database password
	 * @param schemaName schema name
	 * @param tableName table name
	 * @param override true: able will be re-created
	 * @param batchSize minimal number of queries to be committed at once
	 */
	public HierarchyJDBCOutputHandler(
			String wrapperId,
			boolean allowMultipleValuesPerAttribute,
			String[][] entityRelPathList,
			boolean includeId,
			boolean includeRecordLabel,
			IFilter<String> valFilter,
			IFilter<List<List<String>>> rowFilter,
			char pathSeparator,
			char valueSeparator,
			char escapeChar,
			
			String driver,
			String dbUrl,
			String dbUser,
			String dbPsw,
			String schemaName,
			String tableName,
			boolean override,
			int batchSize) {
		super(wrapperId, allowMultipleValuesPerAttribute,
				entityRelPathList, includeId, includeRecordLabel, valFilter, rowFilter);
		this.driver = driver;
		this.dbUrl = dbUrl;
		this.dbUser = dbUser;
		this.dbPsw = dbPsw;
		this.schemaName = schemaName;
		this.tableName = tableName;
		
		this.override = override;
		this.batchSize = (batchSize<1)?1:batchSize;
		
		this.pathSeparator = pathSeparator;
		this.valueSeparator = valueSeparator;
		this.escapeChar = escapeChar;
	}

	private Connection con;
	private boolean autocommitOrig;
	private PreparedStatement insertStmt = null;
	private int batchCounter;
	
	@Override
	public void serialize() {
		RelationModel m = getAccumulativeOutput();
		init();
		List<String> columns = m.flatternColumnNamePaths(pathSeparator, escapeChar);
		prepareTable(columns, includeId, m.getEntityIndexes());
		PreparedStatement stmt = getInsertStatement(tableName, columns.size(), con);
		for (List<String> row: m.flatternRows(valueSeparator, escapeChar)) {
			appendBatch(row, includeId, m.getEntityIndexes());
			commit(stmt, con);
		}
		if (batchCounter>0)
			commit(stmt, 1, con);
		close();
	}
	
	/**
	 * Create a table if it does not exist
	 * @return return {@literal this} object for cascade invokations.
	 */
	private void init() {
		try {
		      Class.forName(driver);
		      con = DriverManager.getConnection(dbUrl, dbUser, dbPsw);
		      autocommitOrig = con.getAutoCommit();
	    	  con.setAutoCommit(false);
		    } catch (final ClassNotFoundException e) {
		      throw new RuntimeException("Can't load jdbc driver", e);
		    } catch (SQLException e) {
		    	processSQLException2("cannot get the connection to the DB", e, con);
			}
	}
	
	private void appendBatch(List<String> data, boolean withId, List<Integer> entityIndexes) {
		try {
			insertStmt.clearParameters();
			int i = 0, j=0;
			for (String val: data) {
				if (withId && j<entityIndexes.size() && i == entityIndexes.get(j)) {
					insertStmt.setInt(i+1, Integer.parseInt(val));
					j++;
				}
				else
					insertStmt.setString(i+1, val);
				i++;
			}
			insertStmt.addBatch();
			batchCounter++;
		} catch (SQLException e) {
			processSQLException2("error in preparing the insert statement", e, con);
		}
	}
	
	private boolean commit(PreparedStatement stat, Connection con) {
		return commit(stat, batchSize, con);
	}
	private boolean commit(PreparedStatement stat, int batchSize, Connection con) {
	    try {
	      if (batchCounter < batchSize)
	        return false;
	      final int[] rc = stat.executeBatch();
	      int rowsAffected = 0;
	      boolean success = true;
    	  for (final int element : rc) {
    		  	if (element>0)
    		  		rowsAffected += element;
		        if (element <=0 && element != PreparedStatement.SUCCESS_NO_INFO) {
		        	success = false;
		        }
		      }
	      if (log.isDebugEnabled()) log.debug("Update affected {} rows", rowsAffected);
	      if (!success) {
	        log.error("Error committing new tuple, no rows affected");
	        throw new SQLException("Error committing new tuple, no rows affected");
	      }
	      con.commit(); // commit transaction
	      stat.clearBatch();
	      // reset counter and batch
	      batchCounter = 0;
	      return true;
	    } catch (final SQLException e) {
	    	processSQLException1("Problem in executing batch insert", e, con);
	    }
	    return false;
	}
	
	private void prepareTable(
			List<String> columns,
			boolean includeId,
			List<Integer> entityIndexes) {
		createSchemaIfNotExists(schemaName, con);
		createTableIfNotExists(tableName, columns, includeId, entityIndexes, override, con);
	}
	
	private void createSchemaIfNotExists(String schemaName, Connection con) {
		try {
			con.createStatement().executeUpdate("create schema if not exists "+schemaName);
			con.createStatement().executeUpdate("set search_path to " + schemaName);
			con.commit();
		} catch (SQLException e) {
			processSQLException1("error creating a schema", e, con);
		}
	}
	
	private void createTableIfNotExists(
			String tableName,
			List<String> columns,
			boolean includeId,
			List<Integer> entityIndexes,
			boolean override,
			Connection con) {
		if (override) {
			try {
				con.createStatement().executeUpdate("drop table if exists " + tableName);
				con.commit();
			} catch (SQLException e) {
				processSQLException1("cannot drop the table "+tableName, e, con);
			}
		}
		
		final StringBuilder sb = new StringBuilder();
		sb.append("create table if not exists " + tableName);
		int i = 0, j=0;
		for (String clmn: columns) {
			if (i==0) {
				sb.append("(");
			} else {
				sb.append(",");
			}
			if (includeId && j<entityIndexes.size() && i == entityIndexes.get(j)) {
				sb.append(clmn+" integer not null");
				j++;
			} else {
				sb.append(clmn+" text");
			}
			i++;
		}
		sb.append(")");
		try {
			con.createStatement().executeUpdate(sb.toString());
			con.commit();
		} catch (SQLException e) {
			processSQLException1("error creating a table", e, con);
		}
	}
	
	private PreparedStatement getInsertStatement(String tableName, int columnsNum, Connection con) {
		if (insertStmt == null) {
			String q = Strings.repeat("?,", columnsNum);
			q = q.substring(0, q.length()-1);
			try {
				insertStmt = con.prepareStatement("insert into "+tableName+" values ("+q+")");
			} catch (SQLException e) {
				processSQLException2("cannot create the insert statement", e, con);
			}
		}
		return insertStmt;
	}
	
	private void close() {
		try {
		      if ((con != null) && !con.isClosed() && !con.getAutoCommit()) {
		    	  con.commit();
		        // restore the autocommit
		    	  con.setAutoCommit(autocommitOrig);
		      }
		      if ((insertStmt != null) && !insertStmt.isClosed()) {
		    	  insertStmt.close();
		      }
		} catch (final SQLException e) {
	    	processSQLException1("Database error in committing or closing the statement.", e, con);
		}
		try {
		      if (!con.isClosed()) {
		    	  con.close();
		      }
	    } catch (final SQLException e) {
		    	processSQLException2("Database error in committing or closing the connection.", e, con);
		}
	}
	
	private void processSQLException1(String text, SQLException ex, Connection con) {
		try {
	        if (con != null) {
	          if (!con.getAutoCommit()) {
	        	  con.rollback();
	        	  log.debug("performed rollback");
	          }
	          con.close();
	        }
	        throw new OXPathRuntimeException(text, ex, log);
	      } catch (final SQLException ex2) {
	        throw new OXPathRuntimeException("error performing the rollback", ex2, log);
	      }
	}
	
	private void processSQLException2(String text, SQLException ex, Connection con) {
		try {
	        if (con != null) {
	          con.close();
	        }
	        throw new OXPathRuntimeException(text, ex, log);
	      } catch (final SQLException ex2) {
	        throw new OXPathRuntimeException("error clsing the connection", ex2, log);
	      }
	}
	
	
	
}
