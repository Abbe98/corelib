package eu.europeana.corelib.solr.denormalization.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;

import com.ctc.wstx.stax.WstxInputFactory;

import eu.europeana.corelib.definitions.model.EdmLabel;
import eu.europeana.corelib.solr.denormalization.ControlledVocabulary;

/**
 * Denormalization Utility. It retrieves the description of a reference URI
 * according to the stored mappings
 * 
 * @author Yorgos.Mamakis@ kb.nl
 * 
 */
public class Extractor {
	private VocabularyMongoServer mongoServer;
	private static ControlledVocabulary vocabulary;

	/**
	 * Constructor for use with object injection
	 */
	public Extractor(ControlledVocabulary controlledVocabulary) {
		vocabulary = controlledVocabulary;
	}

	/**
	 * Constructor with the MongoDBServer for use without object Injection
	 * 
	 * @param server
	 */
	public Extractor(ControlledVocabulary controlledVocabulary,
			VocabularyMongoServer server) {
		vocabulary = controlledVocabulary;
		this.mongoServer = server;
	}

	/**
	 * Return the stored controlled vocabulary from its URI
	 * 
	 * @param URI
	 *            The URI to search with
	 * @return The stored ControlledVocabulary to be used
	 */
	public ControlledVocabulary getControlledVocabulary(String URI) {
		vocabulary = mongoServer.getDatastore()
				.find(ControlledVocabularyImpl.class).filter("URI", URI).get();
		return vocabulary;
	}

	/**
	 * Retrieve all the stored controlled vocabularies
	 * 
	 * @return A list with all the stored controlled vocabularies
	 */
	public List<ControlledVocabularyImpl> getControlledVocabularies() {
		return mongoServer.getDatastore().find(ControlledVocabularyImpl.class) != null ? mongoServer
				.getDatastore().find(ControlledVocabularyImpl.class).asList()
				: null;
	}

	/**
	 * Denormalization method
	 * 
	 * @param resource
	 *            The URI to retrieve the denormalized information from
	 * @param controlledVocabulary
	 *            The controlled vocabulary holding the mappings
	 * @return A List of pairs of strings representing the <Europeana Mapped
	 *         Field , Denormalized values>
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public List<List<String>> denormalize(String resource,
			ControlledVocabularyImpl controlledVocabulary)
			throws MalformedURLException, IOException {
		List<List<String>> denormalizedValues = new ArrayList<List<String>>();
		String suffix = controlledVocabulary.getSuffix();
		String xmlString = retrieveValueFromResource(resource + suffix != null ? suffix
				: "");
		XMLInputFactory inFactory = new WstxInputFactory();
		Source source;
		try {
			source = new StreamSource(new ByteArrayInputStream(
					xmlString.getBytes()), "UTF-8");
			XMLStreamReader xml = inFactory.createXMLStreamReader(source);
			String element = "";
			boolean mapped = false;
			while (xml.hasNext()) {
				List<String> tempList = new ArrayList<String>();
				switch (xml.getEventType()) {
				case XMLStreamConstants.START_DOCUMENT:

					break;
				case XMLStreamConstants.START_ELEMENT:
					element = xml.getPrefix() + ":" + xml.getLocalName();

					if (isMapped(element)) {
						if (xml.getAttributeCount() > 0) {
							String attribute = xml.getAttributePrefix(0) + ":"
									+ xml.getAttributeLocalName(0);
							if (isMapped(element + "_" + attribute)) {
								mapped = false;
								tempList.add(getEdmLabel(attribute).toString());
								tempList.add(xml.getAttributeValue(0));
								denormalizedValues.add(tempList);
								tempList = new ArrayList<String>();
							}
							xml.next();
						} else {
							tempList.add(getEdmLabel(element).toString());
							tempList.add(xml.getElementText());
							mapped = true;
							denormalizedValues.add(tempList);
							tempList = new ArrayList<String>();
						}
					}
					break;
				case XMLStreamConstants.CHARACTERS:
					if (!mapped) {
						tempList.add(getEdmLabel(element).toString());
						tempList.add(xml.getElementText());
						denormalizedValues.add(tempList);
						tempList = new ArrayList<String>();
					}
				}
				xml.next();
			}
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return denormalizedValues;
	}

	/**
	 * Retrieve an XML string from a ControlledVocabulary
	 * 
	 * @param resource
	 *            - The name of the resource to retrieve
	 * @return An XML string representing the RDF/XML resource from an online
	 *         Controlled Vocabulary
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	private String retrieveValueFromResource(String resource)
			throws MalformedURLException, IOException {
		URLConnection urlConnection = new URL(resource).openConnection();
		InputStream inputStream = urlConnection.getInputStream();
		StringWriter writer = new StringWriter();
		IOUtils.copy(inputStream, writer, "UTF-8");
		;
		return writer.toString();
	}

	public EdmLabel getEdmLabel(String field) {

		return vocabulary.getElements().get(field);
	}

	public void setMappedField(String fieldToMap, EdmLabel europeanaField) {
		vocabulary.getElements().put(fieldToMap, europeanaField);

	}

	public String getMappedField(EdmLabel europeanaField) {
		for (String key : vocabulary.getElements().keySet()) {
			if (europeanaField.equals(vocabulary.getElements().get(key))) {
				return key;
			}
		}
		return null;
	}

	public boolean isMapped(String field) {
		return vocabulary.getElements().containsKey(field);
	}

	public Map<String, EdmLabel> readSchema(String location) {

		vocabulary.setElements(readFromFile(location));
		return vocabulary.getElements();
	}

	private Map<String, EdmLabel> readFromFile(String localLocation) {
		Map<String, EdmLabel> elements = new HashMap<String, EdmLabel>();
		XMLInputFactory inFactory = new WstxInputFactory();
		Source source;
		try {
			source = new StreamSource(new FileInputStream(new File(
					localLocation)), "UTF-8");
			XMLStreamReader xml = inFactory.createXMLStreamReader(source);
			String element = "";
			String attribute = "";
			while (xml.hasNext()) {
				switch (xml.getEventType()) {
				case XMLStreamConstants.START_DOCUMENT:
					xml.next();
					break;
				case XMLStreamConstants.START_ELEMENT:
					element = xml.getName().getPrefix() + ":"
							+ xml.getName().getLocalPart();
					System.out.println(element);
					elements.put(element, EdmLabel.NULL);
					int i = 0;
					while (i < xml.getAttributeCount()) {
						attribute = element + "_" + xml.getAttributePrefix(i)
								+ ":" + xml.getAttributeLocalName(i);
						elements.put(attribute, EdmLabel.NULL);
						i++;
					}
					xml.next();
					break;
				default:
					xml.next();
				}
			}

		} catch (FileNotFoundException e) {
			// Should never happen
			e.printStackTrace();
		} catch (XMLStreamException e) {

			e.printStackTrace();
		}
		return elements;
	}
}
