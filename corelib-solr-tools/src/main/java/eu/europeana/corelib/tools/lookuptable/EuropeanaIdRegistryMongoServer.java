package eu.europeana.corelib.tools.lookuptable;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.google.code.morphia.query.UpdateOperations;
import com.mongodb.Mongo;

import eu.europeana.corelib.solr.MongoServer;
import eu.europeana.corelib.tools.utils.EuropeanaUriUtils;

/**
 * Class for setting and accessing the EuropeanaIdRegistry Lookup Table
 * 
 * @author yorgos.mamakis@ kb.nl
 * 
 */
public class EuropeanaIdRegistryMongoServer implements MongoServer {

	private Mongo mongoServer;
	private String databaseName;
	private Datastore datastore;

	private final static String EID = "eid";
	private final static String ORID = "orid";
	private final static String DATE = "last_checked";
	private final static String SESSION = "sessionID";
	private final static String CID = "cid";
	private final static String XMLCHECKSUM = "xmlchecksum";
	private EuropeanaIdMongoServer europeanaIdMongoServer;

	/**
	 * Constructor of the EuropeanaIDRegistryMongoServer
	 * 
	 * @param mongoServer
	 *            The server to connect to
	 * @param databaseName
	 *            The database to connect to
	 */
	public EuropeanaIdRegistryMongoServer(Mongo mongoServer, String databaseName) {
		this.mongoServer = mongoServer;
		this.databaseName = databaseName;
		europeanaIdMongoServer = new EuropeanaIdMongoServer(mongoServer,
				databaseName);
		createDatastore();
	}

	private void createDatastore() {
		Morphia morphia = new Morphia();
		morphia.map(EuropeanaIdRegistry.class);
		morphia.map(FailedRecord.class);
		datastore = morphia.createDatastore(mongoServer, databaseName);

		datastore.ensureIndexes();
	}

	/**
	 * Get the datastore
	 */
	@Override
	public Datastore getDatastore() {
		return this.datastore;
	}

	/**
	 * Close the connection to the server
	 */
	@Override
	public void close() {
		mongoServer.close();
	}

