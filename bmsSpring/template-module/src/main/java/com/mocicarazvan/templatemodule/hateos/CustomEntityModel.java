package com.mocicarazvan.templatemodule.hateos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.Link;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomEntityModel<T> {

    private T content;

    private Map<String, Link> _links = new HashMap<>();

    public void add(Link link) {
        if (_links == null)
            _links = new HashMap<>();
        _links.put(link.getRel().value(), link);
    }

    public static <T> CustomEntityModel<T> of(T content, Link... links) {
        CustomEntityModel<T> model = new CustomEntityModel<>();
        model.setContent(content);
        for (Link link : links) {
            model.add(link);
        }
        return model;

    }

    public void add(Collection<Link> links) {
        if (_links == null)
            _links = new HashMap<>();
        links.forEach(link -> _links.put(link.getRel().value(), link));
    }

    public <M> CustomEntityModel<M> convertContent(M content) {
        return new CustomEntityModel<>(content, _links);
    }
}
