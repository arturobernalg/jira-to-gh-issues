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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.pivotal.github.GithubComment;
import io.pivotal.github.ImportGithubIssue;
import io.pivotal.jira.JiraComment;
import io.pivotal.jira.JiraIssue;

import org.springframework.context.annotation.Bean;

/**
 * Configuration shared by the Apache HttpComponents Jira projects
 * HTTPCLIENT, HTTPCORE and HTTPASYNC.
 */
public class CommonHttpComponentsMigrationConfig {

	/**
	 * Jira versions that are not releases. "Stuck" becomes a label instead.
	 */
	static final Set<String> SKIP_VERSIONS = Set.of("Future", "Stuck");

	/**
	 * Comment authors whose comments are not migrated. githubbot mirrors pull request
	 * comments that remain available on GitHub, hudson reports old CI builds.
	 * jira-bot comments link commits and are kept.
	 */
	static final Set<String> SKIP_COMMENT_AUTHORS = Set.of("githubbot", "hudson");


	@Bean
	public MilestoneFilter milestoneFilter() {
		return version -> !SKIP_VERSIONS.contains(version.getName());
	}

	@Bean
	public LabelHandler labelHandler() {
		FieldValueLabelHandler handler = new FieldValueLabelHandler();
		handler.addMapping(FieldValueLabelHandler.FieldType.ISSUE_TYPE, "Bug", "bug");
		handler.addMapping(FieldValueLabelHandler.FieldType.ISSUE_TYPE, "Improvement", "enhancement");
		handler.addMapping(FieldValueLabelHandler.FieldType.ISSUE_TYPE, "New Feature", "enhancement");
		handler.addMapping(FieldValueLabelHandler.FieldType.ISSUE_TYPE, "Wish", "enhancement");
		handler.addMapping(FieldValueLabelHandler.FieldType.ISSUE_TYPE, "Task", "maintenance");
		handler.addMapping(FieldValueLabelHandler.FieldType.ISSUE_TYPE, "Test", "test");

		handler.addMapping(FieldValueLabelHandler.FieldType.PRIORITY, "Blocker", "blocker");
		handler.addMapping(FieldValueLabelHandler.FieldType.PRIORITY, "Critical", "critical");

		// HTTPCLIENT
		handler.addMapping(FieldValueLabelHandler.FieldType.COMPONENT, "HttpClient (classic)", "classic");
		handler.addMapping(FieldValueLabelHandler.FieldType.COMPONENT, "HttpClient (async)", "async");
		handler.addMapping(FieldValueLabelHandler.FieldType.COMPONENT, "HttpClient (Windows)", "windows");
		handler.addMapping(FieldValueLabelHandler.FieldType.COMPONENT, "HttpCache", "cache");
		handler.addMapping(FieldValueLabelHandler.FieldType.COMPONENT, "Fluent HC", "fluent");
		handler.addMapping(FieldValueLabelHandler.FieldType.COMPONENT, "Android Port", "android");
		// HTTPCORE
		handler.addMapping(FieldValueLabelHandler.FieldType.COMPONENT, "HttpCore", "core");
		handler.addMapping(FieldValueLabelHandler.FieldType.COMPONENT, "HttpCore NIO", "nio");
		handler.addMapping(FieldValueLabelHandler.FieldType.COMPONENT, "Examples", "examples");
		// Both
		handler.addMapping(FieldValueLabelHandler.FieldType.COMPONENT, "Documentation", "documentation");
		handler.addMapping(FieldValueLabelHandler.FieldType.COMPONENT, "Site", "site");
		handler.addMapping(FieldValueLabelHandler.FieldType.COMPONENT, "Contrib", "contrib");

		// Same name and color from both sources, so only one label is created
		handler.addMapping(FieldValueLabelHandler.FieldType.VERSION, "Stuck", "stuck");
		handler.addMapping(FieldValueLabelHandler.FieldType.LABEL, "stuck", "stuck", LabelFactories.TYPE_LABEL);
		handler.addMapping(FieldValueLabelHandler.FieldType.LABEL, "volunteers-wanted", "help wanted",
				LabelFactories.TYPE_LABEL);

		CompositeLabelHandler composite = new CompositeLabelHandler();
		composite.addLabelHandler(handler);
		return composite;
	}

	@Bean
	public IssueProcessor issueProcessor() {
		return new CompositeIssueProcessor(new SkipCommentAuthorsIssueProcessor(SKIP_COMMENT_AUTHORS));
	}

	@Bean
	public JiraIssueFilter jiraIssueFilter() {
		return new CompositeJiraIssueFilter();
	}


	/**
	 * Removes the comments of the given Jira users. Converted comments are matched to
	 * Jira comments by position, as created by {@code MigrationClient#initComments}.
	 */
	static class SkipCommentAuthorsIssueProcessor implements IssueProcessor {

		private final Set<String> authors;

		SkipCommentAuthorsIssueProcessor(Set<String> authors) {
			this.authors = authors;
		}

		@Override
		public void beforeImport(JiraIssue jiraIssue, ImportGithubIssue importIssue) {
			List<JiraComment> jiraComments = jiraIssue.getFields().getComment().getVisibleComments();
			List<GithubComment> comments = importIssue.getComments();
			if (jiraComments.size() != comments.size()) {
				throw new IllegalStateException("Cannot match " + comments.size() + " converted comments to " +
						jiraComments.size() + " Jira comments for " + jiraIssue.getKey());
			}
			List<GithubComment> kept = new ArrayList<>(comments.size());
			for (int i = 0; i < comments.size(); i++) {
				if (!authors.contains(jiraComments.get(i).getAuthor().getKey())) {
					kept.add(comments.get(i));
				}
			}
			importIssue.setComments(kept);
		}
	}

}
