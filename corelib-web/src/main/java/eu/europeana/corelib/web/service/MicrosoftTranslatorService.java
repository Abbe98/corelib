package eu.europeana.corelib.web.service;

import com.google.gson.Gson;
import eu.europeana.corelib.definitions.ApplicationContextContainer;
import eu.europeana.corelib.web.model.ApiResult;
import eu.europeana.corelib.web.utils.UrlBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @deprecated since 2017-09-22 (BUT THIS IS NOT TO BE REMOVED YET!)
 */
@Deprecated
public class MicrosoftTranslatorService extends JsonApiService  {

	private static final Logger LOG = LogManager.getLogger(MicrosoftTranslatorService.class);
	private static final String TRANSLATOR_URL = "http://api.microsofttranslator.com/V2/Ajax.svc/Translate";

	public static MicrosoftTranslatorService getBeanInstance() {
		return ApplicationContextContainer.getBean(MicrosoftTranslatorService.class);
	}

	public String translate(String text, String languageCode) {
		String url = buildTranslateUrl(text, languageCode);
		LOG.debug("Calling translate url: {} ", url);
		ApiResult result = getJsonResponse(url);
		return parseJson(result);
	}

	private String parseJson(ApiResult result) {
		Gson gson = new Gson();
		return gson.fromJson(result.getContent(), String.class);
	}

	private String buildTranslateUrl(String text, String languageCode) {
		UrlBuilder url = new UrlBuilder(TRANSLATOR_URL);
		//url.addParam("appId", config.getBingTranslateId());
		url.addParam("text", text);
		url.addParam("to", languageCode);
		return url.toString();
	}
}
