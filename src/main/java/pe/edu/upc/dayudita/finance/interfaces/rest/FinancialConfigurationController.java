package pe.edu.upc.dayudita.finance.interfaces.rest;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.dayudita.finance.application.FinancialCalculator;
import pe.edu.upc.dayudita.finance.application.FinancialConfigurationService;
import pe.edu.upc.dayudita.finance.domain.model.FinancialConfiguration;
import pe.edu.upc.dayudita.finance.interfaces.rest.dto.FinancialConfigurationResponse;
import pe.edu.upc.dayudita.finance.interfaces.rest.dto.UpdateFinancialConfigurationRequest;
import pe.edu.upc.dayudita.iam.application.CurrentUserService;

@RestController
@RequestMapping("/api/stores/{storeId}/credit-policy")
@PreAuthorize("hasRole('STORE_ADMIN')")
public class FinancialConfigurationController {

    private final FinancialConfigurationService financialConfigurationService;
    private final CurrentUserService currentUserService;

    public FinancialConfigurationController(
            FinancialConfigurationService financialConfigurationService,
            CurrentUserService currentUserService
    ){
        this.financialConfigurationService = financialConfigurationService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public FinancialConfigurationResponse getCreditPolicy(@PathVariable Long storeId){
        currentUserService.validateStoreAdmin(storeId);
        return toResponse(financialConfigurationService.getConfiguration(storeId));
    }

    @PutMapping
    public FinancialConfigurationResponse updateCreditPolicy(
            @PathVariable Long storeId,
            @Valid @RequestBody UpdateFinancialConfigurationRequest request
    ){
        currentUserService.validateStoreAdmin(storeId);
        return toResponse(financialConfigurationService.updateConfiguration(storeId, request));
    }

    private FinancialConfigurationResponse toResponse(FinancialConfiguration configuration){
        return new FinancialConfigurationResponse(
                configuration.getId(),
                configuration.getStore().getId(),
                "PEN",
                FinancialCalculator.COMMERCIAL_YEAR_DAYS,
                FinancialCalculator.PAYMENT_PERIOD_DAYS,
                configuration.getCapitalMinimo(),
                configuration.getCapitalMaximo(),
                configuration.getTeaMinima(),
                configuration.getTeaMaxima(),
                configuration.getPlazoMaximoMeses(),
                configuration.getOtorgaGracia()
        );
    }
}
