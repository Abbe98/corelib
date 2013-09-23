package eu.europeana.corelib.web.service;

import static org.junit.Assert.assertEquals;

import java.io.UnsupportedEncodingException;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/corelib-web-context.xml", "/corelib-web-test.xml" })
public class EuropeanaUrlServiceTest extends AbstractJUnit4SpringContextTests {

	@Resource
	private EuropeanaUrlService europeanaUrlService;

	
	@Test
	public void getApi2RedirectTest() {
		String expected = "http://localhost:8080/api/123/redirect?shownAt=http://www.kb.nl&provider=example&id=http://www.europeana.eu/resolve/record/kb/record&profile=full";
		assertEquals("getPortalHome full url failed", expected, europeanaUrlService.getApi2Redirect(123, "http://www.kb.nl", "example", "/kb/record", "full").toString());
	}
	
	@Test
	public void getPortalHomeTest() {
		String expected = "http://localhost:8081/portal/";
		assertEquals("getPortalHome full url failed", expected, europeanaUrlService.getPortalHome(false).toString());

		expected = "/portal/";
		assertEquals("getPortalHome relative url failed", expected, europeanaUrlService.getPortalHome(true).toString());
	}

	@Test
	public void getPortalResolveTest() {
		String expected = "http://www.europeana.eu/resolve/record/euro/testrecord";
		assertEquals("getPortalResolve url failed", expected, europeanaUrlService.getPortalResolve("/euro/testrecord"));

	}

	@Test
	public void getPortalSearchTest() throws UnsupportedEncodingException {
		final String query = "leerdam";
		final String rows = "24";
		String expected = "http://localhost:8081/portal/search.html?query=leerdam&rows=24";
		assertEquals("getPortalHome full url failed", expected, europeanaUrlService.getPortalSearch(false, query, rows)
				.toString());

		expected = "/portal/search.html?query=leerdam&rows=24";
		assertEquals("getPortalHome relative url failed", expected,
				europeanaUrlService.getPortalSearch(true, query, rows).toString());
	}

	@Test
	public void getPortalRecordTest() {
		final String collectionId = "euro";
		final String recordId = "testrecord";
		String expected = "http://localhost:8081/portal/record/euro/testrecord.html";
		assertEquals("getPortalHome full url failed", expected,
				europeanaUrlService.getPortalRecord(false, collectionId, recordId).toString());

		expected = "/portal/record/euro/testrecord.html";
		assertEquals("getPortalHome relative url failed", expected,
				europeanaUrlService.getPortalRecord(true, collectionId, recordId).toString());
		
		assertEquals("getPortalHome relative url failed", expected,
				europeanaUrlService.getPortalRecord(true, "/euro/testrecord").toString());

	}

}
