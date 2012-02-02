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

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eu.europeana.corelib.db.exception.DatabaseException;
import eu.europeana.corelib.definitions.db.entity.Token;
import eu.europeana.corelib.definitions.db.entity.User;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/corelib-db-context.xml", "/corelib-db-test.xml" })
public class AuthorisationServiceTest {
	
	@Resource
	private UserService userService;

	@Resource
	private TokenService tokenService;
	
	@Resource
	private AuthorisationService authorisationService;
	
	private static final String APIKEY = "TESTKEY";
	private boolean keyCreated = false;
	
	@Before
	public void setupKey() throws DatabaseException {
		if (!keyCreated) {
			Token token = tokenService.create("apikey@europeana.eu");
			User user = userService.create(token.getToken(), "apikey", "apikey");
			user.setApiKey(APIKEY);
			userService.store(user);
			keyCreated = true;
		}
	}


}
