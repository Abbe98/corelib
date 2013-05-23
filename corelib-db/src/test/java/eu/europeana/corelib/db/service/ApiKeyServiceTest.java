package eu.europeana.corelib.db.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import javax.annotation.Resource;

import org.apache.commons.lang.math.RandomUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eu.europeana.corelib.db.dao.NosqlDao;
import eu.europeana.corelib.db.entity.enums.RecordType;
import eu.europeana.corelib.db.entity.nosql.ImageCache;
import eu.europeana.corelib.db.entity.relational.ApiKeyImpl;
import eu.europeana.corelib.db.exception.DatabaseException;
import eu.europeana.corelib.db.exception.LimitReachedException;
import eu.europeana.corelib.definitions.db.entity.relational.ApiKey;
import eu.europeana.corelib.definitions.db.entity.relational.Token;
import eu.europeana.corelib.definitions.db.entity.relational.User;
import eu.europeana.corelib.definitions.exception.ProblemType;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/corelib-db-context.xml" , "/corelib-db-test.xml"})
// "/corelib-db-test.xml"
public class ApiKeyServiceTest {

	private static final long DEFAULT_USAGE_LIMIT = 10000;

	@Resource
	private UserService userService;

	@Resource
	private ApiKeyService apiKeyService;

	@Resource
	private TokenService tokenService;

	@Resource
	private ApiLogService apiLogService;

	@Resource(name = "corelib_db_apiLogDao")
	NosqlDao<ImageCache, String> apiLogDao;

	@Test
	public void createApiKeyTest() throws DatabaseException {
		String email = "test@kb.nl";
		String apiKey = generatePassPhrase(9);
		String privateKey = generatePassPhrase(9);
		String tokenString = "test_token_new";
		String username = "test";
		String company = "test_company";
		String country = "europe";
		String firstName = "testName";
		String lastName= "testLastName";
		String website = "test_website";
		String address = "test_address";
		String phone = "test_phone";
		String fieldOfWork = "test_fieldOfWork";
		
		Token token = tokenService.create(email);
		token.setToken(tokenString);
		tokenService.store(token);
		ApiKey createdApiKey = apiKeyService.createApiKey(tokenString, email, apiKey, privateKey,
				DEFAULT_USAGE_LIMIT, username, company, country, firstName,
				lastName, website, address, phone, fieldOfWork);
		User user = createdApiKey.getUser();
		assertEquals(apiKey, createdApiKey.getId());
		assertEquals(user.getId(), createdApiKey.getUser().getId());

		userService.remove(user);
	}

	@Test
	public void checkReachedLimitSuccessTest() {
		apiLogDao.getCollection().drop();

		ApiKey apiKey = new ApiKeyImpl();
		apiKey.setApiKey("testKey");
		apiKey.setPrivateKey("testKey");
		apiKey.setUsageLimit(2);

		apiLogService.logApiRequest(apiKey.getId(), "paris", RecordType.SEARCH, "standard");

		int branch = -1;
		try {
			long requested = apiKeyService.checkReachedLimit(apiKey);
			assertEquals(1, requested);
			branch = 1;
		} catch (DatabaseException e) {
			fail("This line should not be reached");
			branch = 2;
		} catch (LimitReachedException e) {
			fail("This line should not be reached");
			branch = 3;
		}
		assertEquals(1, branch);
	}

	@Test
	public void checkReachedLimitWithDatabaseExceptionTest() {
		apiLogDao.getCollection().drop();

		ApiKey apiKey = new ApiKeyImpl();
		apiKey.setApiKey("testKey");
		apiKey.setPrivateKey("testKey");
		apiKey.setUsageLimit(2);

		apiLogService.logApiRequest(apiKey.getId(), "paris", RecordType.SEARCH, "standard");

		int branch = -1;
		try {
			long requested = apiKeyService.checkReachedLimit(null);
			fail("This line should not be reached");
			branch = 1;
		} catch (DatabaseException e) {
			assertEquals(ProblemType.INVALIDARGUMENTS, e.getProblem());
			branch = 2;
		} catch (LimitReachedException e) {
			fail("This line should not be reached");
			branch = 3;
		}
		assertEquals(2, branch);
	}

	@Test
	public void checkReachedLimitWithLimitReachedExceptionTest() {
		apiLogDao.getCollection().drop();

		ApiKey apiKey = new ApiKeyImpl();
		apiKey.setApiKey("testKey");
		apiKey.setPrivateKey("testKey");
		apiKey.setUsageLimit(2);

		apiLogService.logApiRequest(apiKey.getId(), "paris", RecordType.SEARCH, "standard");
		apiLogService.logApiRequest(apiKey.getId(), "berlin", RecordType.SEARCH, "standard");

		int branch = -1;
		try {
			long requested = apiKeyService.checkReachedLimit(apiKey);
			fail("This line should never be reached");
			branch = 1;
		} catch (DatabaseException e) {
			fail("This line should never be reached");
			branch = 2;
		} catch (LimitReachedException e) {
			assertEquals(2, e.getRequested());
			assertEquals(2, e.getLimit());
			branch = 3;
		}
		assertEquals(3, branch);
	}

	private String generatePassPhrase(int length) {
		// This variable contains the list of allowable characters for the
		// pass phrase. Note that the number 0 and the letter 'O' have been
		// removed to avoid confusion between the two. The same is true
		// of 'I', 1, and 'l'.
		final char[] allowableCharacters = new char[] { 'a', 'b', 'c', 'd',
				'e', 'f', 'g', 'h', 'i', 'j', 'k', 'm', 'n', 'o', 'p', 'q',
				'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C',
				'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M', 'N', 'P', 'Q',
				'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '2', '3', '4',
				'5', '6', '7', '8', '9' };

		final int max = allowableCharacters.length - 1;

		StringBuilder pass = new StringBuilder();

		for (int i = 0; i < length; i++) {
			pass.append(allowableCharacters[RandomUtils.nextInt(max)]);
		}

		return pass.toString();
	}
}
