package br.com.ada.currencyapi.repository;

import br.com.ada.currencyapi.domain.Currency;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
public class CurrencyRepositoryTest {

    @Autowired
    CurrencyRepository currencyRepository;

    @BeforeEach
    void setup() {
        currencyRepository.save(new Currency(1l, "USD", "Dollar", Map.of()));
    }

    @AfterEach
    void tearDown() {
        currencyRepository.deleteAll();
    }

    @Test
    void testFindByName() {
        var currency = currencyRepository.findByName("USD");
        assertThat(currency.getId()).isEqualTo(1l);
        assertThat(currency.getName()).isEqualTo("USD");
    }

}
