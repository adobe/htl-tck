/*******************************************************************************
 * Copyright 2017 Adobe Systems Incorporated
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
package io.sightly.tck.html;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class HTMLExtractorTest {

    @Test
    public void testHasAttribute() {
        assertTrue(HTMLExtractor.hasAttribute("hasAttribute-t01", "<div id='test' title></div>", "#test", "title"));
        assertTrue(HTMLExtractor.hasAttribute("hasAttribute-t02", "<div id='test' title='a title'></div>", "#test", "title"));
        assertTrue(HTMLExtractor.hasAttribute("hasAttribute-t03", "<div id='test' title=''></div>", "#test", "title"));
        assertFalse(HTMLExtractor.hasAttribute("hasAttribute-t04", "<div id='test'></div>", "#test", "title"));
    }

    @Test
    public void testHasAttributeValue() {

        class Combination {
            private String url;
            private String markup;
            private String selector;
            private String attributeName;
            private String attributeValue;
            private boolean expectedTrue;

            private Combination(String url,
                                String markup,
                                String selector,
                                String attributeName,
                                String attributeValue,
                                boolean expectedTrue) {
                this.url = url;
                this.markup = markup;
                this.selector = selector;
                this.attributeName = attributeName;
                this.attributeValue = attributeValue;
                this.expectedTrue = expectedTrue;
            }
        }

        Combination[] combinations = new Combination[]{
                new Combination("hasAttributeValue-t01", "<div id=\"test\" title=\"something\"></div>", "#test", "title", "", false),
                new Combination("hasAttributeValue-t01", "<div id=\"test\" title=\"something\"></div>", "#test", "title", "something", true),

                new Combination("hasAttributeValue-t02", "<div id=\"test\" title=\"\"></div>", "#test", "title", "", true),
                new Combination("hasAttributeValue-t02", "<div id=\"test\" title=\"\"></div>", "#test", "title", "something", false),

                new Combination("hasAttributeValue-t03", "<div id=\"test\" title></div>", "#test", "title", "", true),
                new Combination("hasAttributeValue-t03", "<div id=\"test\" title></div>", "#test", "title", "something", false),

                new Combination("hasAttributeValue-t04", "<div id=\"test\"></div>", "#test", "title", "", false),
                new Combination("hasAttributeValue-t04", "<div id=\"test\"></div>", "#test", "title", "something", false)
        };

        StringBuilder sb = new StringBuilder();
        int index = 0;
        for (Combination c : combinations) {
            String message =
                    String.format("%s: Expected %s when looking up a%s existing attribute named %s with value %s for selector %s in \n   " +
                                    " %s",
                            c.url,
                            c.expectedTrue,
                            c.expectedTrue ? "n" : " not",
                            "'" + c.attributeName + "'",
                            c.attributeValue == null ? null : "'" + c.attributeValue + "'",
                            c.selector,
                            c.markup);
            if (c.expectedTrue != HTMLExtractor.hasAttributeValue(c.url, c.markup, c.selector, c.attributeName, c.attributeValue)) {
                if (index++ == 0) {
                    sb.append("\n");
                }
                sb.append(message).append("\n");
            }
        }
        if (sb.length() > 0) {
            fail(sb.toString());
        }
    }
}

