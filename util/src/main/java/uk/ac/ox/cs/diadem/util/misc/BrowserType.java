package uk.ac.ox.cs.diadem.util.misc;


/**
 * Types of web browsers.
 */
public enum BrowserType {
	 
	FIREFOX("Firefox");
	
	private final String name;
	private BrowserType(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
}
