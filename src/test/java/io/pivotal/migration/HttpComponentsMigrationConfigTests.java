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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.pivotal.github.GithubComment;
import io.pivotal.github.ImportGithubIssue;
import io.pivotal.jira.JiraComment;
import io.pivotal.jira.JiraCommentPage;
import io.pivotal.jira.JiraComponent;
import io.pivotal.jira.JiraFixVersion;
import io.pivotal.jira.JiraIssue;
import io.pivotal.jira.JiraIssueType;
import io.pivotal.jira.JiraPriority;
import io.pivotal.jira.JiraUser;
import io.pivotal.jira.JiraVersion;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

public class HttpComponentsMigrationConfigTests {

	private final CommonHttpComponentsMigrationConfig config = new CommonHttpComponentsMigrationConfig();

	@Test
	public void futureAndStuckAreNotMilestones() {
		MilestoneFilter filter = config.milestoneFilter();
		assertThat(filter.test(version("5.6.1"))).isTrue();
		assertThat(filter.test(version("Future"))).isFalse();
		assertThat(filter.test(version("Stuck"))).isFalse();
	}

	@Test
	public void labels() {
		JiraIssue issue = issue("Bug", "Blocker", "HttpClient (async)", "Stuck", "stuck", "volunteers-wanted", "ssl");
		assertThat(config.labelHandler().getLabelsFor(issue))
				.containsExactlyInAnyOrder("bug", "in:async", "stuck", "help wanted", "priority:blocker");

		JiraIssue minor = issue("Improvement", "Major", "HttpCore NIO", "5.4", "patch");
		assertThat(config.labelHandler().getLabelsFor(minor)).containsExactlyInAnyOrder("enhancement", "in:nio");
	}

	@Test
	public void labelNamesAreUnique() {
		Set<Map<String, String>> labels = config.labelHandler().getAllLabels();
		List<String> names = labels.stream().map(label -> label.get("name")).toList();
		assertThat(names).doesNotHaveDuplicates();
		assertThat(names).contains("stuck", "help wanted", "bug", "enhancement");
	}

	@Test
	public void botCommentsAreRemoved() {
		JiraIssue issue = issue("Bug", "Major", "HttpCore", "5.4", "patch");
		issue.getFields().getComment().setComments(Arrays.asList(
				comment("olegk"), comment("githubbot"), comment("jira-bot"), comment("hudson"), comment("abernal")));
		ImportGithubIssue importIssue = new ImportGithubIssue();
		importIssue.setComments(issue.getFields().getComment().getComments().stream()
				.map(c -> githubComment(c.getAuthor().getKey())).collect(Collectors.toList()));

		config.issueProcessor().beforeImport(issue, importIssue);

		assertThat(importIssue.getComments()).extracting(GithubComment::getBody)
				.containsExactly("olegk", "jira-bot", "abernal");
	}

	@Test
	public void commentMismatchFails() {
		JiraIssue issue = issue("Bug", "Major", "HttpCore", "5.4", "patch");
		issue.getFields().getComment().setComments(Arrays.asList(comment("olegk"), comment("githubbot")));
		ImportGithubIssue importIssue = new ImportGithubIssue();
		importIssue.setComments(Collections.singletonList(githubComment("olegk")));

		assertThatIllegalStateException().isThrownBy(() -> config.issueProcessor().beforeImport(issue, importIssue));
	}

	private static JiraVersion version(String name) {
		JiraVersion version = new JiraVersion();
		version.setName(name);
		return version;
	}

	private static JiraIssue issue(String type, String priority, String component, String fixVersion, String... labels) {
		JiraIssueType issueType = new JiraIssueType();
		issueType.setName(type);
		JiraPriority jiraPriority = new JiraPriority();
		jiraPriority.setName(priority);
		JiraComponent jiraComponent = new JiraComponent();
		jiraComponent.setName(component);
		JiraCommentPage comments = new JiraCommentPage();
		comments.setComments(Collections.emptyList());
		JiraIssue.Fields fields = new JiraIssue.Fields();
		fields.setIssuetype(issueType);
		fields.setPriority(jiraPriority);
		fields.setComponents(Collections.singletonList(jiraComponent));
		fields.setFixVersions(Collections.singletonList(new JiraFixVersion(fixVersion)));
		fields.setSubtasks(Collections.emptyList());
		fields.setLabels(Arrays.asList(labels));
		fields.setComment(comments);
		JiraIssue issue = new JiraIssue();
		issue.setKey("HTTPCLIENT-1");
		issue.setFields(fields);
		issue.initFixAndBackportVersions();
		return issue;
	}

	private static JiraComment comment(String authorKey) {
		JiraUser author = new JiraUser();
		author.setKey(authorKey);
		JiraComment comment = new JiraComment();
		comment.setAuthor(author);
		comment.setBody(authorKey);
		return comment;
	}

	private static GithubComment githubComment(String body) {
		GithubComment comment = new GithubComment();
		comment.setBody(body);
		return comment;
	}

}
