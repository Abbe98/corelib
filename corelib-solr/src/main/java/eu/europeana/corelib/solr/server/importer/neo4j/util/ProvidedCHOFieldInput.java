/*
 * Copyright 2007-2012 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 * 
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */
package eu.europeana.corelib.solr.server.importer.neo4j.util;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.SolrInputDocument;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;
import eu.europeana.corelib.definitions.jibx.ProvidedCHOType;
import eu.europeana.corelib.definitions.jibx.SameAs;
import eu.europeana.corelib.definitions.model.EdmLabel;
import eu.europeana.corelib.solr.server.Neo4jServer;
import eu.europeana.corelib.solr.entity.ProvidedCHOImpl;
import eu.europeana.corelib.solr.utils.MongoUtils;
import eu.europeana.corelib.solr.utils.SolrUtils;

/**
 * Class constructing a SOLR document and MongoDB representation of a
 * ProvidedCHO
 * 
 * 
 * @author Yorgos.Mamakis@ kb.nl
 * 
 */
public final class ProvidedCHOFieldInput {

	public ProvidedCHOFieldInput() {

	}

	/**
	 * Method filling in a SolrInputDocument with the fields of a providedCHO
	 * 
	 * @param providedCHO
	 *            The ProvidedCHO representation from the JiBX bindings
	 * @param solrInputDocument
	 *            The SolrInputDocument whose fields the class fills in
	 * @return A SolrInputDocument with filled in ProvidedCHO fields
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public SolrInputDocument createProvidedCHOFields(
			ProvidedCHOType providedCHO, SolrInputDocument solrInputDocument)
			throws InstantiationException, IllegalAccessException {
		solrInputDocument.addField(EdmLabel.EUROPEANA_ID.toString(),
				providedCHO.getAbout());
		if (providedCHO.getSameAList() != null) {
			for (SameAs sameAs : providedCHO.getSameAList()) {
				solrInputDocument.addField(
						EdmLabel.PROXY_OWL_SAMEAS.toString(),
						sameAs.getResource());
			}
		}

		solrInputDocument.addField(
				EdmLabel.EUROPEANA_COLLECTIONNAME.toString(),
				StringUtils.substringBetween(providedCHO.getAbout(), "/", "/"));
		return solrInputDocument;
	}

	/**
	 * 
	 * Method Creating a MongoDB Entity of a ProvidedCHO
	 * 
	 * @param providedCHO
	 *            The ProvidedCHO representation from the JiBX bindings
	 * @param neo4jServer
	 *            The MongoDB Server object to save the ProvidedCHO
	 * @return The MongoDB ProvidedCHO Entity
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public ProvidedCHOImpl createProvidedCHOMongoFields(
			ProvidedCHOType providedCHO, Neo4jServer neo4jServer)
			throws InstantiationException, IllegalAccessException {
		
		ProvidedCHOImpl mongoProvidedCHO = neo4jServer.find(providedCHO.getAbout(),new ProvidedCHOImpl());
		// If the ProvidedCHO does not exist create it
		if (mongoProvidedCHO == null) {
			mongoProvidedCHO = new ProvidedCHOImpl();
			// mongoProvidedCHO.setId(new ObjectId());
			mongoProvidedCHO.setAbout("/item" + providedCHO.getAbout());

			mongoProvidedCHO.setOwlSameAs(SolrUtils
					.resourceListToArray(providedCHO.getSameAList()));

			neo4jServer.save(mongoProvidedCHO);
		} else {
			// update the ProvidedCHO
			List<String> owlSameAsList = null;
			if (providedCHO.getSameAList() != null) {
				owlSameAsList = new ArrayList<String>();
				for (SameAs sameAs : providedCHO.getSameAList()) {
					owlSameAsList.add(sameAs.getResource());
				}

				neo4jServer.save(mongoProvidedCHO);
			}

		}
		return (ProvidedCHOImpl) mongoProvidedCHO;
	}

	/**
	 * Delete a providedCHO from mongoDB storage based on its about field
	 * 
	 * @param about
	 *            the about field to search
	 * @param neo4jServer
	 *            the neo4jServer to use
	 */
	public void deleteProvideCHOFromMongo(String about,
			Neo4jServer neo4jServer) {
		neo4jServer.delete(about);
	}

}
