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

package eu.europeana.corelib.db.service;

import eu.europeana.corelib.db.dao.RelationalDao;
import eu.europeana.corelib.db.entity.relational.TokenImpl;
import eu.europeana.corelib.db.entity.relational.UserImpl;
import eu.europeana.corelib.db.exception.DatabaseException;
import eu.europeana.corelib.definitions.db.entity.relational.*;
import eu.europeana.corelib.definitions.solr.DocType;
import eu.europeana.corelib.definitions.users.Role;
import eu.europeana.corelib.solr.service.mock.SearchServiceMock;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import static org.junit.Assert.*;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/corelib-db-context.xml", "/corelib-db-test.xml"})
public class UserServiceTest {

    @Resource
    private UserService userService;

    @Resource
    private TokenService tokenService;

    @Resource(name = "corelib_db_tokenDao")
    private RelationalDao<TokenImpl> tokenDao;

    @Resource(name = "corelib_db_userDao")
    private RelationalDao<UserImpl> userDao;

    @Before
    public void prepareDatabase() {
        userDao.deleteAll();
        tokenDao.deleteAll();
    }

    @Test
    public void testCreate() throws DatabaseException {
        final String EMAIL = "testCreate@europeana.eu";
        final String USERNAME = "testCreate";
        final String PASSWORD = "test";
        final String HASH = hashPassword(PASSWORD);

        Token token = tokenService.create(EMAIL);
        assertNotNull("Unable to create token", token);

        User user = userService.create(token.getToken(), USERNAME, PASSWORD);
        assertNotNull("Unable to create user", user);

        user = userService.findByID(user.getId());
        assertNotNull("Unable to retrieve user", user);
        assertEquals("Email address not copied from token.", user.getEmail(),
                token.getEmail());
        assertEquals("Username not stored correctly.", user.getUserName(),
                USERNAME);
        assertFalse("Password not encoded at all.",
                StringUtils.equals(user.getPassword(), PASSWORD));
        assertEquals("Password not correctly encoded.", user.getPassword(),
                HASH);
        assertNotNull("No User ID generated", user.getId());
        assertNotNull("User registration date should have value",
                user.getRegistrationDate());
        assertNull("User last login should by null for new user",
                user.getLastLogin());
        assertEquals("User should have USER role by default", user.getRole(),
                Role.ROLE_USER);
        // create does not delete the token anymore
        // assertNull("Token not removed from database",
        // tokenService.findByID(token.getToken()));
        tokenService.remove(token);
    }

    @Test(expected = DatabaseException.class)
    public void testCreateWithoutToken() throws DatabaseException {
        userService.create(null, "ignore", "ignore");
        fail("This line should never be reached!!!");
    }

    @Test(expected = DatabaseException.class)
    public void testCreateWithInvalidToken() throws DatabaseException {
        userService.create("invalidToken", "ignore", "ignore");
        fail("This line should never be reached!!!");
    }

    @Test
    public void testFindByEmail() throws DatabaseException {
        final String EMAIL = "testFindByEmail@europeana.eu";
        final String USERNAME = "testFindByEmail";
        final String PASSWORD = "test";
        final String HASH = hashPassword(PASSWORD);

        Token token = tokenService.create(EMAIL);
        assertNotNull("Unable to create token", token);

        User user = userService.create(token.getToken(), USERNAME, PASSWORD);
        assertNotNull("Unable to create user", user);

        user = userService.findByEmail(EMAIL);
        assertNotNull("Unable to retrieve user by email adres", user);
        assertEquals("Password not correctly encoded.", user.getPassword(),
                HASH);
        assertNotNull("No User ID generated", user.getId());

    }

    @Test
    public void testChangePassword() throws DatabaseException {
        final String EMAIL = "testChangePassword@europeana.eu";
        final String USERNAME = "testChangePassword";
        final String PASSWORD1 = "test";
        final String PASSWORD2 = "changed";
        final String HASH1 = hashPassword(PASSWORD1);
        final String HASH2 = hashPassword(PASSWORD2);

        Token token = tokenService.create(EMAIL);
        assertNotNull("Unable to create token", token);

        User user = userService.create(token.getToken(), USERNAME, PASSWORD1);
        assertNotNull("Unable to create user", user);
        assertEquals("Password not correctly encoded.", user.getPassword(), HASH1);

        user = userService.changePassword(user.getId(), HASH1, PASSWORD2);
        assertEquals("Password not correctly changed/encoded.",
                user.getPassword(), HASH2);

        try {
            // try changing with wrong password
            userService.changePassword(user.getId(), PASSWORD1, PASSWORD2);
            fail("This line should never be reached!!!");
        } catch (DatabaseException e) {
            // expecting this
        }

        try {
            // try changing with wrong new password
            userService.changePassword(user.getId(), PASSWORD2, "");
            fail("This line should never be reached!!!");
        } catch (DatabaseException e) {
            // expecting this
        }

        try {
            // try changing with wrong user id
            userService.changePassword(Long.MAX_VALUE, PASSWORD1, PASSWORD2);
            fail("This line should never be reached!!!");
        } catch (DatabaseException e) {
            // expecting this
        }

        user = userService.authenticateUser(EMAIL, PASSWORD2);
        assertNotNull("Authenticate method is NOT return user with valid password", user);

    }

