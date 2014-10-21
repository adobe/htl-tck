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
package io.sightly.tck.html;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HTMLExtractor {

    private static Map<String, Document> documents = new ConcurrentHashMap<String, Document>();

    /**
     * Retrieves the content of an element, without its own markup tags, identified by the {@code selector} from the given {@code markup}.
     * The {@code url} is used only for caching purposes, to avoid parsing multiple times the markup returned for the same resource.
     *
     * @param url      the url that identifies the markup
     * @param markup   the markup
     * @param selector the selector used for retrieval
     * @return the contents of the selected element
     */
    public static String innerHTML(String url, String markup, String selector) {
        ensureMarkup(url, markup);
        Document document = documents.get(url);
        Elements elements = document.select(selector);
        return elements.html();
    }

    /**
     * Checks if the element from the {@code markup} identified by the {@code selector} contains the text from {@code value}. The
     * {@code url} is used only for caching purposes, to avoid parsing multiple times the markup returned for the same resource.
     *
     * @param url      the url that identifies the markup
     * @param markup   the markup
     * @param selector the selector used for retrieval
     * @param value    the text that should exist in the markup
     * @return {@code true} if the {@code value} was found in the markup, {@code false} otherwise
     */
    public static boolean contains(String url, String markup, String selector, String value) {
        ensureMarkup(url, markup);
        Document document = documents.get(url);
        Elements elements = document.select(selector);
        return elements.outerHtml().contains(value);
    }

    /**
     * Checks if the {@code selector} identifies an element from the {@code markup}. The {@code url} is used only for caching purposes,
     * to avoid parsing multiple times the markup returned for the same resource.
     *
     * @param url      the url that identifies the markup
     * @param markup   the markup
     * @param selector the selector used for retrieval
     * @return {@code true} if the element identified by the {@code selector} exists, {@code false} otherwise
     */
    public static boolean exists(String url, String markup, String selector) {
        ensureMarkup(url, markup);
        Document document = documents.get(url);
        Elements elements = document.select(selector);
        return elements.size() > 0;
    }

    /**
     * Checks if an element matched by the {@code selector} contains or not the attribute {@code attributeName},
     * depending on the value of the {@code exists} flag. Additionally, the attribute's value can be checked against {@code attributeValue}.
     *
     * @param url            the url that identifies the markup
     * @param markup         the markup
     * @param selector       the selector used for retrieval
     * @param exists         flag that defines if the attribute is expected to exist or not
     * @param attributeName  the attribute's name
     * @param attributeValue the attribute's value
     * @return {@code true} if the attribute matches the defined conditions, {@code false} otherwise
     */
    public static boolean hasAttribute(String url, String markup, String selector, boolean exists, String attributeName,
                                       String attributeValue) {
        ensureMarkup(url, markup);
        Document document = documents.get(url);
        Elements elements = document.select(selector);
        if (elements.size() > 0) {
            if (exists) {
                if (StringUtils.isNotEmpty(attributeValue)) {
                    String value = elements.attr(attributeName);
                    return attributeValue.equals(value);
                }
                return true;
            } else {
                return elements.hasAttr(attributeName);
            }
        }
        return false;
    }

    /**
     * Checks if the element matched by the {@code selector} has children and if their number is equal to {@code howMany}.
     *
     * @param url      the url that identifies the markup
     * @param markup   the markup
     * @param selector the selector used for retrieval
     * @param howMany  the number of expected children
     * @return {@code true} if the number of children is equal to {@code howMany}, {@code false} otherwise
     */
    public static boolean hasChildren(String url, String markup, String selector, int howMany) {
        ensureMarkup(url, markup);
        Document document = documents.get(url);
        Element element = document.select(selector).first();
        if (element == null) {
            return false;
        }
        return element.children().size() == howMany;

    }

    private static void ensureMarkup(String url, String markup) {
        if (!documents.containsKey(url)) {
            documents.put(url, Jsoup.parse(markup));
        }
    }
}
