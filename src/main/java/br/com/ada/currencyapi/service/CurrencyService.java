package br.com.ada.currencyapi.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import br.com.ada.currencyapi.client.AwesomeClient;
import br.com.ada.currencyapi.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.ada.currencyapi.exception.CoinNotFoundException;
import br.com.ada.currencyapi.exception.CurrencyException;
import br.com.ada.currencyapi.repository.CurrencyRepository;

@Service
public class CurrencyService {

    private final CurrencyRepository currencyRepository;

    private final AwesomeClient awesomeClient;

    @Autowired
    public CurrencyService(CurrencyRepository currencyRepository, AwesomeClient awesomeClient) {
        this.currencyRepository = currencyRepository;
        this.awesomeClient = awesomeClient;
    }

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

    public ConvertCurrencyResponse convertUsingExternalApi(ConvertCurrencyRequest request) throws CoinNotFoundException {
        BigDecimal amount = getAmountUsingExternalApi(request);
        return ConvertCurrencyResponse.builder()
                .amount(amount)
                .build();

    }

    private BigDecimal getAmountUsingExternalApi(ConvertCurrencyRequest request) throws CoinNotFoundException {
        String currencyToCurrency = String.format("%s-%s".toUpperCase(), request.getFrom(), request.getTo());

        var response = awesomeClient.get(currencyToCurrency);

        BigDecimal high = new BigDecimal(response
                .getBody()
                .getQuotes()
                .get(currencyToCurrency.replace("-", ""))
                .getHigh());

        return request.getAmount().multiply(high);

    }

}
