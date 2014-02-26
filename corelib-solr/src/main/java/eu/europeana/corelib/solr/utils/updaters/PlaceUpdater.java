package eu.europeana.corelib.solr.utils.updaters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;

import eu.europeana.corelib.definitions.jibx.PlaceType;
import eu.europeana.corelib.definitions.jibx.SameAs;
import eu.europeana.corelib.solr.MongoServer;
import eu.europeana.corelib.solr.entity.PlaceImpl;
import eu.europeana.corelib.solr.utils.MongoUtils;
import eu.europeana.corelib.utils.StringArrayUtils;

public class PlaceUpdater implements Updater<PlaceImpl, PlaceType> {

	public void update(PlaceImpl place, PlaceType jibxPlace,
			MongoServer mongoServer) {
		Query<PlaceImpl> query = mongoServer.getDatastore()
				.createQuery(PlaceImpl.class).field("about")
				.equal(place.getAbout());
		UpdateOperations<PlaceImpl> ops = mongoServer.getDatastore()
				.createUpdateOperations(PlaceImpl.class);
		boolean update = false;
		if (jibxPlace.getNoteList() != null) {
			Map<String, List<String>> note = MongoUtils
					.createLiteralMapFromList(jibxPlace.getNoteList());
			if (note != null) {
				if (place.getNote() == null
						|| !MongoUtils.mapEquals(note, place.getNote())) {
					ops.set("note", note);
					update = true;
				}
			}
		} else {
			if (place.getNote() != null) {
				ops.unset("note");
				update = true;
			}
		}

		if (jibxPlace.getAltLabelList() != null) {
			Map<String, List<String>> altLabel = MongoUtils
					.createLiteralMapFromList(jibxPlace.getAltLabelList());
			if (altLabel != null) {
				if (place.getAltLabel() == null
						|| !MongoUtils.mapEquals(altLabel, place.getAltLabel())) {
					ops.set("altLabel", altLabel);
					update = true;
				}
			}

		} else {
			if (place.getAltLabel() != null) {
				ops.unset("altLabel");
				update = true;
			}
		}

		if (jibxPlace.getPrefLabelList() != null) {
			Map<String, List<String>> prefLabel = MongoUtils
					.createLiteralMapFromList(jibxPlace.getPrefLabelList());
			if (prefLabel != null) {
				if (place.getPrefLabel() == null
						|| !MongoUtils.mapEquals(prefLabel,
								place.getPrefLabel())) {
					ops.set("prefLabel", prefLabel);
					update = true;
				}
			}

		} else {
			if (place.getPrefLabel() != null) {
				ops.unset("prefLabel");
				update = true;
			}
		}

		if (jibxPlace.getIsPartOfList() != null) {
			Map<String, List<String>> isPartOf = MongoUtils
					.createResourceOrLiteralMapFromList(jibxPlace
							.getIsPartOfList());
			if (isPartOf != null) {
				if (place.getIsPartOf() == null
						|| !MongoUtils.mapEquals(isPartOf, place.getIsPartOf())) {
					ops.set("isPartOf", isPartOf);
					update = true;
				}
			}
		} else {
			if (place.getIsPartOf() != null) {
				ops.unset("isPartOf");
				update = true;
			}
		}

		if (jibxPlace.getHasPartList() != null) {
			Map<String, List<String>> hasPart = MongoUtils
					.createResourceOrLiteralMapFromList(jibxPlace
							.getHasPartList());
			if (hasPart != null) {
				if (place.getDcTermsHasPart() == null
						|| !MongoUtils.mapEquals(hasPart,
								place.getDcTermsHasPart())) {
					ops.set("dcTermsHasPart", hasPart);
					update = true;
				}

			}
		} else {
			if(place.getDcTermsHasPart()!=null){
				ops.unset("dcTermsHasPart");
				update =true;
			}
		}
		if (jibxPlace.getSameAList() != null) {
			List<String> owlSameAs = new ArrayList<String>();

			if (jibxPlace.getSameAList() != null) {
				for (SameAs sameAsJibx : jibxPlace.getSameAList()) {
					if (!MongoUtils.contains(place.getOwlSameAs(),
							sameAsJibx.getResource())) {
						owlSameAs.add(sameAsJibx.getResource());
					}
				}
			}

			if (place.getOwlSameAs() != null) {
				for (String owlSameAsItem : place.getOwlSameAs()) {
					owlSameAs.add(owlSameAsItem);
				}
			}
			ops.set("owlSameAs", StringArrayUtils.toArray(owlSameAs));
			update = true;

		} else {
			if(place.getOwlSameAs()!=null){
				ops.unset("owlSameAs");
				update = true;
			}
		}

		if (jibxPlace.getLat() != null) {
			Float lat = jibxPlace.getLat().getLat();
			if (place.getLatitude() == null || lat != place.getLatitude()) {
				ops.set("latitude", lat);
				update = true;
			}
		} else {
			if(place.getLatitude()!=null){
				ops.unset("latitude");
				update=true;
			}
		}

		if (jibxPlace.getLong() != null) {
			Float _long = jibxPlace.getLong().getLong();
			if (place.getLongitude() == null || _long != place.getLongitude()) {
				ops.set("longitude", _long);
				update = true;
			}
		} else {
			if(place.getLongitude()!=null){
				ops.unset("longitude");
				update=true;
			}
		}

		if (jibxPlace.getAlt() != null) {
			Float alt = jibxPlace.getAlt().getAlt();
			if (place.getAltitude() == null || alt != place.getAltitude()) {
				ops.set("altitude", jibxPlace.getAlt().getAlt());
				update = true;
			}
		} else {
			if (place.getAltitude()!=null){
				ops.unset("altitude");
				update=true;
			}
		}
		if (update) {
			mongoServer.getDatastore().update(query, ops);
		}
	}
}
