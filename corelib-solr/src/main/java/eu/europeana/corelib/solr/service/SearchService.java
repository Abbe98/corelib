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

package eu.europeana.corelib.solr.service;

import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrServerException;

import eu.europeana.corelib.definitions.solr.beans.BriefBean;
import eu.europeana.corelib.definitions.solr.beans.FullBean;
import eu.europeana.corelib.definitions.solr.beans.IdBean;
import eu.europeana.corelib.definitions.solr.model.Query;
import eu.europeana.corelib.definitions.solr.model.Term;
import eu.europeana.corelib.solr.exceptions.SolrTypeException;
import eu.europeana.corelib.solr.model.ResultSet;

/**
 * Search service that retrieves BriefBeans or APIBeans in the case of a query search or a FullBean in the case of a
 * user selection. Currently the implementation uses SOLR for Brief/APIBeans and MongoDB for FullBean retrieval.
 * 
 * @author Yorgos.Mamakis@ kb.nl
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
public interface SearchService {

	/**
	 * Retrieve a record by id.
	 * 
	 * @param europeanaObjectId
	 *            - The unique europeana id
	 * @return A full europeana record
	 * @throws SolrTypeException
	 */
	FullBean findById(String europeanaObjectId) throws SolrTypeException;
	
	/**
	 * Retrieve a record by splitted collectionId and recordId
	 * 
	 * @param europeanaObjectId
	 *            - The unique europeana id
	 * @return A full europeana record
	 * @throws SolrTypeException
	 */
	FullBean findById(String collectionId, String recordId) throws SolrTypeException;

	
	/**
	 * Retrieve a record by id.
	 * 
	 * @param europeanaObjectId
	 *            - The unique europeana id
	 * @return A full europeana record
	 * @throws SolrTypeException
	 */
	FullBean resolve(String europeanaObjectId) throws SolrTypeException;
	
	/**
	 * Retrieve a record by splitted collectionId and recordId
	 * 
	 * @param europeanaObjectId
	 *            - The unique europeana id
	 * @return A full europeana record
	 * @throws SolrTypeException
	 */
	FullBean resolve(String collectionId, String recordId) throws SolrTypeException;

	/**
	 * Perform a search in SOLR based on the given query and return the results in the format of the given class.
	 * 
	 * @param beanInterface
	 *            The required bean type, should be ApiBean or BriefBean
	 * @param query
	 *            Model class containing the search specification.
	 * @return The search results, including facets, breadcrumb and original query.
	 * @throws SolrTypeException
	 */
	<T extends IdBean> ResultSet<T> search(Class<T> beanInterface, Query query) throws SolrTypeException;
	
	/**
	 * returns a list of search suggestions and frequencies
	 * 
	 * @param query
	 * 			The search term to find suggestions for
	 * @param pageSize
	 * 			Amount of requested suggestions
	 * @return List of search suggestions
	 * @throws SolrTypeException 
	 */
	List<Term> suggestions(String query, int pageSize) throws SolrTypeException;

	/**
	 * returns a list of search suggestions and frequencies
	 * 
	 * @param query
	 * 			The search term to find suggestions for
	 * @param pageSize
	 * 			Amount of requested suggestions
	 * @return List of search suggestions
	 * @throws SolrTypeException 
	 */
	List<Term> suggestions(String query, int pageSize, String field) throws SolrTypeException;

	List<BriefBean> findMoreLikeThis(String europeanaObjectId)
			throws SolrServerException;

	/**
	 * Returns a list of "see also" suggestions.
	 * The suggestions are organized by fields (who, what, where, when, and title). Each suggestion contains a
	 * field value and the number of documents it matches.
	 *
	 * @param fields
	 *   Map of field names, and corresponding field values.
	 *   
	 * @return
	 *   The see also suggestions
	 */
	Map<String, Map<String,Integer>> seeAlso(Map<String, List<String>> fields);
}
