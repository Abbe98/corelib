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

package eu.europeana.corelib.definitions.solr.model;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.Map.Entry;

import eu.europeana.corelib.definitions.solr.SolrFacetType;
import eu.europeana.corelib.definitions.solr.TechnicalFacetType;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import eu.europeana.corelib.utils.EuropeanaStringUtils;
import eu.europeana.corelib.utils.StringArrayUtils;
import eu.europeana.corelib.utils.model.LanguageVersion;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
public class Query implements Cloneable {

    private String currentCursorMark;

    private final static String OR = " OR ";

    /**
     * Default start parameter for Solr
     */
    private static final int DEFAULT_START = 0;

    /**
     * Default number of items in the SERP
     */
    private static final int DEFAULT_PAGE_SIZE = 12;

    /**
     * Use these instead of the ones provided in the apache Solr package
     * in order to avoid introducing a dependency to that package in all modules
     * they're public because they are read from SearchServiceImpl
     */
    public static final int ORDER_DESC = 0;
    public static final int ORDER_ASC  = 1;


    private boolean produceFacetUnion        = true;
    private boolean allowSpellcheck          = true;
    private boolean allowFacets              = true;
    private boolean apiQuery                 = false;
    private boolean defaultFacetsRequested   = false;


    private int start;
    private int pageSize;
    private int sortOrder = ORDER_DESC;

    private String query;
    private String queryType;
    private String executedQuery;
    private String sort;

    private QueryTranslation queryTranslation;

    private Map<String, String> valueReplacementMap;
    private Map<String, String> parameterMap = new HashMap<>();

    private        String[]     refinementArray;
    private static List<String> defaultSolrFacetList;
    private static List<String> defaultTechnicalFacetList;
    static {
        defaultSolrFacetList = new ArrayList<>();
        for (SolrFacetType solrFacet : SolrFacetType.values()) {
            defaultSolrFacetList.add(solrFacet.toString());
        }
        defaultTechnicalFacetList = new ArrayList<>();
        for (TechnicalFacetType technicalFacet : TechnicalFacetType.values()) {
            defaultTechnicalFacetList.add(technicalFacet.toString());
        }
    }
    private List<String>     solrFacetList = new ArrayList<>(defaultSolrFacetList);
    private List<String>     requestedTechnicalFacetsList;
    private List<String>     allSolrFacetsList;
    private List<String>     searchRefinementList;
    private List<String>     facetRefinementList;
    private List<String>     filteredFacetList;
    private List<QueryFacet> queryFacetList;

    /**
     * CONSTRUCTORS
     */

    public Query(String query) {
        this.query = query;
        start = DEFAULT_START;
        pageSize = DEFAULT_PAGE_SIZE;
        createAllFacetList();
    }

    /**
     * GETTERS & SETTERS
     */

    public String getQuery() {
        return query;
    }

    public String getQuery(boolean withTranslations) {
        if (withTranslations && queryTranslation != null && StringUtils.isNotBlank(queryTranslation.getModifiedQuery())) {
            return queryTranslation.getModifiedQuery();
        }
        return query;
    }

    public Query setQuery(String query) {
        this.query = query;
        return this;
    }

    public String[] getRefinements() {
        return getRefinements(false);
    }

    public String[] getRefinements(boolean useDividedRefinements) {
        if (!useDividedRefinements) {
            return refinementArray;
        } else {
            divideRefinements();
            return (String[]) ArrayUtils.addAll(searchRefinementList.toArray(new String[searchRefinementList.size()]), facetRefinementList.toArray(new String[facetRefinementList.size()]));
        }
    }

    public List<String> getFilteredFacets() {
        return filteredFacetList;
    }

    public Query setRefinements(String... refinementArray) {
        if (refinementArray != null) {
            this.refinementArray = refinementArray.clone();
        } else {
            this.refinementArray = StringArrayUtils.EMPTY_ARRAY;
        }
        return this;
    }

    public String getCurrentCursorMark() {
        return this.currentCursorMark;
    }

    public Query setCurrentCursorMark(String currentCursorMark) {
        this.currentCursorMark = currentCursorMark;
        return this;
    }

    public Query addRefinement(String refinement) {
        if (this.refinementArray == null) {
            this.refinementArray = StringArrayUtils.EMPTY_ARRAY;
        }
        this.refinementArray = (String[]) ArrayUtils.add(this.refinementArray, refinement);
        return this;
    }

    public Query setValueReplacements(Map<String, String> valueReplacementMap) {
        this.valueReplacementMap = valueReplacementMap;
        return this;
    }