	/**
	 * @param origID
	 * @param collectionID
	 * @param xml
	 * @return
	 */
	public LookupResult lookupUiniqueId(String origID, String collectionID,
			String xml, String sessionID) {

		String xmlChecksum = generatechecksum(xml);
		LookupResult lookupresult = new LookupResult();

		// Generate EuropeanaID by originalid and collectionID
		String europeanaIDString = EuropeanaUriUtils.createEuropeanaId(
				collectionID, origID);

		lookupresult.setEuropeanaID(europeanaIDString);

		EuropeanaIdRegistry constructedeuropeanaId = new EuropeanaIdRegistry();
		constructedeuropeanaId.setCid(collectionID);
		constructedeuropeanaId.setEid(europeanaIDString);
		constructedeuropeanaId.setOrid(origID);
		constructedeuropeanaId.setLast_checked(new Date());
		constructedeuropeanaId.setSessionID(sessionID);
		constructedeuropeanaId.setXmlchecksum(xmlChecksum);

		// Retrieve by the EuropeanaID to check if the item is already
		// registered
		EuropeanaIdRegistry retrievedeuropeanaID = retrieveEuropeanaIdFromNew(europeanaIDString);

		UpdateOperations<EuropeanaIdRegistry> updateops = datastore
				.createUpdateOperations(EuropeanaIdRegistry.class);

		// If it is not then save and return a new collectionID
		if (retrievedeuropeanaID == null) {
			if (!checkForChangedCollection(constructedeuropeanaId)) {
				datastore.save(constructedeuropeanaId);
				lookupresult.setState(LookupState.ID_REGISTERED);
				lookupresult.setEuropeanaID(europeanaIDString);
				return lookupresult;
			} else {
				lookupresult.setState(LookupState.COLLECTION_CHANGED);
				updateops.set(EID, constructedeuropeanaId.getEid());
				updateops.set(CID, collectionID);
				return lookupresult;
			}
		} else {
			
			constructedeuropeanaId.setId(retrievedeuropeanaID.getId());
		}

		// Otherwise proceed to UUID scenaria

		// First check if its an Update (eid cid origid are the same, but xml is
		// different)
		// Update the XML checksum
		if (constructedeuropeanaId.getCid().equals(
				retrievedeuropeanaID.getCid())
				&& constructedeuropeanaId.getEid().equals(
						retrievedeuropeanaID.getEid())
				&& constructedeuropeanaId.getOrid().equals(
						retrievedeuropeanaID.getOrid())
				&& !constructedeuropeanaId.getXmlchecksum().equals(
						retrievedeuropeanaID.getXmlchecksum())) {

			lookupresult.setState(LookupState.UPDATE);

			updateops.set(XMLCHECKSUM, xmlChecksum);

		}

		// Check if it is exactly the same (eid cid origid and xml are the same)
		else if (constructedeuropeanaId.getCid().equals(
				retrievedeuropeanaID.getCid())
				&& constructedeuropeanaId.getEid().equals(
						retrievedeuropeanaID.getEid())
				&& constructedeuropeanaId.getOrid().equals(
						retrievedeuropeanaID.getOrid())
				&& constructedeuropeanaId.getXmlchecksum().equals(
						retrievedeuropeanaID.getXmlchecksum())) {
			lookupresult.setState(LookupState.IDENTICAL);

			generateFailedRecord(constructedeuropeanaId, xml,
					LookupState.IDENTICAL);
		}

		// Then check if it is a duplicate in the same collection (eid cid
		// origid xml and session) are the same
		else if (constructedeuropeanaId.getCid().equals(
				retrievedeuropeanaID.getCid())
				&& constructedeuropeanaId.getEid().equals(
						retrievedeuropeanaID.getEid())
				&& constructedeuropeanaId.getOrid().equals(
						retrievedeuropeanaID.getOrid())
				&& constructedeuropeanaId.getXmlchecksum().equals(
						retrievedeuropeanaID.getXmlchecksum())
				&& constructedeuropeanaId.getSessionID().equals(
						retrievedeuropeanaID.getSessionID())) {
			lookupresult.setState(LookupState.DUPLICATE_INCOLLECTION);
			generateFailedRecord(constructedeuropeanaId, xml,
					LookupState.DUPLICATE_INCOLLECTION);
		}

		// There is a duplicate ID in a split collection containing different
		// information:
		// This implies that the eid and rid is the same even though the cid and
		// xml field values are different

		else if (!constructedeuropeanaId.getCid().equals(
				retrievedeuropeanaID.getCid())
				&& constructedeuropeanaId.getEid().equals(
						retrievedeuropeanaID.getEid())
				&& constructedeuropeanaId.getOrid().equals(
						retrievedeuropeanaID.getOrid())
				&& constructedeuropeanaId.getXmlchecksum().equals(
						retrievedeuropeanaID.getXmlchecksum())) {
			lookupresult
					.setState(LookupState.DUPLICATE_RECORD_ACROSS_COLLECTIONS);
			generateFailedRecord(constructedeuropeanaId, xml,
					LookupState.DUPLICATE_RECORD_ACROSS_COLLECTIONS);
		} else if (!constructedeuropeanaId.getCid().equals(
				retrievedeuropeanaID.getCid())
				&& constructedeuropeanaId.getEid().equals(
						retrievedeuropeanaID.getEid())
				&& constructedeuropeanaId.getOrid().equals(
						retrievedeuropeanaID.getOrid())
				&& !constructedeuropeanaId.getXmlchecksum().equals(
						retrievedeuropeanaID.getXmlchecksum())) {
			lookupresult
					.setState(LookupState.DUPLICATE_IDENTIFIER_ACROSS_COLLECTIONS);
			generateFailedRecord(constructedeuropeanaId, xml,
					LookupState.DUPLICATE_IDENTIFIER_ACROSS_COLLECTIONS);
		}

		// Update Session ID
		updateops.set(DATE, new Date());

		// Update Date
		updateops.set(SESSION, sessionID);

		datastore.update(retrievedeuropeanaID, updateops);

		return lookupresult;
	}

	private boolean checkForChangedCollection(
			EuropeanaIdRegistry constructedeuropeanaId) {
		EuropeanaIdRegistry retrievedId = retrieveFromOriginalXML(
				constructedeuropeanaId.getOrid(),
				constructedeuropeanaId.getXmlchecksum());
		if (retrievedId != null) {
			EuropeanaId eurId = new EuropeanaId();
			eurId.setNewId(constructedeuropeanaId.getEid());
			eurId.setOldId(retrievedId.getEid());
			eurId.setTimestamp(new Date().getTime());
			europeanaIdMongoServer.saveEuropeanaId(eurId);
			return true;

		}
		return false;
	}

