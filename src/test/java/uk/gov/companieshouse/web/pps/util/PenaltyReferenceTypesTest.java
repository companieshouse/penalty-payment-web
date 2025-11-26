package uk.gov.companieshouse.web.pps.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.financialpenalty.PenaltyReferenceType;
import uk.gov.companieshouse.web.pps.exception.ServiceException;
import uk.gov.companieshouse.web.pps.service.penaltypayment.PenaltyPaymentService;

import java.util.List;
import java.util.Optional;

import static java.time.ZonedDateTime.parse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PenaltyReferenceTypesTest {

    private PenaltyReferenceTypes penaltyReferenceTypes;

    @Mock
    private PenaltyPaymentService mockPenaltyPaymentService;

    final PenaltyReferenceType lateFilingPenaltyReferenceType = getLateFilingPenaltyReferenceType();
    final PenaltyReferenceType sanctionsPenaltyReferenceType = getSanctionsPenaltyReferenceType();
    final PenaltyReferenceType sanctionsRoePenaltyReferenceType = getSanctionsRoePenaltyReferenceType();

    @BeforeEach
    void setUp() throws ServiceException {
        when(mockPenaltyPaymentService.getPenaltyReferenceTypes())
                .thenReturn(new PenaltyReferenceType[]{sanctionsPenaltyReferenceType, lateFilingPenaltyReferenceType});

        penaltyReferenceTypes = new PenaltyReferenceTypes(mockPenaltyPaymentService);
    }

    @Test
    void getEnabled() throws ServiceException {
        var expected = List.of(getLateFilingPenaltyReferenceType(), getSanctionsPenaltyReferenceType());
        assertEquals(expected, penaltyReferenceTypes.getEnabled());
    }

    @Test
    void fromReferenceStartsWith() throws ServiceException {
        assertEquals(Optional.of(lateFilingPenaltyReferenceType), penaltyReferenceTypes.fromReferenceStartsWith("A"));
        assertEquals(Optional.of(sanctionsPenaltyReferenceType), penaltyReferenceTypes.fromReferenceStartsWith("P"));
        assertEquals(Optional.empty(), penaltyReferenceTypes.fromReferenceStartsWith("U"));
    }

    @Test
    void isPenaltyReferenceStartsWithEnabled() throws ServiceException {
        assertTrue(penaltyReferenceTypes.isPenaltyReferenceStartsWithEnabled(lateFilingPenaltyReferenceType.getReferenceStartsWith()));
        assertTrue(penaltyReferenceTypes.isPenaltyReferenceStartsWithEnabled(sanctionsPenaltyReferenceType.getReferenceStartsWith()));
        assertFalse(penaltyReferenceTypes.isPenaltyReferenceStartsWithEnabled(sanctionsRoePenaltyReferenceType.getReferenceStartsWith()));
    }

    @Test
    void fromPenaltyReference() throws ServiceException {
        assertEquals(Optional.of(lateFilingPenaltyReferenceType), penaltyReferenceTypes.fromPenaltyReference("A1234567"));
        assertEquals(Optional.of(sanctionsPenaltyReferenceType), penaltyReferenceTypes.fromPenaltyReference("P1234567"));
        assertEquals(Optional.empty(), penaltyReferenceTypes.fromPenaltyReference("U1234567"));
    }

    private static PenaltyReferenceType getLateFilingPenaltyReferenceType() {
        PenaltyReferenceType lateFilingPenaltyReferenceType = new PenaltyReferenceType();
        lateFilingPenaltyReferenceType.setEnabledFrom(parse("2025-10-06T08:00:00Z"));
        lateFilingPenaltyReferenceType.setEnabledTo(null);
        lateFilingPenaltyReferenceType.setReason("Late filing of accounts");
        lateFilingPenaltyReferenceType.setReferenceRegex("^[Aa]\\d{7}$");
        lateFilingPenaltyReferenceType.setReferenceStartsWith("A");
        lateFilingPenaltyReferenceType.setReferenceType("LATE_FILING");
        return lateFilingPenaltyReferenceType;
    }

    private static PenaltyReferenceType getSanctionsPenaltyReferenceType() {
        PenaltyReferenceType sanctionsPenaltyReferenceType = new PenaltyReferenceType();
        sanctionsPenaltyReferenceType.setEnabledFrom(parse("2025-10-06T08:00:00Z"));
        sanctionsPenaltyReferenceType.setEnabledTo(null);
        sanctionsPenaltyReferenceType.setReason("Sanctions Penalty");
        sanctionsPenaltyReferenceType.setReferenceRegex("^[Pp]\\d{7}$");
        sanctionsPenaltyReferenceType.setReferenceStartsWith("P");
        sanctionsPenaltyReferenceType.setReferenceType("SANCTIONS");
        return sanctionsPenaltyReferenceType;
    }

    private static PenaltyReferenceType getSanctionsRoePenaltyReferenceType() {
        PenaltyReferenceType sanctionsRoePenaltyReferenceType = new PenaltyReferenceType();
        sanctionsRoePenaltyReferenceType.setEnabledFrom(parse("2025-10-06T08:00:00Z"));
        sanctionsRoePenaltyReferenceType.setEnabledTo(parse("2025-10-09T08:00:00Z"));
        sanctionsRoePenaltyReferenceType.setReason("Failure to update the Register of Overseas Entities");
        sanctionsRoePenaltyReferenceType.setReferenceRegex("^[Uu]\\d{7}$");
        sanctionsRoePenaltyReferenceType.setReferenceStartsWith("U");
        sanctionsRoePenaltyReferenceType.setReferenceType("SANCTIONS_ROE");
        return sanctionsRoePenaltyReferenceType;
    }

}