    public Query setQueryTranslation(QueryTranslation queryTranslation) {
        this.queryTranslation = queryTranslation;
        return this;
    }

    public QueryTranslation getQueryTranslation() {
        return queryTranslation;
    }

    public Query addFacetQuery(QueryFacet queryFacet) {
        if (queryFacetList == null) {
            queryFacetList = new ArrayList<>();
        }
        queryFacetList.add(queryFacet);
        return this;
    }

    public Query setQueryFacets(List<QueryFacet> queryFacets) {
        this.queryFacetList = queryFacets;
        return this;
    }

    public List<String> getQueryFacets() {
        List<String> queries = new ArrayList<>();
        if (queryFacetList != null) {
            for (QueryFacet queryFacet : queryFacetList) {
                queries.add(queryFacet.getQueryFacetString());
            }
        }
        return queries;
    }

    public Integer getStart() {
        return start;
    }

    public Query setStart(int start) {
        this.start = start;
        return this;
    }

    public String getSort() {
        return sort;
    }

    public Query setSort(String sort) {
        String[] parts;
        if (sort == null || sort.isEmpty()) {
            this.sort = "";
        } else if (!sort.matches(".*\\s.*") && sort.length() > 0) {
            this.sort = sort;
        } else if (sort.matches(".+\\s(asc|ASC|desc|DESC)(ending|ENDING)?")) {
            parts = sort.split("\\s");
            this.sort = parts[0];
            if (parts[1].matches("(asc|ASC).*")) {
                this.sortOrder = ORDER_ASC;
            }
        } else {
            this.sort = "";
        }
        return this;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public int getPageSize() {
        return pageSize;
    }

    public Query setPageSize(int pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public List<String> getSolrFacets() {
        return solrFacetList;
    }

    public Query setSolrFacets(String... solrFacets) {
        return setSolrFacets(Arrays.asList(solrFacets));
    }

    public Query setSolrFacets(List<String> solrFacetList) {
        if (solrFacetList != null) {
            replaceSpecialSolrFacets(solrFacetList);
        } else {
            this.solrFacetList = defaultSolrFacetList;
        }
        return this;
    }

    public Query setRequestedTechnicalFacets(String... requestedTechnicalFacets) {
        return setRequestedTechnicalFacets(Arrays.asList(requestedTechnicalFacets));
    }

    public Query setRequestedTechnicalFacets(List<String> requestedTechnicalFacetsList) {
        this.requestedTechnicalFacetsList = requestedTechnicalFacetsList;
        return this;
    }

    public List<String> getRequestedTechnicalFacets() {
        return requestedTechnicalFacetsList;
    }

    public boolean isDefaultFacetsRequested() {
        return this.defaultFacetsRequested;
    }

    public Query setDefaultFacetsRequested(boolean defaultFacetsRequested) {
        this.defaultFacetsRequested = defaultFacetsRequested;
        return this;
    }

    /**
     * Replace special (solr)facets.
     * <p>
     * Right now there are two special Solr facets: DEFAULT and REUSABILITY. DEFAULT
     * is replaced to the portal's default facet list. REUSABILITY will be skipped,
     * because it is a special query facet
     */
    private void replaceSpecialSolrFacets(List<String> solrFacetList) {
        Set<String> replacedFacetSet = new HashSet<>();
        for (String solrFacet : solrFacetList) {
            if (defaultFacetsRequested) {
                replacedFacetSet.addAll(defaultSolrFacetList);
            } else if (StringUtils.equalsIgnoreCase("MEDIA", solrFacet)) {
                replacedFacetSet.add("has_media");
            } else if (StringUtils.equalsIgnoreCase("THUMBNAIL", solrFacet)) {
                replacedFacetSet.add("has_thumbnails");
            } else if (StringUtils.equalsIgnoreCase("TEXT_FULLTEXT", solrFacet)) {
                replacedFacetSet.add("is_fulltext");
            } else if (StringUtils.equalsIgnoreCase("REUSABILITY", solrFacet)) {
                continue;
            } else {
                replacedFacetSet.add(solrFacet);
            }
        }
        this.solrFacetList = new ArrayList<>(replacedFacetSet);
    }

    private void generateFacetTagQuery(int... tags) {
        if (null == tags || 0 == tags.length) {
            return;
        }

        for (final int tag : tags) {
            addFacetQuery(new QueryFacet("facet_tags:" + tag, "facet_tags"));
        }
    }

    public boolean isApiQuery() {
        return apiQuery;
    }

    public Query setApiQuery(boolean apiQuery) {
        this.apiQuery = apiQuery;
        return this;
    }

    public Map<String, String> getParameterMap() {
        return parameterMap;
    }

    public boolean hasParameter(String key) {
        return parameterMap.containsKey(key);
    }

    /**
     * Adds Solr parameterMap to the Query object
     *
     * @param key   The parameter name
     * @param value The value of the parameter
     * @return The Query object
     */
    public Query setParameter(String key, String value) {
        parameterMap.put(key, value);
        return this;
    }

    @Override
    public Query clone() throws CloneNotSupportedException {
        return (Query) super.clone();
    }

    @Override
    public String toString() {
        List<String> params = new ArrayList<>();
        params.add("q=" + query);
        params.add("start=" + start);
        params.add("rows=" + pageSize);

        if (sort != null && sort.length() > 0) {
            params.add("sort=" + sort + " " + (sortOrder == ORDER_DESC ? "desc" : "asc"));
        }

        if (refinementArray != null) {
            for (String refinement : refinementArray) {
                params.add("qf=" + refinement);
            }
        }

        if (solrFacetList != null) {
            for (String facet : solrFacetList) {
                params.add("facet.field=" + facet);
            }
        }

        if (parameterMap != null) {
            for (Entry<String, String> parameter : parameterMap.entrySet()) {
                params.add(parameter.getKey() + "=" + parameter.getValue());
            }
        }

        if (getQueryFacets() != null) {
            for (String query : getQueryFacets()) {
                params.add("facet.query=" + query);
            }
        }

        return StringUtils.join(params, "&");
    }

    public String getQueryType() {
        return queryType;
    }

    public void setQueryType(String queryType) {
        this.queryType = queryType;
    }

    public boolean isProduceFacetUnion() {
        return produceFacetUnion;
    }

    public boolean isAllowSpellcheck() {
        return allowSpellcheck;
    }

    public Query setAllowSpellcheck(boolean allowSpellcheck) {
        this.allowSpellcheck = allowSpellcheck;
        return this;
    }

    public boolean isAllowFacets() {
        return allowFacets;
    }

    public Query setAllowFacets(boolean allowFacets) {
        this.allowFacets = allowFacets;
        // check if there are technical facets requested. If so, make sure FACET_TAGS is on the list
        // of Solr facets, because only then will the encoded technical metadata be added to the Solr output
        if (allowFacets && null != requestedTechnicalFacetsList && requestedTechnicalFacetsList.size() > 0 &&
                !solrFacetList.contains(SolrFacetType.FACET_TAGS)) solrFacetList.add(SolrFacetType.FACET_TAGS.toString());
        return this;
    }

    public Query setProduceFacetUnion(boolean produceFacetUnion) {
        this.produceFacetUnion = produceFacetUnion;
        return this;
    }
    //refactor to make it clear these are Solr facets only
    private void createAllFacetList() {
        allSolrFacetsList = new ArrayList<>();
        allSolrFacetsList.addAll(solrFacetList);
    }

    public void removeFacet(SolrFacetType facetToRemove) {
        removeFacet(facetToRemove.toString());
    }

    public void removeFacet(String facetToRemove) {
        if (solrFacetList.contains(facetToRemove)) {
            solrFacetList.remove(facetToRemove);
        }
    }

    public void setFacet(String facet) {
        solrFacetList = new ArrayList<>();
        solrFacetList.add(facet);
    }

    public String getExecutedQuery() {
        return executedQuery;
    }

    public void setExecutedQuery(String executedQuery) {
        try {
            this.executedQuery = URLDecoder.decode(executedQuery, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            this.executedQuery = executedQuery;
            e.printStackTrace();
        }
    }

    public void divideRefinements() {
        searchRefinementList = new ArrayList<>();
        facetRefinementList = new ArrayList<>();

        if (refinementArray == null) {
            return;
        }

        Map<String, FacetCollector> register = new LinkedHashMap<>();
        for (String facetTerm : refinementArray) {
            if (facetTerm.contains(":")) {
                boolean replaced        = false;
                String  pseudoFacetName = null;
                if (valueReplacementMap != null && valueReplacementMap.containsKey(facetTerm)) {
                    pseudoFacetName = facetTerm.substring(0, facetTerm.indexOf(":"));
                    facetTerm = valueReplacementMap.get(facetTerm);
                    replaced = true;
                    if (StringUtils.isBlank(facetTerm)) {
                        continue;
                    }
                }

                int     colon     = facetTerm.indexOf(":");
                String  facetName = facetTerm.substring(0, colon);
                boolean isTagged  = false;
                if (facetName.contains("!tag")) {
                    facetName = facetName.replaceFirst("\\{!tag=.*?\\}", "");
                    isTagged = true;
                }

                if (allSolrFacetsList.contains(facetName)) {
                    String         key = pseudoFacetName == null ? facetName : pseudoFacetName;
                    FacetCollector collector;
                    if (register.containsKey(key)) {
                        collector = register.get(key);
                    } else {
                        collector = new FacetCollector(facetName, isApiQuery());
                        if (pseudoFacetName != null) {
                            collector.setTagName(pseudoFacetName);
                        }
                        register.put(key, collector);
                    }
                    if (isTagged && !collector.isTagged()) {
                        collector.setTagged(true);
                    }
                    collector.addValue(facetTerm.substring(colon + 1), replaced);
                } else {
                    searchRefinementList.add(facetTerm);
                }
            } else {
                searchRefinementList.add(facetTerm);
            }
        }

        filteredFacetList = new ArrayList<String>(register.keySet());
        for (FacetCollector collector : register.values()) {
            facetRefinementList.add(collector.toString());
        }
    }

    public static String concatenateQueryTranslations(List<LanguageVersion> languageVersions) {
        List<String> queryTranslationTerms = new ArrayList<>();
        for (LanguageVersion term : languageVersions) {
            String phrase = EuropeanaStringUtils.createPhraseValue(term.getText());
            if (!queryTranslationTerms.contains(phrase)) {
                queryTranslationTerms.add(phrase);
            }
        }
        return StringUtils.join(queryTranslationTerms, " OR ");
    }

    private class FacetCollector {
        private boolean isTagged = true;
        private String name;
        private String tagName;
        private List<String> values         = new ArrayList<>();
        private List<String> replacedValues = new ArrayList<>();
        private boolean      isApiQuery     = false;
        private boolean      replaced       = false;

        public FacetCollector(String name) {
            this.name = name;
            this.tagName = name;
        }

        public FacetCollector(String name, boolean isApiQuery) {
            this(name);
            this.isApiQuery = isApiQuery;
        }

        public boolean isTagged() {
            return isTagged;
        }

        public void setTagged(boolean isTagged) {
            this.isTagged = isTagged;
        }

        public void setTagName(String tagName) {
            this.tagName = tagName;
        }

        private boolean isAlreadyQuoted(String value) {
            return value.startsWith("\"") && value.endsWith("\"");
        }

        private boolean hasOr(String value) {
            return value.startsWith("(") && value.endsWith(")") && value.contains(" OR ");
        }

        public void addValue(String value, boolean isReplaced) {
            if (name.equals(SolrFacetType.RIGHTS.name())) {
                if (value.endsWith("*")) {
                    value = value.replace(":", "\\:").replace("/", "\\/");
                } else if (!isAlreadyQuoted(value) && !hasOr(value)) {
                    value = '"' + value + '"';
                }
            } else if (name.equals(SolrFacetType.TYPE.name())) {
                value = value.toUpperCase().replace("\"", "");
            } else {
                if (!isApiQuery && (value.contains(" ") || value.contains("!"))) {
                    if (!value.startsWith("\"")) {
                        value = '"' + value;
                    }
                    if (!value.endsWith("\"")) {
                        value += '"';
                    }
                }
            }
            if (isReplaced) {
                replacedValues.add(value);
            } else {
                values.add(value);
            }
        }

        private String join(List<String> valueList, String booleanOperator) {
            if (valueList.size() == 0) {
                return null;
            }
            StringBuilder sb = new StringBuilder();
            if (valueList.size() > 1) {
                sb.append("(");
                sb.append(StringUtils.join(valueList, booleanOperator));
                sb.append(")");
            } else {
                sb.append(valueList.get(0));
            }

            return sb.toString();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (isTagged && !replaced) {
                sb.append("{!tag=").append(tagName).append("}");
            }
            sb.append(name);
            sb.append(":");

            String valuesString         = join(values, OR);
            String replacedValuesString = join(replacedValues, OR);

            if (StringUtils.isNotBlank(valuesString)) {
                if (StringUtils.isNotBlank(replacedValuesString)) {
                    sb.append(String.format("(%s AND %s)", valuesString, replacedValuesString));
                } else {
                    sb.append(valuesString);
                }
            } else {
                if (StringUtils.isNotBlank(replacedValuesString)) {
                    sb.append(replacedValuesString);
                }
            }

            return sb.toString();
        }
    }
}
