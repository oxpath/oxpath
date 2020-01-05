/*
 * Copyright (c) 2016, OXPath Team
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

package uk.ac.ox.cs.diadem.oxpath.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration2.BaseHierarchicalConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

import uk.ac.ox.cs.diadem.oxpath.core.OXPath;
import uk.ac.ox.cs.diadem.oxpath.output.IOutputHandler;
import uk.ac.ox.cs.diadem.oxpath.output.ISerializable;
import uk.ac.ox.cs.diadem.oxpath.output.IStreaming;
import uk.ac.ox.cs.diadem.oxpath.output.IStreaming.IStreamCloser;
import uk.ac.ox.cs.diadem.oxpath.output.IStringSerializable;
import uk.ac.ox.cs.diadem.oxpath.output.hierarchy.csv.HierarchyCSVOutputHandler;
import uk.ac.ox.cs.diadem.oxpath.output.hierarchy.jdbc.HierarchyJDBCOutputHandler;
import uk.ac.ox.cs.diadem.oxpath.output.json.JsonOutputHandler;
import uk.ac.ox.cs.diadem.oxpath.output.recstream.csv.RecStreamCSVOutputHandler;
import uk.ac.ox.cs.diadem.oxpath.output.recstream.jdbc.RecStreamJDBCOutputHandler;
import uk.ac.ox.cs.diadem.oxpath.output.xml.XMLOutputHandler;
import uk.ac.ox.cs.diadem.oxpath.utils.OXPathRuntimeException;
import uk.ac.ox.cs.diadem.util.configuration.ConfigurationObject;
import uk.ac.ox.cs.diadem.util.configuration.ConfigurationObjectFactory;
import uk.ac.ox.cs.diadem.webapi.WebBrowser;
import uk.ac.ox.cs.diadem.webapi.WebBrowserBuilder;
import uk.ac.ox.cs.diadem.webapi.WebBrowserBuilder.ECombineConfigurationMode;
import uk.ac.ox.cs.diadem.webapi.configuration.RunConfiguration;

/**
 * It implements the command line interface.
 * @author Ruslan Fayzrakhmanov
 * 29 Nov 2016
 */
public class OXPathCLI {
	private static final Logger log = LoggerFactory.getLogger(OXPathCLI.class);
	
	public static final String DEFAULT_LOG4J_CONFIG = "/uk/ac/ox/cs/diadem/oxpath/cli/defaultLog4j.properties";
	public static final String CLI_CONFIG_FILE_RELATIVE_PATH = "/uk/ac/ox/cs/diadem/oxpath/cli/config.xml";
	
	public static final boolean JDBC_OVERRIDE_DEFAULT=false;
	public static final int JDBC_BATCHSIZE_DEFAULT=20;
	
	/**
	 * Different output formats
	 */
	private enum EOutputFormat {
		XML("xml"),
		JSON("json"),
		RSCSV("rscsv"),
		RSJDBC("rsjdbc"),
		HCSV("hcsv"),
		HJDBC("hjdbc"),
		;
		private final String value;
		private EOutputFormat(String value) {
			this.value = value;
		}
		public static EOutputFormat parse(String val) {
			for (EOutputFormat of: EOutputFormat.values()) {
				if (of.value.equalsIgnoreCase(val))
					return of;
			}
			return null;
		}
	}
	
	/**
	 * JDBC parameters for the database
	 */
	private static class JdbcParams {
		String driver;
		String dbUrl;
		String dbUser;
		String dbPsw;
		String schemaName;
		String tableName;
		boolean override;
		int batchSize;
		
		public static JdbcParams parse(String confFilePath) {
			BaseHierarchicalConfiguration config = new ConfigurationObjectFactory()
					.create(new File(confFilePath))
					.getConfiguration();
			JdbcParams p = new JdbcParams();
			p.driver = config.getString("db/driver");
			p.dbUrl = config.getString("db/url");
			p.dbUser = config.getString("db/user");
			p.dbPsw = config.getString("db/password");
			p.schemaName = config.getString("db/schema/schema-name", "public");
			p.tableName = config.getString("db/schema/table-name");
			p.override = config.getBoolean("db/override", JDBC_OVERRIDE_DEFAULT);
			p.batchSize = config.getInt("db/batch-size", JDBC_BATCHSIZE_DEFAULT);
			return p;
		}
	}
	
