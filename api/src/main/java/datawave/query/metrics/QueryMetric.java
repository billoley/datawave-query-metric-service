package datawave.query.metrics;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.time.DateUtils;

import com.google.common.collect.Lists;

public class QueryMetric extends BaseQueryMetric implements Serializable, Comparable<QueryMetric> {
    
    @Override
    public int compareTo(QueryMetric that) {
        return this.getCreateDate().compareTo(that.getCreateDate());
    }
    
    private static final long serialVersionUID = 1L;
    
    public QueryMetric() {
        this.createDate = DateUtils.truncate(new Date(), Calendar.SECOND);
        this.host = System.getProperty("jboss.host.name");
    }
    
    public QueryMetric(QueryMetric other) {
        this.queryType = other.queryType;
        this.user = other.user;
        this.userDN = other.userDN;
        if (other.createDate != null) {
            this.createDate = new Date(other.createDate.getTime());
        }
        this.queryId = other.queryId;
        this.setupTime = other.setupTime;
        this.query = other.query;
        this.host = other.host;
        this.createCallTime = other.createCallTime;
        if (other.pageTimes != null) {
            this.pageTimes = new ArrayList<PageMetric>();
            for (PageMetric p : other.pageTimes) {
                this.pageTimes.add(p.duplicate());
            }
        }
        this.numPages = other.numPages;
        this.numResults = other.numResults;
        this.proxyServers = other.proxyServers;
        this.errorMessage = other.errorMessage;
        this.errorCode = other.errorCode;
        this.lifecycle = other.lifecycle;
        this.queryAuthorizations = other.queryAuthorizations;
        if (other.beginDate != null) {
            this.beginDate = new Date(other.beginDate.getTime());
        }
        if (other.endDate != null) {
            this.endDate = new Date(other.endDate.getTime());
        }
        if (other.positiveSelectors != null) {
            this.positiveSelectors = Lists.newArrayList(other.positiveSelectors);
        }
        if (other.negativeSelectors != null) {
            this.negativeSelectors = Lists.newArrayList(other.negativeSelectors);
        }
        if (other.lastUpdated != null) {
            this.lastUpdated = new Date(other.lastUpdated.getTime());
        }
        this.columnVisibility = other.columnVisibility;
        this.queryLogic = other.queryLogic;
        this.lastWrittenHash = other.lastWrittenHash;
        this.numUpdates = other.numUpdates;
        this.queryName = other.queryName;
        this.parameters = other.parameters;
        
        this.sourceCount = other.sourceCount;
        this.nextCount = other.nextCount;
        this.seekCount = other.seekCount;
        this.yieldCount = other.yieldCount;
        this.docRanges = other.docRanges;
        this.fiRanges = other.fiRanges;
        this.plan = other.plan;
        this.loginTime = other.loginTime;
        
        if (other.predictions != null) {
            this.predictions = new HashSet<Prediction>();
            for (Prediction p : other.predictions) {
                this.predictions.add(p.duplicate());
            }
        }
    }
    
