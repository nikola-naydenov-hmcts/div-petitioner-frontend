package uk.gov.hmcts.reform.divorce.orchestration.management.monitoring.health;

import com.jayway.jsonpath.JsonPath;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.yml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class HealthCheckITest {

    private static final String HEALTH_UP_RESPONSE = "{ \"status\": \"UP\"}";
    private static final String HEALTH_DOWN_RESPONSE = "{ \"status\": \"DOWN\"}";

    @LocalServerPort
    private int port;

    @Value("${case.formatter.service.api.baseurl}")
    private String caseFormatterServiceHealthUrl;

    @Value("${case.validation.service.api.baseurl}")
    private String caseValidationServiceHealthUrl;

    @Value("${document.generator.service.api.baseurl}")
    private String documentGeneratorServiceHealthUrl;

    @Autowired
    private RestTemplate restTemplate;

    private String healthUrl;
    private MockRestServiceServer mockRestServiceServer;
    private ClientHttpRequestFactory originalRequestFactory;
    private final HttpClient httpClient = HttpClients.createMinimal();

    private HttpResponse getHealth() throws Exception {
        final HttpGet request = new HttpGet(healthUrl);
        request.addHeader("Accept", "application/json;charset=UTF-8");

        return httpClient.execute(request);
    }

    @Before
    public void setUp() {
        healthUrl = "http://localhost:" + String.valueOf(port) + "/health";
        originalRequestFactory = restTemplate.getRequestFactory();
        mockRestServiceServer = MockRestServiceServer.createServer(restTemplate);
    }

    @After
    public void tearDown() {
        restTemplate.setRequestFactory(originalRequestFactory);
    }

    @Test
    public void givenAllDependenciesAreUp_whenCheckHealth_thenReturnStatusUp() throws Exception {
        mockEndpointAndResponse(caseFormatterServiceHealthUrl, true);
        mockEndpointAndResponse(caseValidationServiceHealthUrl, true);
        mockEndpointAndResponse(documentGeneratorServiceHealthUrl, true);

        HttpResponse response = getHealth();
        String body = EntityUtils.toString(response.getEntity());

        assertThat(response.getStatusLine().getStatusCode(), equalTo(200));
        assertThat(JsonPath.read(body, "$.status").toString(), equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.caseFormatterServiceHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.caseValidationServiceHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.documentGeneratorServiceHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.diskSpace.status").toString(), equalTo("UP"));
    }

    @Test
    public void givenCaseFormatterServiceIsDown_whenCheckHealth_thenReturnStatusDown() throws Exception {
        mockEndpointAndResponse(caseFormatterServiceHealthUrl, false);
        mockEndpointAndResponse(caseValidationServiceHealthUrl, true);
        mockEndpointAndResponse(documentGeneratorServiceHealthUrl, true);

        HttpResponse response = getHealth();
        String body = EntityUtils.toString(response.getEntity());

        assertThat(response.getStatusLine().getStatusCode(), equalTo(503));
        assertThat(JsonPath.read(body, "$.status").toString(), equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.details.caseFormatterServiceHealthCheck.status").toString(),
            equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.details.caseValidationServiceHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.documentGeneratorServiceHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.diskSpace.status").toString(), equalTo("UP"));
    }

    @Test
    public void givenCaseValidationServiceIsDown_whenCheckHealth_thenReturnStatusDown() throws Exception {
        mockEndpointAndResponse(caseFormatterServiceHealthUrl, true);
        mockEndpointAndResponse(caseValidationServiceHealthUrl, false);
        mockEndpointAndResponse(documentGeneratorServiceHealthUrl, true);

        HttpResponse response = getHealth();
        String body = EntityUtils.toString(response.getEntity());

        assertThat(response.getStatusLine().getStatusCode(), equalTo(503));
        assertThat(JsonPath.read(body, "$.status").toString(), equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.details.caseFormatterServiceHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.caseValidationServiceHealthCheck.status").toString(),
            equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.details.documentGeneratorServiceHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.diskSpace.status").toString(), equalTo("UP"));
    }

    @Test
    public void givenDocumentGeneratorServiceIsDown_whenCheckHealth_thenReturnStatusDown() throws Exception {
        mockEndpointAndResponse(caseFormatterServiceHealthUrl, true);
        mockEndpointAndResponse(caseValidationServiceHealthUrl, true);
        mockEndpointAndResponse(documentGeneratorServiceHealthUrl, false);

        HttpResponse response = getHealth();
        String body = EntityUtils.toString(response.getEntity());

        assertThat(response.getStatusLine().getStatusCode(), equalTo(503));
        assertThat(JsonPath.read(body, "$.status").toString(), equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.details.caseFormatterServiceHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.caseValidationServiceHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.details.documentGeneratorServiceHealthCheck.status").toString(),
            equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.details.diskSpace.status").toString(), equalTo("UP"));
    }

    @Test
    public void givenAllDependenciesAreDown_whenCheckHealth_thenReturnStatusDown() throws Exception {
        mockEndpointAndResponse(caseFormatterServiceHealthUrl, false);
        mockEndpointAndResponse(caseValidationServiceHealthUrl, false);
        mockEndpointAndResponse(documentGeneratorServiceHealthUrl, false);

        HttpResponse response = getHealth();
        String body = EntityUtils.toString(response.getEntity());

        assertThat(response.getStatusLine().getStatusCode(), equalTo(503));
        assertThat(JsonPath.read(body, "$.status").toString(), equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.details.caseFormatterServiceHealthCheck.status").toString(),
            equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.details.caseValidationServiceHealthCheck.status").toString(),
            equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.details.documentGeneratorServiceHealthCheck.status").toString(),
            equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.details.diskSpace.status").toString(), equalTo("UP"));
    }

    private void mockEndpointAndResponse(String requestUrl, boolean serviceUp) {
        mockRestServiceServer.expect(once(), requestTo(requestUrl + "/health")).andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(serviceUp ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE)
                        .body(serviceUp ? HEALTH_UP_RESPONSE : HEALTH_DOWN_RESPONSE)
                        .contentType(MediaType.APPLICATION_JSON_UTF8));
    }
}