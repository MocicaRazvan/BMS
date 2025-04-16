package com.mocicarazvan.templatemodule.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder;

import java.io.IOException;

//todo remove, this is not actual needed the bridge is one by CustomEntityModel and PageableResponseAssembler
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
        // its fine to block because ser/deser is not in the reactive chain, its blocking by nature
        // this is more like a cast to a concrete type
        Link link = value.toMono().block();
        if (link != null) {
            gen.writeStringField("rel", link.getRel().toString());
            gen.writeStringField("href", link.getHref());
        }
        gen.writeEndObject();
    }


}