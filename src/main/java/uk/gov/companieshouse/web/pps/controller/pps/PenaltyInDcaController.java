package uk.gov.companieshouse.web.pps.controller.pps;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.companieshouse.web.pps.config.PenaltyConfigurationProperties;
import uk.gov.companieshouse.web.pps.controller.BaseController;
import uk.gov.companieshouse.web.pps.service.navigation.NavigatorService;
import uk.gov.companieshouse.web.pps.session.SessionService;
import uk.gov.companieshouse.web.pps.util.PenaltyUtils;

@Controller
@RequestMapping("/pay-penalty/company/{companyNumber}/penalty/{penaltyRef}/penalty-in-dca")
public class PenaltyInDcaController extends BaseController {

    static final String PENALTY_IN_DCA_TEMPLATE_NAME = "pps/penaltyInDCA";

    public PenaltyInDcaController(
            NavigatorService navigatorService,
            SessionService sessionService,
            PenaltyConfigurationProperties penaltyConfigurationProperties) {
        super(navigatorService, sessionService, penaltyConfigurationProperties);
    }

    @Override
    protected String getTemplateName() {
        return PENALTY_IN_DCA_TEMPLATE_NAME;
    }

    @GetMapping
    public String getPenaltyInDCA(@PathVariable String companyNumber,
            @PathVariable String penaltyRef,
            Model model) {

        var penaltyReference = PenaltyUtils.getPenaltyReferenceType(penaltyRef);
        addBaseAttributesToModel(model,
                penaltyConfigurationProperties.getEnterDetailsPath()
                        + "?ref-starts-with=" + penaltyReference.getStartsWith(),
                penaltyConfigurationProperties.getSignOutPath());
        return getTemplateName();
    }

}
