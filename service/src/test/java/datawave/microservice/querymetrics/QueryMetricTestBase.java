package datawave.microservice.querymetrics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import datawave.microservice.authorization.preauth.ProxiedEntityX509Filter;
import datawave.microservice.authorization.user.ProxiedUserDetails;
import datawave.microservice.querymetrics.handler.ShardTableQueryMetricHandler;
import datawave.security.authorization.DatawaveUser;
import datawave.security.authorization.JWTTokenHandler;
import datawave.security.authorization.SubjectIssuerDNPair;
import datawave.webservice.query.metric.QueryMetric;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static datawave.security.authorization.DatawaveUser.UserType.USER;

public class QueryMetricTestBase {

    protected Logger log = LoggerFactory.getLogger(getClass());

    protected static final SubjectIssuerDNPair ALLOWED_CALLER = SubjectIssuerDNPair
                    .of("cn=test.testcorp.com, ou=microservices, ou=development, o=testcorp, c=us", "cn=testcorp ca, ou=security, o=testcorp, c=us");
    
    protected static final String updateUrl = "/querymetric/v1/update";
    
    @Autowired
    protected RestTemplateBuilder restTemplateBuilder;
    
    @Autowired
    protected JWTTokenHandler jwtTokenHandler;
    
    @Autowired
    protected ObjectMapper objectMapper;
    
    @Autowired
    protected ShardTableQueryMetricHandler shardTableQueryMetricHandler;
    
    @Autowired
    protected CacheManager cacheManager;
    
    protected Cache incomingQueryMetricsCache;
    protected Cache lastWrittenQueryMetricCache;
    
    @LocalServerPort
    protected int webServicePort;
    
    protected RestTemplate restTemplate;
    protected ProxiedUserDetails allowedCaller;
    
    @Before
    public void setup() {
        this.restTemplate = restTemplateBuilder.build(RestTemplate.class);
        DatawaveUser allowedDWUser = new DatawaveUser(ALLOWED_CALLER, USER, null, null, null, null, System.currentTimeMillis());
        this.allowedCaller = new ProxiedUserDetails(Collections.singleton(allowedDWUser), allowedDWUser.getCreationTime());
        this.incomingQueryMetricsCache = cacheManager.getCache("incomingQueryMetrics");
        this.lastWrittenQueryMetricCache = cacheManager.getCache("lastWrittenQueryMetrics");
    }
    
    protected QueryMetric createMetric(String queryId) {
        long now = System.currentTimeMillis();
        QueryMetric m = new QueryMetric();
        Date nowDate = new Date(now);
        Map<String,String> markings = new HashMap<String,String>() {
            {
                put("AUTHS", "A,B,C");
            }
        };
        m.setQueryId(queryId);
        m.setMarkings(markings);
        m.setEndDate(nowDate);
        m.setBeginDate(DateUtils.addDays(nowDate, -1));
        m.setLastUpdated(nowDate);
        m.setQuery("USER:testuser");
        m.setQueryLogic("QueryMetricsQuery");
        m.setHost("localhost");
        return m;
    }
    
    protected HttpEntity createRequestEntity(ProxiedUserDetails trustedUser, ProxiedUserDetails jwtUser, Object body) throws JsonProcessingException {
        
        HttpHeaders headers = new HttpHeaders();
        if (this.jwtTokenHandler != null && jwtUser != null) {
            String token = jwtTokenHandler.createTokenFromUsers(jwtUser.getUsername(), jwtUser.getProxiedUsers());
            headers.add("Authorization", "Bearer " + token);
        }
        if (trustedUser != null) {
            headers.add(ProxiedEntityX509Filter.SUBJECT_DN_HEADER, trustedUser.getPrimaryUser().getDn().subjectDN());
            headers.add(ProxiedEntityX509Filter.ISSUER_DN_HEADER, trustedUser.getPrimaryUser().getDn().issuerDN());
        }
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        
        return new HttpEntity<>(objectMapper.writeValueAsString(body), headers);
    }
}
