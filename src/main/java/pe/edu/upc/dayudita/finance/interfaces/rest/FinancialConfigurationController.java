package pe.edu.upc.dayudita.finance.interfaces.rest;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.dayudita.finance.application.CreditPlanService;
import pe.edu.upc.dayudita.finance.application.FinancialConfigurationService;
import pe.edu.upc.dayudita.finance.domain.model.FinancialConfiguration;
import pe.edu.upc.dayudita.finance.interfaces.rest.dto.FinancialConfigurationResponse;
import pe.edu.upc.dayudita.finance.interfaces.rest.dto.UpdateFinancialConfigurationRequest;

@RestController
@RequestMapping("/api/finance/configuration")
public class FinancialConfigurationController {

    private final FinancialConfigurationService financialConfigurationService;

    public FinancialConfigurationController(FinancialConfigurationService financialConfigurationService){
        this.financialConfigurationService = financialConfigurationService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','STORE_ADMIN')")
    public FinancialConfigurationResponse getConfiguration(){
        return toResponse(financialConfigurationService.getConfiguration());
    }

    @PutMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public FinancialConfigurationResponse updateConfiguration(
            @Valid @RequestBody UpdateFinancialConfigurationRequest request
    ){
        return toResponse(financialConfigurationService.updateConfiguration(request));
    }

    private FinancialConfigurationResponse toResponse(FinancialConfiguration configuration){
        return new FinancialConfigurationResponse(
                configuration.getId(),
                "PEN",
                360,
                CreditPlanService.PAYMENT_PERIOD_DAYS,
                configuration.getMinAnnualEffectiveRate(),
                configuration.getMaxAnnualEffectiveRate(),
                configuration.getAnnualEffectiveRate(),
                configuration.getMoratoryAnnualEffectiveRate(),
                configuration.getMinCapital(),
                configuration.getMaxCapital(),
                configuration.getCreditLimit(),
                configuration.getMaxInstallments()
        );
    }
}
