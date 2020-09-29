package eu.europeana.corelib.record.config;

import eu.europeana.corelib.mongo.server.impl.EdmMongoServerImpl;
import eu.europeana.corelib.record.impl.RecordServiceImpl;
import eu.europeana.corelib.storage.impl.MongoProviderImpl;
import eu.europeana.metis.mongo.RecordRedirectDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RecordServerConfig {
    @Value("#{europeanaProperties['mongodb.connectionUrl']}")
    private String mongoConnectionUrl;

    @Value("#{europeanaProperties['mongodb.max.connection.idle.time']}")
    private String mongoMaxConnectionIdleTime;

    @Value("#{europeanaProperties['mongodb.record.dbname']}")
    private String recordDbName;

    @Value("#{europeanaProperties['mongodb.redirect.dbname']}")
    private String redirectDbName;


    @Bean(name = "corelib_db_mongoProvider", destroyMethod = "close")
    public MongoProviderImpl mongoProvider(){
        return new MongoProviderImpl(mongoConnectionUrl, mongoMaxConnectionIdleTime);
    }

    @Bean(name = "corelib_record_mongoServer")
    public EdmMongoServerImpl edmMongoServer() {
        return new EdmMongoServerImpl(mongoProvider().getMongoClient(), recordDbName, false);
    }

    @Bean(name = "metis_redirect_mongo")
    public RecordRedirectDao redirectDao() {
        return new RecordRedirectDao(mongoProvider().getMongoClient(), redirectDbName, false);
    }

    @Bean(name = "corelib_record_recordService")
    public RecordServiceImpl recordService() {
        return new RecordServiceImpl();
    }
}
