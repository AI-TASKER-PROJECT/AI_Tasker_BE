package com.aitasker.be.security.config;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BusinessProfileRouteMatcherTest {

    private final RequestMatcher matcher =
            RegexRequestMatcher.regexMatcher(HttpMethod.GET, "/api/v1/profiles/business/\\d+");

    @Test
    void matches_numericBusinessId() {
        assertTrue(matcher.matches(req("GET", "/api/v1/profiles/business/1")));
        assertTrue(matcher.matches(req("GET", "/api/v1/profiles/business/999")));
    }

    @Test
    void doesNotMatch_meRoute() {
        assertFalse(matcher.matches(req("GET", "/api/v1/profiles/business/me")));
    }

    @Test
    void doesNotMatch_bareBusinessRoute() {
        assertFalse(matcher.matches(req("GET", "/api/v1/profiles/business")));
    }

    @Test
    void doesNotMatch_byJobRoute() {
        assertFalse(matcher.matches(req("GET", "/api/v1/profiles/business/by-job/5")));
    }

    @Test
    void doesNotMatch_nonGet() {
        assertFalse(matcher.matches(req("POST", "/api/v1/profiles/business/1")));
    }

    private MockHttpServletRequest req(String method, String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod(method);
        request.setRequestURI(uri);
        request.setServletPath(uri);
        return request;
    }
}
