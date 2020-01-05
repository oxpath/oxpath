package uk.ac.ox.cs.diadem.util.misc;


/**
 * Type of the Operating System.
 */
public enum OSType {
	 
	LINUX, UNIX, OSX, WINDOWS, UNKNOWN;
	
	public static boolean isWindows(){
		String os = System.getProperty("os.name").toLowerCase();
	    return (os.indexOf( "win" ) >= 0); 
	}
 
	public static boolean isMac(){
		String os = System.getProperty("os.name").toLowerCase();
	    return (os.indexOf( "mac" ) >= 0); 
	}
 
	public static boolean isLinux(){
		String os = System.getProperty("os.name").toLowerCase();
	    return (os.indexOf( "nix") >=0 || os.indexOf( "nux") >=0);
	}
	
	public static boolean isUnix(){
		String os = System.getProperty("os.name").toLowerCase();
	    return (os.indexOf( "unix") >=0);
	}

	public static OSType getOsType() {
		if(isWindows()) return OSType.WINDOWS;
		if(isMac()) return OSType.OSX;
		if(isLinux()) return OSType.LINUX;
		if(isUnix()) return OSType.UNIX;
		return OSType.UNKNOWN;
	}
	
}
