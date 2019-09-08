package mangabot;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Poller {
    static final Logger logger = LoggerFactory.getLogger(Poller.class);

    // Client used to perform HTTP GET requests
    private CloseableHttpClient httpClient;

    // Handler for HTTP responses, returns JSONObject if the
    // request is successful, or null otherwise
    private ResponseHandler<JSONObject> responseHandler;

    // Customized User-Agent header following Reddit's API rule
    final private String USER_AGENT_HEADER = "java:mangabot:v0.1 by /u/phamngtuananh";

    public Poller() {
        this.httpClient = HttpClients.createDefault();
        logger.info("HTTP client successfully created.");

        this.responseHandler = new ResponseHandler<JSONObject>() {
            @Override
            public JSONObject handleResponse(final HttpResponse response) {
                StatusLine statusLine = response.getStatusLine();
                HttpEntity entity = response.getEntity();
                if (statusLine.getStatusCode() >= 300) {
                    logger.error("HTTP request fails with status code {}", statusLine.getStatusCode());
                    return null;
                }

                if (entity == null) {
                    logger.error("HTTP request fails (empty content).");
                    return null;
                }

                try {
                    String entityStr = EntityUtils.toString(entity);
                    logger.debug("Response body: {}", entityStr);
                    JSONParser parser = new JSONParser();
                    JSONObject jsonObj = (JSONObject) parser.parse(entityStr);
                    return jsonObj;
                } catch (Exception e) {
                    logger.error("Error while parsing JSON", e);
                    return null;
                }
            }
        };
        logger.info("Initialize successfully.");
    }

    public void poll() {
        HttpGet request = new HttpGet("https://www.reddit.com/r/manga.json");
        request.addHeader("User-Agent", USER_AGENT_HEADER);
        try {
            JSONObject jsonObj = this.httpClient.execute(request, this.responseHandler);
            if (jsonObj != null) {
                // parse and process the json
            }
        } catch (IOException ioe) {
            logger.error("Error occured while executing HTTP request", ioe);
        }
    }
}
