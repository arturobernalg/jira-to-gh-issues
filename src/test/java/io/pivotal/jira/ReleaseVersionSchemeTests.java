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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ReleaseVersionSchemeTests {

	/** All version names of HTTPCORE, HTTPCLIENT and HTTPASYNC on 2026-10-25. */
	private static final List<String> HTTP_COMPONENTS_VERSIONS = Arrays.asList(
			"4.0-alpha1", "4.0-alpha2", "4.0-alpha3", "4.0-alpha4", "4.0-alpha5", "4.0-alpha6", "4.0-beta1",
			"4.0-beta2", "4.0-beta3", "4.0", "4.0.1", "4.1-alpha1", "4.1-beta1", "4.1-beta2", "4.1", "4.1.1", "4.1.2",
			"4.1.3", "4.1.4", "4.2-alpha1", "4.2-alpha2", "4.2-beta1", "4.2", "4.2.1", "4.2.2", "4.2.3", "4.2.4",
			"4.2.5", "4.3-alpha1", "4.3-beta1", "4.3-beta2", "4.3", "4.3.1", "4.3.2", "4.3.3", "4.4-alpha1",
			"4.4-beta1", "4.4", "4.4.1", "4.4.3", "4.4.4", "4.4.5", "4.4.6", "4.4.7", "4.4.8", "4.4.9", "4.4.10",
			"4.4.11", "4.4.12", "4.4.13", "4.4.14", "4.4.15", "4.4.16", "5.0.4", "5.0-alpha1", "5.0-alpha2",
			"5.0-alpha3", "5.0-alpha4", "5.0-beta1", "5.0-beta2", "5.0-beta3", "5.0-beta5", "5.0-beta6", "5.0-beta7",
			"5.0-beta8", "5.0-beta9", "5.0-beta10", "5.0-beta11", "5.0", "5.0.1", "5.0.2", "5.0.3", "5.1.1", "5.1.2",
			"5.1.3", "5.1.4", "5.1.5", "5.1-beta1", "5.1-beta2", "5.1-beta3", "5.1", "5.2-alpha1", "5.2-alpha2",
			"5.2-beta1", "5.2-beta2", "5.2", "5.2.1", "5.2.2", "5.2.3", "5.2.4", "5.2.5", "5.3-alpha1", "5.3-alpha2",
			"5.3-beta1", "5.3", "5.3.1", "5.3.2", "5.3.3", "5.3.4", "5.3.5", "5.3.6", "5.4-alpha1", "5.4", "5.4.1",
			"5.4.2", "5.4.3", "5.4.4", "5.4.5", "5.5-alpha1", "5.5-beta2", "5.5-beta3", "5.5-beta4", "5.5-beta5",
			"Future", "Stuck", "1.0 Alpha", "1.0 Beta 1", "1.0 Beta 2", "1.0 Final", "1.2 Final", "2.0 Milestone 1",
			"2.0 Milestone 2", "2.0 Alpha 1", "2.0 Alpha 2", "2.0 Alpha 3", "2.0 Beta 1", "2.0 Beta 2", "2.0 Final",
			"2.0.1", "2.0.2", "2.0.3", "3.0 Alpha 1", "3.0 Alpha 2", "3.0 Beta 1", "3.0 RC1", "3.0 RC2", "3.0 RC3",
			"3.0 RC4", "3.0 Final", "3.0.1", "3.1 Alpha 1", "3.1 Beta 1", "3.1 RC1", "4.0 Alpha 1",
			"3.1 (end of life)", "4.0 Alpha 2", "4.0 Alpha 3", "4.0 Alpha 4", "3.1.1", "4.0 Beta 1", "4.0 Beta 2",
			"4.0 Final", "4.0.2", "4.0.3", "4.1 Alpha1", "4.1 Alpha2", "4.1 Beta1", "4.1 Final", "4.2 Alpha1",
			"4.2 Beta1", "4.2 Final", "4.2.6", "4.3 Alpha1", "4.3 Beta1", "4.3 Beta2", "4.3 Final", "4.3.4", "4.3.5",
			"4.3.5.1-android", "4.3.6", "4.4 Alpha1", "4.4 Beta1", "4.4 Final", "4.5", "4.5.1", "4.5.2", "4.5.3",
			"4.5.4", "4.5.5", "4.5.6", "4.5.7", "4.5.8", "4.5.9", "4.5.10", "4.5.11", "4.5.12", "4.5.13", "4.5.14",
			"4.5.15", "5.6.2", "5.6.3", "5.6.4", "5.6.5", "5.6.6", "5.0 Alpha1", "5.0 Alpha2", "5.0 Alpha3",
			"5.0 Beta1", "5.0 Beta2", "5.0 Beta3", "5.0 Beta4", "5.0 Beta5", "5.0 Beta6", "5.0 Beta7", "5.4-alpha2",
			"5.4-beta1", "5.5", "5.5.1", "5.5.2", "5.6-alpha1", "5.6", "5.6.1", "5.7-alpha1", "5.7-alpha2",
			"4.0-beta4", "4.1.5", "4.1.6");

	@Test
	public void alphaAndBetaSortBeforeRelease() {
		assertSorted("5.0", "5.4-alpha2").as("5.4-alpha2", "5.0");
		assertSorted("4.1.4", "4.2-alpha1").as("4.2-alpha1", "4.1.4");
		assertSorted("5.0-beta1", "5.0-alpha4", "5.0", "5.0-beta10", "5.0-beta9")
				.as("5.0", "5.0-beta10", "5.0-beta9", "5.0-beta1", "5.0-alpha4");
		assertSorted("4.1 Alpha1", "4.1 Beta1", "4.1 Final", "4.0.3")
				.as("4.1 Final", "4.1 Beta1", "4.1 Alpha1", "4.0.3");
	}

	@Test
	public void commonsHttpClientVersions() {
		assertSorted("2.0 Milestone 1", "2.0 Alpha 1", "2.0 Beta 2", "2.0 Final", "1.0 Alpha")
				.as("2.0 Final", "2.0 Beta 2", "2.0 Alpha 1", "2.0 Milestone 1", "1.0 Alpha");
		assertSorted("3.0 RC4", "3.0 RC1", "3.0 Final", "3.1 (end of life)", "3.1 RC1")
				.as("3.1 (end of life)", "3.1 RC1", "3.0 Final", "3.0 RC4", "3.0 RC1");
	}

	@Test
	public void numericParts() {
		assertSorted("4.4.9", "4.4.10").as("4.4.10", "4.4.9");
		assertSorted("4.3.5", "4.3.5.1-android", "4.3.6").as("4.3.6", "4.3.5.1-android", "4.3.5");
		assertSorted("5.5", "5.5.0").as("5.5", "5.5.0");
	}

	@Test
	public void namesWithoutVersionSortLast() {
		assertSorted("Stuck", "5.6", "Future").as("5.6", "Future", "Stuck");
	}

	@Test
	public void allHttpComponentsVersionsHaveTotalOrder() {
		Comparator<JiraFixVersion> comparator = JiraFixVersion.releaseComparator();
		List<JiraFixVersion> versions = new ArrayList<>(HTTP_COMPONENTS_VERSIONS.stream().map(JiraFixVersion::new).toList());
		Collections.shuffle(versions);
		versions.sort(comparator);
		for (int i = 0; i < versions.size(); i++) {
			for (int j = i + 1; j < versions.size(); j++) {
				assertThat(comparator.compare(versions.get(i), versions.get(j)))
						.as(versions.get(i).getName() + " before " + versions.get(j).getName())
						.isNegative();
				assertThat(comparator.compare(versions.get(j), versions.get(i))).isPositive();
			}
		}
		assertThat(versions.get(0).getName()).isEqualTo("5.7-alpha2");
	}

	@Test
	public void releaseSchemeSelectsNewestAsFixVersion() {
		JiraIssue issue = issue("5.0", "5.4-alpha2");
		issue.initFixAndBackportVersions(Collections.emptyMap(), VersionScheme.RELEASE);
		assertThat(issue.getFixVersion().getName()).isEqualTo("5.4-alpha2");
		assertThat(issue.getBackportVersions()).extracting(JiraFixVersion::getName).containsExactly("5.0");
	}

	@Test
	public void releaseSchemeKeepsDevelopmentVersions() {
		JiraIssue issue = issue("3.0 RC1", "3.0 Final", "2.0.3");
		issue.initFixAndBackportVersions(Collections.emptyMap(), VersionScheme.RELEASE);
		assertThat(issue.getFixVersion().getName()).isEqualTo("3.0 Final");
		assertThat(issue.getBackportVersions()).extracting(JiraFixVersion::getName).containsExactly("3.0 RC1", "2.0.3");
	}

	@Test
	public void springSchemeIsUnchanged() {
		JiraIssue issue = issue("4.0 RC1", "4.0", "3.2.5");
		issue.initFixAndBackportVersions(Collections.emptyMap());
		assertThat(issue.getFixVersion().getName()).isEqualTo("4.0");
		assertThat(issue.getBackportVersions()).extracting(JiraFixVersion::getName).containsExactly("3.2.5");
	}

	private static JiraIssue issue(String... fixVersions) {
		JiraIssue.Fields fields = new JiraIssue.Fields();
		fields.setFixVersions(Arrays.stream(fixVersions).map(JiraFixVersion::new).toList());
		fields.setSubtasks(Collections.emptyList());
		JiraIssue issue = new JiraIssue();
		issue.setKey("HTTPCLIENT-1");
		issue.setFields(fields);
		return issue;
	}

	private static SortAssertion assertSorted(String... names) {
		return new SortAssertion(names);
	}

	private record SortAssertion(String[] names) {

		void as(String... expected) {
			List<JiraFixVersion> versions = new ArrayList<>(Arrays.stream(names).map(JiraFixVersion::new).toList());
			versions.sort(JiraFixVersion.releaseComparator());
			assertThat(versions).extracting(JiraFixVersion::getName).containsExactly(expected);
		}
	}

}
