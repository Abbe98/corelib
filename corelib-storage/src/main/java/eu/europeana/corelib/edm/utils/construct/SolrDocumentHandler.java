/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.europeana.corelib.edm.utils.construct;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;

import eu.europeana.corelib.definitions.edm.entity.Agent;
import eu.europeana.corelib.definitions.edm.entity.Aggregation;
import eu.europeana.corelib.definitions.edm.entity.Concept;
import eu.europeana.corelib.definitions.edm.entity.License;
import eu.europeana.corelib.definitions.edm.entity.Place;
import eu.europeana.corelib.definitions.edm.entity.Proxy;
import eu.europeana.corelib.definitions.edm.entity.Timespan;
import eu.europeana.corelib.definitions.model.EdmLabel;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.entity.LicenseImpl;
import eu.europeana.publication.common.ICollection;
import eu.europeana.publication.common.IDocument;
import eu.europeana.publication.common.State;

/**
 *
 * @author Yorgos.Mamakis@ europeana.eu
 */
public class SolrDocumentHandler implements ICollection {
	private HttpSolrServer solrServer;

	public SolrDocumentHandler(HttpSolrServer solrServer) {
		this.solrServer = solrServer;
	}

	public void save(FullBeanImpl fBean) {

		SolrInputDocument doc = new SolrInputDocument();
		List<LicenseImpl> licenses = fBean.getLicenses();
		List<String> licIds = new ArrayList<String>();
		if (licenses!=null &&licenses.size() > 0) {
			for (LicenseImpl lic : licenses) {
				licIds.add(lic.getAbout());
			}
		}
		new ProvidedChoSolrCreator()
				.create(doc, fBean.getProvidedCHOs().get(0));
		new AggregationSolrCreator()
				.create(doc, fBean.getAggregations().get(0),licIds);
		new EuropeanaAggregationSolrCreator().create(doc,
				fBean.getEuropeanaAggregation());
		for (Proxy prx : fBean.getProxies()) {
			new ProxySolrCreator().create(doc, prx);
		}
                if(fBean.getConcepts()!=null){
		for (Concept concept : fBean.getConcepts()) {
			new ConceptSolrCreator().create(doc, concept);
		}
                }
                if(fBean.getTimespans()!=null){
		for (Timespan ts : fBean.getTimespans()) {
			new TimespanSolrCreator().create(doc, ts);
		}
                }
                if(fBean.getAgents()!=null){
		for (Agent agent : fBean.getAgents()) {
			new AgentSolrCreator().create(doc, agent);
		}
                }
                if(fBean.getPlaces()!=null){
		for (Place place : fBean.getPlaces()) {
			new PlaceSolrCreator().create(doc, place);
		}
                }
                if(fBean.getLicenses()!=null){
		for (License lic : fBean.getLicenses()) {
                        boolean isAggregation = false;
                        for(Aggregation aggr:fBean.getAggregations()){
                            if(aggr.getEdmRights()!=null && aggr.getEdmRights().get("def").contains(lic.getAbout())){
                                isAggregation = true;
                                break;
                            }
                        }
                        System.out.println(isAggregation);
			new LicenseSolrCreator().create(doc, lic, isAggregation);
		}
                }
		doc.addField(EdmLabel.EUROPEANA_COMPLETENESS.toString(),
				fBean.getEuropeanaCompleteness());
		doc.addField(EdmLabel.EUROPEANA_COLLECTIONNAME.toString(),
				fBean.getEuropeanaCollectionName()[0]);
		doc.addField("timestamp_created", fBean.getTimestampCreated());
		doc.addField("timestamp_update", fBean.getTimestampUpdated());

		try {
			solrServer.add(doc);
		} catch (SolrServerException ex) {
			Logger.getLogger(SolrDocumentHandler.class.getName()).log(
					Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(SolrDocumentHandler.class.getName()).log(
					Level.SEVERE, null, ex);
		}
	}

	@Override
	public List<IDocument> getDocumentsByStatesUsingBatch(
			List<State> stateVlues, Map<String, List<String>> queryChoices,
			int batchSize) {
		throw new UnsupportedOperationException("Not supported yet."); // To
																		// change
																		// body
																		// of
																		// generated
																		// methods,
																		// choose
																		// Tools
																		// |
																		// Templates.
	}

	@Override
	public IDocument getDocumentById(String id) {
		throw new UnsupportedOperationException("Not supported yet."); // To
																		// change
																		// body
																		// of
																		// generated
																		// methods,
																		// choose
																		// Tools
																		// |
																		// Templates.
	}

	@Override
	public void insertDocument(IDocument document) {
		throw new UnsupportedOperationException("Not supported yet."); // To
																		// change
																		// body
																		// of
																		// generated
																		// methods,
																		// choose
																		// Tools
																		// |
																		// Templates.
	}

	@Override
	public void updateDocumentUsingId(IDocument document) {
		throw new UnsupportedOperationException("Not supported yet."); // To
																		// change
																		// body
																		// of
																		// generated
																		// methods,
																		// choose
																		// Tools
																		// |
																		// Templates.
	}

	@Override
	public void cloneDocument(IDocument originalDocument,
			IDocument clonedDocument) {
		throw new UnsupportedOperationException("Not supported yet."); // To
																		// change
																		// body
																		// of
																		// generated
																		// methods,
																		// choose
																		// Tools
																		// |
																		// Templates.
	}

	@Override
	public void deleteDocument(IDocument id) {
		throw new UnsupportedOperationException("Not supported yet."); // To
																		// change
																		// body
																		// of
																		// generated
																		// methods,
																		// choose
																		// Tools
																		// |
																		// Templates.
	}

	@Override
	public void commit() throws Exception {
		throw new UnsupportedOperationException("Not supported yet."); // To
																		// change
																		// body
																		// of
																		// generated
																		// methods,
																		// choose
																		// Tools
																		// |
																		// Templates.
	}
}