    @Test
    public void testAuthenticateUser() throws DatabaseException {
        final String EMAIL = "testAuthenticateUser@europeana.eu";
        final String USERNAME = "testAuthenticateUser";
        final String PASSWORD = "test";

        Token token = tokenService.create(EMAIL);
        assertNotNull("Unable to create token", token);

        User user = userService.create(token.getToken(), USERNAME, PASSWORD);
        assertNotNull("Unable to create user", user);

        user = userService.authenticateUser(EMAIL, "invalidPassword");
        assertNull("Authenticate method is return user with invalid password??", user);

        user = userService.authenticateUser("invalidEmail@europeana.eu", PASSWORD);
        assertNull("Authenticate method is return user with invalid email??", user);

        user = userService.authenticateUser(EMAIL, PASSWORD);
        assertNotNull("Authenticate method is NOT return user with valid password", user);
    }

    @Test
    public void testCreateSavedSearch() throws DatabaseException {
        final String EMAIL = "testCreateSavedSearch@europeana.eu";
        final String USERNAME = "testCreateSavedSearch";
        final String PASSWORD = "test";
        Token token = tokenService.create(EMAIL);
        assertNotNull("Unable to create token", token);

        User user = userService.create(token.getToken(), USERNAME, PASSWORD);
        assertNotNull("Unable to create user", user);
        assertTrue("Saved Searches list should be empty!", user.getSavedSearches().size() == 0);

        try {
            userService.createSavedSearch(Long.MAX_VALUE, "ignore", "ignore");
            fail("This line should never be reached!!!");
        } catch (DatabaseException e) {
            // expecting this
        }

        try {
            userService.createSavedSearch(null, "ignore", "ignore");
            fail("This line should never be reached!!!");
        } catch (DatabaseException e) {
            // expecting this
        }

        try {
            userService.createSavedSearch(user.getId(), " ", "ignore");
            fail("This line should never be reached!!!");
        } catch (DatabaseException e) {
            // expecting this
        }

        userService.createSavedSearch(user.getId(), "query1", "queryString1");

        user = userService.findByEmail(EMAIL);
        assertTrue("Saved Searches list should have 3 elements!", user.getSavedSearches().size() == 1);

        SavedSearch savedSearch = user.getSavedSearches().iterator().next();
        assertEquals("query1", savedSearch.getQuery());
        assertEquals("queryString1", savedSearch.getQueryString());
        assertNotNull("No creation date set", savedSearch.getDateSaved());

        userService.createSavedSearch(user.getId(), "query2", "queryString2");
        userService.createSavedSearch(user.getId(), "query3", "queryString3");

        user = userService.findByEmail(EMAIL);
        assertTrue("Saved Searches list should have 3 elements!", user.getSavedSearches().size() == 3);

        SavedSearch[] savedSearches =
                user.getSavedSearches().toArray(new SavedSearch[user.getSavedSearches().size()]);
        userService.removeSavedSearch(user.getId(), savedSearches[1].getId());
        user = userService.findByEmail(EMAIL);
        assertTrue("Saved Searches list should be one less!", user
                .getSavedSearches().size() == 2);
    }

    @Test
    public void testCreateSavedItem() throws DatabaseException {
        final String EMAIL = "testCreateSavedItem@europeana.eu";
        final String USERNAME = "testCreateSavedItem";
        final String PASSWORD = "test";
        final String EUROPEANA_ID = "testCreateSavedItem";

        Token token = tokenService.create(EMAIL);
        assertNotNull("Unable to create token", token);

        User user = userService.create(token.getToken(), USERNAME, PASSWORD);
        assertNotNull("Unable to create user", user);
        assertTrue("Saved Items list should be empty!", user.getSavedItems().size() == 0);

        try {
            userService.createSavedItem(Long.MAX_VALUE, EUROPEANA_ID);
            fail("This line should never be reached!!!");
        } catch (DatabaseException e) {
            // expecting this
        }

        userService.createSavedItem(user.getId(), EUROPEANA_ID);
        user = userService.findByEmail(EMAIL);
        assertTrue("Saved Items list should be one!", user.getSavedItems().size() == 1);

        SavedItem item = user.getSavedItems().iterator().next();
        assertEquals(EUROPEANA_ID, item.getEuropeanaUri());
        assertEquals(SearchServiceMock.THUMBNAIL[0], item.getEuropeanaObject());
        assertEquals(SearchServiceMock.TITLE[0], item.getTitle());
        assertEquals(SearchServiceMock.AUTHOR[0], item.getAuthor());
        assertEquals(DocType.TEXT, item.getDocType());
        assertNotNull("No creation date set", item.getDateSaved());

        userService.removeSavedItem(user.getId(), item.getId());
        user = userService.findByEmail(EMAIL);
        assertTrue("Saved Items list should be empty!", user.getSavedItems().size() == 0);
    }

