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
package io.sightly.tck.http;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.IOUtils;

/**
 * Basic wrapper on top of the Apache HTTP Client.
 */
public class Client {


    public static final int CONNECTION_TIMEOUT = 5000;
    public static final int SO_TIMEOUT = 5000;

    private HttpClient client;

    /**
     * Creates a basic HTTP client.
     */
    public Client() {
        client = new HttpClient();
        client.getHttpConnectionManager().setParams(prepareDefaultClientParameters());
        DefaultHttpMethodRetryHandler retryHandler = new DefaultHttpMethodRetryHandler(3, true);
        client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, retryHandler);
    }

    /**
     * Creates a basic HTTP client capable of authenticating using Basic authentication.
     *
     * @param user     the user
     * @param password the password
     */
    public Client(String user, String password) {
        this();
        client.getParams().setAuthenticationPreemptive(true);
        Credentials credentials = new UsernamePasswordCredentials(user, password);
        client.getState().setCredentials(AuthScope.ANY, credentials);
    }

    /**
     * Retrieves the content available at {@code url} as a {@link String}. The server must respond with a status code equal to {@code
     * expectedStatusCode}, otherwise this method will throw a {@link ClientException};
     *
     * @param url                the URL from which to retrieve the content
     * @param expectedStatusCode the expected status code from the server
     * @return the content, as a {@link String}
     * @throws ClientException if the server's status code differs from the {@code expectedStatusCode} or if any other error is encountered
     */
    public String getStringContent(String url, int expectedStatusCode) {
        GetMethod method = new GetMethod(url);
        try {
            int statusCode = client.executeMethod(method);
            if (statusCode == expectedStatusCode) {
                InputStream is = method.getResponseBodyAsStream();
                return IOUtils.toString(is, "UTF-8");
            } else {
                throw new ClientException(String.format("Received status code %d, expected %d - url %s", statusCode, expectedStatusCode,
                        url));
            }
        } catch (IOException e) {
            throw new ClientException("Unable to complete request to " + url, e);
        }
    }


    private HttpConnectionManagerParams prepareDefaultClientParameters() {
        HttpConnectionManagerParams params = new HttpConnectionManagerParams();
        params.setSoTimeout(SO_TIMEOUT);
        params.setConnectionTimeout(CONNECTION_TIMEOUT);
        return params;
    }

}
