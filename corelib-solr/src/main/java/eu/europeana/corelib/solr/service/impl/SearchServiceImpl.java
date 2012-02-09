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

package eu.europeana.corelib.solr.service.impl;



import javax.annotation.Resource;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.code.morphia.Datastore;

import eu.europeana.corelib.definitions.exception.ProblemType;
import eu.europeana.corelib.definitions.solr.beans.ApiBean;
import eu.europeana.corelib.definitions.solr.beans.BriefBean;
import eu.europeana.corelib.definitions.solr.beans.FullBean;
import eu.europeana.corelib.definitions.solr.beans.IdBean;
import eu.europeana.corelib.solr.exceptions.MongoDBException;
import eu.europeana.corelib.solr.exceptions.SolrTypeException;
import eu.europeana.corelib.solr.model.Query;
import eu.europeana.corelib.solr.model.ResultSet;
import eu.europeana.corelib.solr.mongodb.MongoDBServer;
import eu.europeana.corelib.solr.mongodb.impl.MongoDBServerImpl;
import eu.europeana.corelib.solr.server.SolrServer;
import eu.europeana.corelib.solr.service.SearchService;

/**
 * @see eu.europeana.corelib.solr.service.SearchService
 * 
 * @author Yorgos.Mamakis@kb.nl
 *
 */

public class SearchServiceImpl implements SearchService {
	@Resource(name="corelib_solr_solrSelectServer1")
	private SolrServer solrServer;
	
	@Resource(name="corelib_solr_mongoServer")
	MongoDBServer mongoServer;
	
	@Override
	public FullBean findById(String europeanaObjectId) {
		ObjectId id = ObjectId.massageToObjectId(europeanaObjectId);
		return mongoServer.getFullBean(id);
	}

	@Override
	public <T extends IdBean> ResultSet<T> search(Class<T> beanClazz,
			Query query) throws SolrTypeException {
		
		if (beanClazz.isInstance(BriefBean.class)){
			//TODO: what will BriefBean contain
		}
		else if (beanClazz.isInstance(ApiBean.class)){
			//TODO: what will ApiBean contain
		}
		else{
			throw new SolrTypeException(ProblemType.INVALIDARGUMENTS);
		}
		return null;
	}

	
}
