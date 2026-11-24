package it.drosariodev.calibrazione.config;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * Configurazione Jackson 3 usata da Spring MVC.
 *
 * Le date esposte dai DTO sono LocalDateTime: per rispettare il contratto
 * API vengono serializzate in ISO-8601 con il fuso orario del server.
 */
@Configuration
public class JacksonConfig {

    private static final DateTimeFormatter FORMATO_DATA_ORA =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    @Bean
    public JsonMapperBuilderCustomizer calibrazioneJacksonCustomizer() {
        return builder -> {
            builder.disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS);
            builder.addModule(moduloDate());
        };
    }

    private SimpleModule moduloDate() {
        SimpleModule modulo = new SimpleModule("calibrazione-date");
        modulo.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());
        return modulo;
    }

    private static final class LocalDateTimeSerializer extends StdSerializer<LocalDateTime> {

        private LocalDateTimeSerializer() {
            super(LocalDateTime.class);
        }

        @Override
        public void serialize(LocalDateTime value, JsonGenerator generator, SerializationContext context)
                throws JacksonException {
            if (value == null) {
                generator.writeNull();
                return;
            }
            OffsetDateTime dataUtc = value.atZone(ZoneId.systemDefault())
                    .toInstant()
                    .atOffset(ZoneOffset.UTC);
            generator.writeString(FORMATO_DATA_ORA.format(dataUtc));
        }
    }
}
