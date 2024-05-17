package br.com.ada.currencyapi.service;

import br.com.ada.currencyapi.domain.Conversion;

public interface QuoteService {
    Conversion getQuote(String currencies);
}