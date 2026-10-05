package pe.edu.upc.dayudita.finance.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.dayudita.finance.domain.model.FinancialConfiguration;
import pe.edu.upc.dayudita.finance.domain.repository.FinancialConfigurationRepository;
import pe.edu.upc.dayudita.finance.interfaces.rest.dto.UpdateFinancialConfigurationRequest;
import pe.edu.upc.dayudita.stores.domain.model.Store;

import java.math.BigDecimal;

@Service
public class FinancialConfigurationService {

    private final FinancialConfigurationRepository financialConfigurationRepository;

    public FinancialConfigurationService(FinancialConfigurationRepository financialConfigurationRepository){
        this.financialConfigurationRepository = financialConfigurationRepository;
    }

    public FinancialConfiguration getConfiguration(Long storeId){
        return financialConfigurationRepository.findByStore_Id(storeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "La tienda aun no tiene una politica de credito registrada"
                ));
    }

    @Transactional
    public FinancialConfiguration createDefaultConfiguration(Store store){
        FinancialConfiguration configuration = new FinancialConfiguration();
        configuration.setStore(store);
        configuration.setCapitalMinimo(new BigDecimal("200.00"));
        configuration.setCapitalMaximo(new BigDecimal("500.00"));
        configuration.setTeaMinima(new BigDecimal("0.15"));
        configuration.setTeaMaxima(new BigDecimal("0.25"));
        configuration.setPlazoMaximoMeses(4);
        configuration.setOtorgaGracia(true);
        return financialConfigurationRepository.save(configuration);
    }

    @Transactional
    public FinancialConfiguration updateConfiguration(Long storeId, UpdateFinancialConfigurationRequest request){
        validateConfiguration(request);

        FinancialConfiguration configuration = getConfiguration(storeId);

        configuration.setCapitalMinimo(request.capitalMinimo());
        configuration.setCapitalMaximo(request.capitalMaximo());
        configuration.setTeaMinima(request.teaMinima());
        configuration.setTeaMaxima(request.teaMaxima());
        configuration.setPlazoMaximoMeses(request.plazoMaximoMeses());
        configuration.setOtorgaGracia(request.otorgaGracia());

        return financialConfigurationRepository.save(configuration);
    }

    private void validateConfiguration(UpdateFinancialConfigurationRequest request){
        if(request.capitalMinimo().compareTo(request.capitalMaximo()) > 0){
            throw new IllegalArgumentException("El capital minimo no puede ser mayor al capital maximo");
        }

        if(request.teaMinima().compareTo(request.teaMaxima()) > 0){
            throw new IllegalArgumentException("La TEA minima no puede ser mayor a la TEA maxima");
        }
    }
}
