package eu.europeana.corelib.tools.lookuptable;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.mongodb.Mongo;
import com.mongodb.MongoException;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.distribution.Version;

import eu.europeana.corelib.tools.lookuptable.impl.EuropeanaIdRegistryMongoServerImpl;

public class EuropeanaIdRegistryMongoServerTest {

	EuropeanaIdRegistryMongoServerImpl server;
	EuropeanaIdRegistry registry;
	String cid="12345";
	String oid="test_oid";
	String eid="/12345/test_oid";
	String sid="test_sid";
	String sid2="test_sid2";
	String xml_checksum="tst_checksum";
	MongodExecutable mongodExecutable;
	@Before
	public void prepare(){
		try{
			int port = 10000;
			MongodConfig conf = new MongodConfig(Version.V2_0_7, port,
					false);

			MongodStarter runtime = MongodStarter.getDefaultInstance();

			 mongodExecutable = runtime.prepare(conf);
			mongodExecutable.start();
			server = new EuropeanaIdRegistryMongoServerImpl(new Mongo("localhost",port), "europeana_registry_test");
			server.getEuropeanaIdMongoServer().createDatastore();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MongoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	@Test 
	public void testRegistered(){
			Assert.assertTrue(!server.newIdExists(oid));
			Assert.assertEquals(server.retrieveEuropeanaIdFromOriginal(oid, cid).size(),0);
			Assert.assertTrue(!server.oldIdExists(eid));
			Assert.assertEquals(server.retrieveEuropeanaIdFromNew(eid),null);
			Assert.assertEquals(server.retrieveFromOriginalXML(oid, generatechecksum(xml_checksum)), null);
			LookupResult lresult = server.lookupUiniqueId(oid, cid, xml_checksum, sid);			
			Assert.assertEquals(LookupState.ID_REGISTERED,lresult.getState());
			
			server.getDatastore().getDB().dropDatabase();
	}
	
	
	@Test 
	public void testIdentical(){
		
			saverecord(sid);		
			Assert.assertTrue(server.newIdExists(oid));
			Assert.assertEquals(server.retrieveEuropeanaIdFromOriginal(oid, cid).get(0).getEid(),registry.getEid());
			Assert.assertTrue(server.oldIdExists(eid));
			Assert.assertEquals(server.retrieveEuropeanaIdFromNew(eid).getOrid(),registry.getOrid());
			Assert.assertEquals(server.retrieveFromOriginalXML(oid, generatechecksum(xml_checksum)).getEid(), registry.getEid());
			Assert.assertEquals(LookupState.IDENTICAL,server.lookupUiniqueId(oid, cid, xml_checksum, sid2).getState());
			
			server.getDatastore().getDB().dropDatabase();
	}
	
	@Test 
	public void testDuplicateInCollection(){
		
			saverecord(sid);
			
			Assert.assertTrue(server.newIdExists(oid));
			Assert.assertEquals(server.retrieveEuropeanaIdFromOriginal(oid, cid).get(0).getEid(),registry.getEid());
			Assert.assertTrue(server.oldIdExists(eid));
			Assert.assertEquals(server.retrieveEuropeanaIdFromNew(eid).getOrid(),registry.getOrid());
			Assert.assertEquals(server.retrieveFromOriginalXML(oid, generatechecksum(xml_checksum)).getEid(), registry.getEid());
			Assert.assertEquals(LookupState.DUPLICATE_INCOLLECTION,server.lookupUiniqueId(oid, cid, xml_checksum, sid).getState());
			
			server.getDatastore().getDB().dropDatabase();
	}
	
	
	
	@Test
	public void testCollectionChanged(){
		saverecord(sid);
		Assert.assertEquals(server.lookupUiniqueId(oid, "12346", xml_checksum, sid).getState(),LookupState.COLLECTION_CHANGED);
		server.getDatastore().getDB().dropDatabase();
	}
	
	@Test
	public void testDuplicateIdAcross(){
		saverecord(sid);
		Assert.assertEquals(server.lookupUiniqueId(oid, "12345a", xml_checksum+"new", sid).getState(),LookupState.DUPLICATE_IDENTIFIER_ACROSS_COLLECTIONS);
		server.getDatastore().getDB().dropDatabase();
	}
	
	@Test
	public void testDuplicateRecAcross(){
		saverecord(sid);
		Assert.assertEquals(server.lookupUiniqueId(oid, "12345a", xml_checksum, sid).getState(),LookupState.DUPLICATE_RECORD_ACROSS_COLLECTIONS);
		server.getDatastore().getDB().dropDatabase();
	}
	
	@Test
	public void testUpdate(){
		saverecord(sid);
		Assert.assertEquals(server.lookupUiniqueId(oid, "12345", xml_checksum+"new", sid2).getState(),LookupState.UPDATE);
		server.getDatastore().getDB().dropDatabase();
	}
	
	@Test
	public void testFailedrecords(){
		
		saverecord(sid);
		
		Assert.assertTrue(server.newIdExists(oid));
		Assert.assertEquals(server.retrieveEuropeanaIdFromOriginal(oid, cid).get(0).getEid(),registry.getEid());
		Assert.assertTrue(server.oldIdExists(eid));
		Assert.assertEquals(server.retrieveEuropeanaIdFromNew(eid).getOrid(),registry.getOrid());
		Assert.assertEquals(server.retrieveFromOriginalXML(oid, generatechecksum(xml_checksum)).getEid(), registry.getEid());
		Assert.assertEquals(LookupState.DUPLICATE_INCOLLECTION,server.lookupUiniqueId(oid, cid, xml_checksum, sid).getState());
		List<Map<String, String>> frecords = server.getFailedRecords(cid);
		Assert.assertEquals(1,  frecords.size());
		Map<String, String> valuemap = frecords.get(0);
		Assert.assertEquals(valuemap.get("edm"), "tst_checksum");
		Assert.assertEquals(valuemap.get("collectionId"), "12345");
		Assert.assertEquals(valuemap.get("originalId"), "test_oid");
		Assert.assertEquals(valuemap.get("lookupState"), "DUPLICATE_INCOLLECTION");
		Assert.assertEquals(valuemap.get("europeanaId"), "/12345/test_oid");
		
		server.getDatastore().getDB().dropDatabase();
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

	
	private void saverecord(String sessionID){
		registry = new EuropeanaIdRegistry();
		registry.setCid(cid);
		registry.setEid(eid);
		registry.setOrid(oid);
		registry.setSessionID(sessionID);
		registry.setXmlchecksum(generatechecksum(xml_checksum));
		server.getDatastore().save(registry);
	}
	
	
	
	@After
	public void cleanup(){
		mongodExecutable.stop();
	}
}
