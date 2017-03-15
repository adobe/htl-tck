/*******************************************************************************
 * Copyright 2014 Adobe Systems Incorporated
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package io.sightly.tck.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import io.sightly.tck.Constants;
import io.sightly.tck.html.HTMLExtractor;
import io.sightly.tck.http.Client;
import junit.framework.TestCase;

/**
 * The {@code TestBuilder} is responsible for creating JUnit tests from JSON test descriptions.
 */
public class TestBuilder {

    public static final String JSON_SUITE = "suite";
    public static final String JSON_URL = "url";
    public static final String JSON_EXPECTED_MARKUP = "expectedMarkup";
    public static final String JSON_METHOD = "method";
    public static final String JSON_GROUPS = "groups";
    public static final String JSON_GROUP_NAME = "name";
    public static final String JSON_GROUP_CASES = "cases";
    public static final String JSON_CASE_SELECTOR = "selector";
    public static final String JSON_CASE_VALUE = "value";
    public static final String JSON_CASE_POSITIVE = "positive";
    public static final String JSON_CASE_ATTRIBUTE = "attribute";
    public static final String JSON_EXPECTED_STATUS_CODE = "expectedStatusCode";

    private static Map<String, String> content = new ConcurrentHashMap<String, String>();

    /**
     * Creates a JUnit tests from a JSON test description. Examples of such files can be found in the {@code
     * src/main/resources/testfiles/definitions} folder.
     *
     * @param testDescription a JSON object encapsulating a test description
     * @return the list of tests
     */
    public static List<TestCase> getTests(JSONObject testDescription) {
        String serverURL = System.getProperty(Constants.SYS_PROP_SERVER_URL);
        String user = System.getProperty(Constants.SYS_PROP_USER);
        String password = System.getProperty(Constants.SYS_PROP_PASS);
        Client client;
        if (StringUtils.isNotEmpty(user) && StringUtils.isNotEmpty(password)) {
            client = new Client(user, password);
        } else {
            client = new Client();
        }
        ArrayList<TestCase> tests = new ArrayList<TestCase>();
        if (testDescription != null) {
            int expectedStatusCode = 200;
            String expectedMarkupPath = null;
            if (testDescription.has(JSON_EXPECTED_MARKUP)) {
                expectedMarkupPath = testDescription.getString(JSON_EXPECTED_MARKUP);
            }
            String suite = testDescription.getString(JSON_SUITE);
            String suiteUrl = null;
            if (testDescription.has(JSON_URL)) {
                suiteUrl = testDescription.getString(JSON_URL);
            }
            String suiteMethod = null;
            if (testDescription.has(JSON_METHOD)) {
                suiteMethod = testDescription.getString(JSON_METHOD);
            }
            JSONArray groups = testDescription.getJSONArray(JSON_GROUPS);
            if (testDescription.has(JSON_EXPECTED_STATUS_CODE)) {
                expectedStatusCode = testDescription.getInt(JSON_EXPECTED_STATUS_CODE);
            }
            for (int i = 0; i < groups.length(); i++) {
                JSONObject group = groups.getJSONObject(i);
                String groupName = group.getString(JSON_GROUP_NAME);
                JSONArray groupCases = group.getJSONArray(JSON_GROUP_CASES);
                if (group.has(JSON_EXPECTED_MARKUP)) {
                    expectedMarkupPath = group.getString(JSON_EXPECTED_MARKUP);
                }
                String groupMethod = null;
                if (group.has(JSON_METHOD)) {
                    groupMethod = group.getString(JSON_METHOD);
                }
                String groupURL = null;
                if (group.has(JSON_URL)) {
                    groupURL = group.getString(JSON_URL);
                }
                if (group.has(JSON_EXPECTED_STATUS_CODE)) {
                    expectedStatusCode = group.getInt(JSON_EXPECTED_STATUS_CODE);
                }
                String contentURL = serverURL + (StringUtils.isEmpty(groupURL) ? suiteUrl : groupURL);
                String method = StringUtils.isEmpty(groupMethod) ? suiteMethod : groupMethod;
                for (int j = 0; j < groupCases.length(); j++) {
                    JSONObject testCase = groupCases.getJSONObject(j);
                    tests.add(buildTestCase(client, expectedStatusCode, contentURL, suite, groupName, method, testCase,
                            expectedMarkupPath));
                }
            }
        }
        return tests;
    }

