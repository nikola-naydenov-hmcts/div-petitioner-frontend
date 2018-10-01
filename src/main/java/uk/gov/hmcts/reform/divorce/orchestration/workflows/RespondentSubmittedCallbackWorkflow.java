package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CreateEvent;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GenericEmailNotification;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_INFERRED_RESPONDENT_GENDER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RELATIONSHIP_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_TEMPLATE_VARS;

@Component
public class RespondentSubmittedCallbackWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private static final String MALE_GENDER = "male";
    private static final String FEMALE_GENDER = "female";
    private static final String MALE_GENDER_IN_RELATION = "husband";
    private static final String FEMALE_GENDER_IN_RELATION = "wife";

    private final GenericEmailNotification emailNotificationTask;

    @Autowired
    public RespondentSubmittedCallbackWorkflow(GenericEmailNotification emailNotificationTask) {
        this.emailNotificationTask = emailNotificationTask;
    }

    public Map<String, Object> run(CreateEvent caseDetailsRequest, String authToken) throws WorkflowException {
        CaseDetails caseDetails = caseDetailsRequest.getCaseDetails();
        String ref = caseDetailsRequest.getCaseDetails().getCaseId();

        String firstName = getFieldAsStringOrNull(caseDetails,D_8_PETITIONER_FIRST_NAME);
        String lastName = getFieldAsStringOrNull(caseDetails,D_8_PETITIONER_LAST_NAME);
        String relationship = getRespondentRelationship(caseDetails);

        Map<String, String> notificationTemplateVars = new HashMap<>();
        notificationTemplateVars.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, firstName);
        notificationTemplateVars.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, lastName);
        notificationTemplateVars.put(NOTIFICATION_RELATIONSHIP_KEY, relationship);
        notificationTemplateVars.put(NOTIFICATION_REFERENCE_KEY, ref);

        String petitionerEmail = getFieldAsStringOrNull(caseDetails, D_8_PETITIONER_EMAIL);

        return this.execute(
            new Task[]{
                emailNotificationTask
            },
            caseDetailsRequest.getCaseDetails().getCaseData(),
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(NOTIFICATION_EMAIL, petitionerEmail),
            ImmutablePair.of(NOTIFICATION_TEMPLATE_VARS, notificationTemplateVars),
            ImmutablePair.of(NOTIFICATION_TEMPLATE, EmailTemplateNames.RESPONDENT_SUBMISSION_CONSENT)
        );
    }

    private String getFieldAsStringOrNull(final CaseDetails caseDetails, String fieldKey) {
        Object fieldValue = caseDetails.getCaseData().get(fieldKey);
        if (fieldValue == null) {
            return null;
        }
        return fieldValue.toString();
    }

    private String getRespondentRelationship(CaseDetails caseDetails) {
        String gender = getFieldAsStringOrNull(caseDetails, D_8_INFERRED_RESPONDENT_GENDER);
        if (gender == null) {
            return null;
        }

        switch (gender.toLowerCase(Locale.ENGLISH)) {
            case MALE_GENDER : return MALE_GENDER_IN_RELATION;
            case FEMALE_GENDER : return  FEMALE_GENDER_IN_RELATION;
            default:
                return null;
        }
    }
}