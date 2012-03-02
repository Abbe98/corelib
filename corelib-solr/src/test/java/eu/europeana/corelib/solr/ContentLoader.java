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
package eu.europeana.corelib.solr;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;

import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.solr.exceptions.MongoDBException;
import eu.europeana.corelib.solr.server.impl.MongoDBServerImpl;
import eu.europeana.corelib.solr.server.impl.SolrServerImpl;
import eu.europeana.corelib.solr.util.MongoConstructor;
import eu.europeana.corelib.solr.util.SolrConstructor;

/**
 * Sample Class for uploading content in a local Mongo and Solr Instance
 *
 * @author Yorgos.Mamakis@ kb.nl
 */
public class ContentLoader {

    private static String solrHome = "src/test/resources/solr";
    private static String mongoHost = "localhost";
    private static String mongoPort = "27017";
    private static String databaseName = "europeana";
    private static String collectionName = "src/test/resources/records.zip";

    /**
     * Method to load content in SOLR and MongoDB
     *
     * @param args solrHome parameter holding "solr.solr.home"
     *                      mongoHost the host the mongoDB is located
     *                      mongoPort the port mongoDB listens
     *                      databaseName the databaseName to save
     *                      collectionName the name of the collection (an XML, a folder or a Zip file)
     *
     */
    public static void main(String[] args) {
        Map<String, String> params = new HashMap<String, String>();
        for (String parameter : args) {
            params.put(StringUtils.split(parameter, ":")[0],
                    StringUtils.split(parameter, ":")[1]);
        }

        if (params.get("solrHome") != null
                && params.get("solrHome").length() > 0) {
            solrHome = params.get("solrHome");
        } else {
            System.out.println("Parameter solrHome is not set correctly");
            System.out.println("Using default solrHome: " + solrHome);
        }

        if (params.get("mongoHost") != null
                && params.get("mongoHost").length() > 0) {
            mongoHost = params.get("mongoHost");
        } else {
            System.out.println("Parameter mongoHost is not set correctly");
            System.out.println("Using default mongoHost: " + mongoHost);
        }

        if (params.get("mongoPort") != null
                && params.get("mongoPort").length() > 0) {
            try {
                Integer.parseInt(mongoPort);
                mongoPort = params.get("mongoPort");
            } catch (NumberFormatException nbe) {
                System.out.println("Parameter mongoPort is not set correctly");
                System.out.println("Using default mongoPort: " + mongoPort);
            }
        } else {
            System.out.println("Parameter mongoPort is not set correctly");
            System.out.println("Using default mongoPort: " + mongoPort);
        }

        if (params.get("databaseName") != null
                && params.get("databaseName").length() > 0) {
            databaseName = params.get("databaseName");
        } else {
            System.out.println("Parameter databaseName is not set correctly");
            System.out.println("Using default databaseName: " + databaseName);
        }

        if (params.get("collectionName") != null
                && params.get("collectionName").length() > 0) {
            collectionName = params.get("collectionName");
        } else {
            System.out.println("Parameter collectionName is not set correctly");
            System.out.println("Using default collectionName: "
                    + collectionName);
        }
        ArrayList<File> collectionXML = new ArrayList<File>();
        if (isZipped(collectionName)) {
            collectionXML = unzip(collectionName);
        } else {
            if (new File(collectionName).isDirectory()) {
                for (File file : new File(collectionName).listFiles()) {
                    if (StringUtils.endsWith(file.getName(), ".xml")) {
                        collectionXML.add(file);
                    }
                }
            } else {
                collectionXML.add(new File(collectionName));
            }
        }
        List<SolrInputDocument> records = new ArrayList<SolrInputDocument>();
        SolrServerImpl solrServer = null;
        try {
            solrServer = new SolrServerImpl(solrHome);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        int i = 0;
        int failed=0;
        int imported=0;
        try {
            MongoConstructor.setParameters(new MongoDBServerImpl(
                    mongoHost, Integer.parseInt(mongoPort), databaseName));
        } catch (NumberFormatException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (MongoDBException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        for (File f : collectionXML) {
            try {
                IBindingFactory bfact = BindingDirectory.getFactory(RDF.class);
                IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
                i++;
                RDF rdf = (RDF) uctx.unmarshalDocument(new FileInputStream(f),
                        null);

                MongoConstructor.constructFullBean(rdf);
                records.add(SolrConstructor.constructSolrDocument(rdf));
                
                if (i % 1000 == 0 || i == collectionXML.size()) {
                    System.out.println("Sending " + i + " records to SOLR");
                    imported+= records.size();
                    solrServer.add(records);
                    records.clear();
                }

            } catch (JiBXException e) {
            	failed++;
                System.out.println("Error unmarshalling document "
                        + f.getName()
                        + " from the input file. Check for Schema changes");
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                System.out.println("File does not exist");

            } catch (NumberFormatException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InstantiationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (SolrServerException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try {

            solrServer.commit();
            solrServer.optimize();

            System.out.println("Finished Importing");
            System.out.println("Records read: "+i);
            System.out.println("Records imported: " + imported);
            System.out.println("Records failed: "+ failed);
            solrServer = null;
            System.out.println("Deleting files");
            for (File f : collectionXML) {
                f.delete();
            }
            System.out.println("Files deleted");
        } catch (SolrServerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Unzip a zipped collection file
     *
     * @param collectionName The path to the collection file
     * @return The unzipped file
     */
    private static ArrayList<File> unzip(String collectionName) {
        BufferedOutputStream dest = null;
        String fileName = "";
        ArrayList<File> records = new ArrayList<File>();
        try {
            FileInputStream fis = new FileInputStream(collectionName);
            ZipInputStream zis = new ZipInputStream(
                    new BufferedInputStream(fis));
            ZipEntry entry;
            File workingDir = new File("src/test/resources/records");
            workingDir.mkdir();
            while ((entry = zis.getNextEntry()) != null) {
                int count;
                byte data[] = new byte[2048];
                fileName = workingDir.getAbsolutePath() + "/" + entry.getName();
                records.add(new File(fileName));
                FileOutputStream fos = new FileOutputStream(fileName);
                dest = new BufferedOutputStream(fos, 2048);
                while ((count = zis.read(data, 0, 2048)) != -1) {
                    dest.write(data, 0, count);
                }
                dest.flush();
                dest.close();

            }
            zis.close();

        } catch (Exception e) {
            System.out.println("The zip file is destroyed. Could not import records");
            e.printStackTrace();
        }
        return records;
    }

    /**
     * Check if the collection file is zipped
     *
     * @param collectionName
     * @return true if it is in gzip or zip format, false if not
     */
    private static boolean isZipped(String collectionName) {
        return StringUtils.endsWith(collectionName, ".gzip")
                || StringUtils.endsWith(collectionName, ".zip");
    }
}
