package eu.europeana.corelib.db.service;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eu.europeana.corelib.db.dao.NosqlDao;
import eu.europeana.corelib.db.entity.enums.RecordType;
import eu.europeana.corelib.db.entity.nosql.ApiLog;
import eu.europeana.corelib.db.entity.nosql.ImageCache;
import eu.europeana.corelib.db.service.ApiLogService;
import eu.europeana.corelib.utils.DateIntervalUtils;
import eu.europeana.corelib.utils.model.DateInterval;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/corelib-db-context.xml", "/corelib-db-test.xml"})
public class ApiLogServiceTest {

	String apiKey = "testKey";

	@Resource private ApiLogService apiLogService;

	@Resource(name = "corelib_db_apiLogDao")
	NosqlDao<ImageCache, String> apiLogDao;

	/**
	 * Initialise the testing session
	 * 
	 * @throws IOException
	 */
	@Before
	public void setup() throws IOException {
		apiLogDao.getCollection().drop();
	}

	@Test
	public void testCountByApiKeyByInterval() throws CloneNotSupportedException {
		DateInterval interval = DateIntervalUtils.getToday();

		long count = apiLogService.countByApiKeyByInterval(apiKey, interval);
		assertNotNull(count);
		assertEquals(0, count);

		apiLogService.logApiRequest(apiKey, "paris", RecordType.SEARCH, "standard");
		apiLogService.logApiRequest(apiKey, "berlin", RecordType.SEARCH, "standard");

		// the interval contains the end date, which was before insertions, so we have to refresh it.
		interval.setEnd(new Date());
		long count2 = apiLogService.countByApiKeyByInterval(apiKey, interval);
		assertNotNull(count2);
		assertEquals(2, count2);
	}

	@Test
	public void testCountByApiKey() {
		long count = apiLogService.countByApiKey(apiKey);
		assertNotNull(count);
		assertEquals(0, count);

		apiLogService.logApiRequest(apiKey, "paris", RecordType.SEARCH, "standard");
		apiLogService.logApiRequest(apiKey, "berlin", RecordType.SEARCH, "standard");

		count = apiLogService.countByApiKey(apiKey);
		assertNotNull(count);
		assertEquals(2, count);
	}

	@Test
	public void testFindByApiKey() {
		List<ApiLog> logs = apiLogService.findByApiKey(apiKey);
		assertNotNull(logs);
		assertEquals(0, logs.size());

		apiLogService.logApiRequest(apiKey, "paris", RecordType.SEARCH, "standard");
		apiLogService.logApiRequest(apiKey, "berlin", RecordType.SEARCH, "standard");

		logs = apiLogService.findByApiKey(apiKey);
		assertNotNull(logs);
		assertEquals(2, logs.size());
	}

	@Test
	public void testCountByInterval() {
		DateInterval interval = DateIntervalUtils.getToday();

		long count = apiLogService.countByInterval(interval);
		assertNotNull(count);
		assertEquals(0, count);

		apiLogService.logApiRequest(apiKey, "paris", RecordType.SEARCH, "standard");
		apiLogService.logApiRequest(apiKey, "berlin", RecordType.SEARCH, "standard");

		interval.setEnd(new Date());
		long count2 = apiLogService.countByInterval(interval);
		assertNotNull(count2);
		assertEquals(2, count2);
	}
}
