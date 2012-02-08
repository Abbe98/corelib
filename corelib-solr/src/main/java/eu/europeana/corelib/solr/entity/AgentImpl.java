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

package eu.europeana.corelib.solr.entity;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.code.morphia.annotations.*;

import org.bson.types.ObjectId;

import eu.europeana.corelib.definitions.solr.entity.Agent;
/**
 * @see eu.europeana.corelib.definitions.solr.entity.Agent
 * @author yorgos.mamakis@kb.nl
 *
 */
@Entity("Agent")
public class AgentImpl implements Agent {
	
@Id ObjectId agentId;
private Map<String,String> prefLabel;
private Map<String,String> altLabel;
private String[] note;

private Date begin;
private Date end;

	@Override
	public Map<String,String> getPrefLabel() {
		
		return this.prefLabel;
	}

	@Override
	public Map<String,String> getAltLabel() {
		return this.altLabel;
	}

	@Override
	public String[] getNote() {
		return this.note;
	}

	@Override
	public Date getBegin() {
		return this.begin;
	}

	@Override
	public Date getEnd() {
		return this.end;
	}

	@Override
	public ObjectId getAgentId() {
		return this.agentId;
	}
	@Override
	public void setAgentId(ObjectId agentId) {
		this.agentId = agentId;
	}
	@Override
	public void setAltLabel(Map<String,String> altLabel) {
		this.altLabel = altLabel;
	}
	@Override
	public void setPrefLabel(Map<String,String> prefLabel) {
		this.prefLabel = prefLabel;
	}
	@Override
	public void setNote(String[] note) {
		this.note = note;
	}
	
	@Override
	public void setBegin(Date begin) {
		this.begin = begin;
	}
	@Override
	public void setEnd(Date end) {
		this.end = end;
	}
	
	@Override
	public boolean equals(Object o){
		
		return this.getAgentId().equals(((AgentImpl)o).getAgentId());
	}
}