    private static TestCase buildTestCase(final Client client, final int expectedStatusCode, final String url, final String suite,
                                          final String groupName, final String method, final JSONObject testCase,
                                          final String expectedMarkupPath) {

        final String selector = testCase.getString(JSON_CASE_SELECTOR);

        return new TestCase() {
            @Override
            public String getName() {
                return suite + ": " + groupName + " - " + selector;
            }

            @Override
            protected void runTest() throws Throwable {
                if (!content.containsKey(url)) {
                    content.put(url, client.getStringContent(url, expectedStatusCode));
                }
                String overriddenMethod = method;
                if (testCase.has(JSON_METHOD)) {
                    overriddenMethod = testCase.getString(JSON_METHOD);
                }
                String output = content.get(url);
                if ("innerHTMLEquals".equals(overriddenMethod)) {
                    String value = HTMLExtractor.innerHTML(url, output, selector);
                    String expectedValue = testCase.getString(JSON_CASE_VALUE);
                    assertTrue(String.format("Expected to find an element matching selector '%s'. Please check the expected markup " +
                            "from %s.", selector, expectedMarkupPath), HTMLExtractor.exists(url, output, selector));
                    assertTrue(String.format(
                            "Expected value '%s' for selector '%s'. Instead we got '%s'. Please check the expected markup from %s" +
                                    ".", expectedValue, selector, value, expectedMarkupPath), expectedValue.equals(value));
                } else if ("contains".equals(overriddenMethod)) {
                    String expectedValue = testCase.getString(JSON_CASE_VALUE);
                    boolean contains = HTMLExtractor.contains(url, output, selector, expectedValue);
                    assertTrue(String.format("Expected to find an element matching selector '%s'. Please check the expected markup " +
                            "from %s.", selector, expectedMarkupPath), HTMLExtractor.exists(url, output, selector));
                    assertTrue(String.format("Missing content for selector '%s'. Please check the expected markup from %s.", selector,
                            expectedMarkupPath), contains);
                } else if ("exists".equals(overriddenMethod)) {
                    boolean exists = true;
                    if (testCase.has(JSON_CASE_POSITIVE)) {
                        exists = testCase.getBoolean(JSON_CASE_POSITIVE);
                    }
                    if (exists) {
                        assertTrue(String.format("Expected to find an element matching selector '%s'. Please check the expected markup " +
                                "from %s.", selector, expectedMarkupPath), HTMLExtractor.exists(url, output, selector));
                    } else {
                        assertFalse(String.format("Did not expect to find an element matching selector '%s'. Please check the expected " +
                                "markup from " +
                                "%s.", selector, expectedMarkupPath), HTMLExtractor.exists(url, output, selector));
                    }
                } else if ("hasAttribute".equals(overriddenMethod)) {
                    String attributeName = testCase.getString(JSON_CASE_ATTRIBUTE);
                    boolean exists = true;
                    if (testCase.has(JSON_CASE_POSITIVE)) {
                        exists = testCase.getBoolean(JSON_CASE_POSITIVE);
                    }
                    assertTrue(String.format("Expected to find an element matching selector '%s'. Please check the expected markup " +
                            "from %s.", selector, expectedMarkupPath), HTMLExtractor.exists(url, output, selector));
                    if (exists) {
                        assertTrue(String.format("Cannot find attribute '%s' on element matching selector '%s'. Please check the expected" +
                                        " markup from %s.", attributeName, selector, expectedMarkupPath),
                                HTMLExtractor.hasAttribute(url, output, selector, attributeName));
                    } else {
                        assertFalse(String.format("Did not expect to find attribute '%s' on element matching selector '%s'. Please check " +
                                        "the expected markup from %s.", attributeName, selector, expectedMarkupPath),
                                HTMLExtractor.hasAttribute(url, output, selector, attributeName));
                    }
                } else if ("hasAttributeValue".equals(overriddenMethod)) {
                    String attributeName = testCase.getString(JSON_CASE_ATTRIBUTE);
                    String attributeValue = testCase.getString(JSON_CASE_VALUE);
                    boolean positive = true;
                    if (testCase.has(JSON_CASE_POSITIVE)) {
                        positive = testCase.getBoolean(JSON_CASE_POSITIVE);
                    }
                    assertTrue(String.format("Expected to find an element matching selector '%s'. Please check the expected markup " +
                            "from %s.", selector, expectedMarkupPath), HTMLExtractor.exists(url, output, selector));
                    if (positive) {
                        assertTrue(String.format("Cannot find attribute '%s' on element matching selector '%s'. Please check the expected" +
                                        " markup from %s.", attributeName, selector, expectedMarkupPath),
                                HTMLExtractor.hasAttribute(url, output, selector, attributeName));
                        assertTrue(String.format("Cannot find attribute '%s' on element matching selector '%s' with value '%s'. Please " +
                                        "check the expected markup from %s.", attributeName,
                                selector, attributeValue, expectedMarkupPath),
                                HTMLExtractor.hasAttributeValue(url, output, selector, attributeName, attributeValue));
                    } else {
                        assertFalse(String.format("Did not expect to find attribute '%s' on element matching selector '%s'. Please check " +
                                        "the expected markup from %s.", attributeName, selector, expectedMarkupPath),
                                HTMLExtractor.hasAttribute(url, output, selector, attributeName));
                        assertFalse(String.format("Did not expect to find attribute '%s' on element matching selector '%s' with value " +
                                        "'%s'. Please check the expected markup from %s.", attributeName, selector, attributeValue,
                                expectedMarkupPath),
                                HTMLExtractor.hasAttributeValue(url, output, selector, attributeName, attributeValue));
                    }
                } else if ("hasChildren".equals(overriddenMethod)) {
                    assertTrue(String.format("Expected to find an element matching selector '%s'. Please check the expected markup " +
                            "from %s.", selector, expectedMarkupPath), HTMLExtractor.exists(url, output, selector));
                    int expectedChildren = testCase.optInt(JSON_CASE_VALUE);
                    assertTrue(String.format("Element matched by selector '%s' was expected to have %d children. Please check the " +
                                    "expected markup from %s.", selector, expectedChildren, expectedMarkupPath),
                            HTMLExtractor.hasChildren(url, output, selector, expectedChildren));
                } else {
                    fail("Unknown test method: " + overriddenMethod);
                }
            }
        };
    }


}
