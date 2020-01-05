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

package uk.ac.ox.cs.diadem.oxpath.output.recstream.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import uk.ac.ox.cs.diadem.oxpath.model.OXPathExtractionNode;
import uk.ac.ox.cs.diadem.oxpath.output.IOutputHandler;
import uk.ac.ox.cs.diadem.oxpath.output.IStreaming;
import uk.ac.ox.cs.diadem.oxpath.output.recstream.RecStreamOutputHandler;
import uk.ac.ox.cs.diadem.oxpath.output.recstream.RecStreamOutputHandler.IFilter;
import uk.ac.ox.cs.diadem.oxpath.output.recstream.Record;
import uk.ac.ox.cs.diadem.oxpath.utils.OXPathRuntimeException;

/**
 * Simple records are streamed into the relational database. 
 * 
 * The schema and the table are automatically created if do not exist. 
 * 
 * @author Ruslan Fayzrakhmanov
 * @author Giovanni Grasso {@literal <gio@oxpath.org>}
 * 16 Feb 2017
 */
public class RecStreamJDBCOutputHandler
	implements IOutputHandler, IStreaming {
//	private static final Logger log = LoggerFactory.getLogger(RecStreamJDBCOutputHandler.class);
	
	public static final char VALUE_SEPARATOR_DEFAULT = '|';
	public static final char ESCAPE_CHAR_DEFAULT = '\\';
	
	private RecStreamOutputHandler oh;
	
	private class StreamJDBCProcessor implements IStreamProcessor<Record> {
		private final Logger log = LoggerFactory.getLogger(StreamJDBCProcessor.class);
		
		private final String driver;
		private final String dbUrl;
		private final String dbUser;
		private final String dbPsw;
		private final String schemaName;
		private final String tableName;
		
		private final boolean override;
		private final int batchSize;
		
		private final char valueSeparator;
		private final char escapeChar;
		
		private Connection con;
		private List<String> columns;
		private PreparedStatement insertStmt = null;
		private int batchCounter;
		
		private boolean autocommitOrig;
		
		private boolean streamClosed = false;
		
		public StreamJDBCProcessor(
				String driver,
				String dbUrl,
				String dbUser,
				String dbPsw,
				String schemaName,
				String tableName,
				
				boolean override,
				int batchSize,
				
				char valueSeparator,
				char escapeChar) {
			this.driver = driver;
			this.dbUrl = dbUrl;
			this.dbUser = dbUser;
			this.dbPsw = dbPsw;
			this.schemaName = schemaName;
			this.tableName = tableName;
			
			this.override = override;
			this.batchSize = (batchSize<1)?1:batchSize;
			
			this.valueSeparator = valueSeparator;
			this.escapeChar = escapeChar;
		}
		
		/**
		 * Create a table if it does not exist
		 * @return return {@literal this} object for cascade invokations.
		 */
		public StreamJDBCProcessor init() {
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
			return this;
		}

		public void process(final Record data) {
			PreparedStatement stmt = getInsertStatement(tableName, columns.size(), con);
			// insert the record into the table
			appendBatch(data);
			if (isEndNodeReceived()) {
				commit(stmt, 1, con);
			} else {
				commit(stmt, con);
			}
		}
		
		private void appendBatch(Record data) {
			try {
				insertStmt.clearParameters();
				int i = 0;
				for (String val: data.flattern(valueSeparator, escapeChar)) {
					if (i==0 && data.isWithId())
						insertStmt.setInt(i+1, Integer.parseInt(val));
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
		
		public void prepareTable(
				List<String> columns,
				boolean includeId) {
			createSchemaIfNotExists(schemaName, con);
			createTableIfNotExists(tableName, columns, includeId, override, con);
			this.columns = columns;
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
			boolean firstItem = true;
			for (String clmn: columns) {
				if (firstItem) {
					if (includeId) {
						sb.append("("+clmn+" integer not null");
					} else {
						sb.append("("+clmn+" text");
					}
				} else {
					sb.append(","+clmn+" text");
				}
				firstItem = false;
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
		
		public void close() {
			if (!streamClosed) {
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
				streamClosed = true;
			}
		}
		
		@Override
		public boolean isClosed() {
			return streamClosed;
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

	/**
	 * @param allowMultipleValuesPerAttribute true: multiple values are allowed per attribute
	 * @param entity the name of the entity
	 * @param attributes attributes to be serialised
	 * @param includeId true: the id is to be included into the result
	 * @param initId initial id used for records
	 * @param valFilter a function for filtering values (can be null)
	 * @param recFilter a function for filtering records (can be null)
	 * @param driver JDBC driver (class)
	 * @param dbUrl JDBC database URL
	 * @param dbUser database user
	 * @param dbPsw database password
	 * @param schemaName schema name
	 * @param tableName table name
	 * @param override true: able will be re-created
	 * @param batchSize minimal number of queries to be committed at once
	 */
	public RecStreamJDBCOutputHandler(
			boolean allowMultipleValuesPerAttribute,
			String entity,
			String[] attributes,
			boolean includeId,
			int initId,
			IFilter<String> valFilter,
			IFilter<Record> recFilter,
			
			String driver,
			String dbUrl,
			String dbUser,
			String dbPsw,
			String schemaName,
			String tableName,
			boolean override,
			int batchSize
			) {
		this(null, allowMultipleValuesPerAttribute, entity, attributes,
				includeId, initId, valFilter, recFilter, VALUE_SEPARATOR_DEFAULT, ESCAPE_CHAR_DEFAULT,
				driver, dbUrl, dbUser, dbPsw, schemaName, tableName, override, batchSize);
	}
	
	/**
	 * @param wrapperId
	 * @param allowMultipleValuesPerAttribute true: multiple values are allowed nullper attribute
	 * @param entity the name of the entity
	 * @param attributes attributes to be serialised
	 * @param includeId true: the id is to be included into the result
	 * @param initId initial id used for records
	 * @param valFilter a function for filtering values (can be null)
	 * @param recFilter a function for filtering records (can be null)
	 * @param driver JDBC driver (class)
	 * @param dbUrl JDBC database URL
	 * @param dbUser database user
	 * @param dbPsw database password
	 * @param schemaName schema name
	 * @param tableName table name
	 * @param override true: able will be re-created
	 * @param batchSize minimal number of queries to be committed at once
	 */
	public RecStreamJDBCOutputHandler(
			String wrapperId,
			boolean allowMultipleValuesPerAttribute,
			String entity,
			String[] attributes,
			boolean includeId,
			int initId,
			IFilter<String> valFilter,
			IFilter<Record> recFilter,
			
			String driver,
			String dbUrl,
			String dbUser,
			String dbPsw,
			String schemaName,
			String tableName,
			boolean override,
			int batchSize
			) {
		this(wrapperId, allowMultipleValuesPerAttribute, entity, attributes,
				includeId, initId, valFilter, recFilter, VALUE_SEPARATOR_DEFAULT, ESCAPE_CHAR_DEFAULT,
				driver, dbUrl, dbUser, dbPsw, schemaName, tableName, override, batchSize);
	}
	
	/**
	 * @param wrapperId
	 * @param allowMultipleValuesPerAttribute true: multiple values are allowed per attribute
	 * @param entity the name of the entity
	 * @param attributes attributes to be serialised
	 * @param includeId true: the id is to be included into the result
	 * @param initId initial id used for records
	 * @param valFilter a function for filtering values (can be null)
	 * @param recFilter a function for filtering records (can be null)
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
	public RecStreamJDBCOutputHandler(
			String wrapperId,
			boolean allowMultipleValuesPerAttribute,
			String entity,
			String[] attributes,
			boolean includeId,
			int initId,
			IFilter<String> valFilter,
			IFilter<Record> recFilter,
			char valueSeparator,
			char escapeChar,
			
			String driver,
			String dbUrl,
			String dbUser,
			String dbPsw,
			String schemaName,
			String tableName,
			boolean override,
			int batchSize
			) {
		this.oh = initOh(wrapperId, allowMultipleValuesPerAttribute,
				entity, attributes, includeId, initId, valFilter, recFilter, valueSeparator, escapeChar,
				driver, dbUrl, dbUser, dbPsw, schemaName, tableName, override, batchSize);
	}
	
	private RecStreamOutputHandler initOh(
			String wrapperId,
			boolean allowMultipleValuesPerAttribute,
			String entity,
			String[] attributes,
			boolean includeId,
			int initId,
			IFilter<String> valFilter,
			IFilter<Record> recFilter,
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
		StreamJDBCProcessor streamJDBCProcessor = new StreamJDBCProcessor(driver, dbUrl, dbUser, dbPsw, schemaName, tableName,
				override, batchSize, valueSeparator, escapeChar).init();
		RecStreamOutputHandler oh = (wrapperId == null)
				? new RecStreamOutputHandler(wrapperId, allowMultipleValuesPerAttribute,
						entity, attributes, streamJDBCProcessor, includeId, initId, valFilter, recFilter)
				: new RecStreamOutputHandler(allowMultipleValuesPerAttribute,
						entity, attributes, streamJDBCProcessor, includeId, initId, valFilter, recFilter);
				streamJDBCProcessor.prepareTable(oh.getAttributes(), includeId);
		return oh;
	}
	
	/**
	 * Get last received record
	 * @return
	 */
	public Record getLastRecord() {
		return oh.getLastRecord();
	}
	/**
	 * Get last received record's ID
	 * @return
	 */
	public Integer getLastRecordId() {
		return oh.getLastRecordId();
	}
	
	public void processNode(OXPathExtractionNode node) {
		oh.processNode(node);
	}

	public void receiveOuterException(Throwable e) {
		oh.receiveOuterException(e);
	}

	public boolean hasOuterException() {
		return oh.hasOuterException();
	}

	public boolean isEndNodeReceived() {
		return oh.isEndNodeReceived();
	}

	public String getWrapperId() {
		return oh.getWrapperId();
	}

	public List<Throwable> getOuterExceptions() {
		return oh.getOuterExceptions();
	}

	public String getOuterExceptionStackTracesAsString() {
		return oh.getOuterExceptionStackTracesAsString();
	}

	@Override
	public IStreamCloser getStreamCloser() {
		return oh==null?null:oh.getStreamCloser();
	}
}
