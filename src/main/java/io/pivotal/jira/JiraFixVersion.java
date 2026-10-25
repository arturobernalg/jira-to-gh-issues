/*
 * Copyright 2002-2016 the original author or authors.
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
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Rob Winch
 *
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class JiraFixVersion {
	String name;

	public boolean isBeforeGA() {
		return name.matches(".*RC[1-9]") || name.matches(".*M[1-9]");
	}


	public static Comparator<JiraFixVersion> comparator() {
		return new JiraFixVersionComparator();
	}

	/**
	 * Comparator for {@link VersionScheme#RELEASE}: newest first, ordering
	 * milestone &lt; alpha &lt; beta &lt; RC &lt; final within the same numeric version.
	 * Names without a leading number (e.g. "Future") sort last.
	 */
	public static Comparator<JiraFixVersion> releaseComparator() {
		return new ReleaseVersionComparator();
	}


	private static class JiraFixVersionComparator implements Comparator<JiraFixVersion> {
		static final String PARTS_EXPRESSION = "[. ]";

		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(JiraFixVersion lhs, JiraFixVersion rhs) {
			String[] lhsParts = lhs.getName().split(PARTS_EXPRESSION);
			String[] rhsParts = rhs.getName().split(PARTS_EXPRESSION);

			for(int i=0;i<Math.max(lhsParts.length, rhsParts.length);i++) {
				int part = comparePart(i, lhsParts, rhsParts);
				if(part != 0) {
					return -1 * part;
				}
			}
			return 0;
		}

		private int comparePart(int index, String[] lhs, String[] rhs) {
			return getPart(lhs,index) - getPart(rhs,index);
		}

		private int getPart(String[] parts,int index) {
			if(index >= parts.length) {
				return 0;
			}
			if (parts[index].equals("GA")) {
				return 0;
			}
			String toParse = parts[index];
			try {
				return Integer.parseInt(toParse);
			} catch(NumberFormatException e) {
				return toNumber(toParse);
			}
		}

		private int toNumber(String str) {
			int n = Integer.MIN_VALUE;
			for(int i=0;i<str.length();i++) {
				n += str.charAt(i);
			}
			return n;
		}
	}

	private static class ReleaseVersionComparator implements Comparator<JiraFixVersion> {

		private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d{1,9}(?:\\.\\d{1,9})*)(.*)");

		private static final Pattern TRAILING_NUMBER_PATTERN = Pattern.compile("(\\d{1,9})\\s*$");

		private static final int RANK_FINAL = 4;

		@Override
		public int compare(JiraFixVersion lhs, JiraFixVersion rhs) {
			int result = compareNewestFirst(lhs.getName(), rhs.getName());
			return result != 0 ? result : lhs.getName().compareTo(rhs.getName());
		}

		private static int compareNewestFirst(String lhs, String rhs) {
			Matcher lhsMatcher = VERSION_PATTERN.matcher(lhs);
			Matcher rhsMatcher = VERSION_PATTERN.matcher(rhs);
			boolean lhsVersion = lhsMatcher.matches();
			boolean rhsVersion = rhsMatcher.matches();
			if (!lhsVersion || !rhsVersion) {
				return Boolean.compare(rhsVersion, lhsVersion);
			}
			String[] lhsNumbers = lhsMatcher.group(1).split("\\.");
			String[] rhsNumbers = rhsMatcher.group(1).split("\\.");
			for (int i = 0; i < Math.max(lhsNumbers.length, rhsNumbers.length); i++) {
				int result = Integer.compare(number(rhsNumbers, i), number(lhsNumbers, i));
				if (result != 0) {
					return result;
				}
			}
			String lhsQualifier = qualifier(lhsMatcher.group(2));
			String rhsQualifier = qualifier(rhsMatcher.group(2));
			int lhsRank = rank(lhsQualifier);
			int rhsRank = rank(rhsQualifier);
			if (lhsRank != rhsRank) {
				return Integer.compare(rhsRank, lhsRank);
			}
			if (lhsRank == RANK_FINAL) {
				return 0;
			}
			return Integer.compare(trailingNumber(rhsQualifier), trailingNumber(lhsQualifier));
		}

		private static int number(String[] numbers, int index) {
			return index < numbers.length ? Integer.parseInt(numbers[index]) : 0;
		}

		private static String qualifier(String text) {
			return text.replaceFirst("^[\\s.\\-]+", "").toLowerCase(Locale.ROOT);
		}

		private static int rank(String qualifier) {
			if (qualifier.startsWith("milestone") || qualifier.matches("m\\d*")) {
				return 0;
			}
			if (qualifier.startsWith("alpha")) {
				return 1;
			}
			if (qualifier.startsWith("beta")) {
				return 2;
			}
			if (qualifier.startsWith("rc") || qualifier.startsWith("cr")) {
				return 3;
			}
			// "", "final", "ga" and anything else, e.g. "(end of life)" or "android"
			return RANK_FINAL;
		}

		private static int trailingNumber(String qualifier) {
			Matcher matcher = TRAILING_NUMBER_PATTERN.matcher(qualifier);
			return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
		}
	}
}
