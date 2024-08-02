package com.mocicarazvan.templatemodule.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder;

import java.io.IOException;

public class WebFluxLinkSerializer extends StdSerializer<WebFluxLinkBuilder.WebFluxLink> {

    public WebFluxLinkSerializer() {
        this(null);
    }

    public WebFluxLinkSerializer(Class<WebFluxLinkBuilder.WebFluxLink> t) {
        super(t);
    }

    @Override
    public void serialize(
            WebFluxLinkBuilder.WebFluxLink value, JsonGenerator gen, SerializerProvider provider)
            throws IOException {

        gen.writeStartObject();
        value.toMono().map(
                        link -> {
                            try {
                                gen.writeStringField("rel", link.getRel().toString());
                                gen.writeStringField("href", link.getHref());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return link;
                        })
                .subscribe(
                        link -> {
                            try {
                                gen.writeEndObject();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                );

    }


}