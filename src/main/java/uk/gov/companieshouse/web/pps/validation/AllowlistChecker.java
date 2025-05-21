package uk.gov.companieshouse.web.pps.validation;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.web.pps.PPSWebApplication;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

@Component
public class AllowlistChecker {


    protected static final Logger LOGGER = LoggerFactory
            .getLogger(PPSWebApplication.APPLICATION_NAME_SPACE);

    private static final String HOME = "/late-filing-penalty";
    private static final String REGEX = "\\/late-filing-penalty[\\/[a-zA-Z0-9-]+]+$";
    private static final String SIGN_OUT = "late-filing-penalty/sign-out";


    public String checkURL(String url) {
        Pattern p = Pattern.compile(REGEX);
        Matcher m = p.matcher(url);
        if (m.find()) {
            LOGGER.info("URL valid, returning to " + url);
            return url;
        }
        LOGGER.error("URL not valid. Returning to landing page...");
        return HOME;
    }

    public boolean checkSignOutIsReferer(String url) {
        Pattern p = Pattern.compile(SIGN_OUT);
        Matcher m = p.matcher(url);
        return m.find();
    }


}



