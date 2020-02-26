package eu.europeana.corelib.lookup.impl;

import java.util.List;

import com.mongodb.MongoClient;
import eu.europeana.corelib.storage.impl.MongoProviderImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import eu.europeana.corelib.storage.MongoServer;
import eu.europeana.corelib.tools.lookuptable.Collection;
import eu.europeana.corelib.tools.lookuptable.CollectionMongoServer;

/**
 * Class for creating and accessing the Collection Lookup table
 * 
 * @author yorgos.mamakis@ kb.nl
 * @author Patrick Ehlert
 * 
 */
public class CollectionMongoServerImpl implements MongoServer, CollectionMongoServer {

	private static final Logger LOG = LogManager.getLogger(CollectionMongoServerImpl.class.getName());

	private MongoClient mongoClient;
	private String databaseName;
	private Datastore datastore;

	/**
	 * Constructor for the CollectionMongoServer to ensure that everything has been set upon initialization
	 * Any required login credentials should be present in the provided mongoClient
	 * 
	 * @param mongoClient the Mongo Server to connect to
	 * @param databaseName the database to connect to
	 */
	public CollectionMongoServerImpl(MongoClient mongoClient, String databaseName) {
		this.mongoClient = mongoClient;
		this.databaseName = databaseName;
		createDatastore();
	}

	/**
	 * Setup a new datastore to do get/delete/save operations on the database
	 * @param datastore
	 */
	public CollectionMongoServerImpl(Datastore datastore){
		this.mongoClient = datastore.getMongo();
		this.databaseName = datastore.getCollection(Collection.class).getName();
		this.datastore = datastore;
	}

	/**
	 * Create a new datastore to do get/delete/save operations on the database
	 * @deprecated 	not called from anywhere
	 * @param 		host
	 * @param 		port
	 * @param 		databaseName
	 * @param 		username
	 * @param 		password
	 */
	@Deprecated
	public CollectionMongoServerImpl(String host, int port, String databaseName, String username, String password) {
		this.mongoClient = new MongoProviderImpl(host, String.valueOf(port), databaseName, username, password).getMongoClient();
		this.databaseName = databaseName;
		createDatastore();
	}

	private void createDatastore() {
		Morphia morphia = new Morphia();
		morphia.map(Collection.class);
		datastore = morphia.createDatastore(mongoClient, databaseName);
		datastore.ensureIndexes();
		LOG.info("[corelib.lookup CollectionMongoServer] CollectionMongoServer datastore is created");
	}

	/**
	 * Return the datastore. Useful for exposing surplus functionality
	 */
	@Override
	public Datastore getDatastore() {
		LOG.info("[corelib.lookup CollectionMongoServer] get datastore");
		return this.datastore;
	}

	/**
	 * Close the connection to the Mongo server
	 */
	@Override
	public void close() {
		if (mongoClient != null) {
			LOG.info("[corelib.lookup CollectionMongoServer] closing MongoClient");
			mongoClient.close();
		}
	}

	/* (non-Javadoc)
	 * @see eu.europeana.corelib.tools.lookuptable.impl.CollectionMongoServer#findNewCollectionId(java.lang.String)
	 */
	@Override
	public String findNewCollectionId(String oldCollectionId) {
		return datastore.find(Collection.class).field("oldCollectionId")
				.equal(oldCollectionId).get() != null ? datastore
				.find(Collection.class).field("oldCollectionId")
				.equal(oldCollectionId).get().getNewCollectionId() : null;
	}

	/* (non-Javadoc)
	 * @see eu.europeana.corelib.tools.lookuptable.impl.CollectionMongoServer#findOldCollectionId(java.lang.String)
	 */
	@Override
	public String findOldCollectionId(String newCollectionId) {
		return datastore.find(Collection.class).field("newCollectionId")
				.equal(newCollectionId).get() != null ? datastore
				.find(Collection.class).field("newCollectionId")
				.equal(newCollectionId).get().getOldCollectionId() : null;
	}

	/* (non-Javadoc)
	 * @see eu.europeana.corelib.tools.lookuptable.impl.CollectionMongoServer#saveCollection(eu.europeana.corelib.tools.lookuptable.Collection)
	 */
	@Override
	public void saveCollection(Collection collection) {
		datastore.save(collection);
	}

	/* (non-Javadoc)
	 * @see eu.europeana.corelib.tools.lookuptable.impl.CollectionMongoServer#retrieveAllCollections()
	 */
	@Override
	public List<Collection> retrieveAllCollections() {
		return datastore.find(Collection.class).asList();
	}

	/* (non-Javadoc)
	 * @see eu.europeana.corelib.tools.lookuptable.impl.CollectionMongoServer#setDatastore(org.mongodb.morphia.Datastore)
	 */
	@Override
	public void setDatastore(Datastore datastore) {
		this.datastore = datastore;
	}
}
