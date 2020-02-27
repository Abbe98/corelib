package eu.europeana.corelib.storage.impl;

import com.mongodb.*;
import com.mongodb.event.*;
import eu.europeana.corelib.storage.MongoProvider;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Helper class to create a MongoClient
 */
public class MongoProviderImpl implements MongoProvider, ConnectionPoolListener {
    private static final Logger LOG                   = LogManager.getLogger(MongoProviderImpl.class);

    // most of these taken from https://stackoverflow.com/questions/6520439/how-to-configure-mongodb-java-driver-mongooptions-for-production-use
    // a fairly old post, but it's very hard to find anything else.
    // Exception is the value for threadsAllowedToBlockForConnectionMultiplier - searching for that yields in
    // anything between 5 and 10000. So I'm using the value most often seen.
    // For all these goes: let's test and see what happens.
    private static final int MAX_CONNECTION_IDLE_MILLIS   = 30000;
    private static final int MAX_CONNECTION_LIFE_MILLIS   = 60000;
    private static final int CONNECTIONS_PER_HOST         = 40;
    private static final int THREADS_MAY_BLOCK_MULTIPLIER = 100;
    private static final int SOCKET_TIMEOUT_MILLIS        = 60000;
    private static final int CONNECT_TIMEOUT_MILLIS       = 30000;

    private MongoClient mongo;
    private String      definedDatabase;

    // I do not see the 2 connections directly after start-up that Patrick reported, so let's start at 0
    private int nrConnections = 0;


    /**
     * Create a new MongoClient based on a connectionUrl, e.g.
     * mongodb://user:password@mongo1.eanadev.org:27000/europeana_1?replicaSet=europeana
     * Called directly from corelib-db ApiMongoConnector
     *
     * @param connectionUrl the connection URL (!)
     * @see <a href="http://api.mongodb.com/java/current/com/mongodb/MongoClientURI.html">
     * MongoClientURI documentation</a>
     */
    public MongoProviderImpl(String connectionUrl) {
        // Let's add a connectionPoolListener so we can keep track of the number of connections
        MongoClientOptions.Builder clientOptionsBuilder = new MongoClientOptions.Builder().addConnectionPoolListener(this);

        // note that in the new driver architecture (v. 3.7 and later) something different is used. I tried that here
        // but it has so many tangles that I think we best reserve that for Api / Corelib v.3
        clientOptionsBuilder.maxConnectionIdleTime(MAX_CONNECTION_IDLE_MILLIS);
        clientOptionsBuilder.maxConnectionLifeTime(MAX_CONNECTION_LIFE_MILLIS);
        clientOptionsBuilder.connectionsPerHost(CONNECTIONS_PER_HOST);
        clientOptionsBuilder.socketTimeout(SOCKET_TIMEOUT_MILLIS);
        clientOptionsBuilder.connectTimeout(CONNECT_TIMEOUT_MILLIS);
        clientOptionsBuilder.threadsAllowedToBlockForConnectionMultiplier(THREADS_MAY_BLOCK_MULTIPLIER);

        MongoClientURI uri = new MongoClientURI(connectionUrl, clientOptionsBuilder);

        definedDatabase = uri.getDatabase();
        LOG.info("[MongoProvider] [constructor] creating new MongoClient for {}, {}",
                 uri.getHosts(),
                 (StringUtils.isEmpty(definedDatabase) ? "default database" : "database: " + definedDatabase));
        mongo = new MongoClient(uri);
        LOG.info("[MongoProvider] [constructor] connection count: {}", this.nrConnections);
    }

    /**
     * Create a new MongoClient without any credentials
     * @deprecated This constructor is not used anywhere
     *
     * @param hosts comma-separated host names
     * @param ports omma-separated port numbers
     */
    @Deprecated
    public MongoProviderImpl(String hosts, String ports) {
        this(hosts, ports, null, null, null, null);
    }

    /**
     * Create a new MongoClient with the supplied credentials
     * <p>
     * Used only in corelib.lookup EuropeanaIdRegistryMongoServerImpl; two other usages (in corelib-db ApiMongoConnector
     * and in corelib.lookup CollectionMongoServerImpl) are unused themselves
     *
     * @param hosts    comma-separated host names
     * @param ports    comma-separated port numbers
     * @param dbName   optional
     * @param username optional
     * @param password optional
     */
    public MongoProviderImpl(String hosts, String ports, String dbName, String username, String password) {
        this(hosts, ports, dbName, username, password, null);
    }

