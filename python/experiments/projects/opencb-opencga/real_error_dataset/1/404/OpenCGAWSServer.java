/*
 * Copyright 2015-2020 OpenCB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opencb.opencga.server.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.base.Splitter;
import io.swagger.annotations.ApiParam;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.opencb.biodata.models.variant.Genotype;
import org.opencb.biodata.models.variant.stats.VariantStats;
import org.opencb.commons.datastore.core.*;
import org.opencb.commons.utils.ListUtils;
import org.opencb.opencga.analysis.variant.manager.VariantStorageManager;
import org.opencb.opencga.catalog.db.api.StudyDBAdaptor;
import org.opencb.opencga.catalog.exceptions.CatalogAuthenticationException;
import org.opencb.opencga.catalog.exceptions.CatalogAuthorizationException;
import org.opencb.opencga.catalog.exceptions.CatalogException;
import org.opencb.opencga.catalog.exceptions.CatalogParameterException;
import org.opencb.opencga.catalog.managers.AbstractManager;
import org.opencb.opencga.catalog.managers.CatalogManager;
import org.opencb.opencga.catalog.utils.Constants;
import org.opencb.opencga.catalog.utils.ParamUtils;
import org.opencb.opencga.core.api.ParamConstants;
import org.opencb.opencga.core.config.Configuration;
import org.opencb.opencga.core.exceptions.VersionException;
import org.opencb.opencga.core.models.common.Enums;
import org.opencb.opencga.core.models.study.Study;
import org.opencb.opencga.core.response.OpenCGAResult;
import org.opencb.opencga.core.response.RestResponse;
import org.opencb.opencga.core.tools.ToolParams;
import org.opencb.opencga.server.WebServiceException;
import org.opencb.opencga.storage.core.StorageEngineFactory;
import org.opencb.opencga.storage.core.config.StorageConfiguration;
import org.opencb.opencga.storage.core.variant.io.json.mixin.GenericRecordAvroJsonMixin;
import org.opencb.opencga.storage.core.variant.io.json.mixin.GenotypeJsonMixin;
import org.opencb.opencga.storage.core.variant.io.json.mixin.VariantStatsJsonMixin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.QueryParam;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static org.opencb.opencga.core.common.JacksonUtils.getExternalOpencgaObjectMapper;

@ApplicationPath("/")
@Produces(MediaType.APPLICATION_JSON)
public class OpenCGAWSServer {

    @DefaultValue("v2")
    @PathParam("apiVersion")
    @ApiParam(name = "apiVersion", value = "OpenCGA major version", allowableValues = "v2", defaultValue = "v2")
    protected String apiVersion;
    protected String exclude;
    protected String include;
    protected int limit;
    protected long skip;
    protected boolean count;
    protected boolean lazy;
    protected String token;

    @DefaultValue("")
    @QueryParam("sid")
    @ApiParam(value = "Session id", hidden = true)
    protected String dummySessionId;

    @HeaderParam("Authorization")
    @DefaultValue("Bearer ")
    @ApiParam("JWT Authentication token")
    protected String authentication;

    protected UriInfo uriInfo;
    protected HttpServletRequest httpServletRequest;
    protected ObjectMap params;
    private String requestDescription;

    protected String sessionIp;

    protected long startTime;

    protected Query query;
    protected QueryOptions queryOptions;

    private static ObjectWriter jsonObjectWriter;
    private static ObjectMapper jsonObjectMapper;

    protected static Logger logger; // = LoggerFactory.getLogger(this.getClass());

    protected static AtomicBoolean initialized;

    protected static java.nio.file.Path opencgaHome;

    protected static Configuration configuration;
    protected static CatalogManager catalogManager;

    protected static StorageConfiguration storageConfiguration;
    protected static StorageEngineFactory storageEngineFactory;
    protected static VariantStorageManager variantManager;

    private static final int DEFAULT_LIMIT = AbstractManager.DEFAULT_LIMIT;
    private static final int MAX_LIMIT = 5000;
    private static final int MAX_ID_SIZE = 100;

    private static String errorMessage;

    static {
        initialized = new AtomicBoolean(false);

        jsonObjectMapper = getExternalOpencgaObjectMapper();
        jsonObjectMapper.addMixIn(GenericRecord.class, GenericRecordAvroJsonMixin.class);
        jsonObjectMapper.addMixIn(VariantStats.class, VariantStatsJsonMixin.class);
        jsonObjectMapper.addMixIn(Genotype.class, GenotypeJsonMixin.class);

        jsonObjectWriter = jsonObjectMapper.writer();

        //Disable MongoDB useless logging
        org.apache.log4j.Logger.getLogger("org.mongodb.driver.cluster").setLevel(Level.WARN);
        org.apache.log4j.Logger.getLogger("org.mongodb.driver.connection").setLevel(Level.WARN);
    }


    public OpenCGAWSServer(@Context UriInfo uriInfo, @Context HttpServletRequest httpServletRequest, @Context HttpHeaders httpHeaders)
            throws IOException, VersionException {
        this(uriInfo.getPathParameters().getFirst("apiVersion"), uriInfo, httpServletRequest, httpHeaders);
    }

    public OpenCGAWSServer(@PathParam("apiVersion") String version, @Context UriInfo uriInfo,
                           @Context HttpServletRequest httpServletRequest, @Context HttpHeaders httpHeaders)
            throws VersionException {
        this.apiVersion = version;
        this.uriInfo = uriInfo;
        this.httpServletRequest = httpServletRequest;
        httpServletRequest.setAttribute(OpenCGAWSServer.class.getName(), this);

        this.params = new ObjectMap();
        for (String key : uriInfo.getQueryParameters().keySet()) {
            this.params.put(key, uriInfo.getQueryParameters().getFirst(key));
        }
        for (String key : uriInfo.getPathParameters().keySet()) {
            if (!"apiVersion".equals(key)) {
                this.params.put(key, uriInfo.getPathParameters().getFirst(key));
            }
        }

        // This is only executed the first time to initialize configuration and some variables
        if (initialized.compareAndSet(false, true)) {
            init();
        }

        if (StringUtils.isNotEmpty(errorMessage)) {
            throw new IllegalStateException(errorMessage);
        }
//        if (catalogManager == null) {
//            throw new IllegalStateException("OpenCGA was not properly initialized. Please, check if the configuration files are reachable "
//                    + "or properly defined.");
//        }

        try {
            verifyHeaders(httpHeaders);
        } catch (CatalogAuthenticationException e) {
            throw new IllegalStateException(e);
        }

        query = new Query();
        queryOptions = new QueryOptions();

        parseParams();
        // take the time for calculating the whole duration of the call
        startTime = System.currentTimeMillis();
    }

    private void init() {
        ServletContext context = httpServletRequest.getServletContext();
        init(context.getInitParameter("OPENCGA_HOME"));
    }

    static void init(String opencgaHomeStr) {
        initialized.set(true);

        logger = LoggerFactory.getLogger("org.opencb.opencga.server.rest.OpenCGAWSServer");
        logger.info("========================================================================");
        logger.info("| Starting OpenCGA REST server, initializing OpenCGAWSServer");
        logger.info("| This message must appear only once.");

        // We must load the configuration files and init catalogManager, storageManagerFactory and Logger only the first time.
        // We first read 'config-dir' parameter passed

//        String configDirString = context.getInitParameter("config-dir");
//        if (StringUtils.isEmpty(configDirString)) {
        // If not environment variable then we check web.xml parameter

        // Preference for the env var OPENCGA_HOME
        String opencgaHomeEnv = System.getenv("OPENCGA_HOME");
        if (StringUtils.isNotEmpty(opencgaHomeEnv)) {
            opencgaHomeStr = opencgaHomeEnv;
        }
        if (StringUtils.isEmpty(opencgaHomeEnv) && StringUtils.isEmpty(opencgaHomeStr)) {
            logger.error("No valid OpenCGA home directory provided!");
            throw new IllegalStateException("No valid OpenCGA home directory provided!");
        }
        OpenCGAWSServer.opencgaHome = Paths.get(opencgaHomeStr);

//        if (StringUtils.isNotEmpty(context.getInitParameter("OPENCGA_HOME"))) {
//            configDirString = context.getInitParameter("OPENCGA_HOME") + "/conf";
//        } else if (StringUtils.isNotEmpty(opencgaHomeStr)) {
//            // If not exists then we try the environment variable OPENCGA_HOME
//            configDirString = opencgaHomeStr + "/conf";
//        } else {
//            logger.error("No valid configuration directory provided!");
//        }
//        }


        // Check and execute the init methods
        java.nio.file.Path configDirPath = OpenCGAWSServer.opencgaHome.resolve("conf");
        if (Files.exists(configDirPath) && Files.isDirectory(configDirPath)) {
            logger.info("|  * Configuration folder: '{}'", configDirPath.toString());
            initOpenCGAObjects(configDirPath);

            // TODO use configuration.yml for getting the server.log, for now is hardcoded
            logger.info("|  * Server logfile: " + OpenCGAWSServer.opencgaHome.resolve("logs").resolve("server.log"));
            initLogger(OpenCGAWSServer.opencgaHome.resolve("logs"));
        } else {
            errorMessage = "No valid configuration directory provided: '" + configDirPath.toString() + "'";
            logger.error(errorMessage);
            throw new IllegalStateException(errorMessage);
        }

        logger.info("========================================================================\n");
    }

    /**
     * This method loads OpenCGA configuration files and initialize CatalogManager and StorageManagerFactory.
     * This must be only executed once.
     *
     * @param configDir directory containing the configuration files
     */
    private static void initOpenCGAObjects(java.nio.file.Path configDir) {
        try {
            logger.info("|  * Catalog configuration file: '{}'", configDir.toFile().getAbsolutePath() + "/configuration.yml");
            configuration = Configuration
                    .load(new FileInputStream(new File(configDir.toFile().getAbsolutePath() + "/configuration.yml")));
            catalogManager = new CatalogManager(configuration);

            logger.info("|  * Storage configuration file: '{}'", configDir.toFile().getAbsolutePath() + "/storage-configuration.yml");
            storageConfiguration = StorageConfiguration
                    .load(new FileInputStream(new File(configDir.toFile().getAbsolutePath() + "/storage-configuration.yml")));
            storageEngineFactory = StorageEngineFactory.get(storageConfiguration);
            variantManager = new VariantStorageManager(catalogManager, storageEngineFactory);
        } catch (IOException | CatalogException e) {
            errorMessage = e.getMessage();
            e.printStackTrace();
            logger.error("Error while creating CatalogManager", e);
        }
    }

    private static void initLogger(java.nio.file.Path logs) {
        try {
            org.apache.log4j.Logger rootLogger = LogManager.getRootLogger();
            PatternLayout layout = new PatternLayout("%d{yyyy-MM-dd HH:mm:ss} [%t] %-5p %c{1}:%L - %m%n");
            String logFile = logs.resolve("server.log").toString();
            RollingFileAppender rollingFileAppender = new RollingFileAppender(layout, logFile, true);
            rollingFileAppender.setThreshold(Level.DEBUG);
            rollingFileAppender.setMaxFileSize("20MB");
            rollingFileAppender.setMaxBackupIndex(10);
            rootLogger.setLevel(Level.TRACE);
            rootLogger.addAppender(rollingFileAppender);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void shutdown() {
        logger.info("========================================================================");
        logger.info("| Stopping OpenCGA REST server");
        try {
            if (OpenCGAWSServer.variantManager != null) {
                logger.info("| * Closing VariantStorageManager");
                OpenCGAWSServer.variantManager.close();
            }
        } catch (Exception e) {
            logger.error("Error closing VariantManager", e);
        }
        try {
            if (OpenCGAWSServer.catalogManager != null) {
                logger.info("| * Closing CatalogManager");
                OpenCGAWSServer.catalogManager.close();
            }
        } catch (Exception e) {
            logger.error("Error closing CatalogManager", e);
        }
        logger.info("| OpenCGA destroyed");
        logger.info("========================================================================\n");
    }

    private void parseParams() throws VersionException {
        // If by any reason 'apiVersion' is null we try to read it from the URI path, if not present an Exception is thrown
        if (apiVersion == null) {
            if (uriInfo.getPathParameters().containsKey("apiVersion")) {
                logger.warn("Setting 'apiVersion' from UriInfo object");
                this.apiVersion = uriInfo.getPathParameters().getFirst("apiVersion");
            } else {
                throw new VersionException("Version not valid: '" + apiVersion + "'");
            }
        }

        // Check apiVersion parameter, must be: v1, v2, ... If 'latest' then is converted to appropriate apiVersion.
        if (apiVersion.equalsIgnoreCase("latest")) {
            logger.info("Version 'latest' detected, setting 'apiVersion' parameter to 'v1'");
            apiVersion = "v1";
        }

        MultivaluedMap<String, String> multivaluedMap = uriInfo.getQueryParameters();
        queryOptions.put("metadata", multivaluedMap.get("metadata") == null || multivaluedMap.get("metadata").get(0).equals("true"));

        // By default, we will avoid counting the number of documents unless explicitly specified.
        queryOptions.put(QueryOptions.SKIP_COUNT, true);

        // Add all the others QueryParams from the URL
        for (Map.Entry<String, List<String>> entry : multivaluedMap.entrySet()) {
            String value = entry.getValue().get(0);
            switch (entry.getKey()) {
                case QueryOptions.INCLUDE:
                case QueryOptions.EXCLUDE:
                    queryOptions.put(entry.getKey(), new LinkedList<>(Splitter.on(",").splitToList(value)));
                    break;
                case QueryOptions.LIMIT:
                    limit = Integer.parseInt(value);
                    break;
                case QueryOptions.TIMEOUT:
                    queryOptions.put(entry.getKey(), Integer.parseInt(value));
                    break;
                case QueryOptions.SKIP:
                    int skip = Integer.parseInt(value);
                    queryOptions.put(entry.getKey(), (skip >= 0) ? skip : -1);
                    break;
                case QueryOptions.SORT:
                case QueryOptions.ORDER:
                    queryOptions.put(entry.getKey(), value);
                    break;
                case QueryOptions.SKIP_COUNT:
                    queryOptions.put(QueryOptions.SKIP_COUNT, Boolean.parseBoolean(value));
                    break;
                case Constants.INCREMENT_VERSION:
                    queryOptions.put(Constants.INCREMENT_VERSION, Boolean.parseBoolean(value));
                    break;
                case Constants.REFRESH:
                    queryOptions.put(Constants.REFRESH, Boolean.parseBoolean(value));
                    break;
                case QueryOptions.COUNT:
                    count = Boolean.parseBoolean(value);
                    queryOptions.put(entry.getKey(), count);
                    break;
                case Constants.SILENT:
                    queryOptions.put(entry.getKey(), Boolean.parseBoolean(value));
                    break;
                case Constants.FORCE:
                    queryOptions.put(entry.getKey(), Boolean.parseBoolean(value));
                    break;
                case Constants.FLATTENED_ANNOTATIONS:
                    queryOptions.put(Constants.FLATTENED_ANNOTATIONS, Boolean.parseBoolean(value));
                    break;
                case ParamConstants.OTHER_STUDIES_FLAG:
                    queryOptions.put(ParamConstants.OTHER_STUDIES_FLAG, Boolean.parseBoolean(value));
                    break;
                case "includeIndividual": // SampleWS
                    lazy = !Boolean.parseBoolean(value);
                    queryOptions.put("lazy", lazy);
                    break;
                case "lazy":
                    lazy = Boolean.parseBoolean(value);
                    queryOptions.put(entry.getKey(), lazy);
                    break;
                case QueryOptions.FACET:
                    queryOptions.put(entry.getKey(), value);
                    break;
                default:
                    // Query
                    query.put(entry.getKey(), value);
                    break;
            }
        }

        queryOptions.put(QueryOptions.LIMIT, (limit > 0) ? Math.min(limit, MAX_LIMIT) : (count ? 0 : DEFAULT_LIMIT));
        query.remove("sid");

//      Exceptions
        if (query.containsKey("status")) {
            query.put("status.name", query.get("status"));
            query.remove("status");
        }

        // Remove deprecated fields
        query.remove("variableSet");
        query.remove("annotationsetName");

        try {
            requestDescription = httpServletRequest.getMethod() + ": " + uriInfo.getAbsolutePath().toString()
                    + ", " + jsonObjectWriter.writeValueAsString(query)
                    + ", " + jsonObjectWriter.writeValueAsString(queryOptions);
            logger.info(requestDescription);
        } catch (JsonProcessingException e) {
            requestDescription = httpServletRequest.getMethod() + ": " + uriInfo.getRequestUri();
            logger.info(requestDescription);
            logger.error("Error writing as Json", e);
        }
    }

    private void parseIncludeExclude(MultivaluedMap<String, String> multivaluedMap, String key, String value) {
        if (value != null && !value.isEmpty()) {
            queryOptions.put(key, new LinkedList<>(Splitter.on(",").splitToList(value)));
        } else {
            queryOptions.put(key, (multivaluedMap.get(key) != null)
                    ? Splitter.on(",").splitToList(multivaluedMap.get(key).get(0))
                    : null);
        }
    }


    protected void addParamIfNotNull(Map<String, Object> params, String key, Object value) {
        if (key != null && value != null) {
            params.put(key, value.toString());
        }
    }

    protected void addParamIfTrue(Map<String, Object> params, String key, boolean value) {
        if (key != null && value) {
            params.put(key, Boolean.toString(value));
        }
    }

    protected Response createErrorResponse(Throwable e) {
        // First we print the exception in Server logs
        logger.error("Catch error: " + e.getMessage(), e);

        // Now we prepare the response to client
        RestResponse<ObjectMap> queryResponse = new RestResponse<>();
        queryResponse.setTime(new Long(System.currentTimeMillis() - startTime).intValue());
        queryResponse.setApiVersion(apiVersion);
        queryResponse.setParams(params);
        addErrorEvent(queryResponse, e);

        OpenCGAResult<ObjectMap> result = OpenCGAResult.empty();
        queryResponse.setResponses(Arrays.asList(result));

        Response.Status errorStatus = Response.Status.INTERNAL_SERVER_ERROR;
        if (e instanceof CatalogAuthorizationException) {
            errorStatus = Response.Status.FORBIDDEN;
        } else if (e instanceof CatalogAuthenticationException) {
            errorStatus = Response.Status.UNAUTHORIZED;
        }

        Response response = Response.fromResponse(createJsonResponse(queryResponse)).status(errorStatus).build();
        logResponse(response.getStatusInfo(), queryResponse);
        return response;
    }

    protected Response createErrorResponse(String errorMessage, OpenCGAResult result) {
        RestResponse<ObjectMap> dataResponse = new RestResponse<>();
        dataResponse.setApiVersion(apiVersion);
        dataResponse.setParams(params);
        addErrorEvent(dataResponse, errorMessage);
        dataResponse.setResponses(Arrays.asList(result));

        Response response = Response.fromResponse(createJsonResponse(dataResponse)).status(Response.Status.INTERNAL_SERVER_ERROR).build();
        logResponse(response.getStatusInfo(), dataResponse);
        return response;
    }

    protected Response createErrorResponse(String method, String errorMessage) {
        try {
            Response response = buildResponse(Response.ok(jsonObjectWriter.writeValueAsString(new ObjectMap("error", errorMessage)),
                    MediaType.APPLICATION_JSON_TYPE));
            logResponse(response.getStatusInfo());
            return response;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return buildResponse(Response.ok("{\"error\":\"Error parsing json error\"}", MediaType.APPLICATION_JSON_TYPE));
    }

    private <T> void addErrorEvent(RestResponse<T> response, String message) {
        if (response.getEvents() == null) {
            response.setEvents(new ArrayList<>());
        }
        response.getEvents().add(new Event(Event.Type.ERROR, message));
    }

    private <T> void addErrorEvent(RestResponse<T> response, Throwable e) {
        if (response.getEvents() == null) {
            response.setEvents(new ArrayList<>());
        }
        response.getEvents().add(
                new Event(Event.Type.ERROR, 0, e.getClass().getName(), e.getClass().getSimpleName(), e.getMessage()));
    }

    // TODO: Change signature
    //    protected <T> Response createOkResponse(OpenCGAResult<T> result)
    //    protected <T> Response createOkResponse(List<OpenCGAResult<T>> results)
    protected Response createOkResponse(Object obj) {
        RestResponse queryResponse = new RestResponse();
        queryResponse.setTime(new Long(System.currentTimeMillis() - startTime).intValue());
        queryResponse.setApiVersion(apiVersion);
        queryResponse.setParams(params);

        // Guarantee that the RestResponse object contains a list of results
        List<OpenCGAResult<?>> list = new ArrayList<>();
        if (obj instanceof List) {
            if (!((List) obj).isEmpty()) {
                Object firstObject = ((List) obj).get(0);
                if (firstObject instanceof OpenCGAResult) {
                    list = (List) obj;
                } else if (firstObject instanceof DataResult) {
                    List<DataResult> results = (List) obj;
                    // We will cast each of the DataResults to OpenCGAResult
                    for (DataResult result : results) {
                        list.add(new OpenCGAResult<>(result));
                    }
                } else {
                    list = Collections.singletonList(new OpenCGAResult<>(0, Collections.emptyList(), 1, (List) obj, 1));
                }
            }
        } else {
            if (obj instanceof OpenCGAResult) {
                list.add(((OpenCGAResult) obj));
            } else if (obj instanceof DataResult) {
                list.add(new OpenCGAResult<>((DataResult) obj));
            } else {
                list.add(new OpenCGAResult<>(0, Collections.emptyList(), 1, Collections.singletonList(obj), 1));
            }
        }
        queryResponse.setResponses(list);

        Response response = createJsonResponse(queryResponse);
        logResponse(response.getStatusInfo(), queryResponse);
        return response;
    }

    protected Response createRawOkResponse(Object obj) {
        try {
            String res = jsonObjectWriter.writeValueAsString(obj);
//            System.out.println("\n\n\n" + res + "\n\n");
            Response response = buildResponse(Response.ok(res, MediaType.APPLICATION_JSON_TYPE));
            logResponse(response.getStatusInfo());
            return response;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            logger.error("Error parsing response object");
            return createErrorResponse("", "Error parsing response object:\n" + Arrays.toString(e.getStackTrace()));
        }
    }

    //Response methods
    protected Response createOkResponse(Object o1, MediaType o2) {
        return buildResponse(Response.ok(o1, o2));
    }

    protected Response createOkResponse(Object o1, MediaType o2, String fileName) {
        return buildResponse(Response.ok(o1, o2).header("content-disposition", "attachment; filename =" + fileName));
    }

    private void logResponse(Response.StatusType statusInfo) {
        logResponse(statusInfo, null);
    }

    private void logResponse(Response.StatusType statusInfo, RestResponse<?> queryResponse) {
        StringBuilder sb = new StringBuilder();
        try {
            if (statusInfo.getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
                sb.append("OK");
            } else {
                sb.append("ERROR");
            }
            sb.append(" [").append(statusInfo.getStatusCode()).append(']');

            if (queryResponse == null) {
                sb.append(", ").append(System.currentTimeMillis() - startTime).append("ms");
            } else {
                sb.append(", ").append(queryResponse.getTime()).append("ms");
                if (queryResponse.getResponses().size() == 1) {
                    OpenCGAResult<?> result = queryResponse.getResponses().get(0);
                    if (result != null) {
                        sb.append(", num: ").append(result.getNumResults());
                        if (result.getNumTotalResults() >= 0) {
                            sb.append(", total: ").append(result.getNumTotalResults());
                        }
                    }
                }
            }
            sb.append(", ").append(requestDescription);
            logger.info(sb.toString());
        } catch (RuntimeException e) {
            logger.warn("Error logging response", e);
            logger.info(sb.toString()); // Print incomplete response
        }
    }

    protected Response createJsonResponse(RestResponse queryResponse) {
        try {
            return buildResponse(Response.ok(jsonObjectWriter.writeValueAsString(queryResponse), MediaType.APPLICATION_JSON_TYPE));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            logger.error("Error parsing queryResponse object");
            return createErrorResponse("", "Error parsing RestResponse object:\n" + Arrays.toString(e.getStackTrace()));
        }
    }

    protected Response buildResponse(Response.ResponseBuilder responseBuilder) {
        return responseBuilder
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Headers", "x-requested-with, content-type, authorization")
                .header("Access-Control-Allow-Credentials", "true")
                .header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
                .build();
    }

    private void verifyHeaders(HttpHeaders httpHeaders) throws CatalogAuthenticationException {
        List<String> authorization = httpHeaders.getRequestHeader("Authorization");
        if (authorization != null && authorization.get(0).length() > 7) {
            String token = authorization.get(0);
            if (!token.startsWith("Bearer ")) {
                throw new CatalogAuthenticationException("Authorization header must start with Bearer JWToken");
            }
            this.token = token.substring("Bearer".length()).trim();
        }

        if (StringUtils.isEmpty(this.token)) {
            this.token = this.params.getString("sid");
        }
    }

    protected List<String> getIdListOrEmpty(String id) throws WebServiceException {
        return id == null ? Collections.emptyList() : getIdList(id, true);
    }

    protected List<String> getIdList(String id) throws WebServiceException {
        return getIdList(id, true);
    }

    protected List<String> getIdList(String id, boolean checkMaxNumberElements) throws WebServiceException {
        if (StringUtils.isNotEmpty(id)) {
            List<String> ids = checkUniqueList(id);
            if (checkMaxNumberElements && ids.size() > MAX_ID_SIZE) {
                throw new WebServiceException("More than " + MAX_ID_SIZE + " IDs are provided");
            }
            return ids;
        } else {
            throw new WebServiceException("ID is null or Empty");
        }
    }

    protected static List<String> checkUniqueList(String ids) throws WebServiceException {
        if (StringUtils.isNotEmpty(ids)) {
            List<String> idsList = Arrays.asList(ids.split(","));
            return checkUniqueList(idsList, "");
        } else {
            throw new WebServiceException("ID is null or Empty");
        }
    }

    protected static List<String> checkUniqueList(List<String> ids, String field) throws WebServiceException {
        if (ListUtils.isNotEmpty(ids)) {
            Set<String> hashSet = new HashSet<>(ids);
            if (hashSet.size() == ids.size()) {
                return ids;
            } else {
                throw new WebServiceException("Provided " + field + " IDs are not unique. Only unique IDs are accepted.");
            }
        }
        return null;
    }

    protected void areSingleIds(String... ids) throws CatalogParameterException {
        for (String id : ids) {
            ParamUtils.checkIsSingleID(id);
        }
    }

    public Response run(Callable<DataResult<?>> c) {
        try {
            return createOkResponse(c.call());
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    public Response submitJob(String toolId, String project, String study, Map<String, Object> paramsMap,
                               String jobName, String jobDescription, String jobDependsOne, String jobTags) {
        return run(() -> submitJobRaw(toolId, project, study, paramsMap, jobName, jobDescription, jobDependsOne, jobTags));
    }

    public Response submitJob(String toolId, String study, ToolParams bodyParams, String jobId, String jobDescription,
                              String jobDependsOnStr, String jobTagsStr) {
        return submitJob(toolId, null, study, bodyParams, jobId, jobDescription, jobDependsOnStr, jobTagsStr);
    }

    public Response submitJob(String toolId, String project, String study, ToolParams bodyParams, String jobId, String jobDescription,
                              String jobDependsOnStr, String jobTagsStr) {
        return run(() -> {
            Map<String, Object> paramsMap = bodyParams.toParams();
            if (StringUtils.isNotEmpty(study)) {
                paramsMap.putIfAbsent(ParamConstants.STUDY_PARAM, study);
            }
            return submitJobRaw(toolId, project, study, paramsMap, jobId, jobDescription, jobDependsOnStr, jobTagsStr);
        });
    }

    private DataResult<?> submitJobRaw(String toolId, String project, String study, Map<String, Object> paramsMap,
                                       String jobId, String jobDescription, String jobDependsOnStr, String jobTagsStr)
            throws CatalogException {

        if (StringUtils.isNotEmpty(project) && StringUtils.isEmpty(study)) {
            // Project job
            QueryOptions options = new QueryOptions(QueryOptions.INCLUDE, StudyDBAdaptor.QueryParams.FQN.key());
            // Peek any study. The ExecutionDaemon will take care of filling up the rest of studies.
            List<String> studies = catalogManager.getStudyManager()
                    .get(project, new Query(), options, token)
                    .getResults()
                    .stream()
                    .map(Study::getFqn)
                    .collect(Collectors.toList());
            if (studies.isEmpty()) {
                throw new CatalogException("Project '" + project + "' not found!");
            }
            study = studies.get(0);
        }

        List<String> jobTags;
        if (StringUtils.isNotEmpty(jobTagsStr)) {
            jobTags = Arrays.asList(jobTagsStr.split(","));
        } else {
            jobTags = Collections.emptyList();
        }
        List<String> jobDependsOn;
        if (StringUtils.isNotEmpty(jobDependsOnStr)) {
            jobDependsOn = Arrays.asList(jobDependsOnStr.split(","));
        } else {
            jobDependsOn = Collections.emptyList();
        }
        return catalogManager.getJobManager()
                .submit(study, toolId, Enums.Priority.MEDIUM, paramsMap, jobId, jobDescription, jobDependsOn, jobTags, token);
    }

    public Response createPendingResponse() {
        return createErrorResponse(new NotImplementedException("Pending " + uriInfo.getPath()));
    }

    public Response createDeprecatedRemovedResponse() {
        return createErrorResponse(new NotImplementedException("Deprecated " + uriInfo.getPath()));
    }

    public Response createDeprecatedMovedResponse(String newEndpoint) {
        return createErrorResponse(new NotImplementedException("Deprecated " + uriInfo.getPath() + " . Use instead " + newEndpoint));
    }

}
