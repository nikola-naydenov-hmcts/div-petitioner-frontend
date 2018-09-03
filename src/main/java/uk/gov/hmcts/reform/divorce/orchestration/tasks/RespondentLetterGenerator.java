package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.DocumentGeneratorClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GenerateDocumentRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ACCESS_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_INVITATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.INVITATION_FILE_NAME_FORMAT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PIN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_INVITATION_TEMPLATE_NAME;

@Component
public class RespondentLetterGenerator implements Task<Map<String, Object>> {
    private final DocumentGeneratorClient documentGeneratorClient;

    @Autowired
    public RespondentLetterGenerator(DocumentGeneratorClient documentGeneratorClient) {
        this.documentGeneratorClient = documentGeneratorClient;
    }

    @Override
    public Map<String, Object> execute(TaskContext context,
                                       Map<String, Object> caseData,
                                       Object... params) throws TaskException {
        CaseDetails caseDetails = (CaseDetails) params[1];
        GeneratedDocumentInfo aosinvitation =
                documentGeneratorClient.generatePDF(
                        GenerateDocumentRequest.builder()
                                .template(RESPONDENT_INVITATION_TEMPLATE_NAME)
                                .values(ImmutableMap.of(
                                        CASE_DETAILS_JSON_KEY, caseDetails,
                                        ACCESS_CODE, caseData.get(PIN)
                                        )
                                )
                                .build(),
                        String.valueOf(params[0])
                );

        aosinvitation.setDocumentType(DOCUMENT_TYPE_INVITATION);
        aosinvitation.setFileName(String.format(INVITATION_FILE_NAME_FORMAT,
                caseDetails.getCaseId()));

        caseData.put(RESPONDENT_INVITATION_TEMPLATE_NAME, aosinvitation);

        return caseData;
    }
}