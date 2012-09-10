package eu.europeana.corelib.solr.test.importer;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.solr.common.SolrInputDocument;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eu.europeana.corelib.definitions.jibx.ProvidedCHOType;
import eu.europeana.corelib.definitions.jibx.SameAs;
import eu.europeana.corelib.definitions.model.EdmLabel;
import eu.europeana.corelib.solr.entity.ProvidedCHOImpl;
import eu.europeana.corelib.solr.server.EdmMongoServer;
import eu.europeana.corelib.solr.server.importer.util.ProvidedCHOFieldInput;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/corelib-solr-context.xml", "/corelib-solr-test.xml" })
public class ProvidedCHOFieldInputTest {

	@Resource(name = "corelib_solr_mongoServer")
	private EdmMongoServer mongoServer;

	@Test
	public void testProvidedCHO() {
		ProvidedCHOType providedCHO = new ProvidedCHOType();
		providedCHO.setAbout("test about");
		
		List<SameAs> sameAsList = new ArrayList<SameAs>();
		SameAs sameAs = new SameAs();
		sameAs.setResource("test same as");
		sameAsList.add(sameAs);
		providedCHO.setSameAList(sameAsList);

		// create mongo providedCHO
		try {
			ProvidedCHOImpl providedCHOMongo = new ProvidedCHOFieldInput()
					.createProvidedCHOMongoFields(providedCHO, mongoServer);
			assertEquals(providedCHO.getAbout(), providedCHOMongo.getAbout());
			assertEquals(providedCHO.getSameAList().get(0).getResource(),
					providedCHOMongo.getOwlSameAs()[0]);
			
			SolrInputDocument solrDocument = new SolrInputDocument();
			solrDocument = new ProvidedCHOFieldInput().createProvidedCHOFields(
					providedCHO, solrDocument);
			assertEquals(
					providedCHO.getAbout(),
					solrDocument.getFieldValue(EdmLabel.EUROPEANA_ID.toString()));
			
			assertEquals(providedCHO.getSameAList().get(0).getResource(),
					solrDocument.getFieldValue(EdmLabel.PROXY_OWL_SAMEAS.toString()));
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@After
	public void cleanup() {
		mongoServer.getDatastore().getDB().dropDatabase();
	}
}
