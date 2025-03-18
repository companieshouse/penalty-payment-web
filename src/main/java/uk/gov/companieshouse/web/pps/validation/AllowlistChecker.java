package uk.gov.companieshouse.web.pps.validation;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.web.pps.PPSWebApplication;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AllowlistChecker {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(PPSWebApplication.APPLICATION_NAME_SPACE);

    private static final Pattern URL_PATTERN = Pattern.compile(
            "/late-filing-penalty?[/a-zA-Z\\d-?=+]+$");
    private static final Pattern SIGNOUT_PATTERN = Pattern.compile(
            "late-filing-penalty/sign-out");


    public String checkURL(String url) {
        Matcher m = URL_PATTERN.matcher(url);
        if (m.find()) {
            LOGGER.info("URL valid, returning to " + url);
            return url;
        }
        LOGGER.error("URL not valid. Returning to landing page...");
        return "/late-filing-penalty/ref-starts-with";
    }

    public boolean checkSignOutIsReferer(String url) {
        Matcher m = SIGNOUT_PATTERN.matcher(url);
        return m.find();
    }


}



