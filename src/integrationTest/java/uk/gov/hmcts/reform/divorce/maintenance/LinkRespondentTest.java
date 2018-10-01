package uk.gov.hmcts.reform.divorce.maintenance;

import io.restassured.response.Response;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.http.entity.ContentType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.model.PinResponse;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.support.RetrieveAosCaseSupport;
import uk.gov.hmcts.reform.divorce.util.RestUtil;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class LinkRespondentTest extends RetrieveAosCaseSupport {
    private static final String PIN_USER_FIRST_NAME = "pinuserfirstname";
    private static final String PIN_USER_LAST_NAME = "pinuserfirstname";

    @Value("${case.orchestration.maintenance.link-respondent.context-path}")
    private String contextPath;

    @Test
    public void givenUserTokenIsNull_whenLinkRespondent_thenReturnBadRequest() {
        Response cosResponse = linkRespondent(null, 1L, "somepin");

        assertEquals(HttpStatus.BAD_REQUEST.value(), cosResponse.getStatusCode());
    }

    @Test
    public void givenInvalidPin_whenLinkRespondent_thenReturnUnAuthorised() {
        final UserDetails petitionerUserDetails = createCitizenUser();

        Response cosResponse = linkRespondent(petitionerUserDetails.getAuthToken(), 1L, "somepin");

        assertEquals(HttpStatus.UNAUTHORIZED.value(), cosResponse.getStatusCode());
    }

    @Test
    public void givenCaseIdDoesNotPresent_whenLinkRespondent_thenReturnBadRequest() {
        final UserDetails petitionerUserDetails = createCitizenUser();

        final PinResponse pinResponse =
            idamTestSupportUtil.generatePin(PIN_USER_FIRST_NAME, PIN_USER_LAST_NAME,
                petitionerUserDetails.getAuthToken());

        Response cosResponse = linkRespondent(petitionerUserDetails.getAuthToken(), 1L, pinResponse.getPin());

        assertEquals(HttpStatus.BAD_REQUEST.value(), cosResponse.getStatusCode());
    }

    @Test
    public void givenLinkFails_whenLinkRespondent_thenReturnNotFound() {
        final UserDetails petitionerUserDetails = createCitizenUser();

        final PinResponse pinResponse =
            idamTestSupportUtil.generatePin(PIN_USER_FIRST_NAME, PIN_USER_LAST_NAME,
                petitionerUserDetails.getAuthToken());

        final CaseDetails caseDetails = submitCase(
            "submit-complete-case.json",
            createCaseWorkerUser()
        );

        Response cosResponse =
            linkRespondent(petitionerUserDetails.getAuthToken(), caseDetails.getId(), pinResponse.getPin());

        assertEquals(HttpStatus.NOT_FOUND.value(), cosResponse.getStatusCode());
    }

    @Test
    public void givenValidCaseDetails_whenLinkRespondent_thenCaseShouldBeLinked() {
        final UserDetails petitionerUserDetails = createCitizenUser();

        final PinResponse pinResponse =
            idamTestSupportUtil.generatePin(PIN_USER_FIRST_NAME, PIN_USER_LAST_NAME,
                petitionerUserDetails.getAuthToken());

        final CaseDetails caseDetails = submitCase(
            "submit-complete-case.json",
            createCaseWorkerUser(),
            ImmutablePair.of("AosLetterHolderId", pinResponse.getUserId())
        );

        updateCase(String.valueOf(caseDetails.getId()), null, "testAosAwaiting");

        final UserDetails respondentUserDetails = createCitizenUser();

        Response linkResponse =
            linkRespondent(
                respondentUserDetails.getAuthToken(),
                caseDetails.getId(),
                pinResponse.getPin()
            );

        assertEquals(HttpStatus.OK.value(), linkResponse.getStatusCode());

        Response caseResponse = retrieveAosCase(respondentUserDetails.getAuthToken());

        assertEquals(String.valueOf(caseDetails.getId()), caseResponse.path(CASE_ID_KEY));
    }

    private Response linkRespondent(String userToken, Long caseId, String pin) {
        final Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());

        if (userToken != null) {
            headers.put(HttpHeaders.AUTHORIZATION, userToken);
        }

        return RestUtil.postToRestService(
            serverUrl + contextPath + "/" + caseId + "/" + pin,
            headers,
            null
        );
    }
}