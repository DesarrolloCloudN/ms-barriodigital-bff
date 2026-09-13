package cl.duoc.barriodigital.bff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Esta es la clase principal del proyecto. Spring Boot la usa como punto de
// entrada para arrancar toda la aplicación (configuracion, seguridad,
// controllers, etc).
@SpringBootApplication
public class BffApplication {

	// Metodo main: es lo primero que se ejecuta cuando se corre el programa.
	// Solo le pide a Spring Boot que levante la aplicacion.
	public static void main(String[] args) {
		SpringApplication.run(BffApplication.class, args);
	}

}