	/**
	 * Class for the attributes to be serialised for the basic record streaming.
	 */
	private static class RecStreamAttributes {
		String[] attributes = null;
		/**
		 * @param val is of the form "a,b,c"
		 * @return
		 */
		public static RecStreamAttributes parse(String val) {
			RecStreamAttributes a = new RecStreamAttributes();
			if (val!=null) {
				a.attributes = val.split("\\s*,\\s*");
			}
			return a;
		}
	}
	
	/**
	 * Entity paths for the serialisation of the hierarchy.
	 */
	private static class HierarchyEntities {
		String[][] entities = null;
		/**
		 * @param val is of the form "a/b/c,d/e"
		 * @return
		 */
		public static HierarchyEntities parse(String val) {
			HierarchyEntities a = new HierarchyEntities();
			if (val!=null) {
				String[] paths = val.split("\\s*,\\s*");
				a.entities = new String[paths.length][];
				for (int i=0; i<paths.length; i++) {
					a.entities[i] = paths[i].split("\\s*/\\s*");
				}
			}
			return a;
		}
	}
	
	private enum EOption {
		HELP("h", null, "cli/help-desc", null, Boolean.class),
		VERSION("v", null, "cli/version-desc", null, Boolean.class),
		QUERY("q", null, "cli/query-desc", "cli/query-valdesc", String.class),
		
		VARIABLES("var", null, "cli/variables-desc", "cli/variables-valdesc", String.class),
		
		OUTPUT_FORMAT("f", "cli/output-format-default", "cli/output-format-desc", "cli/output-format-valdesc", EOutputFormat.class),
		
		XML_CDATA("xmlcd", "cli/xmlcdata-default", "cli/xmlcdata-desc", null, Boolean.class),
		JSON_ARR("jsonarr", "cli/jsonarr-default", "cli/jsonarr-desc", null, Boolean.class),
		
		JDBC_PARAMS("jdbcpar", null, "cli/jdbcpar-desc", "cli/jdbcpar-valdesc", JdbcParams.class),
		
		RS_ENTITY("rsent", null, "cli/rsent-desc", "cli/rsent-valdesc", String.class),
		RS_ATTRS("rsattrs", null, "cli/rsattrs-desc", "cli/rsattrs-valdesc", RecStreamAttributes.class),
		
		H_ENTITIES("hents", null, "cli/hents-desc", "cli/hents-valdesc", HierarchyEntities.class),
		
		MULTIVAL("mval", "cli/multival-default", "cli/multival-desc", null, Boolean.class),
		OUTPUT("o", null, "cli/output-desc", "cli/output-valdesc", String.class),
		
		CONFIGURATION_FILE("conf", null, "cli/conffile-desc", "cli/conffile-valdesc", String.class),
		
		LOG_FILE("log", null, "cli/logfile-desc", "cli/logfile-valdesc", String.class),
		
		EXEC("exe", null, "cli/browser/exec-desc", "cli/browser/exec-valdesc", String.class),
		XVFB("xvfb", "cli/browser/xvfb-default", "cli/browser/xvfb-desc", null, Boolean.class),
		AUTOCOMPLETE("autocomplete", "cli/engine/autocomplete-default", "cli/engine/autocomplete-desc", null, Boolean.class),
		DISPLAY("d", "cli/browser/display-default", "cli/browser/display-desc", "cli/browser/display-valdesc", String.class),
		
		IMG("img", "cli/browser/img-default", "cli/browser/img-desc", null, Boolean.class),
		PLUGINS("pl", "cli/browser/plugins-default", "cli/browser/plugins-desc", null, Boolean.class),
		LATITUDE("lat", null, "cli/browser/lat-desc", "cli/browser/lat-valdesc", Double.class),
		LONGITUDE("lon", null, "cli/browser/lon-desc", "cli/ browser/lon-valdesc", Double.class),
		
