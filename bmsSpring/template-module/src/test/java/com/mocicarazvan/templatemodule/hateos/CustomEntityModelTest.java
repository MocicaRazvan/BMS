package com.mocicarazvan.templatemodule.hateos;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.hateoas.Link;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class CustomEntityModelTest {
    @Test
    void emptyConstructorCreatesInstanceWithEmptyLinks() {
        CustomEntityModel<String> model = new CustomEntityModel<>();

        assertNotNull(model.get_links());
        assertTrue(model.get_links().isEmpty());
        assertNull(model.getContent());
    }

    @Test
    void builderCreatesCorrectInstance() {
        Map<String, Link> links = new HashMap<>();
        links.put("self", Link.of("http://example.com"));

        CustomEntityModel<String> model = CustomEntityModel.<String>builder()
                .content("test")
                ._links(links)
                .build();

        assertEquals("test", model.getContent());
        assertEquals(links, model.get_links());
    }

    @Test
    void addLinkAddsToLinks() {
        CustomEntityModel<String> model = new CustomEntityModel<>();
        Link link = Link.of("http://example.com", "self");

        model.add(link);

        assertEquals(link, model.get_links().get("self"));
        assertEquals(1, model.get_links().size());
    }

    @Test
    void addLinkWithNullLinksInitializesMap() {
        CustomEntityModel<String> model = new CustomEntityModel<>();
        model.set_links(null);
        Link link = Link.of("http://example.com", "self");

        model.add(link);

        assertNotNull(model.get_links());
        assertEquals(link, model.get_links().get("self"));
    }

    @Test
    void ofCreatesModelWithContentAndLinks() {
        String content = "test";
        Link link1 = Link.of("http://example.com/1", "self");
        Link link2 = Link.of("http://example.com/2", "next");

        CustomEntityModel<String> model = CustomEntityModel.of(content, link1, link2);

        assertEquals(content, model.getContent());
        assertEquals(link1, model.get_links().get("self"));
        assertEquals(link2, model.get_links().get("next"));
        assertEquals(2, model.get_links().size());
    }

    @Test
    void ofCreatesModelWithNoLinks() {
        String content = "test";

        CustomEntityModel<String> model = CustomEntityModel.of(content);

        assertEquals(content, model.getContent());
        assertTrue(model.get_links().isEmpty());
    }

    @Test
    void addListOfLinksAddsAllLinks() {
        CustomEntityModel<String> model = new CustomEntityModel<>();
        Link link1 = Link.of("http://example.com/1", "self");
        Link link2 = Link.of("http://example.com/2", "next");
        List<Link> links = Arrays.asList(link1, link2);

        model.add(links);

        assertEquals(link1, model.get_links().get("self"));
        assertEquals(link2, model.get_links().get("next"));
        assertEquals(2, model.get_links().size());
    }

    @Test
    void addListOfLinksWithNullLinksInitializesMap() {
        CustomEntityModel<String> model = new CustomEntityModel<>();
        model.set_links(null);
        Link link = Link.of("http://example.com", "self");
        List<Link> links = List.of(link);

        model.add(links);

        assertNotNull(model.get_links());
        assertEquals(link, model.get_links().get("self"));
    }

    @Test
    void convertContentCreatesNewModelWithDifferentContent() {
        CustomEntityModel<String> stringModel = new CustomEntityModel<>();
        stringModel.setContent("original");
        Link link = Link.of("http://example.com", "self");
        stringModel.add(link);

        CustomEntityModel<Integer> intModel = stringModel.convertContent(42);

        assertEquals(Integer.valueOf(42), intModel.getContent());
        assertEquals(link, intModel.get_links().get("self"));
        assertEquals(1, intModel.get_links().size());
    }
}