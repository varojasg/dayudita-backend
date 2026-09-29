package pe.edu.upc.dayudita.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import pe.edu.upc.dayudita.sales.application.PurchaseService;
import pe.edu.upc.dayudita.sales.interfaces.rest.StorePurchaseController;
import pe.edu.upc.dayudita.shared.exception.GlobalExceptionHandler;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class PurchaseValidationIntegrationTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp(){
        PurchaseService purchaseService = new PurchaseService(null, null, null, null, null, null, null);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = standaloneSetup(new StorePurchaseController(purchaseService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void createPurchase_FutureDateReturnsBadRequest() throws Exception {
        String body = """
                {
                  "clientId": 1,
                  "purchaseDate": "2999-01-01",
                  "paymentMode": "INSTALLMENTS",
                  "installmentCount": 4,
                  "annualEffectiveRate": 0.24,
                  "items": [{"productId": 1, "quantity": 1}]
                }
                """;

        mockMvc.perform(post("/api/stores/1/purchases")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.purchaseDate").value("La fecha de compra no puede ser futura"));
    }

    @Test
    void createPurchase_NegativeQuantityReturnsBadRequest() throws Exception {
        String body = """
                {
                  "clientId": 1,
                  "purchaseDate": "2026-01-01",
                  "paymentMode": "INSTALLMENTS",
                  "installmentCount": 4,
                  "items": [{"productId": 1, "quantity": -2}]
                }
                """;

        mockMvc.perform(post("/api/stores/1/purchases")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
