package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class RetrieveDraftTest {

    @Mock
    private CaseMaintenanceClient caseMaintenanceClient;

    @InjectMocks
    private RetrieveDraft target;

    @Test
    public void givenUserTokenWithoutDraft_whenExecuteRetrieveTask_thenReturnEmptyResponse() {
        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        Map<String, Object> payload  = mock(Map.class);

        when(caseMaintenanceClient.retrievePetition(AUTH_TOKEN, false))
                .thenReturn(CaseDetails.builder().build());

        assertNull(target.execute(context, payload));
    }

    @Test
    public void givenUserToken_whenExecuteRetrieveTask_thenReturnUserPetitionFromCMS() {
        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        Map<String, Object> payload  = mock(Map.class);
        Map<String, Object> expectedResponse  = mock(Map.class);
        CaseDetails clientResponse  = CaseDetails.builder()
                .caseData(expectedResponse)
                .build();

        when(caseMaintenanceClient.retrievePetition(AUTH_TOKEN, false)).thenReturn(clientResponse);

        assertEquals(expectedResponse,target.execute(context, payload));
    }
}