		WIDTH("wh", "cli/browser/width-default", "cli/browser/width-desc", "cli/browser/width-valdesc", Integer.class),
		HEIGHT("ht", "cli/browser/height-default", "cli/browser/height-desc", "cli/browser/width-valdesc", Integer.class);
		
		private final String name;
		public String getName() {
			return name;
		}
		private final String defValConfPath;
		public String getDefValConfPath() {
			return defValConfPath;
		}
		private final String descrConfPath;
		public String getDescrConfPath() {
			return descrConfPath;
		}
		private final String descrValuePath;
		public String getDescrValuePath() {
			return descrValuePath;
		}
		private final Class<?> valClass;
		public Class<?> getValClass() {
			return valClass;
		}
		private EOption(String name
				, String defValConfPath
				, String descrConfPath
				, String descrValuePath
				, Class<?> valClass) {
			this.name = name;
			this.defValConfPath = defValConfPath;
			this.descrConfPath = descrConfPath;
			this.descrValuePath = descrValuePath;
			this.valClass = valClass;
		}
		//private static Object getValue(EOption optType, CommandLine line, Configuration conf) {
		private static Object getValue(EOption optType, CommandLine line, BaseHierarchicalConfiguration conf) {
			if (line.hasOption(optType.getName())) {
				String val = line.getOptionValue(optType.getName());
				if (Boolean.class.equals(optType.getValClass())) {
					return (val==null)?true:Boolean.parseBoolean(val);
				} else if (Integer.class.equals(optType.getValClass())) {
					return Integer.parseInt(val);
				} else if (Double.class.equals(optType.getValClass())) {
					return Double.parseDouble(val);
				} else if (String.class.equals(optType.getValClass())) {
					return val;
				} else if (EOutputFormat.class.equals(optType.getValClass())) {
					return EOutputFormat.parse(val);
				} else if (JdbcParams.class.equals(optType.getValClass())) {
					return JdbcParams.parse(val);
				} else if (RecStreamAttributes.class.equals(optType.getValClass())) {
					return RecStreamAttributes.parse(val);
				} else if (HierarchyEntities.class.equals(optType.getValClass())) {
					return HierarchyEntities.parse(val);
				} else {
					throw new RuntimeException("Class "+optType.getValClass().getName()+" cannot be type casted.");
				}
				// Get default values
			} else if (optType.getDefValConfPath() != null
					&& conf.containsKey(optType.getDefValConfPath())) {
				if (Boolean.class.equals(optType.getValClass())) {
					return conf.getBoolean(optType.getDefValConfPath());
				} else if (Integer.class.equals(optType.getValClass())) {
					return conf.getInt(optType.getDefValConfPath());
				} else if (Double.class.equals(optType.getValClass())) {
					return conf.getDouble(optType.getDefValConfPath());
				} else if (String.class.equals(optType.getValClass())) {
					return conf.getString(optType.getDefValConfPath());
				} else if (EOutputFormat.class.equals(optType.getValClass())) {
					return EOutputFormat.parse(conf.getString(optType.getDefValConfPath()));
				} else {
					throw new RuntimeException("Class "+optType.getValClass().getName()+" cannot be type casted.");
				}
			}
			return null;
		}
		//public static Map<EOption, Object> getValueMap(CommandLine line, Configuration conf) {
		public static Map<EOption, Object> getValueMap(CommandLine line, BaseHierarchicalConfiguration conf) {
			Map<EOption, Object> map = new HashMap<EOption, Object>();
			for (EOption opt: EOption.values()) {
				map.put(opt, EOption.getValue(opt, line, conf));
			}
			return map;
		}
	}
	
	private enum ECmd {
		HELP,
		VERSION,
		RUN;
		public static ECmd classifyCmd(CommandLine cl) {
			if (isHelp(cl)) {
				return HELP;
			} else if (isVersion(cl)){
				return VERSION;
			} else if (isRun(cl)) {
				return RUN;
			}
			return HELP;
		}
		private static boolean isHelp(CommandLine cl) {
			return cl.hasOption(EOption.HELP.getName());
		}
		private static boolean isVersion(CommandLine cl) {
			return cl.hasOption(EOption.VERSION.getName());
		}
		private static boolean isRun(CommandLine cl) {
			return cl.hasOption(EOption.QUERY.getName());
		}
	}
	
