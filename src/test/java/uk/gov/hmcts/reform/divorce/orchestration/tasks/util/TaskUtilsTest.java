package uk.gov.hmcts.reform.divorce.orchestration.tasks.util;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.rules.ExpectedException.none;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

public class TaskUtilsTest {

    @Rule
    public ExpectedException expectedException = none();

    @Test
    public void getMandatoryPropertyValueAsString() throws TaskException {
        Map<String, Object> caseDataPayload = new HashMap<>();
        caseDataPayload.put("testKey", "testValue");

        String value = TaskUtils.getMandatoryPropertyValueAsString(caseDataPayload, "testKey");

        assertThat(value, equalTo("testValue"));
    }

    @Test
    public void shouldThrowExceptionWhenMandatoryFieldIsMissing() throws TaskException {
        expectedException.expect(TaskException.class);
        expectedException.expectMessage("Could not evaluate value of mandatory property \"testKey\"");

        TaskUtils.getMandatoryPropertyValueAsString(new HashMap<>(), "testKey");
    }

    @Test
    public void shouldThrowExceptionWhenMandatoryFieldIsNull() throws TaskException {
        expectedException.expect(TaskException.class);
        expectedException.expectMessage("Could not evaluate value of mandatory property \"testKey\"");

        Map<String, Object> caseDataPayload = new HashMap<>();
        caseDataPayload.put("testKey", null);

        TaskUtils.getMandatoryPropertyValueAsString(caseDataPayload, "testKey");
    }

    @Test
    public void testCaseIdCanBeRetrieved() throws TaskException {
        DefaultTaskContext context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, "123");

        String caseId = TaskUtils.getCaseId(context);

        assertThat(caseId, equalTo("123"));
    }

    @Test
    public void testExceptionIsThrown_WhenCaseIdIsMissing() throws TaskException {
        expectedException.expect(TaskException.class);
        expectedException.expectMessage("Could not evaluate value of mandatory property \"" + CASE_ID_JSON_KEY + "\"");

        DefaultTaskContext context = new DefaultTaskContext();

        TaskUtils.getCaseId(context);
    }

    @Test
    public void testExceptionIsThrown_WhenCaseIdIsEmpty() throws TaskException {
        expectedException.expect(TaskException.class);
        expectedException.expectMessage("Could not evaluate value of mandatory property \"" + CASE_ID_JSON_KEY + "\"");

        DefaultTaskContext context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, "");

        TaskUtils.getCaseId(context);
    }

    @Test
    public void testExceptionIsThrown_WhenCaseIdIsNotString() throws TaskException {
        expectedException.expect(TaskException.class);
        expectedException.expectMessage("Could not evaluate value of mandatory property \"" + CASE_ID_JSON_KEY + "\"");

        DefaultTaskContext context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, 123);

        TaskUtils.getCaseId(context);
    }

}