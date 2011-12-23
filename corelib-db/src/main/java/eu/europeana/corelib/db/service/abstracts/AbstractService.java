/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they 
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "License");
 * you may not use this work except in compliance with the
 * License.
 * You may obtain a copy of the License at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the License is
 * distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 */

package eu.europeana.corelib.db.service.abstracts;

import java.io.Serializable;
import java.util.List;

import eu.europeana.corelib.db.entity.abstracts.IdentifiedEntity;
import eu.europeana.corelib.db.exception.DatabaseException;

/**
 * @author Willem-Jan Boogerd <www.eledge.net>
 */
public interface AbstractService<E extends IdentifiedEntity<?>> {

	/**
	 * 
	 * 
	 * @param object
	 * @throws DatabaseException 
	 */
	E store(E object) throws DatabaseException;

	/**
	 * 
	 * 
	 * @param object
	 * @throws DatabaseException 
	 */
	void remove(E object) throws DatabaseException;

	/*
	 * FINDERS
	 */

	/**
	 * Find all elements for this service.
	 * 
	 * @return All entities for the defined entity type
	 */
	List<E> findAll();

	/**
	 * 
	 * 
	 * @param id
	 * @return
	 * @throws DatabaseException 
	 */
	E findByID(final Serializable id) throws DatabaseException;

}
