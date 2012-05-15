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

package eu.europeana.corelib.tools.lookuptable;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.mongodb.Mongo;

import eu.europeana.corelib.solr.MongoServer;

/**
 * Class for creating and accessing the Collection Lookup table
 * 
 * @author yorgos.mamakis@ kb.nl
 * 
 */
public class CollectionMongoServer implements MongoServer {

	private Mongo mongoServer;
	private String databaseName;
	private Datastore datastore;

	/**
	 * Constructor for the CollectionMongoServer to ensure that everything has
	 * been set upon initialization
	 * 
	 * @param mongoServer
	 *            The Mongo Server to connect to
	 * @param databaseName
	 *            The database to connect to
	 */
	public CollectionMongoServer(Mongo mongoServer, String databaseName) {
		this.mongoServer = mongoServer;
		this.databaseName = databaseName;
		createDatastore();
	}

	private void createDatastore() {
		Morphia morphia = new Morphia();
		morphia.map(Collection.class);
		datastore = morphia.createDatastore(mongoServer, databaseName);
		datastore.ensureIndexes();
	}

	/**
	 * Return the datastore. Useful for exposing surplus functionality
	 */
	@Override
	public Datastore getDatastore() {
		return this.datastore;
	}

	/**
	 * Close the connection to the Mongo server
	 */
	@Override
	public void close() {
		mongoServer.close();
	}

	/**
	 * Retrieve the new CollectionId based on the old CollectionID. This is a
	 * fast method as the old CollectionID is indexed in the Mongo Schema
	 * 
	 * @param oldCollectionId
	 *            The old Collection ID
	 * @return
	 */
	public String findNewCollectionId(String oldCollectionId) {
		return datastore.find(Collection.class).field("oldCollectionId")
				.equal(oldCollectionId).get() != null ? datastore
				.find(Collection.class).field("oldCollectionId")
				.equal(oldCollectionId).get().getNewCollectionId() : null;
	}

	/**
	 * Retrieve the old CollectionId based on the new CollectionID. This is a
	 * slow method as the new CollectionId is NOT indexed in the Mongo Schema
	 * 
	 * @param newCollectionId
	 * @return
	 */
	public String findOldCollectionId(String newCollectionId) {
		return datastore.find(Collection.class).field("newCollectionId")
				.equal(newCollectionId).get() != null ? datastore
				.find(Collection.class).field("newCollectionId")
				.equal(newCollectionId).get().getOldCollectionId() : null;
	}

	/**
	 * Save a Collection
	 * @param collection The collection to save
	 */
	public void saveCollection(Collection collection) {
		datastore.save(collection);
	}
}
