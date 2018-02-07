package eu.europeana.corelib.edm.utils.construct;

import eu.europeana.corelib.definitions.edm.entity.WebResource;
import eu.europeana.corelib.edm.exceptions.MongoUpdateException;
import eu.europeana.corelib.edm.utils.MongoUtils;
import eu.europeana.corelib.solr.entity.EuropeanaAggregationImpl;
import eu.europeana.corelib.storage.MongoServer;
import org.apache.commons.lang.StringUtils;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;

public class EuropeanaAggregationUpdater implements	Updater<EuropeanaAggregationImpl> {
	private static String PORTALURL;

	@Value("#{europeanaProperties['portal.url']}")
	private void setPortalUrl(String ps){
		PORTALURL = ps;
	}

	private static final String PORTAL_PREFIX = PORTALURL + "portal/record/";
    private static final String PORTAL_SUFFIX = ".html";

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
		update = MongoUtils.updateString(mongoEntity, newEntity,
				"edmIsShownBy", ops) || update;
		update = MongoUtils.updateMap(mongoEntity, newEntity, "edmRights", ops)
				|| update;
		update = MongoUtils
				.updateMap(mongoEntity, newEntity, "edmCountry", ops) || update;
		String landingPage = PORTAL_PREFIX
				+ StringUtils.substringAfter(mongoEntity.getAggregatedCHO(),
						"/item/") + PORTAL_SUFFIX;

		if (!StringUtils.equals(mongoEntity.getEdmLandingPage(), landingPage)) {
			mongoEntity.setEdmLandingPage(landingPage);
			ops.set("edmLandingPage", landingPage);
		}
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