	private boolean processHelp(final Options options, BaseHierarchicalConfiguration conf) {
		final HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(conf.getString("cli/usage") + "\n\n", options);
		return true;
	}
	
	private boolean processVersion(BaseHierarchicalConfiguration conf) {
		System.out.println(conf.getString("version"));
		return true;
	}
	
	private boolean processQuery(Map<EOption, Object> options, ConfigurationObject configuration) {
		boolean success = false;
		IOutputHandler oh = null;
		try {
			WebBrowserBuilder builder = new WebBrowserBuilder(configuration);
			RunConfiguration runcofig = builder.getRunConfiguration();
		
		    if (options.get(EOption.EXEC) != null)
		    	runcofig.setUserExecutable((String)options.get(EOption.EXEC));
		    if (options.get(EOption.XVFB) != null)
		    	runcofig.setXvfbMode((boolean)options.get(EOption.XVFB));
	    	if (options.get(EOption.DISPLAY) != null)
		    	runcofig.setDisplayNumber((String)options.get(EOption.DISPLAY));
		    if (!(boolean)options.get(EOption.IMG)) {
		    	runcofig.setDisabledContentTypes(WebBrowser.ContentType.IMAGE);
		    }
		    runcofig.setEnablePlugins((boolean)options.get(EOption.PLUGINS));
		    if (options.get(EOption.LATITUDE) != null
		    		&& options.get(EOption.LONGITUDE) != null) {
		    	
		    	runcofig.setGeoLocation(
			    		(double)options.get(EOption.LATITUDE),
			    		(double)options.get(EOption.LONGITUDE));
		    }
		    runcofig.setAutoPosition(false);
		    runcofig.setBrowserWidth((int)options.get(EOption.WIDTH));
		    runcofig.setBrowserHeight((int)options.get(EOption.HEIGHT));
		    
log.info("Read OXPath query");
		    String query = FileUtils.readFileToString(
		    		new File((String)options.get(EOption.QUERY)), StandardCharsets.UTF_8.name());
		    if (options.get(EOption.VARIABLES) != null) {		    	
		    	File file = new File((String)options.get(EOption.VARIABLES));
		    	Properties properties = new Properties();
				try (FileInputStream fileInput = new FileInputStream(file)) {
					properties.load(fileInput);
				}
				StrSubstitutor substitutor = new StrSubstitutor(Maps.fromProperties(properties));
				query = substitutor.replace(query);
		    }
log.info("Instantiate output handler");		    
		    oh = getOutputHandler(options);
		    
		    if (options.get(EOption.AUTOCOMPLETE) != null) {
		    	OXPath.ENGINE.getOptions().setAutocompleteReaction((Boolean)options.get(EOption.AUTOCOMPLETE));
		    }
	    
log.info("START: The oxpath evaluation");
	      // invoke OXPath
	      OXPath.ENGINE.evaluate(query, builder, oh);
log.info("DONE: The oxpath evaluation");

			success = outputResults(oh, options);
			
	    } catch (IOException e) {
			e.printStackTrace();
		} catch (OXPathRuntimeException e) {
			e.printStackTrace();
		} finally {
			if (oh !=null && oh instanceof IStreaming) {
				IStreamCloser sc = ((IStreaming)oh).getStreamCloser();
				if (sc !=null && !sc.isClosed())
					sc.close();
			}
	    }
		return success;
	}
	
