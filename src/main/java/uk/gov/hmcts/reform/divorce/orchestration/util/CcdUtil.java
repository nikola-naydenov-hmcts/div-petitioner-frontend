package uk.gov.hmcts.reform.divorce.orchestration.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_DATE_FORMAT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PAYMENT_DATE_PATTERN;

@SuppressWarnings("squid:S1118")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CcdUtil {

    public static String getCurrentDate() {
        return LocalDate.now().toString(CCD_DATE_FORMAT);
    }

    public static String getCurrentDatePlusDays(int days) {
        return LocalDate.now().plusDays(days).toString(CCD_DATE_FORMAT);
    }

    public static String mapCCDDateToDivorceDate(String date) {
        return LocalDate.parse(date, DateTimeFormat.forPattern(CCD_DATE_FORMAT)).toString(PAYMENT_DATE_PATTERN);
    }
}