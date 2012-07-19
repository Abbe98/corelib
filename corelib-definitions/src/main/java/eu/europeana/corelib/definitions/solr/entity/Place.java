/*
 * Copyright 2007-2012 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 * 
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */
package eu.europeana.corelib.definitions.solr.entity;

/**
 * EDM Place fields representation
 * 
 * @author Yorgos.Mamakis@ kb.nl
 * 
 */
public interface Place extends ContextualClass {

	/**
	 * Retrieves the dcterms:isPartOf fields for a Place
	 * 
	 * @return A String array representing the dcterms:isPartOf fields for a
	 *         Place
	 */
	String[] getIsPartOf();

	/**
	 * Retrieves the latitude of a Place
	 * 
	 * @return A float representing the latitude of a Place
	 */
	float getLatitude();

	/**
	 * Retrieves the longitude of a Place
	 * 
	 * @return A float representing the longitude of a Place
	 */
	float getLongitude();

	/**
	 * Set the dcterms:isPartOf fields for a Place
	 * 
	 * @param isPartOf
	 *            A String array representing the dcterms:isPartOf fields for a
	 *            Place
	 */
	void setIsPartOf(String[] isPartOf);

	/**
	 * Set the latitude for a place
	 * 
	 * @param latitude
	 *            A float representing the latitude of a Place
	 */
	void setLatitude(float latitude);

	/**
	 * Set the longitude for a place
	 * 
	 * @param longitude
	 *            A float representing the longitude of a Place
	 */
	void setLongitude(float longitude);

	void setAltitude(float altitude);
	
	float getAltitude();
	
	void setPosition(float[] position);
	
	float[] getPosition();
	
	void setDcTermsHasPart(String[] dcTermsHasPart);
	
	String[] getDcTermsHasPart();
	
	void setOwlSameAs(String[] owlSameAs);
	
	String[] getOwlSameAs();
}
