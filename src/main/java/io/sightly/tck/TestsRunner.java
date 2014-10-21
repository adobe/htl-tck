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
package io.sightly.tck;

import java.util.List;

import org.json.JSONObject;
import org.junit.runner.RunWith;
import org.junit.runners.AllTests;

import io.sightly.tck.tests.TestBuilder;
import junit.framework.TestCase;
import junit.framework.TestSuite;

@RunWith(AllTests.class)
/**
 * JUnit test runner which groups all the tests under a single {@link TestSuite}.
 */
public class TestsRunner {

    /**
     * Creates the test suite.
     *
     * @return a test suite
     */
    public static TestSuite suite() {
        List<JSONObject> testDefinitions = TCK.INSTANCE.getTestDefinitions();
        TestSuite suite = new TestSuite();
        for (JSONObject testDefinition : testDefinitions) {
            List<TestCase> testCases = TestBuilder.getTests(testDefinition);
            for (TestCase tc : testCases) {
                suite.addTest(tc);
            }
        }
        return suite;
    }

}
