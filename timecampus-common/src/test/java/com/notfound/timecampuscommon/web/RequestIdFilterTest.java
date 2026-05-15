package com.notfound.timecampuscommon.web;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class RequestIdFilterTest {

    private final RequestIdFilter requestIdFilter = new RequestIdFilter();

    @Test
    void shouldUseIncomingRequestIdAndCleanupMdc() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(RequestIdFilter.REQUEST_ID_HEADER, "req-123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        final String[] mdcValueInChain = new String[1];
        jakarta.servlet.FilterChain chain = (req, res) -> mdcValueInChain[0] = MDC.get(RequestIdFilter.MDC_KEY);

        requestIdFilter.doFilter(request, response, chain);

        assertEquals("req-123", mdcValueInChain[0]);
        assertEquals("req-123", response.getHeader(RequestIdFilter.REQUEST_ID_HEADER));
        assertNull(MDC.get(RequestIdFilter.MDC_KEY));
    }

    @Test
    void shouldGenerateRequestIdWhenHeaderMissing() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        jakarta.servlet.FilterChain chain = (req, res) -> {
            // no-op
        };

        requestIdFilter.doFilter(request, response, chain);

        String requestId = response.getHeader(RequestIdFilter.REQUEST_ID_HEADER);
        assertNotNull(requestId);
        assertFalse(requestId.isBlank());
        assertNull(MDC.get(RequestIdFilter.MDC_KEY));
    }
}