    @Test
    public void testCreateSocialTag() throws DatabaseException {
        final String EMAIL = "testCreateSocialTag@europeana.eu";
        final String USERNAME = "testCreateSocialTag";
        final String PASSWORD = "test";
        final String EUROPEANA_ID = "testCreateSocialTag";
        final String TAG = "testCreateSocialTag";

        Token token = tokenService.create(EMAIL);
        assertNotNull("Unable to create token", token);

        User user = userService.create(token.getToken(), USERNAME, PASSWORD);
        assertNotNull("Unable to create user", user);
        assertTrue("SocialTag list should be empty!", user.getSocialTags().size() == 0);

        try {
            userService.createSocialTag(Long.MAX_VALUE, EUROPEANA_ID, "ignore");
            fail("This line should never be reached!!!");
        } catch (DatabaseException e) {
            // expecting this
        }

        userService.createSocialTag(user.getId(), EUROPEANA_ID, TAG);
        user = userService.findByEmail(EMAIL);
        assertTrue("SocialTag list should be one!", user.getSocialTags().size() == 1);

        SocialTag tag = user.getSocialTags().iterator().next();
        assertEquals(EUROPEANA_ID, tag.getEuropeanaUri());
        assertEquals(SearchServiceMock.THUMBNAIL[0], tag.getEuropeanaObject());
        assertEquals(StringUtils.lowerCase(TAG), tag.getTag());
        assertEquals(SearchServiceMock.TITLE[0], tag.getTitle());
        assertNotNull("No creation date set", tag.getDateSaved());

        userService.removeSocialTag(user.getId(), tag.getId());
        user = userService.findByEmail(EMAIL);
        assertTrue("SocialTag list should be empty!", user.getSocialTags().size() == 0);
    }

    @Test
    public void testUserLanguageSettings() throws DatabaseException {
        final String EMAIL = "testCreateSocialTag@europeana.eu";
        final String USERNAME = "testCreateSocialTag";
        final String PASSWORD = "test";

        final String LANGCODE = "nl";
        final String[] LANGCODES_ARRAY = {"nl", "en", "de"};
        final String LANGCODES = "fi|es|it|fr";

        Token token = tokenService.create(EMAIL);
        assertNotNull("Unable to create token", token);

        User user = userService.create(token.getToken(), USERNAME, PASSWORD);
        assertNotNull("Unable to create user", user);
        assertNull("By default the value should be empty", user.getLanguagePortal());
        assertNull("By default the value should be empty", user.getLanguageItem());
        assertNotNull("By default the value should be empty", user.getLanguageSearch());
        assertTrue("By default it should return an empty array", user.getLanguageSearch().length == 0);

        user = userService.updateUserLanguagePortal(user.getId(), LANGCODE);
        assertEquals("Value should be set to NL", LANGCODE, user.getLanguagePortal());

        user = userService.updateUserLanguageItem(user.getId(), LANGCODE);
        assertEquals("Value should be set to NL", LANGCODE, user.getLanguageItem());

        user = userService.updateUserLanguageSearch(user.getId(), LANGCODES);
        assertNotNull("Should never return null", user.getLanguageSearch());
        assertEquals("Shoud contain all values", 4, user.getLanguageSearch().length);

        user = userService.updateUserLanguageSearch(user.getId(), LANGCODES_ARRAY);
        assertNotNull("Should never return null", user.getLanguageSearch());
        assertEquals("Shoud contain all values", LANGCODES_ARRAY.length, user.getLanguageSearch().length);

        user = userService.updateUserLanguagePortal(user.getId(), null);
        user = userService.updateUserLanguageItem(user.getId(), null);
        user = userService.updateUserLanguageSearch(user.getId(), (String) null);

        assertNotNull("Unable to create user", user);
        assertNull("By default the value should be empty", user.getLanguagePortal());
        assertNull("By default the value should be empty", user.getLanguageItem());
        assertNotNull("By default the value should be empty", user.getLanguageSearch());
        assertTrue("By default it should return an empty array", user.getLanguageSearch().length == 0);
    }

    private String hashPassword(String password) {
        return new ShaPasswordEncoder().encodePassword(password, null);
    }
}