    /**
     * Create a new MongoClient with the supplied credentials and optionsBuilder.
     * If no optionsBuilder is provided a default one will be constructed.
     * Hosts array should contain one or more host names. If number of the hosts is greater
     * than 1 then the ports array should contain an entry for each host or just one port
     * which will be used for all the hosts.
     *
     * Only used from within this class
     *
     * @param hosts          array of host names
     * @param ports          array of ports
     * @param dbName         optional
     * @param username       optional
     * @param password       optional
     * @param optionsBuilder optional
     */
    public MongoProviderImpl(String[] hosts,
                             String[] ports,
                             String dbName,
                             String username,
                             String password,
                             MongoClientOptions.Builder optionsBuilder) {
        List<ServerAddress> serverAddresses = new ArrayList<>();
        int                 i               = 0;
        for (String host : hosts) {
            if (host.length() > 0) {
                try {
                    ServerAddress address = new ServerAddress(host, getPort(ports, i));
                    serverAddresses.add(address);
                } catch (NumberFormatException e) {
                    LOG.error("[MongoProvider] [params constructor] error parsing port numbers", e);
                }
            }
            i++;
        }

        MongoClientOptions.Builder builder = optionsBuilder;
        if (optionsBuilder == null) {
            // use defaults
            builder = MongoClientOptions.builder();
        }

        if (StringUtils.isEmpty(dbName) || StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            LOG.info("[MongoProvider] [params constructor] creating new MongoClient: {} {}",
                     Arrays.toString(hosts),
                     " (no credentials)");
            definedDatabase = null;
            mongo           = new MongoClient(serverAddresses, builder.build());
        } else {
            List<MongoCredential> credentials = new ArrayList<>();
            credentials.add(MongoCredential.createCredential(username, dbName, password.toCharArray()));
            LOG.info("[MongoProvider] [params constructor] Creating new MongoClient - {} {} {} {}",
                     Arrays.toString(hosts),
                     ", database ",
                     dbName,
                     " (with credentials)");
            definedDatabase = dbName;
            mongo           = new MongoClient(serverAddresses, credentials, builder.build());
        }
    }

    private int getPort(String[] ports, int index) {
        if (ports == null || index < 0) {
            throw new NumberFormatException("Empty port");
        }
        if (ports.length > 1 && index < ports.length) {
            return Integer.parseInt(ports[index]);
        }
        return Integer.parseInt(ports[0]);
    }

    /**
     * Create a new MongoClient with the supplied credentials and optionsBuilder
     * If no optionsBuilder is provided a default one will be constructed.
     *
     * Usages:  corelib-storage:    EdmMongoServerImpl, but that method is not used
     *          internally:         #49 (deprecated, nog used)
     *                              #65 => corelib.lookup EuropeanaIdRegistryMongoServerImpl
     *
     * @param hosts          comma-separated host names
     * @param ports          comma-separated port numbers
     * @param dbName         optional
     * @param username       optional
     * @param password       optional
     * @param optionsBuilder optional
     */
    public MongoProviderImpl(String hosts,
                             String ports,
                             String dbName,
                             String username,
                             String password,
                             MongoClientOptions.Builder optionsBuilder) {
        this(StringUtils.split(hosts, ","),
             StringUtils.split(ports, ","),
             dbName,
             username,
             password,
             optionsBuilder);
    }

    /**
     * @see MongoProvider#getMongo()
     */
    @Override
    public MongoClient getMongo() {
        LOG.info("[MongoProvider] [getMongo()] connection count: {}", this.nrConnections);
        return mongo;
    }

    /**
     * @see MongoProvider#getDefaultDatabase()
     */
    public String getDefaultDatabase() {
        return definedDatabase;
    }

    /**
     * @see MongoProvider#close()
     */
    @Override
    public void close() {
        if (mongo != null) {
            LOG.info("[MongoProvider] ... closing MongoClient ... {}", mongo.getServerAddressList().get(0));
            mongo.close();
        }
    }

    @Override
    public void connectionPoolOpened(ConnectionPoolOpenedEvent connectionPoolOpenedEvent) {
        LOG.info("[MongoProvider] Connection pool opened - connection count: {}", nrConnections);
    }

    @Override
    public void connectionPoolClosed(ConnectionPoolClosedEvent connectionPoolClosedEvent) {
        LOG.info("[MongoProvider] Connection pool closed - connection count: {}", nrConnections);
    }

    @Override
    public void connectionCheckedOut(ConnectionCheckedOutEvent connectionCheckedOutEvent) {
        // ignore
    }

    @Override
    public void connectionCheckedIn(ConnectionCheckedInEvent connectionCheckedInEvent) {
        // ignore
    }

    @Override
    public void waitQueueEntered(ConnectionPoolWaitQueueEnteredEvent connectionPoolWaitQueueEnteredEvent) {
        // ignore
    }

    @Override
    public void waitQueueExited(ConnectionPoolWaitQueueExitedEvent connectionPoolWaitQueueExitedEvent) {
        // ignore
    }

    @Override
    public synchronized void connectionAdded(ConnectionAddedEvent connectionAddedEvent) {
        nrConnections++;
        LOG.info("[MongoProvider] Connection added - count: {}", nrConnections);
    }

    @Override
    public synchronized void connectionRemoved(ConnectionRemovedEvent connectionRemovedEvent) {
        nrConnections--;
        LOG.info("[MongoProvider] Connection removed - count: {}", nrConnections);
    }

//    private void logNrOfMongoConnections(MongoClient mongo){
//        if (StringUtils.isNotBlank(definedDatabase)){
//            Document status = mongo.getDatabase(definedDatabase).runCommand(new Document("serverStatus", 1));
//            LOG.info("[MongoProvider] [logNrOfMongoConnections()] {}", status.toJson());
//        }
//    }

}
