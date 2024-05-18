package br.com.ada.currencyapi.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import br.com.ada.currencyapi.domain.ConvertCurrencyResponse;
import br.com.ada.currencyapi.domain.Currency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.ada.currencyapi.domain.CurrencyRequest;
import br.com.ada.currencyapi.domain.CurrencyResponse;
import br.com.ada.currencyapi.service.CurrencyService;
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
public class CurrencyControllerUnitTest {

    @InjectMocks
    private CurrencyController currencyController;

    @Mock
    private CurrencyService currencyService;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(currencyController).build();
    }

    @Test
    void testGetReturns200() throws Exception {
        Mockito.when(currencyService.get()).thenReturn(List.of(CurrencyResponse.builder()
                        .label("1 - USD")
                .build()));

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/currency")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].label").value("1 - USD"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void testConvertReturns200() throws Exception {
        Mockito.when(currencyService.convert(any())).thenReturn(
                ConvertCurrencyResponse.builder()
                        .amount(new BigDecimal("50.0"))
                .build());
        mockMvc.perform(
                        get("/currency/convert?from=BRL&to=USD&amount=5")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(new BigDecimal("50.0")))
                .andDo(print());
    }

    @Test
    void testConvertUsingExternalApiReturns200() throws Exception {
        Mockito.when(currencyService.convertUsingExternalApi(any())).thenReturn(
                ConvertCurrencyResponse.builder()
                        .amount(new BigDecimal("50.0"))
                        .build());
        mockMvc.perform(
                        get("/currency/api-convert?from=BRL&to=USD&amount=5")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(new BigDecimal("50.0")))
                .andDo(print());
    }

    @Test
    void testCreateCurrencyReturns200() throws Exception {
        Mockito.when(currencyService.create(Mockito.any(CurrencyRequest.class))).thenReturn(5L);
        CurrencyRequest request = CurrencyRequest.builder()
                .name("USD")
                .build();
        var content = new ObjectMapper().writeValueAsString(request);

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/currency")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(content)
                )
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(jsonPath("$").value(5L))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void testCreateCurrencyReturns400() throws Exception {
        CurrencyRequest request = CurrencyRequest.builder()
                .description("Dollars")
                .build();

        var content = new ObjectMapper().writeValueAsString(request);

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/currency")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(content)
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void deleteReturns200() throws Exception {
        Currency currency = new Currency(5L, "BRL", "BRL", null);
        doNothing().when(currencyService).delete(anyLong());
        mockMvc.perform(
                        delete("/currency/" + currency.getId())
                )
                .andExpect(status().isOk())
                .andDo(print());
    }


}
