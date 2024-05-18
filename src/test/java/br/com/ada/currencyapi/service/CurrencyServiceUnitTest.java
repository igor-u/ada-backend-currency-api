package br.com.ada.currencyapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.ada.currencyapi.client.AwesomeClient;
import br.com.ada.currencyapi.client.Conversion;
import br.com.ada.currencyapi.client.Quote;
import br.com.ada.currencyapi.domain.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.ada.currencyapi.exception.CoinNotFoundException;
import br.com.ada.currencyapi.exception.CurrencyException;
import br.com.ada.currencyapi.repository.CurrencyRepository;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
public class CurrencyServiceUnitTest {

    @InjectMocks
    private CurrencyService currencyService;

    @Mock
    private CurrencyRepository currencyRepository;

    @Mock
    private AwesomeClient awesomeClient;

    @Test
    void testGet() {
        List<Currency> list = new ArrayList<>();
        list.add(Currency.builder()
                .id(1L)
                .name("EUR")
                .description("Euro")
                .build());
        list.add(Currency.builder()
                .id(2L)
                .name("USD")
                .description("Dollar")
                .build());

        when(currencyRepository.findAll()).thenReturn(list);

        List<CurrencyResponse> responses = currencyService.get();
        Assertions.assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("1 - EUR", responses.get(0).getLabel());
        assertEquals("2 - USD", responses.get(1).getLabel());
    }

    @Test
    void testCreate() {
        Mockito.when(currencyRepository.findByName(anyString())).thenReturn(null);
        Mockito.when(currencyRepository.save(any(Currency.class))).thenReturn(Currency.builder().id(3L).build());

        Long id = currencyService.create(CurrencyRequest.builder().name("name").build());
        Assertions.assertNotNull(id);
    }

    @Test
    void testCreateThrowsCurrencyException() {
        Mockito.when(currencyRepository.findByName(any())).thenReturn(Currency.builder().build());

        CurrencyException exception = Assertions.assertThrows(CurrencyException.class, () -> currencyService.create(CurrencyRequest.builder().build()));

        assertEquals("Coin already exists", exception.getMessage());
    }

    @Test
    void testDelete() {
        doNothing().when(currencyRepository).deleteById(anyLong());
        currencyService.delete(1L);
        verify(currencyRepository, times(1)).deleteById(anyLong());
        verifyNoMoreInteractions(currencyRepository);
    }

    @Test
    void testConvert() {
        Mockito.when(currencyRepository.findByName(any())).thenReturn(
                Currency.builder()
                        .exchanges(Map.of("EUR", new BigDecimal("2")))
                        .build()
        );

        ConvertCurrencyRequest request = ConvertCurrencyRequest
                .builder()
                .to("EUR")
                .amount(BigDecimal.TEN)
                .build();

        ConvertCurrencyResponse response = currencyService.convert(request);
        assertEquals(new BigDecimal("20"), response.getAmount());
    }

    @Test
    void testConvertThrowsCoinNotFoundException() {
        Mockito.when(currencyRepository.findByName(any())).thenReturn(null);
        ConvertCurrencyRequest request = ConvertCurrencyRequest
                .builder()
                .from("USD")
                .to("EUR")
                .amount(BigDecimal.TEN)
                .build();

        CoinNotFoundException exception = Assertions.assertThrows(CoinNotFoundException.class, () -> currencyService.convert(request));

        assertEquals("Coin not found: USD", exception.getMessage());
    }

    @Test
    void testConvertThrowsCoinNotFoundExceptionForExchange() {
        Mockito.when(currencyRepository.findByName(any())).thenReturn(Currency.builder()
                .exchanges(Map.of("BRL", new BigDecimal("2")))
                .build());
        ConvertCurrencyRequest request = ConvertCurrencyRequest
                .builder()
                .from("USD")
                .to("EUR")
                .amount(BigDecimal.TEN)
                .build();

        CoinNotFoundException exception = Assertions.assertThrows(CoinNotFoundException.class, () -> currencyService.convert(request));

        assertEquals("Exchange EUR not found for USD", exception.getMessage());
    }

    @Test
    void testConvertUsingExternalApi() {
        Conversion conversion = new Conversion();

        Map<String, Quote> quotes = new HashMap<>();
        Quote usdbrl = new Quote();
        usdbrl.setHigh("5.50");
        quotes.put("USDBRL", usdbrl);

        conversion.setQuotes(quotes);

        var convertCurrencyRequest = ConvertCurrencyRequest.builder()
                .from("USD")
                .to("BRL")
                .amount(BigDecimal.TEN)
                .build();

        Mockito.when(awesomeClient.get(any())).thenReturn(ResponseEntity.ok(conversion));

        var quote = currencyService.convertUsingExternalApi(convertCurrencyRequest);

        assertEquals(new BigDecimal("55.00"), quote.getAmount());
    }
}