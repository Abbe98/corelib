package eu.europeana.corelib.web.service;

import java.io.UnsupportedEncodingException;

import eu.europeana.corelib.definitions.solr.DocType;
import eu.europeana.corelib.web.utils.UrlBuilder;

public interface EuropeanaUrlService {
	
	// HARDCODED URLS
	static final String URL_EUROPEANA 		= "http://www.europeana.eu";
	static final String URL_IMAGE_SITE 		= "http://europeanastatic.eu/api/image";

	// ENCODING
	static final String ENC_UTF8 			= "UTF-8";
	
	// GENERAL PATHS
	static final String PATH_RECORD 		= "record";
	
	// PORTAL PATHS
	static final String PATH_PORTAL 		= "portal";
	static final String PATH_PORTAL_RESOLVE = "resolve";
	
	// API PATHS
	static final String PATH_API 			= "api";
	static final String PATH_API_V1 		= "v1";
	static final String PATH_API_V2 		= "v2";
	static final String PATH_API_REDIRECT 	= "redirect";
	
	// EXTENTIONS
	static final String EXT_JSON			= ".json";
	static final String EXT_JSON_LD			= ".jsonld";
	static final String EXT_HTML			= ".html";
	
	// GENERAL PARAMS
	static final String PARAM_SEARCH_QUERY 	= "query";
	static final String PARAM_SEARCH_ROWS 	= "rows";
	static final String PARAM_SEARCH_START 	= "start";
	static final String PARAM_SEARCH_FACET 	= "qf";

	// API PARAMS
	static final String PARAM_API_V2_APIKEY	= "wskey";
	
	UrlBuilder getApi2Home(String apikey);
	
	UrlBuilder getApi2SearchJson(String apikey, String query, String rows) throws UnsupportedEncodingException;

	UrlBuilder getApi2RecordJson(String apikey, String collectionid, String objectid);

	UrlBuilder getApi2RecordJson(String apikey, String europeanaId);
	
	UrlBuilder getApi2Redirect(long uid, String showAt, String provider, String europeanaId, String profile);
	
	UrlBuilder getPortalHome(boolean relative);

	String getPortalResolve(String europeanaId);
	
	UrlBuilder getPortalSearch(boolean relative, String query, String rows) throws UnsupportedEncodingException;

	UrlBuilder getPortalSearch(boolean relative, String searchpage, String query, String rows) throws UnsupportedEncodingException;
	
	UrlBuilder getPortalRecord(boolean relative, String collectionid, String objectid);
	
	UrlBuilder getPortalRecord(boolean relative, String europeanaId);

	UrlBuilder getThumbnailUrl(String thumbnail, DocType type);
	
}
