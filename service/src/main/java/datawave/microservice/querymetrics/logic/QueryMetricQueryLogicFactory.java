package datawave.microservice.querymetrics.logic;

import datawave.marking.MarkingFunctions;
import datawave.microservice.querymetrics.config.QueryMetricHandlerProperties;
import datawave.query.config.ShardQueryConfiguration;
import datawave.query.planner.DefaultQueryPlanner;
import datawave.query.planner.MetadataHelperQueryModelProvider;
import datawave.query.planner.QueryPlanner;
import datawave.query.util.DateIndexHelperFactory;
import datawave.query.util.MetadataHelperFactory;
import datawave.webservice.common.audit.Auditor;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class QueryMetricQueryLogicFactory implements FactoryBean<QueryMetricQueryLogic> {

    private MarkingFunctions markingFunctions;
    private QueryMetricHandlerProperties queryMetricHandlerProperties;

    @Autowired
    public QueryMetricQueryLogicFactory(MarkingFunctions markingFunctions, QueryMetricHandlerProperties queryMetricHandlerProperties) {
        this.markingFunctions = markingFunctions;
        this.queryMetricHandlerProperties = queryMetricHandlerProperties;
    }

    @Override
    public QueryMetricQueryLogic getObject() throws Exception {

        QueryMetricQueryLogic logic = new QueryMetricQueryLogic();
        logic.setConfig(new ShardQueryConfiguration());
        logic.setMarkingFunctions(markingFunctions);
        logic.setTableName(this.queryMetricHandlerProperties.getShardTableName());
        logic.setIndexTableName(this.queryMetricHandlerProperties.getIndexTableName());
        logic.setDateIndexTableName(this.queryMetricHandlerProperties.getDateIndexTableName());
        logic.setReverseIndexTableName(this.queryMetricHandlerProperties.getReverseIndexTableName());
        logic.setMetadataTableName(this.queryMetricHandlerProperties.getMetadataTableName());
        logic.setAuditType(Auditor.AuditType.NONE);
        logic.setModelName("NONE");
        logic.setMetadataHelperFactory(new MetadataHelperFactory());
        logic.setDateIndexHelperFactory(new DateIndexHelperFactory());

        logic.setConfig();

        DefaultQueryPlanner planner = new DefaultQueryPlanner();
        MetadataHelperQueryModelProvider.Factory mhqmp = new MetadataHelperQueryModelProvider.Factory();
        planner.setQueryModelProviderFactory(mhqmp);
        return logic;
    }

    @Override
    public Class<?> getObjectType() {
        return QueryMetricQueryLogic.class;
    }
}
