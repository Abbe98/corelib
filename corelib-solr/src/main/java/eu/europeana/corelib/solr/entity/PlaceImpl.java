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

import java.util.Map;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import com.google.code.morphia.annotations.Entity;

import eu.europeana.corelib.definitions.solr.entity.Place;
import eu.europeana.corelib.utils.StringArrayUtils;

/**
 * @see eu.europeana.corelib.definitions.solr.entity.Place
 * @author Yorgos.Mamakis@ kb.nl
 * 
 */
@JsonSerialize(include = Inclusion.NON_EMPTY)
@Entity("Place")
public class PlaceImpl extends ContextualClassImpl implements Place {

	private Map<String,String> isPartOf;
	private Map<String,Float> latitude;
	private Map<String,Float> longitude;
	private Map<String,Float> altitude;
	private Map<String,Float> position;
	private Map<String,String> dcTermsHasPart;
	private String[] owlSameAs;

	@Override
	public Map<String,String> getIsPartOf() {
		return this.isPartOf;
	}

	@Override
	public Map<String,Float> getLatitude() {
		return this.latitude;
	}

	@Override
	public Map<String,Float> getLongitude() {
		return this.longitude;
	}

	@Override
	public void setIsPartOf(Map<String,String> isPartOf) {
		this.isPartOf = isPartOf;
	}

	@Override
	public void setLatitude(Map<String,Float> latitude) {
		this.latitude = latitude;
	}

	@Override
	public void setLongitude(Map<String,Float> longitude) {
		this.longitude = longitude;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null){
			return false;
		}
		if (o.getClass() == this.getClass()){
			return ((PlaceImpl) o).getAbout() != null ? this.getAbout().equals(((PlaceImpl) o).getAbout()) : false;
		}
		return false;
	}
	
	@Override
	public int hashCode(){ 
		return (int) (this.getAbout() != null ? this.getAbout().hashCode() : ((Float)this.latitude.values().toArray()[0]) * 100 + (Float)this.longitude.values().toArray()[0]);
	}

	@Override
	public void setAltitude(Map<String,Float> altitude) {
		this.altitude = altitude;
	}

	@Override
	public Map<String,Float> getAltitude() {
		return this.altitude;
	}

	@Override
	public void setPosition(Map<String, Float> position) {
		this.position = position;
	}

	@Override
	public Map<String,Float> getPosition() {
		return this.position;
	}

	@Override
	public void setDcTermsHasPart(Map<String,String> dcTermsHasPart) {
		this.dcTermsHasPart = dcTermsHasPart;
	}

	@Override
	public Map<String,String> getDcTermsHasPart() {
		return this.dcTermsHasPart;
	}

	@Override
	public void setOwlSameAs(String[] owlSameAs) {
		this.owlSameAs = owlSameAs;
	}

	@Override
	public String[] getOwlSameAs(){
		return (StringArrayUtils.isNotBlank(owlSameAs) ? this.owlSameAs.clone() : null);
	}
}
