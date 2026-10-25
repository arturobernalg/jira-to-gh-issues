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
package io.pivotal.jira;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

public class JiraClientLoadCheckTests {

	@Test
	public void allPagesLoaded() {
		List<JiraIssue> issues = JiraClient.assertAllIssuesLoaded(Arrays.asList(
				page(1000, 500, 1500), page(0, 1000, 1500), page(2000, 0, 1500)));
		assertThat(issues).hasSize(1500);
	}

	@Test
	public void missingPageFails() {
		assertThatIllegalStateException()
				.isThrownBy(() -> JiraClient.assertAllIssuesLoaded(Arrays.asList(page(0, 1000, 1500))))
				.withMessage("Loaded 1000 issues but Jira reports 1500");
	}

	@Test
	public void missingFirstPageFails() {
		assertThatIllegalStateException()
				.isThrownBy(() -> JiraClient.assertAllIssuesLoaded(Collections.emptyList()))
				.withMessage("First page of Jira search results could not be loaded");
	}

	@Test
	public void truncatedCommentsFail() {
		JiraCommentPage comments = new JiraCommentPage();
		comments.setComments(Collections.singletonList(new JiraComment()));
		comments.setTotal(2);
		JiraIssue.Fields fields = new JiraIssue.Fields();
		fields.setComment(comments);
		JiraIssue issue = new JiraIssue();
		issue.setKey("HTTPCORE-1");
		issue.setFields(fields);
		assertThatIllegalStateException()
				.isThrownBy(() -> JiraClient.assertAllCommentsLoaded(issue))
				.withMessage("Loaded 1 of 2 comments for HTTPCORE-1");
		comments.setTotal(1);
		JiraClient.assertAllCommentsLoaded(issue);
	}

	private static JiraSearchResult page(long startAt, int size, long total) {
		JiraSearchResult result = new JiraSearchResult();
		result.setStartAt(startAt);
		result.setMaxResults(1000);
		result.setTotal(total);
		result.setIssues(Collections.nCopies(size, new JiraIssue()));
		return result;
	}

}
