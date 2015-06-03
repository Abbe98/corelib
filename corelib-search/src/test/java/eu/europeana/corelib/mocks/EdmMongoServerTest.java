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
package eu.europeana.corelib.mocks;

import org.junit.Ignore;
import org.junit.Test;

import eu.europeana.corelib.mongo.server.impl.EdmMongoServerImpl;

/**
 * Mock for EdmMongoServeImpl to bypass spring injection
 * @author Yorgos.Mamakis@ kb.nl
 *
 */
@Ignore
public class EdmMongoServerTest extends EdmMongoServerImpl {
	//Should be empty or else Junit complains

}
