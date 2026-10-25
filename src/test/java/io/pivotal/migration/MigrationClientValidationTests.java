/*
 * Copyright 2002-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.pivotal.migration;

import io.pivotal.github.GithubConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class MigrationClientValidationTests {

	private static final String REPO = "https://api.github.com/repos/arturobernalg/hc-core-issues-test";

	private GithubConfig config;

	private MigrationClient client;

	private MockRestServiceServer server;

	@BeforeEach
	public void setup() throws Exception {
		config = new GithubConfig();
		config.setRepositorySlug("arturobernalg/hc-core-issues-test");
		config.setAccessToken("test");
		client = new MigrationClient(config, null, version -> true, null, null, null);
		client.setUserMappingResource(new ByteArrayResource("olegk:ok2c\n".getBytes()));
		server = MockRestServiceServer.bindTo(client.getRest()).build();
	}

	@Test
	public void defaultsAreSafe() {
		GithubConfig defaults = new GithubConfig();
		assertThat(defaults.isLinkPullRequests()).isFalse();
		assertThat(defaults.isCreateBackportHolders()).isFalse();
		assertThat(defaults.isTestMode()).isFalse();
		defaults.setDryRun(true);
		assertThat(defaults.isTestMode()).isTrue();
	}

	@Test
	public void dryRunRejectsPublicRepository() {
		config.setDryRun(true);
		server.expect(requestTo(REPO)).andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess("{\"private\": false}", MediaType.APPLICATION_JSON));

		assertThatIllegalStateException().isThrownBy(() -> client.validateDryRunRepository())
				.withMessage("github.dry-run requires a private repository: arturobernalg/hc-core-issues-test");
		server.verify();
	}

	@Test
	public void dryRunAcceptsPrivateRepository() {
		config.setDryRun(true);
		server.expect(requestTo(REPO)).andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess("{\"private\": true}", MediaType.APPLICATION_JSON));

		client.validateDryRunRepository();
		server.verify();
	}

	@Test
	public void dryRunSkipsAssigneeCheck() {
		config.setDryRun(true);
		client.validateAssignees();
		server.verify();
	}

	@Test
	public void realRunRejectsUnassignableUser() {
		server.expect(requestTo(REPO + "/assignees/ok2c")).andExpect(method(HttpMethod.GET))
				.andRespond(withStatus(HttpStatus.NOT_FOUND));

		assertThatIllegalStateException().isThrownBy(() -> client.validateAssignees())
				.withMessageContaining("[ok2c]");
		server.verify();
	}

	@Test
	public void realRunAcceptsAssignableUser() {
		client.validateDryRunRepository();
		server.expect(requestTo(REPO + "/assignees/ok2c")).andExpect(method(HttpMethod.GET))
				.andRespond(withStatus(HttpStatus.NO_CONTENT));

		client.validateAssignees();
		server.verify();
	}

}
