package eu.europeana.corelib.definitions.edm.beans;

public interface RichBean extends ApiBean {

	/**
	 * 
	 * @return edm:isShownBy
	 */
	String[] getEdmIsShownBy();

	/**
	 * 
	 * @return edm:isShownBy
	 */
	String[] getDcDescription();

	/**
	 * 
	 * @return edm:landingPage
	 */
	String[] getEdmLandingPage();
}