	private IOutputHandler getOutputHandler(Map<EOption, Object> options) {
		IOutputHandler oh = null;
		switch ((EOutputFormat)options.get(EOption.OUTPUT_FORMAT)) {
			case XML:
				oh = new XMLOutputHandler(
						(boolean)options.get(EOption.MULTIVAL)
						, (boolean)options.get(EOption.XML_CDATA)
						, true);
				break;
			case JSON:
				oh = new JsonOutputHandler(
						(boolean)options.get(EOption.MULTIVAL)
						, (boolean)options.get(EOption.JSON_ARR)
						, true);
				break;
			case RSCSV:
				FileWriter fw=null;
				try {
					fw = new FileWriter(new File((String)options.get(EOption.OUTPUT)));
				} catch (IOException e) {
					throw new RuntimeException("Cannot write into the file "+options.get(EOption.OUTPUT));
				}
				oh = new RecStreamCSVOutputHandler(
						(boolean)options.get(EOption.MULTIVAL)
						, (String)options.get(EOption.RS_ENTITY)
						, ((RecStreamAttributes)options.get(EOption.RS_ATTRS)).attributes
						, fw, true, 0, true, null, null);
				break;
			case RSJDBC:
				JdbcParams jdbcParams = ((JdbcParams)options.get(EOption.JDBC_PARAMS));
				oh = new RecStreamJDBCOutputHandler(
						(boolean)options.get(EOption.MULTIVAL)
						, (String)options.get(EOption.RS_ENTITY)
						, ((RecStreamAttributes)options.get(EOption.RS_ATTRS)).attributes
						, true, 0, null, null
						, jdbcParams.driver, jdbcParams.dbUrl, jdbcParams.dbUser, jdbcParams.dbPsw, jdbcParams.schemaName, jdbcParams.tableName
						, jdbcParams.override, jdbcParams.batchSize);
				break;
			case HCSV:
				oh = new HierarchyCSVOutputHandler(
						(boolean)options.get(EOption.MULTIVAL)
						, ((HierarchyEntities)options.get(EOption.H_ENTITIES)).entities
						, true, true, null, null);
				break;
			case HJDBC:
				jdbcParams = ((JdbcParams)options.get(EOption.JDBC_PARAMS));
				oh = new HierarchyJDBCOutputHandler(
						(boolean)options.get(EOption.MULTIVAL)
						, ((HierarchyEntities)options.get(EOption.H_ENTITIES)).entities
						, true, true, null, null
						, jdbcParams.driver, jdbcParams.dbUrl, jdbcParams.dbUser, jdbcParams.dbPsw, jdbcParams.schemaName, jdbcParams.tableName
						, jdbcParams.override, jdbcParams.batchSize);
				break;
			default:
				throw new RuntimeException("Output handler isn't defined.");
		}
		return oh;
	}
	
	private boolean outputResults(IOutputHandler oh, Map<EOption, Object> options) {
		boolean success = false;
		if (!oh.isEndNodeReceived())
			throw new RuntimeException("Only a fraction of the data has been received by the output handler");
		EOutputFormat of = (EOutputFormat)options.get(EOption.OUTPUT_FORMAT);
		// string-serializable output
		if (of == EOutputFormat.XML || of == EOutputFormat.JSON || of == EOutputFormat.HCSV) {
			if (oh instanceof IStringSerializable) {
				String output = ((IStringSerializable<?>)oh).asString();
				if (options.get(EOption.OUTPUT) == null) {
					System.out.println(output);
				} else { try {
							FileUtils.write(
									new File((String)options.get(EOption.OUTPUT))
									, output
									, StandardCharsets.UTF_8.name());
							success = true;
					} catch (IOException e) {
						e.printStackTrace();
					};
				}
				
			} else
				throw new RuntimeException("Output handler should implement interface "+IStringSerializable.class.getName());
		} else if (of == EOutputFormat.HJDBC) {
			if (oh instanceof ISerializable) {
				((ISerializable<?>)oh).serialize();
			} else
				throw new RuntimeException("Output handler should implement interface "+ISerializable.class.getName());
		}
		return success;
	}

