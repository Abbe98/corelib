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
package eu.europeana.corelib.solr.service.impl;

import javax.annotation.Resource;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.beans.factory.annotation.Value;

import eu.europeana.corelib.definitions.exception.ProblemType;
import eu.europeana.corelib.definitions.solr.QueryType;
import eu.europeana.corelib.definitions.solr.beans.FullBean;
import eu.europeana.corelib.definitions.solr.beans.IdBean;
import eu.europeana.corelib.solr.bean.impl.ApiBeanImpl;
import eu.europeana.corelib.solr.bean.impl.BriefBeanImpl;
import eu.europeana.corelib.solr.exceptions.SolrTypeException;
import eu.europeana.corelib.solr.model.Query;
import eu.europeana.corelib.solr.model.ResultSet;
import eu.europeana.corelib.solr.server.MongoDBServer;
import eu.europeana.corelib.solr.service.SearchService;
import eu.europeana.corelib.solr.service.query.MoreLikeThis;
import eu.europeana.corelib.solr.utils.SolrUtil;

/**
 * @see eu.europeana.corelib.solr.service.SearchService
 * 
 * @author Yorgos.Mamakis@ kb.nl
 * 
 */
public class SearchServiceImpl implements SearchService {

	@Resource(name = "corelib_solr_solrServer")
	private SolrServer solrServer;
	
	@Resource(name = "corelib_solr_mongoServer")
	MongoDBServer mongoServer;
	
	@Value("#{europeanaProperties['solr1.facetLimit']}")
	private int facetLimit;


	public SearchServiceImpl() {
	}
	
	public SearchServiceImpl(SolrServer solrServer) {
		super();
		this.solrServer = solrServer;
	}

	@Override
	public FullBean findById(String europeanaObjectId) throws SolrTypeException {

//		if (!solrServer1.isActive() && !solrServer2.isActive()) {
//			throw new SolrTypeException(ProblemType.SOLR_UNREACHABLE);
//		}
		SolrQuery solrQuery = new SolrQuery().setQuery("europeana_id:\""
				+ europeanaObjectId + "\"");
		solrQuery.set("mlt",true);
		String[] mlt = new String[MoreLikeThis.values().length];
		int i=0;
		for (MoreLikeThis mltField : MoreLikeThis.values()){
			mlt[i]=mltField.toString();
			i++;
		}
		solrQuery.set("mlt.fl", mlt);
		solrQuery.setQueryType(QueryType.ADVANCED.toString());
		QueryResponse queryResponse = null;

		FullBean fullBean = mongoServer.getFullBean(europeanaObjectId);
		try {
			queryResponse = solrServer.query(solrQuery);
			fullBean.setRelatedItems(queryResponse.getBeans(BriefBeanImpl.class));
		} catch (SolrServerException e) {
			//LOG HERE
		}
		return fullBean;
	}

	@Override
	public <T extends IdBean> ResultSet<T> search(Class<T> beanClazz,
			Query query) throws SolrTypeException {
		ResultSet<T> resultSet = new ResultSet<T>();

//		if (!solrServer1.isActive() && !solrServer2.isActive()) {
//			throw new SolrTypeException(ProblemType.SOLR_UNREACHABLE);
//		}
		if (beanClazz == BriefBeanImpl.class || beanClazz == ApiBeanImpl.class) {
			String[] refinements = query.getRefinements();
			if (SolrUtil.checkTypeFacet(refinements)) {
				SolrQuery solrQuery = new SolrQuery()
						.setQuery(query.getQuery());
				solrQuery.setFacet(true);
				if (refinements != null) {
					for (String refinement : refinements) {
						solrQuery.addFacetField(refinement);
					}
				}
				solrQuery.setFacetLimit(facetLimit);
				solrQuery.setRows(query.getPageSize());
				solrQuery.setStart(query.getStart());
				solrQuery.setQueryType(QueryType.ADVANCED.toString());
				solrQuery.setSortField("COMPLETENESS", ORDER.desc);
				solrQuery.setSortField("score", ORDER.desc);
				try {
					QueryResponse queryResponse = solrServer.query(solrQuery);
					
					resultSet.setResults(queryResponse.getBeans(beanClazz));
					
					resultSet.setFacetFields(queryResponse.getFacetFields());
					resultSet.setResultSize(queryResponse.getResults().size());
					resultSet.setSearchTime(queryResponse.getElapsedTime());
					resultSet.setSpellcheck(queryResponse
							.getSpellCheckResponse());
				} catch (SolrServerException e) {
					throw new SolrTypeException(e, ProblemType.MALFORMED_QUERY);
				}

			} else {
				throw new SolrTypeException(ProblemType.INVALIDARGUMENTS);
			}

		} else {
			throw new SolrTypeException(ProblemType.INVALIDARGUMENTS);
		}
		return resultSet;
	}
	
	public void setSolrServer(SolrServer solrServer) {
		this.solrServer = solrServer;
	}
}