    public BaseQueryMetric duplicate() {
        return new QueryMetric(this);
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(this.getCreateDate()).append(this.getQueryId()).append(this.getQueryType())
                        .append(this.getQueryAuthorizations()).append(this.getColumnVisibility()).append(this.getBeginDate()).append(this.getEndDate())
                        .append(this.getSetupTime()).append(this.getUser()).append(this.getUserDN()).append(this.getQuery()).append(this.getQueryLogic())
                        .append(this.getHost()).append(this.getPageTimes()).append(this.getProxyServers()).append(this.getLifecycle())
                        .append(this.getErrorMessage()).append(this.getCreateCallTime()).append(this.getErrorCode()).append(this.getQueryName())
                        .append(this.getParameters()).append(this.getSourceCount()).append(this.getNextCount()).append(this.getSeekCount())
                        .append(this.getYieldCount()).append(this.getDocRanges()).append(this.getFiRanges()).append(this.getPlan()).append(this.getLoginTime())
                        .append(this.getPredictions()).toHashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        if (null == o) {
            return false;
        }
        if (this == o) {
            return true;
        }
        if (o instanceof QueryMetric) {
            QueryMetric other = (QueryMetric) o;
            return new EqualsBuilder().append(this.getQueryId(), other.getQueryId()).append(this.getQueryType(), other.getQueryType())
                            .append(this.getQueryAuthorizations(), other.getQueryAuthorizations())
                            .append(this.getColumnVisibility(), other.getColumnVisibility()).append(this.getBeginDate(), other.getBeginDate())
                            .append(this.getEndDate(), other.getEndDate()).append(this.getCreateDate(), other.getCreateDate())
                            .append(this.getSetupTime(), other.getSetupTime()).append(this.getCreateCallTime(), other.getCreateCallTime())
                            .append(this.getUser(), other.getUser()).append(this.getUserDN(), other.getUserDN()).append(this.getQuery(), other.getQuery())
                            .append(this.getQueryLogic(), other.getQueryLogic()).append(this.getQueryName(), other.getQueryName())
                            .append(this.getParameters(), other.getParameters()).append(this.getHost(), other.getHost())
                            .append(this.getPageTimes(), other.getPageTimes()).append(this.getProxyServers(), other.getProxyServers())
                            .append(this.getLifecycle(), other.getLifecycle()).append(this.getErrorMessage(), other.getErrorMessage())
                            .append(this.getErrorCode(), other.getErrorCode()).append(this.getSourceCount(), other.getSourceCount())
                            .append(this.getNextCount(), other.getNextCount()).append(this.getSeekCount(), other.getSeekCount())
                            .append(this.getYieldCount(), other.getYieldCount()).append(this.getDocRanges(), other.getDocRanges())
                            .append(this.getFiRanges(), other.getFiRanges()).append(this.getPlan(), other.getPlan())
                            .append(this.getLoginTime(), other.getLoginTime()).append(this.getPredictions(), other.getPredictions()).isEquals();
        } else {
            return false;
        }
    }
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("Type: ").append(queryType);
        buf.append(" User: ").append(user);
        buf.append(" UserDN: ").append(userDN);
        buf.append(" Date: ").append(createDate);
        buf.append(" QueryId: ").append(queryId);
        buf.append(" Query: ").append(query);
        buf.append(" Query Plan: ").append(this.getPlan());
        buf.append(" Query Type: ").append(queryType);
        buf.append(" Query Logic: ").append(queryLogic);
        buf.append(" Query Name: ").append(queryName);
        buf.append(" Authorizations: ").append(queryAuthorizations);
        buf.append(" ColumnVisibility: ").append(this.columnVisibility);
        buf.append(" Begin Date: ").append(this.beginDate);
        buf.append(" End Date: ").append(this.endDate);
        buf.append(" Parameters: ").append(this.getParameters());
        buf.append(" Host: ").append(this.getHost());
        buf.append(" SetupTime(ms): ").append(this.getSetupTime());
        buf.append(" CreateCallTime(ms): ").append(this.getCreateCallTime());
        buf.append(" PageTimes(ms): ").append(this.getPageTimes());
        buf.append(" ProxyServers: ").append(this.getProxyServers());
        buf.append(" Lifecycle: ").append(this.getLifecycle());
        buf.append(" ErrorCode: ").append(this.getErrorCode());
        buf.append(" ErrorMessage: ").append(this.getErrorMessage());
        buf.append(" Source Count: ").append(this.getSourceCount());
        buf.append(" NextCount: ").append(this.getNextCount());
        buf.append(" Seek Count: ").append(this.getSeekCount());
        buf.append(" Yield Count: ").append(this.getYieldCount());
        buf.append(" Doc Ranges: ").append(this.getDocRanges());
        buf.append(" FI Ranges: ").append(this.getFiRanges());
        buf.append(" Login Time: ").append(this.getLoginTime());
        buf.append(" Predictions: ").append(this.getPredictions());
        buf.append("\n");
        return buf.toString();
    }
}
