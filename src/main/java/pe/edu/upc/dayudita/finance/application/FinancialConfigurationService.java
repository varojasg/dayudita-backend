package pe.edu.upc.dayudita.finance.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.dayudita.finance.domain.model.FinancialConfiguration;
import pe.edu.upc.dayudita.finance.domain.repository.FinancialConfigurationRepository;
import pe.edu.upc.dayudita.finance.interfaces.rest.dto.UpdateFinancialConfigurationRequest;

@Service
public class FinancialConfigurationService {

    private final FinancialConfigurationRepository financialConfigurationRepository;

    public FinancialConfigurationService(FinancialConfigurationRepository financialConfigurationRepository){
        this.financialConfigurationRepository = financialConfigurationRepository;
    }

    public FinancialConfiguration getConfiguration(){
        return financialConfigurationRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new IllegalArgumentException(
                        "La configuracion financiera aun no ha sido registrada"
                ));
    }

    @Transactional
    public FinancialConfiguration updateConfiguration(UpdateFinancialConfigurationRequest request){
        validateConfiguration(request);

        FinancialConfiguration configuration = financialConfigurationRepository
                .findFirstByOrderByIdAsc()
                .orElseGet(FinancialConfiguration::new);

        configuration.setMinAnnualEffectiveRate(request.minAnnualEffectiveRate());
        configuration.setMaxAnnualEffectiveRate(request.maxAnnualEffectiveRate());
        configuration.setAnnualEffectiveRate(request.annualEffectiveRate());
        configuration.setMoratoryAnnualEffectiveRate(request.moratoryAnnualEffectiveRate());
        configuration.setMinCapital(request.minCapital());
        configuration.setMaxCapital(request.maxCapital());
        configuration.setCreditLimit(request.creditLimit());
        configuration.setMaxInstallments(request.maxInstallments());

        return financialConfigurationRepository.save(configuration);
    }

    private void validateConfiguration(UpdateFinancialConfigurationRequest request){
        if(request.minAnnualEffectiveRate().compareTo(request.maxAnnualEffectiveRate()) > 0){
            throw new IllegalArgumentException("La tasa minima no puede ser mayor a la tasa maxima");
        }

        if(request.annualEffectiveRate().compareTo(request.minAnnualEffectiveRate()) < 0
                || request.annualEffectiveRate().compareTo(request.maxAnnualEffectiveRate()) > 0){
            throw new IllegalArgumentException("La TEA debe encontrarse dentro del rango configurado");
        }

        if(request.minCapital().compareTo(request.maxCapital()) > 0){
            throw new IllegalArgumentException("El capital minimo no puede ser mayor al capital maximo");
        }

        if(request.minCapital().compareTo(request.creditLimit()) > 0){
            throw new IllegalArgumentException("El capital minimo no puede superar el limite de credito");
        }
    }
}
