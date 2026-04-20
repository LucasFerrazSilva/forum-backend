package com.ferraz.forumbackend.integration.util;

import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

public enum HttpMethod {

    GET {
        @Override
        public MockHttpServletRequestBuilder getMockMvcRequestBuilder(String endpoint, String requestBody) {
            return MockMvcRequestBuilders.get(endpoint);
        }
    },
    DELETE {
        @Override
        public MockHttpServletRequestBuilder getMockMvcRequestBuilder(String endpoint, String requestBody) {
            return MockMvcRequestBuilders.delete(endpoint);
        }
    },
    POST {
        @Override
        public MockHttpServletRequestBuilder getMockMvcRequestBuilder(String endpoint, String requestBody) {
            return
                    MockMvcRequestBuilders.post(endpoint)
                            .contentType("application/json")
                            .content(requestBody);
        }
    },
    PATCH {
        @Override
        public MockHttpServletRequestBuilder getMockMvcRequestBuilder(String endpoint, String requestBody) {
            return
                    MockMvcRequestBuilders.patch(endpoint)
                            .contentType("application/json")
                            .content(requestBody);
        }
    };

    public abstract MockHttpServletRequestBuilder getMockMvcRequestBuilder(String endpoint, String requestBody);

}