	/**
	 * Build options for the command line
	 * 
	 * @param conf a configuration with all descriptions of the parameters
	 * @return options which can be used for parsing the command line.
	 */
	private Options buildCliOptions(final BaseHierarchicalConfiguration conf) {
		final Options options = new Options();
	    options.addOption(EOption.HELP.getName(), false, conf.getString(EOption.HELP.getDescrConfPath()));
	    options.addOption(EOption.VERSION.getName(), false, conf.getString(EOption.VERSION.getDescrConfPath()));
	    options.addOption(Option.builder(EOption.QUERY.getName())
		    	.required(false)
		    	.hasArg().argName(conf.getString(EOption.QUERY.getDescrValuePath())).type(String.class).valueSeparator()
		    	.desc(conf.getString(EOption.QUERY.getDescrConfPath()))
		    	.build());
	    
	    options.addOption(Option.builder(EOption.VARIABLES.getName())
		    	.required(false)
		    	.hasArg().argName(conf.getString(EOption.VARIABLES.getDescrValuePath())).type(String.class).valueSeparator()
		    	.desc(conf.getString(EOption.VARIABLES.getDescrConfPath()))
		    	.build());
	    
	    options.addOption(Option.builder(EOption.OUTPUT_FORMAT.getName())
		    	.required(false)
		    	.hasArg().argName(conf.getString(EOption.OUTPUT_FORMAT.getDescrValuePath())).type(String.class).valueSeparator()
		    	.desc(conf.getString(EOption.OUTPUT_FORMAT.getDescrConfPath()))
		    	.build());
	    
	    options.addOption(EOption.XML_CDATA.getName(), false, conf.getString(EOption.XML_CDATA.getDescrConfPath()));
	    options.addOption(EOption.JSON_ARR.getName(), false, conf.getString(EOption.JSON_ARR.getDescrConfPath()));
	    
	    options.addOption(Option.builder(EOption.JDBC_PARAMS.getName())
		    	.required(false)
		    	.hasArg().argName(conf.getString(EOption.JDBC_PARAMS.getDescrValuePath())).type(String.class).valueSeparator()
		    	.desc(conf.getString(EOption.JDBC_PARAMS.getDescrConfPath()))
		    	.build());
	    options.addOption(Option.builder(EOption.RS_ENTITY.getName())
		    	.required(false)
		    	.hasArg().argName(conf.getString(EOption.RS_ENTITY.getDescrValuePath())).type(String.class).valueSeparator()
		    	.desc(conf.getString(EOption.RS_ENTITY.getDescrConfPath()))
		    	.build());
	    options.addOption(Option.builder(EOption.RS_ATTRS.getName())
		    	.required(false)
		    	.hasArg().argName(conf.getString(EOption.RS_ATTRS.getDescrValuePath())).type(String.class).valueSeparator()
		    	.desc(conf.getString(EOption.RS_ATTRS.getDescrConfPath()))
		    	.build());
	    options.addOption(Option.builder(EOption.H_ENTITIES.getName())
		    	.required(false)
		    	.hasArg().argName(conf.getString(EOption.H_ENTITIES.getDescrValuePath())).type(String.class).valueSeparator()
		    	.desc(conf.getString(EOption.H_ENTITIES.getDescrConfPath()))
		    	.build());

	    options.addOption(EOption.MULTIVAL.getName(), false, conf.getString(EOption.MULTIVAL.getDescrConfPath()));
	    
	 // specify the path for the output file that will be generated
	    options.addOption(Option.builder(EOption.OUTPUT.getName())
	    	.required(false)
	    	.hasArg().argName(conf.getString(EOption.OUTPUT.getDescrValuePath())).type(String.class).valueSeparator()
	    	.desc(conf.getString(EOption.OUTPUT.getDescrConfPath()))
	    	.build());
	    options.addOption(Option.builder(EOption.CONFIGURATION_FILE.getName())
		    	.required(false)
		    	.hasArg().argName(conf.getString(EOption.CONFIGURATION_FILE.getDescrValuePath())).type(String.class).valueSeparator()
		    	.desc(conf.getString(EOption.CONFIGURATION_FILE.getDescrConfPath()))
		    	.build());
	    options.addOption(Option.builder(EOption.LOG_FILE.getName())
	    	.required(false)
	    	.hasArg().argName(conf.getString(EOption.LOG_FILE.getDescrValuePath())).type(String.class).valueSeparator()
	    	.desc(conf.getString(EOption.LOG_FILE.getDescrConfPath()))
	    	.build());
	    options.addOption(Option.builder(EOption.EXEC.getName())
	    	.required(false)
	    	.hasArg().argName(conf.getString(EOption.EXEC.getDescrValuePath())).type(String.class).valueSeparator()
	    	.desc(conf.getString(EOption.EXEC.getDescrConfPath()))
	    	.build());
	    options.addOption(EOption.XVFB.getName(), false, conf.getString(EOption.XVFB.getDescrConfPath()));
	    options.addOption(EOption.AUTOCOMPLETE.getName(), false, conf.getString(EOption.AUTOCOMPLETE.getDescrConfPath()));
	    options.addOption(Option.builder(EOption.DISPLAY.getName())
		    	.required(false)
		    	.hasArg().argName(conf.getString(EOption.DISPLAY.getDescrValuePath())).type(String.class).valueSeparator()
		    	.desc(conf.getString(EOption.DISPLAY.getDescrConfPath()))
		    	.build());
	    options.addOption(EOption.IMG.getName(), false, conf.getString(EOption.IMG.getDescrConfPath()));
	    options.addOption(EOption.PLUGINS.getName(), false, conf.getString(EOption.PLUGINS.getDescrConfPath()));
	    options.addOption(Option.builder(EOption.LATITUDE.getName())
		    	.required(false)
		    	.hasArg().argName(conf.getString(EOption.LATITUDE.getDescrValuePath())).type(Integer.class).valueSeparator()
		    	.desc(conf.getString(EOption.LATITUDE.getDescrConfPath()))
		    	.build());
	    options.addOption(Option.builder(EOption.LONGITUDE.getName())
		    	.required(false)
		    	.hasArg().argName(conf.getString(EOption.LONGITUDE.getDescrValuePath())).type(Integer.class).valueSeparator()
		    	.desc(conf.getString(EOption.LONGITUDE.getDescrConfPath()))
		    	.build());
	    options.addOption(Option.builder(EOption.WIDTH.getName())
		    	.required(false)
		    	.hasArg().argName(conf.getString(EOption.WIDTH.getDescrValuePath())).type(Integer.class).valueSeparator()
		    	.desc(conf.getString(EOption.WIDTH.getDescrConfPath()))
		    	.build());
	    options.addOption(Option.builder(EOption.HEIGHT.getName())
		    	.required(false)
		    	.hasArg().argName(conf.getString(EOption.HEIGHT.getDescrValuePath())).type(Integer.class).valueSeparator()
		    	.desc(conf.getString(EOption.HEIGHT.getDescrConfPath()))
		    	.build());
	    return options;
	}
	
