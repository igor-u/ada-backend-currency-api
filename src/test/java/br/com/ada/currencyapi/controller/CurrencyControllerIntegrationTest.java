package br.com.ada.currencyapi.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.ada.currencyapi.domain.Currency;
import br.com.ada.currencyapi.domain.CurrencyRequest;
import br.com.ada.currencyapi.repository.CurrencyRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureMockMvc
public class CurrencyControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CurrencyRepository currencyRepository;

    @Test
    void testGetReturns200() throws Exception {
        assertEquals(0, currencyRepository.count());

        currencyRepository.save(new Currency(1L, "BRL", "BRL", null));

        mockMvc.perform(
                        get("/currency")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andDo(print());

        currencyRepository.deleteAll();
    }

    @Test
    void testCreateReturns200() throws Exception {
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
                .andExpect(jsonPath("$").isNotEmpty())
                .andDo(MockMvcResultHandlers.print());

        currencyRepository.deleteAll();
    }

    @Test
    void testCreateReturns500() throws Exception {
        currencyRepository.save(new Currency(null, "USD", "Dollars", null));
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
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(jsonPath("$").value("Coin already exists"))
                .andDo(MockMvcResultHandlers.print());

        currencyRepository.deleteAll();
    }

    @Test
    void testConvertReturns200() throws Exception {
        currencyRepository.save(new Currency(1L, "BRL", "BRL", Map.of("USD", BigDecimal.TEN)));

        mockMvc.perform(
                        get("/currency/convert?from=BRL&to=USD&amount=5")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(new BigDecimal("50.0")))
                .andDo(print());

        currencyRepository.deleteAll();
    }

    @Test
    void testConvertThrowsCoinNotFoundAndReturns404() throws Exception {

        mockMvc.perform(
                        get("/currency/convert?from=BRL&to=USD&amount=5")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Coin not found: BRL"))
                .andDo(print());

        currencyRepository.deleteAll();
    }

    @Test
    void testConvertExchangeNotFoundAndReturns404() throws Exception {
        currencyRepository.save(new Currency(1L, "BRL", "BRL", Map.of("USD", BigDecimal.TEN)));

        mockMvc.perform(
                        get("/currency/convert?from=BRL&to=EUR&amount=5")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Exchange EUR not found for BRL"))
                .andDo(print());

        currencyRepository.deleteAll();
    }

    @Test
    void convertUsingExternalApiReturns200() throws Exception {
        mockMvc.perform(
                        get("/currency/api-convert?from=USD&to=BRL&amount=2")
                )
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    void convertUsingExternalApiReturns500() throws Exception {
        mockMvc.perform(
                        get("/currency/api-convert?from=XXX&to=XXX&amount=2")
                )
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$").value("Error while processing your request. Try again later."))
                .andDo(print());
    }

    @Test
    void testDeleteReturns200() throws Exception {
        assertEquals(0, currencyRepository.count());

        Currency currency = currencyRepository.save(new Currency(5L, "BRL", "BRL", null));
        mockMvc.perform(
                        delete("/currency/" + currency.getId())
                )
                .andExpect(status().isOk())
                .andDo(print());
    }

}
