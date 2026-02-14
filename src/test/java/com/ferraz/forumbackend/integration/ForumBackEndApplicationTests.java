package com.ferraz.forumbackend.integration;

import com.ferraz.forumbackend.integration.status.StatusService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ForumBackEndApplicationTests extends AbstractIntegrationTest {

	@Autowired
	StatusService statusService;

	@Test
	void contextLoads() {
		Assertions.assertThat(statusService).isNotNull();
	}

}
