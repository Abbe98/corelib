package eu.europeana.corelib.edm.utils.construct;

import java.util.ArrayList;
import java.util.List;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import eu.europeana.corelib.definitions.edm.entity.WebResource;
import eu.europeana.corelib.edm.exceptions.MongoUpdateException;
import eu.europeana.corelib.edm.utils.MongoUtils;
import eu.europeana.corelib.solr.entity.EuropeanaAggregationImpl;
import eu.europeana.corelib.storage.MongoServer;

public class EuropeanaAggregationUpdater implements	Updater<EuropeanaAggregationImpl> {
  
	@Override
	public EuropeanaAggregationImpl update(
			EuropeanaAggregationImpl mongoEntity,
			EuropeanaAggregationImpl newEntity, MongoServer mongoServer)
			throws MongoUpdateException {
		Query<EuropeanaAggregationImpl> updateQuery = mongoServer
				.getDatastore().createQuery(EuropeanaAggregationImpl.class)
				.field("about").equal(mongoEntity.getAbout());
		UpdateOperations<EuropeanaAggregationImpl> ops = mongoServer
				.getDatastore().createUpdateOperations(
						EuropeanaAggregationImpl.class);
		boolean update = false;
		
		update = MongoUtils.updateString(mongoEntity, newEntity,
				"aggregatedCHO", ops) || update;
		newEntity.setEdmLandingPageFromAggregatedCHO();
		update = MongoUtils.updateString(mongoEntity, newEntity, 
		        "edmLandingPage", ops);

		update = MongoUtils.updateString(mongoEntity, newEntity,
				"edmIsShownBy", ops) || update;
		update = MongoUtils.updateMap(mongoEntity, newEntity, "edmRights", ops)
				|| update;
		update = MongoUtils
				.updateMap(mongoEntity, newEntity, "edmCountry", ops) || update;
	
		update = MongoUtils.updateMap(mongoEntity, newEntity, "edmLanguage",
				ops) || update;
		update = MongoUtils.updateMap(mongoEntity, newEntity, "dcCreator", ops)
				|| update;
		update = MongoUtils.updateString(mongoEntity, newEntity, "edmPreview",
				ops) || update;
		update = MongoUtils.updateArray(mongoEntity, newEntity, "aggregates",
				ops) || update;

		List<WebResource> webResources = new ArrayList<WebResource>();
		for (WebResource wr : mongoEntity.getWebResources()) {
			webResources.add(new WebResourceCreator().saveWebResource(wr,
					mongoServer));
		}
		mongoEntity.setWebResources(webResources);
		if (update) {
			mongoServer.getDatastore().update(updateQuery, ops);
		}
		return mongoEntity;
	}

}
