// package datawave.microservice.querymetrics;
//
// import static org.junit.Assert.assertEquals;
// import static org.junit.Assert.assertTrue;
//
// import java.util.ArrayList;
// import java.util.Date;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;
//
// import com.fasterxml.jackson.databind.ObjectMapper;
// import datawave.webservice.query.exception.BadRequestQueryException;
// import datawave.webservice.query.exception.DatawaveErrorCode;
// import datawave.webservice.query.metric.BaseQueryMetric.PageMetric;
// import datawave.webservice.query.metric.BaseQueryMetric.Lifecycle;
// import datawave.webservice.query.metric.QueryMetric;
//
// import org.apache.commons.lang.time.DateUtils;
// import org.junit.BeforeClass;
// import org.junit.Test;
//
// public class QueryMetricTest {
//
// private static QueryMetric queryMetric = null;
// private static Map<String,String> markings = null;
// private static List<String> negativeSelectors = null;
// private static ArrayList<PageMetric> pageTimes = null;
// private static List<String> positiveSelectors = null;
// private static List<String> proxyServers = null;
//
// @BeforeClass
// public static void setup() {
// queryMetric = new QueryMetric();
// markings = new HashMap<String,String>();
// markings.put("test", "colvis");
// negativeSelectors = new ArrayList<String>();
// negativeSelectors.add("negativeSelector1");
// positiveSelectors = new ArrayList<String>();
// positiveSelectors.add("positiveSelector1");
// pageTimes = new ArrayList<>();
// PageMetric pageMetric = new PageMetric();
// pageMetric.setCallTime(0);
// pageTimes.add(pageMetric);
// proxyServers = new ArrayList<String>();
// proxyServers.add("proxyServer1");
// }
//
// @Test
// public void testSetError() {
// BadRequestQueryException qe = new BadRequestQueryException(DatawaveErrorCode.FIELDS_NOT_IN_DATA_DICTIONARY, "test");
// Exception e = new Exception(qe);
//
// queryMetric.setError(e);
// assertEquals(queryMetric.getErrorMessage(), "The query contained fields which do not exist in the data dictionary for any specified datatype. test");
// assertEquals(queryMetric.getErrorCode(), "400-16");
//
// queryMetric.setErrorCode("");
// Throwable t = new Throwable("non-datawave error");
// queryMetric.setError(t);
// assertEquals(queryMetric.getErrorMessage(), "non-datawave error");
// assertEquals(queryMetric.getErrorCode(), "");
// }
//
// @Test
// public void testSettersGetters() {
// Date d = new Date();
// queryMetric.setBeginDate(d);
// queryMetric.setColumnVisibility("colvis");
// queryMetric.setCreateCallTime(0);
// queryMetric.setCreateDate(d);
// queryMetric.setEndDate(d);
// queryMetric.setErrorCode("error");
// queryMetric.setErrorMessage("errorMessage");
// queryMetric.setHost("host");
// queryMetric.setLastUpdated(d);
// queryMetric.setLastWrittenHash(0);
// queryMetric.setLifecycle(Lifecycle.INITIALIZED);
// queryMetric.setMarkings(markings);
// queryMetric.setNegativeSelectors(negativeSelectors);
// queryMetric.setNumUpdates(0);
// queryMetric.setPageTimes(pageTimes);
// queryMetric.setPositiveSelectors(positiveSelectors);
// queryMetric.setProxyServers(proxyServers);
// queryMetric.setQuery("query");
// queryMetric.setQueryAuthorizations("auths");
// queryMetric.setQueryId("queryId");
// queryMetric.setQueryLogic("queryLogic");
// queryMetric.setQueryType(this.getClass());
// queryMetric.setQueryType("queryType");
// queryMetric.setSetupTime(0);
// queryMetric.setUser("user");
// queryMetric.setUserDN("userDN");
//
// assertEquals(queryMetric.getBeginDate(), d);
// assertTrue(queryMetric.getColumnVisibility().contains("colvis"));
// assertEquals(queryMetric.getCreateCallTime(), 0);
// assertEquals(queryMetric.getCreateDate(), d);
// assertEquals(queryMetric.getElapsedTime(), 0);
// assertEquals(queryMetric.getEndDate(), d);
// assertEquals(queryMetric.getErrorCode(), "error");
// assertEquals(queryMetric.getErrorMessage(), "errorMessage");
// assertEquals(queryMetric.getHost(), "host");
// assertEquals(queryMetric.getLastUpdated(), d);
// assertEquals(queryMetric.getLastWrittenHash(), 0);
// assertEquals(queryMetric.getLifecycle(), Lifecycle.INITIALIZED);
// assertEquals(queryMetric.getMarkings().get("test"), "colvis");
// assertEquals(queryMetric.getNegativeSelectors().get(0), "negativeSelector1");
// assertEquals(queryMetric.getNumPages(), 0);
// assertEquals(queryMetric.getNumResults(), 0);
// assertEquals(queryMetric.getNumUpdates(), 0);
// assertEquals(queryMetric.getPageTimes().get(0).getCallTime(), 0);
// assertEquals(queryMetric.getPositiveSelectors().get(0), "positiveSelector1");
// assertEquals(queryMetric.getProxyServers().iterator().next(), "proxyServer1");
// assertEquals(queryMetric.getQuery(), "query");
// assertEquals(queryMetric.getQueryAuthorizations(), "auths");
// assertEquals(queryMetric.getQueryId(), "queryId");
// assertEquals(queryMetric.getQueryLogic(), "queryLogic");
// assertEquals(queryMetric.getQueryType(), "queryType");
// assertEquals(queryMetric.getSetupTime(), 0);
// assertEquals(queryMetric.getUser(), "user");
// assertEquals(queryMetric.getUserDN(), "userDN");
//
// }
//
// @Test
// public void jsonSerializationTest() {
// String queryId = "0000-1111-2222-3333";
// long now = System.currentTimeMillis();
// Date nowDate = new Date(now);
// QueryMetric m = new QueryMetric();
// Map<String,String> markings = new HashMap<String,String>() {
// {
// put("AUTHS", "A,B,C");
// }
// };
// m.setQueryId(queryId);
// m.setMarkings(markings);
// m.setEndDate(nowDate);
// m.setBeginDate(DateUtils.addDays(nowDate, -1));
// m.setQuery("USER:testuser");
// m.setQueryLogic("QueryMetricsQuery");
// m.setHost("localhost");
// m.addPageTime(1000, 1000, now - 1000, now);
//
// ObjectMapper objectMapper = new ObjectMapper();
// try {
// System.out.println(objectMapper.writeValueAsString(m));
// } catch (Exception e) {
// e.printStackTrace();
// }
// }
// }
