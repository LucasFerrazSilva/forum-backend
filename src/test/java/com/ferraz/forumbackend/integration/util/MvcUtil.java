package com.ferraz.forumbackend.integration.util;

import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

public class MvcUtil {

    public static MockHttpServletResponse post(MockMvc mvc, String endpoint, String requestBody) throws Exception {
        return  mvc.perform(
                MockMvcRequestBuilders.post(endpoint)
                        .contentType("application/json")
                        .content(requestBody)
        ).andReturn().getResponse();
    }

    public static MockHttpServletResponse get(MockMvc mvc, String endpoint) throws Exception {
        return  mvc.perform(MockMvcRequestBuilders.get(endpoint)).andReturn().getResponse();
    }

    public static MockHttpServletResponse patch(MockMvc mvc, String endpoint, String requestBody) throws Exception {
        return  mvc.perform(
                MockMvcRequestBuilders.patch(endpoint)
                        .contentType("application/json")
                        .content(requestBody)
        ).andReturn().getResponse();
    }

}
