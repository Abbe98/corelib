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

import eu.europeana.corelib.definitions.jibx.Rights;
import eu.europeana.corelib.definitions.jibx.Rights1;
import eu.europeana.corelib.definitions.jibx.WebResourceType;
import eu.europeana.corelib.definitions.model.EdmLabel;
import eu.europeana.corelib.solr.entity.WebResourceImpl;
import eu.europeana.corelib.solr.server.EdmMongoServer;
import eu.europeana.corelib.solr.server.importer.util.WebResourcesFieldInput;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/corelib-solr-context.xml", "/corelib-solr-test.xml" })
public class WebResourcesFieldInputTest {
	@Resource(name = "corelib_solr_mongoServer")
	private EdmMongoServer mongoServer;
	
	@Test
	public void testWebResource(){
		WebResourceType webResource = new WebResourceType();
		webResource.setAbout("test about");
		Rights1 rights = new Rights1();
		eu.europeana.corelib.definitions.jibx.ResourceOrLiteralType.Resource rightsResource = 
				new eu.europeana.corelib.definitions.jibx.ResourceOrLiteralType.Resource();
		rightsResource.setResource("test resource");
		rights.setResource(rightsResource);
		webResource.setRights(rights);
		List<Rights> rightsList = new ArrayList<Rights>();
		Rights rights1 = new Rights();
		eu.europeana.corelib.definitions.jibx.ResourceOrLiteralType.Resource rights1Resource = 
				new eu.europeana.corelib.definitions.jibx.ResourceOrLiteralType.Resource();
		rights1Resource.setResource("test rights");
		rights1.setResource(rights1Resource);
		rightsList.add(rights1);
		webResource.setRightList(rightsList);
		//create mongo
		WebResourceImpl webResourceMongo = new WebResourcesFieldInput().createWebResourceMongoField(webResource, mongoServer);
		assertEquals(webResource.getAbout(), webResourceMongo.getAbout());
		assertEquals(webResource.getRights().getResource(), webResourceMongo.getWebResourceEdmRights().values().iterator().next().get(0));
		assertEquals(webResource.getRightList().get(0).getResource(), webResourceMongo.getWebResourceDcRights().values().iterator().next().get(0));
		
		//create solr document
		SolrInputDocument solrDocument = new SolrInputDocument();
		try {
			solrDocument = new WebResourcesFieldInput().createWebResourceSolrFields(webResource, solrDocument);
			assertEquals(webResource.getAbout(), solrDocument.getFieldValue(EdmLabel.EDM_WEB_RESOURCE.toString()));
			assertEquals(webResource.getRights().getResource(), solrDocument.getFieldValue(EdmLabel.WR_EDM_RIGHTS.toString()));
			assertEquals(webResource.getRightList().get(0).getResource(), solrDocument.getFieldValue(EdmLabel.WR_DC_RIGHTS.toString()));
			
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@After
	public void cleanup(){
		mongoServer.getDatastore().getDB().dropDatabase();
	}
}
