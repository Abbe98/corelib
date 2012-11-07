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
package eu.europeana.corelib.tools.utils;

import org.apache.commons.lang.StringUtils;

/**
 * Utility class to read mappings developed by SIPCreator 
 * @author Yorgos.Mamakis@ kb.nl
 *
 */
public class SipCreatorUtils extends MappingParser{

	private String repository;
	private final static String INPUT_FOLDER = "/input_source/";
	private final static String SUFFIX = ".xml.mapping";
	private final static String BEGIN_HASH_FUNCTION_RECORD = "createEuropeanaURI(input.record.";
	private final static String BEGIN_HASH_FUNCTION_NO_RECORD = "createEuropeanaURI(input.";
	private final static String END_HASH_FUNCTION = ")";
	
	
	
	@Override
	public String getHashField(String collectionId, String fileName) {
		String inputString = this.readFile(repository + collectionId + INPUT_FOLDER
				+ fileName + SUFFIX);
		
		return inputString == null?null:(StringUtils.substringBetween(inputString, BEGIN_HASH_FUNCTION_RECORD,
				END_HASH_FUNCTION)==null?StringUtils.substringBetween(inputString, BEGIN_HASH_FUNCTION_NO_RECORD,
						END_HASH_FUNCTION):(StringUtils.substringBetween(inputString, BEGIN_HASH_FUNCTION_RECORD,
								END_HASH_FUNCTION)));
	}

	/**
	 * Specify the place the mappings are held
	 * @param repository
	 */
	public void setRepository(String repository) {
		this.repository = repository;
	}
	
	

}
