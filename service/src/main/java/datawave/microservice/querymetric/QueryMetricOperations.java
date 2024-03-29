package datawave.microservice.querymetric;

import com.hazelcast.core.IMap;
import com.hazelcast.spring.cache.HazelcastCacheManager;
import datawave.marking.MarkingFunctions;
import datawave.microservice.authorization.user.ProxiedUserDetails;
import datawave.microservice.querymetric.BaseQueryMetric.Lifecycle;
import datawave.microservice.querymetric.config.QueryMetricHandlerProperties;
import datawave.microservice.querymetric.config.QueryMetricSinkConfiguration.QueryMetricSinkBinding;
import datawave.microservice.querymetric.config.TimelyProperties;
import datawave.microservice.querymetric.factory.BaseQueryMetricListResponseFactory;
import datawave.microservice.querymetric.handler.QueryGeometryHandler;
import datawave.microservice.querymetric.handler.ShardTableQueryMetricHandler;
import datawave.microservice.querymetric.handler.SimpleQueryGeometryHandler;
import datawave.security.util.DnUtils;
import datawave.util.timely.UdpClient;
import datawave.webservice.query.exception.DatawaveErrorCode;
import datawave.webservice.query.exception.QueryException;
import datawave.webservice.query.map.QueryGeometryResponse;
import datawave.webservice.result.VoidResponse;
import io.swagger.annotations.ApiParam;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.accumulo.core.security.ColumnVisibility;
import org.apache.accumulo.core.security.VisibilityEvaluator;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.http.MediaType;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import static datawave.microservice.querymetric.config.HazelcastServerConfiguration.INCOMING_METRICS;
import static datawave.microservice.querymetric.config.QueryMetricSourceConfiguration.QueryMetricSourceBinding.SOURCE_NAME;

@EnableBinding(QueryMetricSinkBinding.class)
@RestController
@RequestMapping(path = "/v1")
public class QueryMetricOperations {
    
    private Logger log = LoggerFactory.getLogger(getClass());
    
    private ShardTableQueryMetricHandler handler;
    private QueryGeometryHandler geometryHandler;
    private Cache incomingQueryMetricsCache;
    private boolean isHazelCast;
    private QueryMetricHandlerProperties queryMetricHandlerProperties;
    private MarkingFunctions markingFunctions;
    private TimelyProperties timelyProperties;
    private BaseQueryMetricListResponseFactory queryMetricListResponseFactory;
    private ReentrantLock caffeineLock = new ReentrantLock();
    
    @Output(SOURCE_NAME)
    @Autowired
    private MessageChannel output;
    
    @Autowired
    public QueryMetricOperations(CacheManager cacheManager, ShardTableQueryMetricHandler handler, QueryGeometryHandler geometryHandler,
                    QueryMetricHandlerProperties queryMetricHandlerProperties, MarkingFunctions markingFunctions,
                    BaseQueryMetricListResponseFactory queryMetricListResponseFactory, TimelyProperties timelyProperties) {
        this.handler = handler;
        this.geometryHandler = geometryHandler;
        this.isHazelCast = cacheManager instanceof HazelcastCacheManager;
        this.incomingQueryMetricsCache = cacheManager.getCache(INCOMING_METRICS);
        this.queryMetricHandlerProperties = queryMetricHandlerProperties;
        this.markingFunctions = markingFunctions;
        this.queryMetricListResponseFactory = queryMetricListResponseFactory;
        this.timelyProperties = timelyProperties;
    }
    
