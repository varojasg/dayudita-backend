package pe.edu.upc.dayudita.validation;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pe.edu.upc.dayudita.clients.interfaces.rest.dto.CreateClientRequest;
import pe.edu.upc.dayudita.finance.interfaces.rest.dto.GenerateStatementRequest;
import pe.edu.upc.dayudita.finance.interfaces.rest.dto.RegisterPaymentRequest;
import pe.edu.upc.dayudita.finance.interfaces.rest.dto.UpdateFinancialConfigurationRequest;
import pe.edu.upc.dayudita.sales.domain.model.PurchasePaymentMode;
import pe.edu.upc.dayudita.sales.interfaces.rest.dto.CreatePurchaseRequest;
import pe.edu.upc.dayudita.sales.interfaces.rest.dto.PurchaseItemRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

class RequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp(){
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void createPurchase_FutureDateIsInvalid(){
        CreatePurchaseRequest request = new CreatePurchaseRequest(
                1L,
                LocalDate.now().plusDays(1),
                PurchasePaymentMode.INSTALLMENTS,
                4,
                new BigDecimal("0.24"),
                List.of(new PurchaseItemRequest(1L, 1))
        );

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void createPurchase_NegativeAnnualRateIsInvalid(){
        CreatePurchaseRequest request = new CreatePurchaseRequest(
                1L,
                LocalDate.now(),
                PurchasePaymentMode.INSTALLMENTS,
                4,
                new BigDecimal("-0.10"),
                List.of(new PurchaseItemRequest(1L, 1))
        );

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void createClient_InvalidCutoffDayIsRejected(){
        CreateClientRequest request = new CreateClientRequest(
                "Valeria",
                "Rojas",
                "70000010",
                "valeria@test.pe",
                "123456",
                "999999999",
                0,
                15
        );

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void purchaseItem_NegativeQuantityIsInvalid(){
        PurchaseItemRequest item = new PurchaseItemRequest(1L, -1);

        assertFalse(validator.validate(item).isEmpty());
    }

    @Test
    void statement_FutureCutoffDateIsInvalid(){
        GenerateStatementRequest request = new GenerateStatementRequest(LocalDate.now().plusDays(1));

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void payment_FutureDateIsInvalid(){
        RegisterPaymentRequest request = new RegisterPaymentRequest(LocalDate.now().plusDays(1));

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void financialConfiguration_NegativeCapitalIsInvalid(){
        UpdateFinancialConfigurationRequest request = new UpdateFinancialConfigurationRequest(
                new BigDecimal("0.05"),
                new BigDecimal("0.80"),
                new BigDecimal("0.24"),
                new BigDecimal("0.35"),
                new BigDecimal("-1.00"),
                new BigDecimal("500.00"),
                new BigDecimal("500.00"),
                12
        );

        assertFalse(validator.validate(request).isEmpty());
    }
}
