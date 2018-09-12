package eu.europeana.corelib.edm.utils;

import eu.europeana.corelib.definitions.edm.entity.WebResource;
import eu.europeana.corelib.edm.model.schema.org.*;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.entity.*;
import eu.europeana.corelib.utils.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Map;

public class SchemaOrgUtils {
    private static final Logger LOG = LogManager.getLogger(SchemaOrgUtils.class);

    private static final String URL_PREFIX = "http://data.europeana.eu";

    private static final String PLACE_PREFIX = "http://data.europeana.eu/place";

    private static final String TIMESPAN_PREFIX = "http://semium.org";

    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);

    private SchemaOrgUtils() { }

    /**
     * Convert full bean to schema.org jsonld
     * @param bean bean with metadata
     * @return string representation of schema.org object
     */
    public static String toSchemaOrg(FullBeanImpl bean) {
        String jsonld = null;

        List<Thing> objectsToSerialize = new ArrayList<>();
        Thing object = SchemaOrgTypeFactory.createObject(bean);
        objectsToSerialize.add(object);

        processProvidedCHO((CreativeWork) object, bean);
        processProxies((CreativeWork) object, bean);
        processAggregations((CreativeWork) object, bean);

        JsonLdSerializer serializer = new JsonLdSerializer();
        try {
            jsonld = serializer.serialize(objectsToSerialize);
        } catch (IOException e) {
            LOG.error("Serialization to schema.org failed for " + object.getId(), e);
        }
        return jsonld;
    }

    /**
     * Process necessary properties from provided CHOs
     * @param object object for which the properties will be added
     * @param bean bean with all properties
     */
    private static void processProvidedCHO(CreativeWork object, FullBeanImpl bean) {
        for (ProvidedCHOImpl providedCHO : bean.getProvidedCHOs()) {
            // @id
            if (!notNullNorEmpty(object.getId())) {
                object.setId(URL_PREFIX + providedCHO.getAbout());
            }

            // sameAs
            addMultilingualProperties(object, toList(providedCHO.getOwlSameAs()), "", SchemaOrgConstants.PROPERTY_SAME_AS);
        }
    }

    /**
     * Helper method to convert array of strings to a list
     * @param array array of strings
     * @return empty list when the array is null, result of Arrays.asList otherwise
     */
    private static List<String> toList(String[] array) {
        if (array == null) {
            return new ArrayList<>();
        }
        return Arrays.asList(array);
    }

    /**
     * Process necessary properties from Aggregations
     * @param object object for which the properties will be added
     * @param bean bean with all properties
     */
    private static void processAggregations(CreativeWork object, FullBeanImpl bean) {
        for (AggregationImpl aggregation : bean.getAggregations()) {
            // sameAs
            object.addProperty(SchemaOrgConstants.PROPERTY_SAME_AS, new Text(aggregation.getEdmIsShownAt()));

            // provider
            Map<String, List<String>> providerMap = new HashMap<>();
            addDistinctValues(providerMap, aggregation.getEdmDataProvider());
            addDistinctValues(providerMap, aggregation.getEdmProvider());
            addDistinctValues(providerMap, aggregation.getEdmIntermediateProvider());
            addMultilingualPropertiesWithReferences(object, providerMap, SchemaOrgConstants.PROPERTY_PROVIDER, Organization.class);

            // associatedMedia
            Set<String> medias = new HashSet<>();
            if (aggregation.getHasView() != null) {
                medias.addAll(Arrays.asList(aggregation.getHasView()));
            }
            if (aggregation.getEdmIsShownBy() != null) {
                medias.add(aggregation.getEdmIsShownBy());
            }
            for (String media : medias) {
                addReference(object, media, SchemaOrgConstants.PROPERTY_ASSOCIATED_MEDIA, SchemaOrgTypeFactory.detectMediaObjectType(getMimeType(media, aggregation.getWebResources())));
            }
        }
    }

    /**
     * Retrieve mime type from web resource corresponding to the media url
     * @param media URL to media
     * @param webResources web resources list
     * @return mime type of the media resource or null if not found
     */
    private static String getMimeType(String media, List<? extends WebResource> webResources) {
        if (webResources == null) {
            return null;
        }
        for (WebResource resource : webResources) {
            if (resource.getAbout().equals(media)) {
                return resource.getEbucoreHasMimeType();
            }
        }
        return null;
    }

    /**
     * Add values to the map only if they don't exist
     * @param map map to which values will be added
     * @param toAdd values to be added
     */
    private static void addDistinctValues(Map<String, List<String>> map, Map<String, List<String>> toAdd) {
        if (toAdd != null) {
            for (Map.Entry<String, List<String>> entry : toAdd.entrySet()) {
                List<String> values = map.computeIfAbsent(entry.getKey(), k -> new ArrayList<>());
                entry.getValue().forEach(value -> {
                    if (!values.contains(value)) {
                        values.add(value);
                    }
                });
            }
        }
    }

    /**
     * Adds multilingual properties or typed references. When a reference is detected its type will be
     * set to <code>referenceClass</code>
     * @param object object for which the properties will be added
     * @param map map of values where key is the language and value is a values list
     * @param propertyName name of property
     * @param referenceClass class of reference
     */
    private static void addMultilingualPropertiesWithReferences(CreativeWork object,
                                                                Map<String, List<String>> map,
                                                                String propertyName,
                                                                Class<? extends Thing> referenceClass) {
        if (map == null) {
            return;
        }
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            addMultilingualPropertiesWithReferences(object, entry.getValue(), entry.getKey(), propertyName, referenceClass);
        }
    }

    /**
     * Adds multilingual properties or typed references. When a reference is detected the corresponding object is
     * being searched on <code>referenced</code> list and an additional object of type <code>referenceClass</code>
     * is created with properties.
     * @param object object for which the properties will be added
     * @param values array of values
     * @param propertyName name of property
     * @param referenceClass class of reference
     */
    private static void addMultilingualPropertiesWithReferences(CreativeWork object,
                                                                List<String> values,
                                                                String language,
                                                                String propertyName,
                                                                Class<? extends Thing> referenceClass) {
        if (values == null) {
            return;
        }
        for (String value : values) {
            addProperty(object, value, language, propertyName, referenceClass);
        }
    }

    /**
     * Process all proxies from the bean and add specific properties to the given object.
     *
     * @param object object for which the properties will be added
     * @param bean bean from database to get values for properties
     */
    private static void processProxies(CreativeWork object, FullBeanImpl bean) {
        for (ProxyImpl proxy : bean.getProxies()) {
            // contributor
            addMultilingualPropertiesWithReferences(object, proxy.getDcContributor(), SchemaOrgConstants.PROPERTY_CONTRIBUTOR, null);

            // about
            addMultilingualPropertiesWithReferences(object, proxy.getDcSubject(), SchemaOrgConstants.PROPERTY_ABOUT, null);
            addMultilingualPropertiesWithReferences(object, proxy.getDcType(), SchemaOrgConstants.PROPERTY_ABOUT, null);
            addMultilingualPropertiesWithReferences(object, proxy.getEdmHasType(), SchemaOrgConstants.PROPERTY_ABOUT, null);
            addProperty(object, proxy.getEdmIsRepresentationOf(), "", SchemaOrgConstants.PROPERTY_ABOUT, null);
            // values from dc:coverage will be added later

            // creator
            addMultilingualPropertiesWithReferences(object, proxy.getDcCreator(), SchemaOrgConstants.PROPERTY_CREATOR, null);

            // description
            addMultilingualProperties(object, proxy.getDcDescription(), SchemaOrgConstants.PROPERTY_DESCRIPTION);

            // inLanguage
            addMultilingualProperties(object, proxy.getDcLanguage(), SchemaOrgConstants.PROPERTY_IN_LANGUAGE);

            // publisher
            addMultilingualPropertiesWithReferences(object, proxy.getDcPublisher(), SchemaOrgConstants.PROPERTY_PUBLISHER, null);

            // name
            addMultilingualProperties(object, proxy.getDcTitle(), SchemaOrgConstants.PROPERTY_NAME);

            // alternateName
            addMultilingualProperties(object, proxy.getDctermsAlternative(), SchemaOrgConstants.PROPERTY_NAME);

            // dateCreated
            addDateProperty(object, proxy.getDctermsCreated(), SchemaOrgConstants.PROPERTY_DATE_CREATED, bean.getTimespans(), true);

            // hasPart
            addMultilingualPropertiesWithReferences(object, proxy.getDctermsHasPart(), SchemaOrgConstants.PROPERTY_HAS_PART, null);
            addMultilingualPropertiesWithReferences(object, toList(proxy.getEdmIncorporates()), "", SchemaOrgConstants.PROPERTY_HAS_PART, null);

            // exampleOfWork
            addMultilingualPropertiesWithReferences(object, proxy.getDctermsIsFormatOf(), SchemaOrgConstants.PROPERTY_EXAMPLE_OF_WORK, CreativeWork.class);
            addMultilingualPropertiesWithReferences(object, toList(proxy.getEdmRealizes()), "", SchemaOrgConstants.PROPERTY_EXAMPLE_OF_WORK, CreativeWork.class);

            // isPartOf
            addMultilingualPropertiesWithReferences(object, proxy.getDctermsIsPartOf(), SchemaOrgConstants.PROPERTY_IS_PART_OF, null);

            // datePublished
            addDateProperty(object, proxy.getDctermsIssued(), SchemaOrgConstants.PROPERTY_DATE_PUBLISHED, bean.getTimespans(), true);

            // mentions
            addMultilingualPropertiesWithReferences(object, proxy.getDctermsReferences(), SchemaOrgConstants.PROPERTY_MENTIONS, null);

            // spatialCoverage
            Map<String, List<String>> dcCoverage = copyMap(proxy.getDcCoverage());
            Map<String, List<String>> places = filterPlaces(dcCoverage);
            addMultilingualPropertiesWithReferences(object, places, SchemaOrgConstants.PROPERTY_SPATIAL_COVERAGE, null);
            addMultilingualPropertiesWithReferences(object, proxy.getDctermsSpatial(), SchemaOrgConstants.PROPERTY_SPATIAL_COVERAGE, null);

            // temporalCoverage
            Map<String, List<String>> dates = filterDates(dcCoverage);
            addDateProperty(object, dates, SchemaOrgConstants.PROPERTY_TEMPORAL_COVERAGE, bean.getTimespans(), false);
            addDateProperty(object, proxy.getDctermsTemporal(), SchemaOrgConstants.PROPERTY_TEMPORAL_COVERAGE, bean.getTimespans(), false);

            // now dcCoverage should only contain values that should be added to about property
            addMultilingualPropertiesWithReferences(object, dcCoverage, SchemaOrgConstants.PROPERTY_ABOUT, null);

            // isBasedOn
            addReferences(object, proxy.getEdmIsDerivativeOf(), SchemaOrgConstants.PROPERTY_IS_BASED_ON, CreativeWork.class);

            // sameAs - none in ProxyImpl though there should be ore:Proxy/owl:sameAs

            if (object instanceof VisualArtwork) {
                // artform
                addMultilingualProperties(object, proxy.getDcFormat(), SchemaOrgConstants.PROPERTY_ARTFORM);

                // artMedium
                addMultilingualProperties(object, proxy.getDctermsMedium(), SchemaOrgConstants.PROPERTY_ART_MEDIUM);
            }
        }
    }

    /**
     * Make a copy of a map if it's not null
     * @param map map to copy
     * @return copy of a map or empty one if the source map is null
     */
    private static Map<String, List<String>> copyMap(Map<String, List<String>> map) {
        if (map == null) {
            return new HashMap<>();
        }
        return new HashMap<>(map);
    }

    /**
     * Filter the date values from the map. The date values are either strings that are a parsable ISO8601 date or
     * a reference to the timespan. The filtered values are returned and removed from the source map.
     * @param map map to filter
     * @return filtered values
     */
    private static Map<String,List<String>> filterDates(Map<String,List<String>> map) {
        Map<String, List<String>> dates = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            for (String value : entry.getValue()) {
                if (isDateOrTimespan(value)) {
                    List<String> datesList = dates.computeIfAbsent(entry.getKey(), k -> new ArrayList<>());
                    datesList.add(value);
                }
            }
            if (dates.get(entry.getKey()) != null) {
                entry.getValue().removeAll(dates.get(entry.getKey()));
            }
        }
        return dates;
    }

    /**
     * Returns true if the string is a ISO8601 date or a reference to timespan
     * @param value value to check
     * @return true when the string is a ISO8601 date or a reference to timespan
     */
    private static boolean isDateOrTimespan(String value) {
        return (EdmUtils.isUri(value) && value.startsWith(TIMESPAN_PREFIX))
                || DateUtils.parse(value) != null;
    }

    /**
     * Searches for places in the provided map and returns the map containing them. They are also removed from the
     * provided map
     *
     * @param map a map containing list of values for each language (key)
     * @return map of places
     */
    private static Map<String,List<String>> filterPlaces(Map<String,List<String>> map) {
        Map<String, List<String>> places = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            for (String value : entry.getValue()) {
                if (isPlace(value)) {
                    List<String> placesList = places.computeIfAbsent(entry.getKey(), k -> new ArrayList<>());
                    placesList.add(value);
                }
            }
            if (places.get(entry.getKey()) != null) {
                entry.getValue().removeAll(places.get(entry.getKey()));
            }
        }
        return places;
    }

    /**
     * Returns true when a value is URI and it starts with place prefix http://data.europeana.eu/place
     * @param value value to check
     * @return true when value is uri starting with http://data.europeana.eu/place
     */
    private static boolean isPlace(String value) {
        return EdmUtils.isUri(value) && value.startsWith(PLACE_PREFIX);
    }

    /**
     * For all entries in the map adds a ISO8601 date range if a corresponding timespan can be found or the unchanged value
     * otherwise.
     *
     * @param object object for which the property will be added
     * @param map map of values (key is language)
     * @param propertyName name of property
     * @param timespans list of timespans
     * @param allowInvalid when true all values are added, otherwise only valid dates are added
     */
    private static void addDateProperty(CreativeWork object,
                                        Map<String,List<String>> map,
                                        String propertyName,
                                        List<TimespanImpl> timespans,
                                        boolean allowInvalid) {
        if (map == null) {
            return;
        }
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            for (String value : entry.getValue()) {
                processDateValue(object, propertyName, timespans, allowInvalid, entry.getKey(), value);
            }
        }
    }

    /**
     * Do the actual processing of the value to be added as a property
     * @param object object for which the property will be added
     * @param propertyName name of property
     * @param timespans list of timespans
     * @param allowInvalid when true all values are added, otherwise only valid dates are added
     * @param language language of the value
     * @param value value to process
     */
    private static void processDateValue(CreativeWork object,
                                         String propertyName,
                                         List<TimespanImpl> timespans,
                                         boolean allowInvalid,
                                         String language,
                                         String value) {
        if (notNullNorEmpty(value)) {
            String valueToAdd = value;
            if (EdmUtils.isUri(value) && timespans != null) {
                // value might be timespan
                valueToAdd = createDateRange(value, language, timespans);

            }
            if (allowInvalid || DateUtils.parse(value) != null) {
                addMultilingualProperty(object,
                        valueToAdd,
                        SchemaOrgConstants.DEFAULT_LANGUAGE.equals(language) ? "" : language,
                        propertyName);
            }
        }
    }

    /**
     * Looks for timespan containing value and creates the ISO8601 date range from its begin/end dates. If no timespan
     * found the unchanged value is returned.
     *
     * @param value value to create data range
     * @param language language of the specified value
     * @param timespans the list of timespans
     * @return ISO8601 date range if a proper timespan was found, the input value otherwise
     */
    private static String createDateRange(String value, String language, List<TimespanImpl> timespans) {
        for (TimespanImpl timespan : timespans) {
            if (timespan.getAbout().equals(value) && timespan.getBegin() != null && timespan.getEnd() != null
                    && timespan.getBegin().get(language) != null && timespan.getEnd().get(language) != null) {
                LocalDate beginDate = LocalDate.parse(timespan.getBegin().get(language).get(0), formatter);
                LocalDate endDate = LocalDate.parse(timespan.getEnd().get(language).get(0), formatter);
                return beginDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "/" + endDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            }
        }
        return value;
    }

    /**
     * Adds references for all values in the array. References may have a specific type if <code>referenceClass</code>
     * is specified.
     *
     * @param object object for which the properties will be added
     * @param values values to be added as references
     * @param propertyName name of property
     * @param referenceClass class of reference
     */
    private static void addReferences(CreativeWork object, String[] values, String propertyName, Class<? extends Thing> referenceClass) {
        if (values == null) {
            return;
        }
        for (String value : values) {
            addReference(object, value, propertyName, referenceClass);
        }
    }

    private static void addMultilingualProperty(CreativeWork object, String value, String language, String propertyName) {
        MultilingualString property = new MultilingualString();
        property.setLanguage(language);
        property.setValue(value);
        object.addProperty(propertyName, property);
    }

    /**
     * Checks whether value is Uri adding a reference in this case or adding the multilingual string otherwise.
     *
     * @param object object for which the property will be added
     * @param propertyName name of property
     * @param language language string, may be empty
     * @param value value to add
     */
    private static void addProperty(CreativeWork object, String value, String language, String propertyName, Class<? extends Thing> referenceClass) {
        if (notNullNorEmpty(value)) {
            if (EdmUtils.isUri(value)) {
                addReference(object, value, propertyName, referenceClass);
            } else {
                addMultilingualProperty(object,
                        value,
                        SchemaOrgConstants.DEFAULT_LANGUAGE.equals(language) ? "" : language,
                        propertyName);
            }
        }
    }

    /**
     * Creates a general (not typed) reference and adds it to the specified object as a property value of the given
     * property name.
     *
     * @param object object for which the reference will be added
     * @param id id of the reference
     * @param propertyName name of property
     * @param referenceClass class of reference that should be used for Reference object
     */
    private static void addReference(CreativeWork object, String id, String propertyName, Class<? extends Thing> referenceClass) {
        Reference reference = new Reference(referenceClass);
        reference.setId(id);
        object.addProperty(propertyName, reference);
    }

    /**
     * Adds multilingual string properties for all values lists in the given map.
     * @param object object for which the property values will be added
     * @param map map of language to list of values to be added
     * @param propertyName name of property
     */
    private static void addMultilingualProperties(CreativeWork object, Map<String, List<String>> map, String propertyName) {
        if (map == null) {
            return;
        }
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            addMultilingualProperties(object, entry.getValue(), SchemaOrgConstants.DEFAULT_LANGUAGE.equals(entry.getKey()) ? "" : entry.getKey(), propertyName);
        }
    }


    /**
     * Adds multilingual string properties to property named with <code>propertyName</code> for all values present in
     * the values list. The given language will be used for each value.
     * @param object object for which the property values will be added
     * @param values values to be added
     * @param language language used for each value
     * @param propertyName name of property
     */
    private static void addMultilingualProperties(CreativeWork object, List<String> values, String language, String propertyName) {
        if (values == null) {
            return;
        }
        for (String value : values) {
            if (notNullNorEmpty(value)) {
                addMultilingualProperty(object,
                        value,
                        language,
                        propertyName);
            }
        }
    }

    /**
     * Checks whether the given parameter is neither null nor empty
     * @param value value to check
     * @return true when the given value is neither null nor empty, false otherwise
     */
    private static boolean notNullNorEmpty(String value) {
        return value != null && !value.isEmpty();
    }
}
