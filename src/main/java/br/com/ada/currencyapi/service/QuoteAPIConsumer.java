package br.com.ada.currencyapi.service;

import br.com.ada.currencyapi.domain.Conversion;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class QuoteAPIConsumer implements QuoteService {

    @Override
    public Conversion getQuote(String currencies) {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Conversion> responseEntity =
                restTemplate
                        .getForEntity(
                                String.format("https://economia.awesomeapi.com.br/json/last/%s", currencies), Conversion.class);
        return responseEntity.getBody();
    }

}
