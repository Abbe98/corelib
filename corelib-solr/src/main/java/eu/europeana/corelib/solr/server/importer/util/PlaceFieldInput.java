package eu.europeana.corelib.solr.server.importer.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.common.SolrInputDocument;

import eu.europeana.corelib.definitions.jibx.AltLabel;
import eu.europeana.corelib.definitions.jibx.IsPartOf;
import eu.europeana.corelib.definitions.jibx.Note;
import eu.europeana.corelib.definitions.jibx.PlaceType;
import eu.europeana.corelib.definitions.jibx.PrefLabel;
import eu.europeana.corelib.definitions.model.EdmLabel;
import eu.europeana.corelib.solr.entity.PlaceImpl;
import eu.europeana.corelib.solr.server.MongoDBServer;

public class PlaceFieldInput {
	public static SolrInputDocument createPlaceSolrFields(PlaceType place,
			SolrInputDocument solrInputDocument) {
		solrInputDocument.addField(EdmLabel.EDM_PLACE.toString(),
				place.getAbout());
		if(place.getAltLabelList()!=null){
		for (AltLabel altLabel : place.getAltLabelList()) {
			try{
			solrInputDocument.addField(EdmLabel.PL_SKOS_ALT_LABEL.toString()
					+ "." + altLabel.getLang().getLang(), altLabel.getString());
			}
			catch(NullPointerException e){
				solrInputDocument.addField(EdmLabel.PL_SKOS_ALT_LABEL.toString(),
						altLabel.getString());
			}
		}
		}
		if(place.getPrefLabelList()!=null){
		for (PrefLabel prefLabel : place.getPrefLabelList()) {
			try{
			solrInputDocument.addField(EdmLabel.PL_SKOS_PREF_LABEL.toString()
					+ "." + prefLabel.getLang().getLang(),
					prefLabel.getString());
			}
			catch(NullPointerException e){
				solrInputDocument.addField(EdmLabel.PL_SKOS_PREF_LABEL.toString(),
						prefLabel.getString());
			}
		}
		}
		if(place.getIsPartOfList()!=null){
		for (IsPartOf isPartOf : place.getIsPartOfList()) {
			solrInputDocument.addField(
					EdmLabel.PL_DCTERMS_ISPART_OF.toString(),
					isPartOf.getString());
		}
		}
		if(place.getNoteList()!=null){
		for (Note note : place.getNoteList()) {
			solrInputDocument.addField(EdmLabel.PL_SKOS_NOTE.toString(),
					note.getString());
		}
		}
		if(place.getPosLong()!=null && place.getPosLat()!=null){
		solrInputDocument.addField(EdmLabel.PL_POSITION.toString(), place
				.getPosLat().getPosLat()
				+ ","
				+ place.getPosLong().getPosLong());
		}
		return solrInputDocument;
	}

	public static PlaceImpl createPlaceMongoFields(PlaceType placeType,
			MongoDBServer mongoServer) throws InstantiationException,
			IllegalAccessException {
		PlaceImpl place = new PlaceImpl();
		// If place exists in mongo
		try {
			place = (PlaceImpl) mongoServer.searchByAbout(placeType.getAbout());
			place.getAbout();
		}
		// if it does not exist
		catch (NullPointerException npe) {
			place = new PlaceImpl();
			place.setAbout(placeType.getAbout());

			if (placeType.getPosLat() != null) {
				place.setLatitude(placeType.getPosLat().getPosLat());
			}

			if (placeType.getPosLong() != null) {
				place.setLongitude(placeType.getPosLong().getPosLong());
			}
			
			if (placeType.getNoteList() != null) {
				List<String> noteList = new ArrayList<String>();
				for (Note note : placeType.getNoteList()) {
					noteList.add(note.getString());
				}
				place.setNote(noteList.toArray(new String[noteList.size()]));
			}

			if (placeType.getPrefLabelList() != null) {
				Map<String, String> prefLabelMongo = new HashMap<String, String>();
				for (PrefLabel prefLabelJibx : placeType.getPrefLabelList()) {
					try{
					prefLabelMongo.put(prefLabelJibx.getLang().getLang(),
							prefLabelJibx.getString());
					}
					catch(NullPointerException e){
						prefLabelMongo.put("def",
								prefLabelJibx.getString());
					}
				}
				place.setPrefLabel(prefLabelMongo);
			}

			if (placeType.getAltLabelList() != null) {
				Map<String, String> altLabelMongo = new HashMap<String, String>();
				for (AltLabel altLabelJibx : placeType.getAltLabelList()) {
					try{
					altLabelMongo.put(altLabelJibx.getLang().getLang(),
							altLabelJibx.getString());
					}
					catch(NullPointerException e){
						altLabelMongo.put("def",
								altLabelJibx.getString());
					}
				}
				place.setAltLabel(altLabelMongo);
			}

			if (placeType.getIsPartOfList() != null) {
				List<String> isPartOfList = new ArrayList<String>();
				for (IsPartOf isPartOf : placeType.getIsPartOfList()) {
					isPartOfList.add(isPartOf.getString());
				}
				place.setIsPartOf(isPartOfList.toArray(new String[isPartOfList
						.size()]));
			}
			mongoServer.getDatastore().save(place);
		}
		return place;
	}
}
