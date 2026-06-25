package com.accounting.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class JacksonConfig {

    private static final long MAX_SAFE_INTEGER = 9007199254740991L;

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer longToStringCustomizer() {
        return builder -> {
            SimpleModule module = new SimpleModule();
            module.addSerializer(Long.class, new JsonSerializer<Long>() {
                @Override
                public void serialize(Long value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                    if (value == null) {
                        gen.writeNull();
                    } else if (value > MAX_SAFE_INTEGER || value < -MAX_SAFE_INTEGER) {
                        gen.writeString(value.toString());
                    } else {
                        gen.writeNumber(value);
                    }
                }
            });
            module.addSerializer(long.class, new JsonSerializer<Long>() {
                @Override
                public void serialize(Long value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                    if (value > MAX_SAFE_INTEGER || value < -MAX_SAFE_INTEGER) {
                        gen.writeString(value.toString());
                    } else {
                        gen.writeNumber(value);
                    }
                }
            });
            builder.modules(module);
        };
    }
}
