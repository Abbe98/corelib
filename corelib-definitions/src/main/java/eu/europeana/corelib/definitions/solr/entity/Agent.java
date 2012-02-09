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

package eu.europeana.corelib.definitions.solr.entity;

import java.util.Date;

import org.bson.types.ObjectId;

/**
 * EDM Agent fields representation
 * 
 * @author Yorgos.Mamakis@ kb.nl
 * 
 */
public interface Agent extends ContextualClass {

	/**
	 * Retrieve the unique agent id (could be anything)
	 * 
	 * @return the unique ObjectId
	 */
	ObjectId getAgentId();

	/**
	 * Retrieves the edm:begin field of an edm:Actor
	 * 
	 * @return Date representing the birth of an actor
	 */
	Date getBegin();

	/**
	 * Retrieves the edm:end
	 * 
	 * @return Date representing the death of an actor
	 */
	Date getEnd();

	void setAgentId(ObjectId agentId);

	void setBegin(Date begin);

	void setEnd(Date end);

}
