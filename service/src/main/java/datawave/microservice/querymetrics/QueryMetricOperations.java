package datawave.microservice.querymetrics;

import datawave.webservice.query.metric.QueryMetric;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping(path = "/v1")
public class QueryMetricOperations {
    
    @RequestMapping(path = "/update", method = {RequestMethod.POST}, consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
                    produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public String update(@RequestBody Collection<QueryMetric> queryMetric) {
        System.out.println(queryMetric.toString());
        return queryMetric.toString();
    }
}