    // Messages that arrive via http/https get placed on the message queue
    // to ensure a quick response and to maintain a single queue of work
    @RolesAllowed({"Administrator", "JBossAdministrator"})
    @RequestMapping(path = "/updateMetrics", method = {RequestMethod.POST}, consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
                    produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public VoidResponse updateMetrics(@RequestBody List<BaseQueryMetric> queryMetrics,
                    @RequestParam(value = "metricType", defaultValue = "DISTRIBUTED") QueryMetricType metricType) {
        VoidResponse response = new VoidResponse();
        for (BaseQueryMetric m : queryMetrics) {
            log.trace("received metric update via REST: " + m.toString());
            output.send(MessageBuilder.withPayload(new QueryMetricUpdate(m, metricType)).build());
        }
        return response;
    }
    
    // Messages that arrive via http/https get placed on the message queue
    // to ensure a quick response and to maintain a single queue of work
    @RolesAllowed({"Administrator", "JBossAdministrator"})
    @RequestMapping(path = "/updateMetric", method = {RequestMethod.POST}, consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
                    produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public VoidResponse updateMetric(@RequestBody BaseQueryMetric queryMetric,
                    @RequestParam(value = "metricType", defaultValue = "DISTRIBUTED") QueryMetricType metricType) {
        log.trace("received metric update via REST: " + queryMetric.toString());
        output.send(MessageBuilder.withPayload(new QueryMetricUpdate(queryMetric, metricType)).build());
        return new VoidResponse();
    }
    
    @StreamListener(QueryMetricSinkBinding.SINK_NAME)
    public void handleEvent(QueryMetricUpdate update) {
        storeMetric(update.getMetric(), update.getMetricType());
    }
    
    public VoidResponse storeMetric(BaseQueryMetric queryMetric, QueryMetricType metricType) {
        
        log.trace("storing metric update: " + queryMetric.toString());
        VoidResponse response = new VoidResponse();
        try {
            Long lastPageNum = null;
            String queryId = queryMetric.getQueryId();
            if (this.isHazelCast) {
                // use a native cache set vs Cache.put to prevent the fetching and return of accumulo value
                IMap<Object,Object> incomingQueryMetricsCacheHz = ((IMap<Object,Object>) incomingQueryMetricsCache.getNativeCache());
                
                incomingQueryMetricsCacheHz.lock(queryId);
                try {
                    BaseQueryMetric updatedMetric = queryMetric;
                    QueryMetricUpdate lastQueryMetricUpdate = (QueryMetricUpdate) incomingQueryMetricsCacheHz.get(queryId);
                    if (lastQueryMetricUpdate != null) {
                        updatedMetric = handler.combineMetrics(queryMetric, lastQueryMetricUpdate.getMetric(), metricType);
                        lastPageNum = getLastPageNumber(lastQueryMetricUpdate.getMetric());
                    }
                    incomingQueryMetricsCacheHz.set(queryId, new QueryMetricUpdate(updatedMetric, metricType));
                    sendMetricsToTimely(updatedMetric, lastPageNum);
                } finally {
                    incomingQueryMetricsCacheHz.unlock(queryId);
                }
            } else {
                caffeineLock.lock();
                try {
                    QueryMetricUpdate lastQueryMetricUpdate = incomingQueryMetricsCache.get(queryId, QueryMetricUpdate.class);
                    BaseQueryMetric updatedMetric = queryMetric;
                    if (lastQueryMetricUpdate != null) {
                        BaseQueryMetric lastQueryMetric = lastQueryMetricUpdate.getMetric();
                        updatedMetric = handler.combineMetrics(queryMetric, lastQueryMetric, metricType);
                        handler.writeMetric(updatedMetric, Collections.singletonList(lastQueryMetric), lastQueryMetric.getLastUpdated(), true);
                        lastPageNum = getLastPageNumber(lastQueryMetric);
                    }
                    handler.writeMetric(updatedMetric, Collections.singletonList(updatedMetric), updatedMetric.getLastUpdated(), false);
                    this.incomingQueryMetricsCache.put(queryId, new QueryMetricUpdate(updatedMetric, metricType));
                    sendMetricsToTimely(updatedMetric, lastPageNum);
                } finally {
                    caffeineLock.unlock();
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            response.addException(new QueryException(DatawaveErrorCode.UNKNOWN_SERVER_ERROR, e));
        }
        return response;
    }
    
    private Long getLastPageNumber(BaseQueryMetric m) {
        Long lastPage = null;
        List<BaseQueryMetric.PageMetric> pageMetrics = m.getPageTimes();
        for (BaseQueryMetric.PageMetric pm : pageMetrics) {
            if (lastPage == null || pm.getPageNumber() > lastPage) {
                lastPage = pm.getPageNumber();
            }
        }
        return lastPage;
    }
    
    /**
     * Returns metrics for the current users queries that are identified by the id
     *
     * @param queryId
     *
     * @return datawave.webservice.result.QueryMetricListResponse
     *
     * @HTTP 200 success
     * @HTTP 500 internal server error
     */
    @PermitAll
    @RequestMapping(path = "/id/{queryId}", method = {RequestMethod.GET},
                    produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_HTML_VALUE})
    public BaseQueryMetricListResponse query(@AuthenticationPrincipal ProxiedUserDetails currentUser,
                    @ApiParam("queryId to return") @PathVariable("queryId") String queryId) {
        
        BaseQueryMetricListResponse response = this.queryMetricListResponseFactory.createDetailedResponse();
        List<BaseQueryMetric> metricList = new ArrayList<>();
        try {
            QueryMetricUpdate metricHolder = incomingQueryMetricsCache.get(queryId, QueryMetricUpdate.class);
            if (metricHolder != null) {
                BaseQueryMetric metric = metricHolder.getMetric();
                String adminRole = queryMetricHandlerProperties.getMetricAdminRole();
                boolean allowAllMetrics = adminRole == null;
                boolean sameUser = false;
                if (currentUser != null) {
                    String metricUser = metric.getUser();
                    String requestingUser = DnUtils.getShortName(currentUser.getPrimaryUser().getName());
                    sameUser = metricUser != null && metricUser.equals(requestingUser);
                    allowAllMetrics = allowAllMetrics || currentUser.getPrimaryUser().getRoles().contains(adminRole);
                }
                // since we are using the incomingQueryMetricsCache, we need to make sure that the
                // requesting user has the necessary Authorizations to view the requested query metric
                Authorizations authorizations = new Authorizations(currentUser.getPrimaryUser().getAuths().toArray(new String[0]));
                VisibilityEvaluator visibilityEvaluator = new VisibilityEvaluator(authorizations);
                ColumnVisibility columnVisibility = this.markingFunctions.translateToColumnVisibility(metric.getMarkings());
                boolean userCanSeeVisibility = visibilityEvaluator.evaluate(columnVisibility);
                if (userCanSeeVisibility && (sameUser || allowAllMetrics)) {
                    metricList.add(metric);
                }
            }
        } catch (Exception e) {
            response.addException(new QueryException(e.getMessage(), 500));
        }
        response.setResult(metricList);
        if (metricList.isEmpty()) {
            response.setHasResults(false);
        } else {
            response.setGeoQuery(metricList.stream().anyMatch(SimpleQueryGeometryHandler::isGeoQuery));
            response.setHasResults(true);
        }
        return response;
    }
    
    /**
     * Returns metrics for the current users queries that are identified by the id
     *
     * @param queryId
     *
     * @return datawave.webservice.result.QueryMetricListResponse
     *
     * @HTTP 200 success
     * @HTTP 500 internal server error
     */
    @PermitAll
    @RequestMapping(path = "/id/{queryId}/map", method = {RequestMethod.GET, RequestMethod.POST},
                    produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_HTML_VALUE})
    public QueryGeometryResponse map(@AuthenticationPrincipal ProxiedUserDetails currentUser, @PathVariable("queryId") String queryId) {
        QueryGeometryResponse queryGeometryResponse = new QueryGeometryResponse();
        BaseQueryMetricListResponse metricResponse = query(currentUser, queryId);
        if (!metricResponse.getExceptions().isEmpty()) {
            metricResponse.getExceptions().forEach(e -> queryGeometryResponse.addException(new QueryException(e.getMessage(), e.getCause(), e.getCode())));
            return queryGeometryResponse;
        } else {
            return geometryHandler.getQueryGeometryResponse(queryId, metricResponse.getResult());
        }
    }
    
    private UdpClient createUdpClient() {
        if (timelyProperties != null && StringUtils.isNotBlank(timelyProperties.getHost())) {
            return new UdpClient(timelyProperties.getHost(), timelyProperties.getPort());
        } else {
            return null;
        }
    }
    
    private void sendMetricsToTimely(BaseQueryMetric queryMetric, Long lastPageNum) {
        
        if (this.timelyProperties.getEnabled()) {
            UdpClient timelyClient = createUdpClient();
            if (queryMetric.getQueryType().equalsIgnoreCase("RunningQuery")) {
                try {
                    String queryId = queryMetric.getQueryId();
                    Lifecycle lifecycle = queryMetric.getLifecycle();
                    Map<String,String> metricValues = handler.getEventFields(queryMetric);
                    long createDate = queryMetric.getCreateDate().getTime();
                    
                    StringBuilder tagSb = new StringBuilder();
                    List<String> configuredMetricTags = timelyProperties.getTags();
                    for (String fieldName : configuredMetricTags) {
                        String fieldValue = metricValues.get(fieldName);
                        if (!StringUtils.isBlank(fieldValue)) {
                            // ensure that there are no spaces in tag values
                            fieldValue = fieldValue.replaceAll(" ", "_");
                            tagSb.append(fieldName).append("=").append(fieldValue).append(" ");
                        }
                    }
                    int tagSbLength = tagSb.length();
                    if (tagSbLength > 0) {
                        if (tagSb.charAt(tagSbLength - 1) == ' ') {
                            tagSb.deleteCharAt(tagSbLength - 1);
                        }
                    }
                    tagSb.append("\n");
                    
                    timelyClient.open();
                    
                    if (lifecycle.equals(Lifecycle.RESULTS) || lifecycle.equals(Lifecycle.NEXTTIMEOUT) || lifecycle.equals(Lifecycle.MAXRESULTS)) {
                        List<BaseQueryMetric.PageMetric> pageTimes = queryMetric.getPageTimes();
                        // there should only be a maximum of one page metric as all but the last are removed by the QueryMetricsBean
                        for (BaseQueryMetric.PageMetric pm : pageTimes) {
                            // prevent duplicate reporting
                            if (lastPageNum == null || pm.getPageNumber() > lastPageNum) {
                                long requestTime = pm.getPageRequested();
                                long callTime = pm.getCallTime();
                                if (callTime == -1) {
                                    callTime = pm.getReturnTime();
                                }
                                if (pm.getPagesize() > 0) {
                                    timelyClient.write("put dw.query.metrics.PAGE_METRIC.calltime " + requestTime + " " + callTime + " " + tagSb);
                                    DecimalFormat df = new DecimalFormat("0.00");
                                    String callTimePerRecord = df.format((double) callTime / pm.getPagesize());
                                    timelyClient.write("put dw.query.metrics.PAGE_METRIC.calltimeperrecord " + requestTime + " " + callTimePerRecord + " "
                                                    + tagSb);
                                }
                            }
                        }
                    }
                    
                    if (lifecycle.equals(Lifecycle.CLOSED) || lifecycle.equals(Lifecycle.CANCELLED)) {
                        // write ELAPSED_TIME
                        timelyClient.write("put dw.query.metrics.ELAPSED_TIME " + createDate + " " + queryMetric.getElapsedTime() + " " + tagSb);
                        
                        // write NUM_RESULTS
                        timelyClient.write("put dw.query.metrics.NUM_RESULTS " + createDate + " " + queryMetric.getNumResults() + " " + tagSb);
                    }
                    
                    if (lifecycle.equals(Lifecycle.INITIALIZED)) {
                        // write CREATE_TIME
                        long createTime = queryMetric.getCreateCallTime();
                        if (createTime == -1) {
                            createTime = queryMetric.getSetupTime();
                        }
                        timelyClient.write("put dw.query.metrics.CREATE_TIME " + createDate + " " + createTime + " " + tagSb);
                        
                        // write a COUNT value of 1 so that we can count total queries
                        timelyClient.write("put dw.query.metrics.COUNT " + createDate + " 1 " + tagSb);
                    }
                    
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }
}
