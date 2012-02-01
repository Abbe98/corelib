/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.0 or? as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * you may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */
package eu.europeana.corelib.solr.model;

import java.util.List;

import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.SpellCheckResponse;

import eu.europeana.corelib.definitions.solr.beans.IdBean;

public class ResultSet<T extends IdBean> {
	
	private Query query;

	private List<T> results;
	
	private List<FacetField> facetFields;
	
	private SpellCheckResponse spellcheck;
	
	// statistics
	
	private long resultSize;
	
	private int searchTime;

	/**
	 * GETTERS & SETTTERS
	 */

	public List<T> getResults() {
		return results;
	}

	public ResultSet<T> setResults(List<T> results) {
		this.results = results;
		return this;
	}

	public Query getQuery() {
		return query;
	}

	public ResultSet<T> setQuery(Query query) {
		this.query = query;
		return this;
	}

	public List<FacetField> getFacetFields() {
		return facetFields;
	}

	public ResultSet<T> setFacetFields(List<FacetField> facetFields) {
		this.facetFields = facetFields;
		return this;
	}

	public SpellCheckResponse getSpellcheck() {
		return spellcheck;
	}

	public ResultSet<T> setSpellcheck(SpellCheckResponse spellcheck) {
		this.spellcheck = spellcheck;
		return this;
	}

	public long getResultSize() {
		return resultSize;
	}

	public ResultSet<T> setResultSize(long resultSize) {
		this.resultSize = resultSize;
		return this;
	}

	public int getSearchTime() {
		return searchTime;
	}

	public ResultSet<T> setSearchTime(int searchTime) {
		this.searchTime = searchTime;
		return this;
	}
	
}