	public void run(String[] args) {
		boolean success = false;
		
		ConfigurationObjectFactory confFactory = new ConfigurationObjectFactory();
		ConfigurationObject config = confFactory
				.create(this.getClass().getResource(CLI_CONFIG_FILE_RELATIVE_PATH));
		
		// parse the command line
		Options cliOptions = buildCliOptions(config.getConfiguration());
		CommandLineParser parser = new DefaultParser();
		
		try {
			CommandLine cmdLine = parser.parse(cliOptions, args);
			Map<EOption, Object> options = EOption.getValueMap(cmdLine, config.getConfiguration());
			// set logf4 property file
			if (options.get(EOption.LOG_FILE) != null) {
				PropertyConfigurator.configure((String)options.get(EOption.LOG_FILE));
			}
			
			ConfigurationObject userConfig = null;
			if (options.get(EOption.CONFIGURATION_FILE) != null) {
				userConfig = confFactory.create(
					new File((String)options.get(EOption.CONFIGURATION_FILE)));
			}
			
			switch(ECmd.classifyCmd(cmdLine)) {
			case HELP:
				success = processHelp(cliOptions, config.getConfiguration());
				break;
			case VERSION:
				success = processVersion(config.getConfiguration());
				break;
			case RUN:
				success = processQuery(options, userConfig);
				break;
			default:
				throw new RuntimeException("Incorrect configuration of options.");
			}
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (RuntimeException e2) {
			e2.printStackTrace();
		}
		System.exit(success?0:1);
	}
	
	public static void main(String[] args) {
		PropertyConfigurator.configure(OXPathCLI.class.getResourceAsStream(DEFAULT_LOG4J_CONFIG));
		OXPathCLI cli = new OXPathCLI();
		cli.run(args);
	}

}
