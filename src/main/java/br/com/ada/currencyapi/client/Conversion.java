package br.com.ada.currencyapi.client;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class Conversion {
    private Map<String, Quote> quotes = new HashMap<>();

    @JsonAnySetter
    public void setQuote(String key, Quote value) {
        this.quotes.put(key, value);
    }

}

