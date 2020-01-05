package org.oxpath.browserinstaller;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.PropertyConfigurator;
import org.oxpath.browserinstaller.BrowserInstallerConfiguration.EArchiveType;
import org.rauschig.jarchivelib.ArchiveFormat;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;
import org.rauschig.jarchivelib.CompressionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ox.cs.diadem.util.configuration.ConfigurationObject;
import uk.ac.ox.cs.diadem.util.configuration.ConfigurationObjectFactory;
import uk.ac.ox.cs.diadem.util.misc.BrowserType;
import uk.ac.ox.cs.diadem.util.misc.OSType;

/**
 * @author Ruslan Fayzrakhmanov
 * 5 Sep 2018
 */
public class BrowserInstallerApp {
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	public static final String DEFAULT_CONFIGURATION_PATH = "/org/oxpath/browserinstaller/defaultConfiguration.xml";
	public static final String DEFAULT_LOG4J_CONFIG = "/org/oxpath/browserinstaller/defaultLog4j.properties";
	
	public static void main(String[] args) {
		PropertyConfigurator.configure(BrowserInstallerApp.class.getResourceAsStream(DEFAULT_LOG4J_CONFIG));
		new BrowserInstallerApp().run(args);
	}
	
	public void run(String[] args) {
		
		if (OSType.getOsType() != OSType.LINUX) {
			throw new BrowserInstallerRuntimeException("Only Linux is supported", log);
		}
		
		ConfigurationObject confObject = new ConfigurationObjectFactory()
				.create(this.getClass().getResource(DEFAULT_CONFIGURATION_PATH));
		BrowserInstallerConfiguration config = new BrowserInstallerConfiguration(confObject, BrowserType.FIREFOX, OSType.getOsType());
		
		if (config.getSourceArchiveType() != EArchiveType.TAR_BZ2) {
			throw new BrowserInstallerRuntimeException("Only extension {} is supported", log, EArchiveType.TAR_BZ2.getName());
		}
		
		FileManager fm = new FileManager();
		File tmpDir = new File(UUID.randomUUID().toString());
		tmpDir.mkdir();
		
		log.info("Downloading browser from {}", config.getSourceUrl());
		File firefoxArch = new File(tmpDir, UUID.randomUUID().toString());
		fm.downloadFile(config.getSourceUrl(), firefoxArch);
		
		log.info("Uncompressing the browser binaries");
		Archiver archiver = ArchiverFactory.createArchiver(ArchiveFormat.TAR, CompressionType.BZIP2);
		File firefoxUnpacked = new File(tmpDir, UUID.randomUUID().toString());
		try {
			archiver.extract(firefoxArch, firefoxUnpacked);
		} catch (IOException e) {
			throw new BrowserInstallerRuntimeException(e.getMessage(), log);
		}
		
		log.info("Copy files into the home directory {}", config.getHomePath());
		
		File destinationDir = new File(config.getDestinationPath().toString());
		if (destinationDir.exists()) {
			try {
				FileUtils.deleteDirectory(destinationDir);
			} catch (IOException e) {
				throw new BrowserInstallerRuntimeException(e.getMessage(), log);
			}
		}
		fm.copyDirectory(new File(firefoxUnpacked, config.getInArcDirName()), destinationDir);
			
		try {
			FileUtils.deleteDirectory(tmpDir);
		} catch (IOException e) {
			throw new BrowserInstallerRuntimeException(e.getMessage(), log);
		}
		
		String pathStr = config.getSourceRunFilePath().toString();
		File destFile = new File(config.getDestinationPath().toString(), FilenameUtils.getName(pathStr));
		fm.copyToFile(BrowserInstallerApp.class.getResourceAsStream(pathStr), destFile);
		fm.setExecutablePermissionOnFiles(destFile);
		
		pathStr = config.getSourceRunXvfbFilePath().toString();
		destFile = new File(config.getDestinationPath().toString(), FilenameUtils.getName(pathStr));
		fm.copyToFile(BrowserInstallerApp.class.getResourceAsStream(pathStr), destFile);
		fm.setExecutablePermissionOnFiles(destFile);
		
		pathStr = config.getSourceXvfbRunPath().toString();
		destFile = new File(config.getDestinationPath().toString(), FilenameUtils.getName(pathStr));
		fm.copyToFile(BrowserInstallerApp.class.getResourceAsStream(pathStr), destFile);
		
		pathStr = config.getSourceDisplaySizeFilePath().toString();
		destFile = new File(config.getDestinationPath().toString(), FilenameUtils.getName(pathStr));
		fm.copyToFile(BrowserInstallerApp.class.getResourceAsStream(pathStr), destFile);
	}
	
}
