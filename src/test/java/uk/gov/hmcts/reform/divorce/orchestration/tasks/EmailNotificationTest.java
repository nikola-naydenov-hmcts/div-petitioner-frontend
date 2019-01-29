package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_EMAIL;

@RunWith(MockitoJUnitRunner.class)
public class EmailNotificationTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private EmailNotification target;

    @Test
    public void whenExecuteEmailNotificationTask_thenSendSaveDraftConfirmationEmailIsCalled() {
        Map<String, Object> payload = mock(Map.class);
        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(NOTIFICATION_EMAIL, TEST_USER_EMAIL);

        target.execute(context, payload);

        verify(emailService).sendSaveDraftConfirmationEmail(TEST_USER_EMAIL);
    }

    @Test
    public void givenBlankEmail_whenExecuteEmailNotificationTask_thenSendSaveDraftConfirmationEmailNoyCalled() {
        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        Map<String, Object> payload = mock(Map.class);

        target.execute(context, payload);

        verify(emailService, never()).sendSaveDraftConfirmationEmail(TEST_USER_EMAIL);
    }

}