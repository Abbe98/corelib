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

package eu.europeana.corelib.definitions.db.entity.relational;

import java.util.Date;
import java.util.Set;

import eu.europeana.corelib.definitions.db.entity.relational.abstracts.IdentifiedEntity;
import eu.europeana.corelib.definitions.users.Role;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
public interface User extends IdentifiedEntity<Long> {

	void setEmail(String email);

	String getEmail();

	String getPassword();

	void setPassword(String password);

	String getApiKey();

	void setApiKey(String apiKey);

	Date getRegistrationDate();

	void setRegistrationDate(Date registrationDate);

	Date getLastLogin();

	void setLastLogin(Date lastLogin);

	Role getRole();

	void setRole(Role role);

	String getUserName();

	void setUserName(String userName);

	Set<SavedItem> getSavedItems();

	Set<SavedSearch> getSavedSearches();

	Set<SocialTag> getSocialTags();
}