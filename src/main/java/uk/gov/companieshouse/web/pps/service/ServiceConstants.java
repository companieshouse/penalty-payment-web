package uk.gov.companieshouse.web.pps.service;

public class ServiceConstants {

    public static final String AMOUNT_ATTR = "outstanding";
    public static final String AVAILABLE_PENALTY_REF_ATTR = "availablePenaltyReference";
    public static final String BACK_LINK_URL_ATTR = "backLinkUrl";
    public static final String COMPANY_NAME_ATTR = "companyName";
    public static final String COMPANY_NUMBER_ATTR = "companyNumber";
    public static final String ENTER_DETAILS_MODEL_ATTR = "enterDetails";
    public static final String PENALTY_REF_ATTR = "penaltyRef";
    public static final String PENALTY_REFERENCE_NAME_ATTR = "penaltyReferenceName";
    public static final String PENALTY_REFERENCE_CHOICE_ATTR = "penaltyReferenceChoice";
    public static final String REASON_ATTR = "reasonForPenalty";
    public static final String SIGN_OUT_URL_ATTR = "signOutPath";

    public static final String PAYMENT_STATE = "payment_state";
    public static final String DATE_STR = "date";

    public static final String SERVICE_UNAVAILABLE_VIEW_NAME = "pps/serviceUnavailable";

    public static final String REFERER = "Referer";
    public static final String SIGN_IN_INFO = "signin_info";
    public static final String URL_PRIOR_SIGN_OUT = "url_prior_signout";

    private ServiceConstants() {
        throw new IllegalAccessError("Constants class");
    }
}
