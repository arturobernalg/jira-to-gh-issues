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

import java.util.Comparator;

/**
 * How the fix versions of an issue are ordered to select the GitHub milestone
 * (the newest version) and the backport versions (all others).
 */
public enum VersionScheme {

	/**
	 * Spring ordering. "M1" and "RC1" versions are development versions that are
	 * dropped in favour of the GA version fixed by the same issue.
	 */
	SPRING(JiraFixVersion.comparator(), true),

	/**
	 * Release ordering for projects that publish their milestone, alpha, beta and RC
	 * versions, such as "5.4-alpha2" or "4.1 Beta1". No version is dropped.
	 */
	RELEASE(JiraFixVersion.releaseComparator(), false);


	private final Comparator<JiraFixVersion> comparator;

	private final boolean dropDevelopmentVersions;


	VersionScheme(Comparator<JiraFixVersion> comparator, boolean dropDevelopmentVersions) {
		this.comparator = comparator;
		this.dropDevelopmentVersions = dropDevelopmentVersions;
	}

	/**
	 * Comparator that sorts the newest version first.
	 */
	public Comparator<JiraFixVersion> getComparator() {
		return comparator;
	}

	public boolean isDropDevelopmentVersions() {
		return dropDevelopmentVersions;
	}

}
