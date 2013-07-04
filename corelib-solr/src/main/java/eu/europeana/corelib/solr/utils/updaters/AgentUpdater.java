package eu.europeana.corelib.solr.utils.updaters;

import java.util.List;
import java.util.Map;

import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;

import eu.europeana.corelib.definitions.jibx.AgentType;
import eu.europeana.corelib.solr.MongoServer;
import eu.europeana.corelib.solr.entity.AgentImpl;
import eu.europeana.corelib.solr.utils.MongoUtils;
import eu.europeana.corelib.solr.utils.SolrUtils;

public class AgentUpdater {

	public static void update(AgentImpl agent, AgentType jibxAgent,
			MongoServer mongoServer) {
		Query<AgentImpl> updateQuery = mongoServer.getDatastore()
				.createQuery(AgentImpl.class).field("about")
				.equal(agent.getAbout());
		UpdateOperations<AgentImpl> ops = mongoServer.getDatastore()
				.createUpdateOperations(AgentImpl.class);
		boolean update = false;
		if (jibxAgent.getBegin() != null) {
			Map<String, List<String>> begin = MongoUtils
					.createLiteralMapFromString(jibxAgent.getBegin());
			if (begin != null) {
				if (agent.getBegin() == null
						|| !MongoUtils.mapEquals(begin, agent.getBegin())) {
					ops.set("begin", begin);
					update = true;
				}
			}
		}

		if (jibxAgent.getDateList() != null) {
			Map<String, List<String>> date = MongoUtils
					.createResourceOrLiteralMapFromList(jibxAgent.getDateList());
			if (date != null) {
				if (agent.getDcDate() == null
						|| !MongoUtils.mapEquals(date, agent.getDcDate())) {
					ops.set("dcDate", date);
					update = true;
				}
			}
		}

		if (jibxAgent.getIdentifierList() != null) {
			Map<String, List<String>> identifier = MongoUtils
					.createLiteralMapFromList(jibxAgent.getIdentifierList());
			if (identifier != null) {
				if (agent.getDcIdentifier() == null
						|| !MongoUtils.mapEquals(identifier,
								agent.getDcIdentifier())) {
					ops.set("dcIdentifier", identifier);
					update = update | true;
				}
			}
		}

		if (jibxAgent.getBiographicalInformation() != null) {
			Map<String, List<String>> biographicalInformation = MongoUtils
					.createLiteralMapFromString(jibxAgent
							.getBiographicalInformation());
			if (biographicalInformation != null) {
				if (agent.getRdaGr2BiographicalInformation() == null
						|| !MongoUtils.mapEquals(biographicalInformation,
								agent.getRdaGr2BiographicalInformation())) {
					ops.set("rdaGr2BiographicalInformation",
							biographicalInformation);
					update = true;
				}
			}
		}

		if (jibxAgent.getDateOfBirth() != null) {
			Map<String, List<String>> dob = MongoUtils
					.createLiteralMapFromString(jibxAgent.getDateOfBirth());
			if (dob != null) {
				if (agent.getRdaGr2DateOfBirth() == null
						|| !MongoUtils.mapEquals(dob,
								agent.getRdaGr2DateOfBirth())) {

					ops.set("rdaGr2DateOfBirth", dob);
					update = true;
				}
			}
		}

		if (jibxAgent.getDateOfDeath() != null) {
			Map<String, List<String>> dod = MongoUtils
					.createLiteralMapFromString(jibxAgent.getDateOfDeath());
			if (dod != null) {
				if (agent.getRdaGr2DateOfDeath() == null
						|| !MongoUtils.mapEquals(dod,
								agent.getRdaGr2DateOfDeath())) {
					ops.set("rdaGr2DateOfDeath", dod);
					update = true;
				}
			}
		}

		if (jibxAgent.getDateOfEstablishment() != null) {
			Map<String, List<String>> doe = MongoUtils
					.createLiteralMapFromString(jibxAgent
							.getDateOfEstablishment());
			if (doe != null) {
				if (agent.getRdaGr2DateOfEstablishment() == null
						|| !MongoUtils.mapEquals(doe,
								agent.getRdaGr2DateOfEstablishment())) {
					ops.set("rdaGr2DateOfEstablishment", doe);
					update = true;
				}
			}
		}

		if (jibxAgent.getDateOfTermination() != null) {
			Map<String, List<String>> dot = MongoUtils
					.createLiteralMapFromString(jibxAgent
							.getDateOfTermination());
			if (dot != null) {
				if (agent.getRdaGr2DateOfTermination() == null
						|| !MongoUtils.mapEquals(dot,
								agent.getRdaGr2DateOfTermination())) {
					ops.set("rdaGr2DateOfTermination", dot);
					update = update | true;
				}
			}
		}

		if (jibxAgent.getGender() != null) {
			Map<String, List<String>> gender = MongoUtils
					.createLiteralMapFromString(jibxAgent.getGender());
			if (gender != null) {
				if (agent.getRdaGr2Gender() == null
						|| !MongoUtils.mapEquals(gender,
								agent.getRdaGr2Gender())) {
					ops.set("rdaGr2Gender", gender);
					update = true;
				}
			}
		}

		if (jibxAgent.getProfessionOrOccupation() != null) {
			Map<String, List<String>> professionOrOccupation = MongoUtils
					.createResourceOrLiteralMapFromString(jibxAgent
							.getProfessionOrOccupation());
			if (professionOrOccupation != null) {
				if (agent.getRdaGr2ProfessionOrOccupation() == null
						|| !MongoUtils.mapEquals(professionOrOccupation,
								agent.getRdaGr2ProfessionOrOccupation())) {
					ops.set("rdaGr2ProfessionOrOccupation",
							professionOrOccupation);
					update = true;
				}
			}
		}

		if (jibxAgent.getHasMetList() != null) {
			Map<String, List<String>> hasMet = MongoUtils
					.createLiteralMapFromList(jibxAgent.getHasMetList());
			if (hasMet != null) {
				if (agent.getEdmHasMet() == null
						|| !MongoUtils.mapEquals(hasMet, agent.getEdmHasMet())) {
					ops.set("edmHasMet", hasMet);
					update = true;
				}
			}
		}

		if (jibxAgent.getIsRelatedToList() != null) {
			Map<String, List<String>> isRelatedTo = MongoUtils
					.createResourceOrLiteralMapFromList(jibxAgent
							.getIsRelatedToList());
			if (isRelatedTo != null) {
				if (agent.getEdmIsRelatedTo() == null
						|| !MongoUtils.mapEquals(isRelatedTo,
								agent.getEdmIsRelatedTo())) {
					ops.set("edmIsRelatedTo", isRelatedTo);
					update = true;
				}
			}
		}

		if (jibxAgent.getNameList() != null) {
			Map<String, List<String>> name = MongoUtils
					.createLiteralMapFromList(jibxAgent.getNameList());
			if (name != null) {
				if (agent.getFoafName() == null
						|| !MongoUtils.mapEquals(name, agent.getFoafName())) {
					ops.set("foafName", name);
					update = true;
				}
			}

		}

		if (jibxAgent.getSameAList() != null) {
			String[] sameAs = SolrUtils.resourceListToArray(jibxAgent
					.getSameAList());
			if (sameAs != null) {
				if (agent.getOwlSameAs() == null
						|| !MongoUtils
								.arrayEquals(sameAs, agent.getOwlSameAs())) {
					ops.set("owlSameAs", sameAs);
					update = true;
				}
			}
		}

		if (jibxAgent.getEnd() != null) {
			Map<String, List<String>> end = MongoUtils
					.createLiteralMapFromString(jibxAgent.getEnd());
			if (end != null) {
				if (agent.getEnd() == null
						|| !MongoUtils.mapEquals(end, agent.getEnd())) {
					ops.set("end", end);
					update = true;
				}
			}
		}

		if (jibxAgent.getNoteList() != null) {
			Map<String, List<String>> note = MongoUtils
					.createLiteralMapFromList(jibxAgent.getNoteList());
			if (note != null) {
				if (agent.getNote() == null
						|| !MongoUtils.mapEquals(note, agent.getNote())) {
					ops.set("note", note);
					update = true;
				}
			}
		}

		if (jibxAgent.getAltLabelList() != null) {
			Map<String, List<String>> altLabel = MongoUtils
					.createLiteralMapFromList(jibxAgent.getAltLabelList());
			if (altLabel != null) {
				if (agent.getAltLabel() == null
						|| !MongoUtils.mapEquals(altLabel, agent.getAltLabel())) {
					ops.set("altLabel", altLabel);
					update = true;
				}
			}
		}

		if (jibxAgent.getPrefLabelList() != null) {
			Map<String, List<String>> prefLabel = MongoUtils
					.createLiteralMapFromList(jibxAgent.getPrefLabelList());
			if (prefLabel != null) {
				if (agent.getPrefLabel() == null
						|| !MongoUtils.mapEquals(prefLabel,
								agent.getPrefLabel())) {
					ops.set("prefLabel", prefLabel);
					update = true;
				}
			}
		}
		if (update) {
			mongoServer.getDatastore().update(updateQuery, ops);
		}
	}
}
