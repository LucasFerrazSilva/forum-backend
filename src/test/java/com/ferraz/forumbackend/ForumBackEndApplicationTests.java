package com.ferraz.forumbackend;

import com.ferraz.forumbackend.util.TestcontainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfig.class)
class ForumBackEndApplicationTests {

	@Test
	void contextLoads() {
	}

}
