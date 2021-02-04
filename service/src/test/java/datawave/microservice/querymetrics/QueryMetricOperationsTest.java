package datawave.microservice.querymetrics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import datawave.microservice.authorization.preauth.ProxiedEntityX509Filter;
import datawave.microservice.authorization.user.ProxiedUserDetails;
import datawave.security.authorization.DatawaveUser;
import datawave.security.authorization.JWTTokenHandler;
import datawave.security.authorization.SubjectIssuerDNPair;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import datawave.webservice.query.metric.QueryMetric;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static datawave.security.authorization.DatawaveUser.UserType.USER;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"http", "QueryMetricTest", "QueryMetricOperationsTest"})
public class QueryMetricOperationsTest {
    
    private static final SubjectIssuerDNPair ALLOWED_CALLER = SubjectIssuerDNPair.of("cn=test.testcorp.com, ou=microservices, ou=development, o=testcorp, c=us",
                    "cn=testcorp ca, ou=security, o=testcorp, c=us");
    private static final String updateUrl = "/querymetric/v1/update";
    
    @Autowired
    private RestTemplateBuilder restTemplateBuilder;
    
    @Autowired
    private JWTTokenHandler jwtTokenHandler;

    @Autowired
    private ObjectMapper objectMapper;

    @LocalServerPort
    private int webServicePort;
    
    private RestTemplate restTemplate;
    private ProxiedUserDetails allowedCaller;
    
    @Before
    public void setup() {
        restTemplate = restTemplateBuilder.build(RestTemplate.class);
        DatawaveUser allowedDWUser = new DatawaveUser(ALLOWED_CALLER, USER, null, null, null, null, System.currentTimeMillis());
        allowedCaller = new ProxiedUserDetails(Collections.singleton(allowedDWUser), allowedDWUser.getCreationTime());
    }
    
    @Test
    public void Test1() {
        String queryId = "0000-1111-2222-3333";
        UriComponents uri = UriComponentsBuilder.newInstance().scheme("http").host("localhost").port(webServicePort).path(updateUrl).build();
        long now = System.currentTimeMillis();
        Date nowDate = new Date(now);
        QueryMetric m = new QueryMetric();
        Map<String,String> markings = new HashMap<String,String>() {
            {
                put("AUTHS", "A,B,C");
            }
        };
        m.setQueryId(queryId);
        m.setMarkings(markings);
        m.setEndDate(nowDate);
        m.setBeginDate(DateUtils.addDays(nowDate, -1));
        m.setQuery("USER:testuser");
        m.setQueryLogic("QueryMetricsQuery");
        m.setHost("localhost");
        m.addPageTime(1000, 1000, now - 1000, now);
        
        try {
            List<QueryMetric> metricList = new ArrayList<>();
            metricList.add(m);
            HttpEntity requestEntity = createRequestEntity(null, allowedCaller, metricList);
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(uri.toUri(), requestEntity, String.class);
            System.out.println(responseEntity.getBody());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public HttpEntity createRequestEntity(ProxiedUserDetails trustedUser, ProxiedUserDetails jwtUser, Object body) throws JsonProcessingException {
        
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
