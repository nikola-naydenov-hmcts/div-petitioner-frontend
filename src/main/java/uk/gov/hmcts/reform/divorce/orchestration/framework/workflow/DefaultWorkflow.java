package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow;

import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

public class DefaultWorkflow<T> extends AbstractWorkflow<T> {

    @SuppressWarnings("unchecked")
    @Override
    public T executeInternal(Task[] tasks, T payload) throws WorkflowException {
        try {
            for (Task<T> task: tasks) {
                payload = task.execute(getContext(), payload);
                if (getContext().getStatus()) {
                    break;
                }
            }
        } catch (TaskException e) {
            throw new WorkflowException(e.getMessage(), e);
        }

        return payload;
    }
}