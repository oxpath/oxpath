package org.oxpath.browserinstaller;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ox.cs.diadem.util.configuration.ConfigurationObject;
import uk.ac.ox.cs.diadem.util.misc.BrowserType;
import uk.ac.ox.cs.diadem.util.misc.OSType;

/**
 * 
 * An interface to the configuration object. 
 * 
 * @author Ruslan Fayzrakhmanov
 * 5 Sep 2018
 */
public class BrowserInstallerConfiguration {
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	public static final String PATH_SEPARATOR = "/";
	public static final String PLATFORM_PATH_TEMPLATE = "browser-installer/platforms/platform[@os-type=\"%s\"]";
	public static final String BROWSER_TEMPLATE = "browser[@name=\"%s\"]";
	
	public static final String SOURCE_URL_RELPATH = "source/url";
	public static final String SOURCE_ARCHIVE_TYPE_RELPATH = "source/archive-type";
	public static final String SOURCE_IN_ARC_DIR_NAME = "source/in-arc-dir-name";
	public static final String SOURCE_RUN_FILE_RELPATH = "source/run-file-path";
	public static final String SOURCE_RUN_XVFB_FILE_RELPATH = "source/run-xvfb-file-path";
	public static final String SOURCE_XVFB_RUN_RELPATH = "source/xvfb-run-path";
	public static final String SOURCE_DISPLAY_SIZE_FILE_RELPATH = "source/display-size-file-path";
	
	public static final String DEST_HOME_RELPATH = "home";
	public static final String DEST_USER_HOME_REL_RELPATH = "home/@user-home-rel";
	public static final String DEST_RELPATH = "destination/relpath";
	
	public enum EArchiveType {
		TAR_BZ2("tar.bz2");
		
		private final String name;
		public String getName() {
			return name;
		}
		
		private EArchiveType(String name) {
			this.name = name;
		}
		
		public static EArchiveType parse(String name) {
			EArchiveType[] values = EArchiveType.values();
			for (int i=0; i<values.length; i++) {
				if (values[i].getName().equalsIgnoreCase(name)) {
					return values[i];
				}
			}
			return null;
		}
	}
	
	private final String PLATFORM_PATH;
	private final String BROWSER_PATH;
	
	private final ConfigurationObject config;
	private final BrowserType browserType;
	public BrowserType getBrowserType() {
		return browserType;
	}
	private final OSType osType;
	public OSType getOsType() {
		return osType;
	}
	
	/**
	 * @param config Configuration object, wrapping the original configuration.
	 */
	public BrowserInstallerConfiguration(ConfigurationObject config, BrowserType browserType, OSType osType) {
		this.config = config;
		this.browserType = browserType;
		this.osType = osType;
		PLATFORM_PATH = String.format(PLATFORM_PATH_TEMPLATE, osType.name());
		BROWSER_PATH = PLATFORM_PATH + PATH_SEPARATOR + String.format(BROWSER_TEMPLATE, browserType.name());
	}
	
	public Path getHomePath() {
		String homePath = config.getConfiguration().getString(PLATFORM_PATH + PATH_SEPARATOR + DEST_HOME_RELPATH);
		boolean userHomeRelative = config.getConfiguration().getBoolean(PLATFORM_PATH + PATH_SEPARATOR + DEST_USER_HOME_REL_RELPATH, false);
		if (userHomeRelative)
			return Paths.get(System.getProperty("user.home"), homePath);
		else
			return Paths.get(homePath);
	}
	
	private String getStringPath(String relPath) {
		return config.getConfiguration().getString(BROWSER_PATH + PATH_SEPARATOR + relPath);
	}
	
	private URL getUrl(String relPath, String errMsg) {
		try {
			return new URL(getStringPath(relPath));
		} catch (MalformedURLException e) {
			log.error(errMsg);
			return null;
		}
	}
	
	private Path getPath(String relPath) {
		return Paths.get(getStringPath(relPath));
	}
	
	public URL getSourceUrl() {
		return getUrl(SOURCE_URL_RELPATH, "Missing source URL");
	}
	
	public EArchiveType getSourceArchiveType() {
		return EArchiveType.parse(getStringPath(SOURCE_ARCHIVE_TYPE_RELPATH));
	}
	
	public String getInArcDirName() {
		return getStringPath(SOURCE_IN_ARC_DIR_NAME);
	}
	
	public Path getSourceRunFilePath() {
		return getPath(SOURCE_RUN_FILE_RELPATH);
	}
	
	public Path getSourceRunXvfbFilePath() {
		return getPath(SOURCE_RUN_XVFB_FILE_RELPATH);
	}
	
	public Path getSourceXvfbRunPath() {
		return getPath(SOURCE_XVFB_RUN_RELPATH);
	}
	
	public Path getSourceDisplaySizeFilePath() {
		return getPath(SOURCE_DISPLAY_SIZE_FILE_RELPATH);
	}
	
	public Path getDestinationPath() {
		return Paths.get(getHomePath().toString(), getStringPath(DEST_RELPATH));
	}
	
}
