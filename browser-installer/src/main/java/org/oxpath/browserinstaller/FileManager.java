package org.oxpath.browserinstaller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * File manipulation methods.
 * 
 * @author Ruslan Fayzrakhmanov
 * 6 Sep 2018
 */
public class FileManager {
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	public static final int CONNECTION_TIMEOUT_DEFAULT = 1000;
	public static final int READ_TIMEOUT_DEFAULT = 1000;
	
//	public void downloadFile(String sourceUrl, String destFilePath) {
//		try {
//			downloadFile(new URL(sourceUrl), new File(destFilePath), CONNECTION_TIMEOUT_DEFAULT, READ_TIMEOUT_DEFAULT);
//		} catch (MalformedURLException e) {
//			throw new BrowserInstallerRuntimeException(e.getMessage(), e, log);
//		}
//	}
	
//	public void downloadFile(String sourceUrl, String destFilePath, int connectionTimeout, int readTimeout) {
//		try {
//			downloadFile(new URL(sourceUrl), new File(destFilePath), connectionTimeout, readTimeout);
//		} catch (MalformedURLException e) {
//			throw new BrowserInstallerRuntimeException(e.getMessage(), e, log);
//		}
//	}
	
	public void downloadFile(URL source, File destination) {
		try {
			FileUtils.copyURLToFile(source, destination, CONNECTION_TIMEOUT_DEFAULT, READ_TIMEOUT_DEFAULT);
		} catch (IOException e) {
			throw new BrowserInstallerRuntimeException(e.getMessage(), e, log);
		}
	}
	
	public void downloadFile(URL source, File destination, int connectionTimeout, int readTimeout) {
		try {
			FileUtils.copyURLToFile(source, destination, connectionTimeout, readTimeout);
		} catch (IOException e) {
			throw new BrowserInstallerRuntimeException(e.getMessage(), e, log);
		}
	}
	
	public void copyDirectory(File source, File destination) {
		try {
			FileUtils.copyDirectory(source, destination);
		} catch (IOException e) {
			throw new BrowserInstallerRuntimeException(e.getMessage(), e, log);
		}
	}
	
	public void copyToFile(InputStream source, File destination) {
		try {
			FileUtils.copyToFile(source, destination);
		} catch (IOException e) {
			throw new BrowserInstallerRuntimeException(e.getMessage(), e, log);
		}
	}
	
	public void setExecutablePermissionOnFiles(File file) {
		  if (!file.canExecute()) {
		    final boolean done = file.setExecutable(true);
		    if (!done)
		      throw new BrowserInstallerRuntimeException("Cannot set executable flag on {}", log, file);
		  }
	  }
	
//	public static final int DEFAULT_BUFFER_SIZE = 1024;
//	
//	public void unzipFile(FileInputStream zipSource, File dirDestination) {
//		
//		if(!dirDestination.exists()) {
//			dirDestination.mkdirs();
//    	}
//		
//        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
//        ZipInputStream zis = new ZipInputStream(zipSource);
//		try {
//			ZipEntry zipEntry = zis.getNextEntry();
//			while(zipEntry != null){
//	            String fileName = zipEntry.getName();
//	            File newFile = new File(dirDestination, fileName);
//	            
//	            if(!newFile.getParentFile().exists()) {
//	            	newFile.getParentFile().mkdirs();
//	        	}
//	            
//	            FileOutputStream fos = new FileOutputStream(newFile);
//	            int len;
//	            while ((len = zis.read(buffer)) > 0) {
//	                fos.write(buffer, 0, len);
//	            }
//	            fos.close();
//	            zipEntry = zis.getNextEntry();
//	        }
//	        zis.closeEntry();
//	        
//		} catch (IOException e) {
//			throw new BrowserInstallerRuntimeException(e.getMessage(), e, log);
//		} finally {
//			try {
//				zis.close();
//			} catch (IOException e) {
//				throw new BrowserInstallerRuntimeException(e.getMessage(), e, log);
//			}
//		}
//	}

}
