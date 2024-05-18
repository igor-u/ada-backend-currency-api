package br.com.ada.currencyapi.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "awesomeClient", url = "https://economia.awesomeapi.com.br")
public interface AwesomeClient {

    @GetMapping("json/last/{currencies}")
    ResponseEntity<Conversion> get(@PathVariable("currencies") String currencies);

}
