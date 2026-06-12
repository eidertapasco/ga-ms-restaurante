package co.edu.sena.ga_ms_restaurante;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
		"co.edu.sena.ga_ms_restaurante",
		"co.edu.sena.security"
})
public class GaMsRestauranteApplication {

	public static void main(String[] args) {
		SpringApplication.run(GaMsRestauranteApplication.class, args);
	}

}