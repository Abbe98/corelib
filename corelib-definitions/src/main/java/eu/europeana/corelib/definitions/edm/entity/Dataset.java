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
package eu.europeana.corelib.definitions.edm.entity;

import java.util.List;
import java.util.Map;

/**
 * Class representing edm:Dataset in mongo
 * @author Yorgos.Mamakis@ europeana.eu
 *
 */
//TODO: NOT TO BE USED
public interface Dataset extends AbstractEdmEntity {

	/**
	 * edm:datasetName
	 * @return
	 */
	String getEdmDatasetName();

	/**
	 * edm:datasetName
	 */
	void setEdmDatasetName(String edmDatasetName);
	/**
	 * edm:provider
	 * @return
	 */
	String getEdmProvider();
	/**
	 * edm:provider
	 */
	void setEdmProvider(String edmProvider);

	/**
	 *edm:intermediateProvider
	 */
	Map<String,List<String>> getEdmIntermediateProvider();
	void setEdmIntermediateProvider(Map<String,List<String>> edmIntermediateProvider);

	/**
	 *edm:dataProvider
	 */
	Map<String,List<String>> getEdmDataProvider();
	void setEdmDataProvider(Map<String,List<String>> edmDataProvider);

	/**
	 *edm:country
	 */
	String getEdmCountry();
	void setEdmCountry(String edmCountry);

	/**
	 *edm:language
	 */
	String getEdmLanguage();
	void setEdmLanguage(String edmLanguage);

	/**
	 * dc:identifier
	 * @return
	 */
	String getDcIdentifier();
	/**
	 * @param dcIdentifier
	 */
	void setDcIdentifier(String dcIdentifier);

	/**
	 *dc:description
	 */
	Map<String,List<String>> getDcDescription();
	void setDcDescription(Map<String,List<String>> dcDescription);

	/**
	 * dcterms:created
	 * @return
	 */
	String getDctermsCreated();
	/**
	 * 
	 * @param dctermsCreated
	 */
	void setDctermsCreated(String dctermsCreated);
	
	/**
	 * dcterms:extent
	 * @return
	 */
	String getDctermsExtent();
	
	/**
	 * 
	 * @param dctermsExtent
	 */
	void setDctermsExtent(String dctermsExtent);

	/**
	 * dcterms:modified
	 */
	String getDctermsModified();
	void setDctermsModified(String dctermsModified);
	
	/**
	 * adms:status
	 * @return
	 */
	String getAdmsStatus();
	
	/**
	 * 
	 * @param admsStatus
	 */
	void setAdmsStatus(String admsStatus);
}
