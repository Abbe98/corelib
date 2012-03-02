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

package eu.europeana.corelib.solr.bean;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eu.europeana.corelib.solr.bean.impl.BriefBeanImpl;
import eu.europeana.corelib.solr.model.Query;
import eu.europeana.corelib.solr.model.ResultSet;
import eu.europeana.corelib.solr.server.SolrServer;
import eu.europeana.corelib.solr.server.impl.SolrServerImpl;
import eu.europeana.corelib.solr.service.impl.SearchServiceImpl;

/**
 * Unit tests for BriefBean
 * 
 * @author Yorgos.Mamakis@ kb.nl
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/corelib-solr-context.xml", "/corelib-solr-test.xml" })
public class BriefBeanTest {

	SolrServer solrServer;
	private static String solrHome = "src/test/resources/solr";

	@Test
	public void testBriefBean() {
		try {
			solrServer = new SolrServerImpl(solrHome);
			assertNotNull(solrServer);
			assertTrue(solrServer.isActive());
			SearchServiceImpl searchService = new SearchServiceImpl(solrServer);
			Query query = new Query();
			
			query.setPageSize(12);
			
			query.setQuery("*:*");
			query.setStart(0);
			ResultSet<BriefBeanImpl> briefBeanResults = searchService.search(BriefBeanImpl.class, query);
			
			assertTrue(briefBeanResults.getResultSize()>0);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
