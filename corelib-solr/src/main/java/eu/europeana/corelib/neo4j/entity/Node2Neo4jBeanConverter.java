/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.europeana.corelib.neo4j.entity;

import eu.europeana.corelib.definitions.solr.DocType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.neo4j.graphdb.Node;

/**
 *
 * @author gmamakis
 */
public class Node2Neo4jBeanConverter {
    
    
    public static Neo4jBean toNeo4jBean(Node node){
        Neo4jBean neo4jBean = new Neo4jBean();
        neo4jBean.setId((String)node.getProperty("rdf:about"));
        neo4jBean.setType(DocType.valueOf((String)node.getProperty("edm:type")));
        neo4jBean.setTitle((String)node.getProperty("dc:title"));
        Map<String,List<String>> descriptions = new HashMap<String, List<String>>();
        Iterable<String> keys = node.getPropertyKeys();
        Iterator<String> keyIterator = keys.iterator();
        while(keyIterator.hasNext()){
            String key =keyIterator.next();
            if(key.startsWith("dc:description")){
                List<String> descriptionValue = descriptions.get(StringUtils.substringAfter(key,"dc:description_xml:lang_"));
                if(descriptionValue==null){
                    descriptionValue = new ArrayList<String>();
                }
                descriptionValue.addAll((List<String>)node.getProperty(key));
                descriptions.put(StringUtils.substringAfter(key,"dc:description_xml:lang_"), descriptionValue);
            }
        }
        neo4jBean.setDescription(descriptions);
        return neo4jBean;
    }
}
