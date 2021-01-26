package datawave.query.metrics;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/v1", produces = MediaType.APPLICATION_JSON_VALUE)
public class QueryMetricOperations {
    
    @RequestMapping(path = "/update/{queryId}", method = {RequestMethod.POST})
    public String update(QueryMetric queryMetric) {
        
        return "";
    }
    
}
