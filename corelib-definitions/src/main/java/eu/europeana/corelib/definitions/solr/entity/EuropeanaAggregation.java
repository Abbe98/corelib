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

import java.util.List;

/**
 * Europeana specific aggregation
 * @author Yorgos.Mamakis@ kb.nl
 *
 */
public interface EuropeanaAggregation extends AbstractEdmEntity {

	String getAggregatedCHO();
	
	void setAggregatedCHO(String aggregatedCHO);
	
	String[] getAggregates();
	
	void setAggregates(String[] aggregates);
	
	String getDcCreator();
	
	void setDcCreator(String dcCreator);
	
	String getEdmLandingPage();
	
	void setEdmLandingPage(String edmLandingPage);
	
	String getEdmIsShownBy();
	
	void setEdmIsShownBy(String edmIsShownBy);
	
	String[] getEdmHasView();
	
	void setEdmHasView(String[] edmHasView);
	
	String getEdmCountry();
	
	void setEdmCountry(String edmCountry);
	
	String getEdmLanguage();
	
	void setEdmLanguage(String edmLanguage);
	
	String getEdmRights();
	
	void setEdmRights(String edmRights);

	List<? extends WebResource> getWebResources();

	void setWebResources(List<? extends WebResource> webResources);
}
