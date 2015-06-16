package eu.europeana.corelib.search;

import eu.europeana.corelib.logging.Logger;
import eu.europeana.corelib.mongo.server.EdmMongoServer;
import eu.europeana.corelib.tools.lookuptable.EuropeanaIdMongoServer;
import org.apache.solr.client.solrj.SolrServer;

public interface TestSearchService extends SearchService {

    void setEdmMongoServer(EdmMongoServer mongoServer);

    void setEuropeanaIdMongoServer(EuropeanaIdMongoServer mongoServer);

    void setLogger(Logger log);
}
