package pa.gob.dntic.ejemploactuator;/*
 * ServicioBase con Spring Boot + Actuator.
 * Arranca con:  mvn spring-boot:run
 * Endpoints:  GET/POST /solicitudes?id=XXX  ·  /actuator/health  ·  /actuator/metrics
 * (Regla de juguete: los id que terminan en 9 "fallan", para generar errores.)
 */
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class ServicioBase {

    public static void main(String[] args) {
        SpringApplication.run(ServicioBase.class, args);
    }

    @RestController
    static class SolicitudesController {
        private static final Logger log = LoggerFactory.getLogger(SolicitudesController.class);
        private final MeterRegistry metricas;

        SolicitudesController(MeterRegistry metricas) {
            this.metricas = metricas;
        }

        @RequestMapping("/solicitudes")
        public ResponseEntity<String> solicitud(@RequestParam(defaultValue = "CAM-000") String id) {
            // MDC: todo log escrito mientras este id este puesto lleva el id automaticamente
            MDC.put("id", id);
            try {
                log.info("solicitud recibida");

                log.info("validando id");
                boolean ok = !id.endsWith("9");

                if (!ok) {
                    log.warn("validacion fallida: el id termina en 9");
                    log.error("timeout DB");
                    metricas.counter("solicitudes.recibidas", "resultado", "error").increment();
                    log.info("respondiendo 500");
                    return ResponseEntity.status(500).body("error");
                }

                log.info("validacion ok");
                log.info("procesando solicitud");
                metricas.counter("solicitudes.recibidas", "resultado", "ok").increment();
                log.info("solicitud procesada, respondiendo 200");
                return ResponseEntity.ok("ok");
            } finally {
                MDC.remove("id");   // limpiar, para que no se filtre a otra peticion
            }
        }
    }
}