package eu.europeana.corelib.db.wrapper;

import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import eu.europeana.corelib.storage.MongoProvider;
import eu.europeana.corelib.storage.impl.MongoProviderImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

/**
 * Let the search-api (and other api's?) connect to a mongo database with Morphia
 * This class uses a basic Morphia connection without any mappings or other specific settings
 */
public class ApiMongoConnector {

    private static final Logger LOG = LogManager.getLogger(ApiMongoConnector.class);

    private MongoClient mongoClient;
    private String      label;

    /**
     * Create a Morphia connection using a mongo connection url. Note that a database name is required in the url
     * @deprecated  not called from anywhere
     * @param connectionUrl, e.g. mongodb://user:password@mongo1.eanadev.org:27000/europeana_1?replicaSet=europeana
     * @return DataStore
     * @see <a href="http://api.mongodb.com/java/current/com/mongodb/MongoClientURI.html">
     * MongoClientURI documentation</a>
     */
    @Deprecated
    public Datastore createDatastore(String connectionUrl) {
        MongoProvider mongo = new MongoProviderImpl(connectionUrl);
        this.label       = mongo.getDefaultDatabase();
        this.mongoClient = mongo.getMongoClient();
        LOG.info("[corelib.db ApiMongoConnector] Creating Morphia datastore for databases {}", label);
        return new Morphia().createDatastore(mongoClient, label);
    }

    /**
     * Create a basic connection to do get/delete/save operations on the database
     * @deprecated      this method is not called from anywhere
     * @param label     The label of the server to connect to (for logging purposes only)
     * @param host      The host to connect to
     * @param port      The port to connect to
     * @param dbName    The database to connect to
     * @param username  Username for connection
     * @param password  Password for connection
     * @return datastore
     */
    @Deprecated
    public Datastore createDatastore(String label,
                                     String host,
                                     int port,
                                     String dbName,
                                     String username,
                                     String password) {
        Datastore datastore  = null;
        Morphia   connection = new Morphia();
        try {
            this.label       = label;
            this.mongoClient = new MongoProviderImpl(host, String.valueOf(port), dbName, username, password).getMongoClient();
            LOG.info("[corelib.db ApiMongoConnector] Creating Morphia datastore for database {}", dbName);
            datastore = connection.createDatastore(mongoClient, dbName);
        } catch (MongoException e) {
            LOG.error(e.getMessage(), e);
        }
        return datastore;
    }

    /**
     * Create a basic connection to do get/delete/save operations on the database
     * Any required login credentials should already be stored in the provided MongoClient object
     * @deprecated          not used
     * @param label         The label of the server to connect to (for logging purposes only)
     * @param mongoClient   mongoClient
     * @param dbName        dbName
     * @return datastore
     */
    @Deprecated
    public Datastore createDatastore(String label, MongoClient mongoClient, String dbName) {
        Datastore datastore  = null;
        Morphia   connection = new Morphia();
        try {
            this.label       = label;
            this.mongoClient = mongoClient;
            LOG.info("[corelib.db ApiMongoConnector] Creating Morphia {} datastore for database {}", label, dbName);
            datastore = connection.createDatastore(mongoClient, dbName);
        } catch (MongoException e) {
            LOG.error(e.getMessage(), e);
        }
        return datastore;
    }

    /**
     * Close the connection to the database
     */
    public void close() {
        if (mongoClient != null) {
            LOG.info("[corelib.db ApiMongoConnector] Closing MongoClient for {}", label);
            mongoClient.close();
        }
    }
}
