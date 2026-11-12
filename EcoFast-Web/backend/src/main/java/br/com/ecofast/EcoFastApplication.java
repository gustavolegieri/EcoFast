package br.com.ecofast;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Ponto de entrada: sobe o servidor em http://localhost:8080 */
@SpringBootApplication
public class EcoFastApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcoFastApplication.class, args);
    }
}