	/**
	 * Creates and stores/updates a new failed record
	 * 
	 * @param eurId
	 *            The record that failed
	 * @param xml
	 *            The original EDM of the record
	 * @param lookupState
	 *            The reason it failed
	 */
	private void generateFailedRecord(EuropeanaIdRegistry eurId, String xml,
			LookupState lookupState) {
		FailedRecord failedRecord = datastore.find(FailedRecord.class)
				.filter("originalId", eurId.getOrid())
				.filter("collectionId", eurId.getCid()).get();
		System.out.println("Generating failed Record " + eurId + ", Reason: "
				+ lookupState.toString());
		// If it has not been found then create
		if (failedRecord == null) {
			failedRecord = new FailedRecord();
			failedRecord.setCollectionId(eurId.getCid());
			failedRecord.setEuropeanaId(eurId.getEid());
			failedRecord.setOriginalId(eurId.getOrid());
			failedRecord.setXml(xml);
			failedRecord.setLookupState(lookupState);
			datastore.save(failedRecord);
		}
		// or else update the fields that might have changed (xml representation
		// and the lookupState)
		else {
			UpdateOperations<FailedRecord> updateops = datastore
					.createUpdateOperations(FailedRecord.class);
			updateops.set("xml", xml);
			updateops.set("lookupState", lookupState);
			datastore.update(failedRecord, updateops);
		}

	}

	/**
	 * Generates the checksum for the given string
	 * 
	 * @param xml
	 * @return
	 */
	@SuppressWarnings("deprecation")
	private String generatechecksum(String xml) {
		return DigestUtils.shaHex(xml);
	}

	/**
	 * Find the EuropeanaId records based on the oldId
	 * 
	 * @param oldId
	 *            The id to search for
	 * @return
	 */
	public List<EuropeanaIdRegistry> retrieveEuropeanaIdFromOriginal(
			String originalId, String collectionid) {
		return datastore.find(EuropeanaIdRegistry.class).field(ORID)
				.equal(originalId).asList();
	}

	/**
	 * Find the EuropeanaId records based on the newId (Europeana id)
	 * 
	 * @param newId
	 *            The id to search for
	 * @return
	 */
	public EuropeanaIdRegistry retrieveEuropeanaIdFromNew(String newId) {

		List<EuropeanaIdRegistry> retrList = datastore
				.find(EuropeanaIdRegistry.class).field(EID).equal(newId)
				.asList();

		if (retrList.isEmpty()) {
			return null;
		} else {
			return retrList.get(0);
		}
	}

	/**
	 * Check if the record has oldIDs based on the newID
	 * 
	 * @param newId
	 *            the newID
	 * @return true if oldIDs are present false otherwise
	 */
	public boolean oldIdExists(String newId) {
		return datastore.find(EuropeanaIdRegistry.class).field(EID)
				.equal(newId).get() != null ? true : false;
	}

	public EuropeanaIdRegistry retrieveFromOriginalXML(String orId, String xml) {
		return datastore.find(EuropeanaIdRegistry.class)
				.filter(XMLCHECKSUM, xml).filter(ORID, orId).get();

	}

	/**
	 * Check if the record has newID based on the oldID
	 * 
	 * @param oldId
	 *            the oldID
	 * @return true if newIDs are present false otherwise
	 */
	public boolean newIdExists(String oldId) {
		return datastore.find(EuropeanaIdRegistry.class).field(ORID)
				.equal(oldId).get() != null ? true : false;
	}

	/**
	 * Delete a specific EuropeanaID record
	 * 
	 * @param oldId
	 *            The oldId to search for
	 * @param newId
	 *            The newId to search for
	 */
	public void deleteEuropeanaId(String oldId, String newId) {
		
	}

	/**
	 * Delete all the records based on the oldID
	 * 
	 * @param oldId
	 *            The id to search for
	 */
	public void deleteEuropeanaIdFromOld(String oldId) {

	}

	/**
	 * Delete all the records based on the newID
	 * 
	 * @param newId
	 *            The id to search for
	 */
	public void deleteEuropeanaIdFromNew(String newId) {

	}

	public List<Map<String, String>> getFailedRecords(String collectionId) {
		List<Map<String, String>> failedRecords = new ArrayList<Map<String, String>>();
		for (FailedRecord failedRecord : datastore.find(FailedRecord.class)
				.filter("collectionId", collectionId).asList()) {
			Map<String, String> record = new HashMap<String, String>();
			record.put("collectionId", failedRecord.getCollectionId());
			record.put("originalId", failedRecord.getOriginalId());
			record.put("europeanaId", failedRecord.getEuropeanaId());
			record.put("edm", failedRecord.getXml());
			record.put("lookupState", failedRecord.getLookupState().toString());
			failedRecords.add(record);
		}
		return failedRecords;
	}
}