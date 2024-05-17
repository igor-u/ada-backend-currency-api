package br.com.ada.currencyapi.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import br.com.ada.currencyapi.domain.*;
import org.springframework.stereotype.Service;

import br.com.ada.currencyapi.exception.CoinNotFoundException;
import br.com.ada.currencyapi.exception.CurrencyException;
import br.com.ada.currencyapi.repository.CurrencyRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CurrencyService {

    private final CurrencyRepository currencyRepository;
    private final QuoteService quoteService;

    public List<CurrencyResponse> get() {
        List<Currency> currencies = currencyRepository.findAll();
        List<CurrencyResponse> dtos = new ArrayList<>();

        currencies.forEach((currency) -> dtos.add(CurrencyResponse.builder()
                .label("%s - %s".formatted(currency.getId(), currency.getName()))
                .build()));

        return dtos;
    }

    public Long create(CurrencyRequest request) throws CurrencyException {
        Currency currency = currencyRepository.findByName(request.getName());

        if (Objects.nonNull(currency)) {
            throw new CurrencyException("Coin already exists");
        }

        Currency saved = currencyRepository.save(Currency.builder()
                .name(request.getName())
                .description(request.getDescription())
                .exchanges(request.getExchanges())
                .build());
        return saved.getId();
    }

    public void delete(Long id) {
        currencyRepository.deleteById(id);
    }

    public ConvertCurrencyResponse convert(ConvertCurrencyRequest request) throws CoinNotFoundException {
        BigDecimal amount = getAmount(request);
        return ConvertCurrencyResponse.builder()
                .amount(amount)
                .build();

    }

    private BigDecimal getAmount(ConvertCurrencyRequest request) throws CoinNotFoundException {
        Currency currency = currencyRepository.findByName(request.getFrom());

        if (Objects.isNull(currency)) {
            throw new CoinNotFoundException(String.format("Coin not found: %s", request.getFrom()));
        }

        BigDecimal exchange = currency.getExchanges().get(request.getTo());

        if (Objects.isNull(exchange)) {
            throw new CoinNotFoundException(String.format("Exchange %s not found for %s", request.getTo(), request.getFrom()));
        }

        return request.getAmount().multiply(exchange);
    }

    public ConvertCurrencyResponse convertUsingExternalApi(ConvertCurrencyRequest request) {
        String currencyToCurrency = String.format("%s-%s".toUpperCase(), request.getFrom(), request.getTo());

        Conversion conversion = quoteService.getQuote(currencyToCurrency);

        BigDecimal high = new BigDecimal(
                conversion.getQuotes()
                        .get(currencyToCurrency.replace("-", ""))
                        .getHigh());

        return ConvertCurrencyResponse.builder()
                .amount(request.getAmount().multiply(high))
                .build();

    }
}
