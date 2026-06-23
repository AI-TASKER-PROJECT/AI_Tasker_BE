package com.aitasker.be.security.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

class PublicJobRouteMatcherTest {

    private final RequestMatcher jobDetailMatcher =
            RegexRequestMatcher.regexMatcher(HttpMethod.GET, "/api/v1/jobs/\\d+");

    private final RequestMatcher jobMilestonesMatcher =
            RegexRequestMatcher.regexMatcher(HttpMethod.GET, "/api/v1/jobs/\\d+/milestones");

    @Test
    void jobDetailMatcher_matchesNumericJobIdOnly() {
        assertTrue(jobDetailMatcher.matches(req("GET", "/api/v1/jobs/1")));
        assertTrue(jobDetailMatcher.matches(req("GET", "/api/v1/jobs/999")));
        assertFalse(jobDetailMatcher.matches(req("GET", "/api/v1/jobs/my")));
        assertFalse(jobDetailMatcher.matches(req("GET", "/api/v1/jobs/abc")));
        assertFalse(jobDetailMatcher.matches(req("POST", "/api/v1/jobs/1")));
    }

    @Test
    void jobMilestonesMatcher_matchesNumericJobIdOnly() {
        assertTrue(jobMilestonesMatcher.matches(req("GET", "/api/v1/jobs/1/milestones")));
        assertTrue(jobMilestonesMatcher.matches(req("GET", "/api/v1/jobs/999/milestones")));
        assertFalse(jobMilestonesMatcher.matches(req("GET", "/api/v1/jobs/my/milestones")));
        assertFalse(jobMilestonesMatcher.matches(req("GET", "/api/v1/jobs/abc/milestones")));
        assertFalse(jobMilestonesMatcher.matches(req("POST", "/api/v1/jobs/1/milestones")));
    }

    private MockHttpServletRequest req(String method, String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod(method);
        request.setRequestURI(uri);
        request.setServletPath(uri);
        return request;
    }
}
