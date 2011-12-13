/*
 * Copyright [2009] [Gerald de Jong]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.npcf.eav.web;

import junit.framework.Assert;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

import java.io.IOException;

/**
 * TODO: javadoc
 */


public class TestWebService {

    private static Logger logger = Logger.getLogger(TestWebService.class);
    private static WebAppContext webAppContext = new WebAppContext("eav-123/src/main/webapp", "/eav-123");
    private static Server server = new Server(8888);
    private static HttpClient httpClient = new HttpClient();
    private static String baseUrl = "http://localhost:8888/eav-123/";

    @BeforeClass
    public static void startUp() throws Exception {
        server.setHandler(webAppContext);
        server.start();
        Thread.sleep(1000);
    }

    @AfterClass
    public static void shutDown() throws Exception {
        server.stop();
    }

    @Test
    public void getMeta() throws IOException {
        logger.info(doRequest("meta"));
    }

    @Test(expected = LocalException.class)
    public void presentImpossibleEntityA() throws IOException {
        doRequest("entity",
                "key=shouldfail",
                "enumeration=ONE"
        );
    }

    @Test(expected = LocalException.class)
    public void presentImpossibleEntityB() throws IOException {
        doRequest("entity",
                "key=shouldfail",
                "enumeration=ONE",
                "regex=2324-11"
        );
    }

    @Test(expected = LocalException.class)
    public void presentImpossibleEntityC() throws IOException {
        doRequest("entity",
                "key=shouldfail",
                "enumeration=ONE",
                "aggregate/rangecheck=15"
        );
    }

    @Test
    public void insertDelete() throws IOException {
        String key = "success";
        doRequest("entity",
                "key="+key,
                "enumeration=ONE",
                "aggregate/rangecheck=15",
                "regex=2324-11"
        );
        logger.info(doRequest("entity/"+key, "delete=true"));
        try {
            doRequest("entity/"+key);
        }
        catch (LocalException e) {
            logger.info("good, the one we deleted is gone");
        }
    }

    @Test
    public void deleteValue() throws IOException {
        String key = "success";
        doRequest("entity",
                "key="+key,
                "enumeration=ONE",
                "aggregate/rangecheck=15",
                "aggregate/lengthcheck=Goober",
                "regex=2324-11"
        );
        try {
            doRequest("entity/"+key+"/aggregate/rangecheck", "delete=true");
        }
        catch (LocalException e) {
            logger.info("good, couldn't delete");
        }
        logger.info("this should not include lengthcheck:\n"+doRequest("entity/"+key+"/aggregate/lengthcheck", "delete=true"));
        doRequest("entity/"+key, "delete=true");
    }

    private String doRequest(String path, String... params) {
        PostMethod getMethod = new PostMethod(baseUrl + path);
        NameValuePair[] pairs = new NameValuePair[params.length];
        for (int walk = 0; walk < params.length; walk++) {
            String param = params[walk];
            int equals = param.indexOf("=");
            Assert.assertTrue("must be an = sign", equals > 0);
            pairs[walk] = new NameValuePair(
                    param.substring(0, equals),
                    param.substring(equals + 1)
            );
        }
        getMethod.setQueryString(pairs);
        try {
            int httpStatus = httpClient.executeMethod(getMethod);
            if (httpStatus != HttpStatus.SC_OK) {
                throw new RuntimeException("httpstatus is " + httpStatus);
            }
            String responseBody = getMethod.getResponseBodyAsString();
            if (responseBody.indexOf("exception") > 0) {
                logger.error("exception!\n" + responseBody);
                throw new LocalException();
            }
            return responseBody;
        }
        catch (IOException e) {
            throw new RuntimeException("expected contact");
        }
    }

    private static class LocalException extends RuntimeException {
        private static final long serialVersionUID = -8668133386873622784L;
        private LocalException() {
        }
    }
}
