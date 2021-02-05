package datawave.microservice.querymetrics;

import datawave.microservice.querymetrics.handler.ShardTableQueryMetricHandler;
import datawave.webservice.query.metric.QueryMetric;
import datawave.webservice.query.metric.QueryMetricHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping(path = "/v1")
public class QueryMetricOperations {

    @Autowired
    private ShardTableQueryMetricHandler handler;

    @RequestMapping(path = "/update", method = {RequestMethod.POST}, consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
                    produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public String update(@RequestBody Collection<QueryMetric> queryMetrics) {
        System.out.println(queryMetrics.toString());
        queryMetrics.forEach(m -> {
            try {
                handler.updateMetric(m);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return queryMetrics.toString();
    }
}
