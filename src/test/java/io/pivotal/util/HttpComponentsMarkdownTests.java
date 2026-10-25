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
package io.pivotal.util;

import java.util.Arrays;

import io.pivotal.jira.JiraConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HttpComponentsMarkdownTests {

	private MarkdownEngine engine;

	@BeforeEach
	public void setup() {
		engine = new MarkdownEngine();
		JiraConfig jiraConfig = new JiraConfig();
		jiraConfig.setBaseUrl("https://issues.apache.org/jira");
		jiraConfig.setProjectId("HTTPCLIENT");
		jiraConfig.setLinkProjectKeys(Arrays.asList("HTTPCLIENT", "HTTPCORE", "HTTPASYNC"));
		jiraConfig.setEscapeIssueReferences(true);
		engine.setJiraConfig(jiraConfig);
	}

	@Test
	public void issueReferencesAreEscaped() {
		assertThat(engine.convert("Fixed by #123 and (#45), see also #6")).isEqualTo("Fixed by `#123` and (`#45`), see also `#6`\n");
		assertThat(engine.convert("#7 at start")).isEqualTo("`#7` at start\n");
		assertThat(engine.convert("*see #8*")).isEqualTo("**see `#8`**\n");
	}

	@Test
	public void otherHashesAreNotEscaped() {
		assertThat(engine.convert("http://example.com/page#123")).isEqualTo("http://example.com/page#123\n");
		assertThat(engine.convert("Item#123 and C# and #abc")).isEqualTo("Item#123 and C# and #abc\n");
	}

	@Test
	public void numberedListsStillConvert() {
		assertThat(engine.convert("# first\n# second #2")).isEqualTo("1. first\n2. second `#2`\n\n");
	}

	@Test
	public void httpComponentsKeysAreLinked() {
		assertThat(engine.convert("See HTTPCLIENT-1234 and HTTPCORE-56."))
				.isEqualTo("See [HTTPCLIENT-1234](https://issues.apache.org/jira/browse/HTTPCLIENT-1234) and " +
						"[HTTPCORE-56](https://issues.apache.org/jira/browse/HTTPCORE-56).\n");
	}

	@Test
	public void springKeysAreNotLinked() {
		assertThat(engine.convert("SEC-1 and SPR-2")).isEqualTo("SEC-1 and SPR-2\n");
	}

	@Test
	public void mentionsAreStillEscaped() {
		assertThat(engine.convert("ping @olegk")).isEqualTo("ping `@olegk`\n");
	}

}
