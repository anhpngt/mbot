package mangabot;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
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

    /**
     * Poller class. At the moment, use {@link Poller#poll()} to fetch JSON from
     * Reddit.
     */
    public Poller() {
        this.httpClient = HttpClients.createDefault();

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

    /**
     * Fetch JSONs from Reddit (subreddit /r/manga) using HTTP requests.
     * 
     * @return the JSONObject object from Reddit. If the HTTP request fails, the
     *         method returns null.
     */
    public JSONObject poll() {
        try {
            URIBuilder uriBuilder = new URIBuilder("https://api.reddit.com/r/manga");
            return this.executeHttpRequest(uriBuilder.build());
        } catch (URISyntaxException exc) {
            logger.error("Error occurred in URI building process", exc);
            return null;
        }
    }

    public JSONObject searchRedditPost(String mangaName) {
        try {
            URIBuilder uriBuilder = new URIBuilder("https://api.reddit.com/search")
                    .addParameter("q", mangaName + " subreddit:manga flair:disc")
                    .addParameter("sort", "new")
                    .addParameter("t", "all");

            return this.executeHttpRequest(uriBuilder.build());
        } catch (URISyntaxException exc) {
            logger.error("Error occurred in URI building process", exc);
            return null;
        }

    }

    private JSONObject executeHttpRequest(URI uri) {
        try {
            logger.debug("Executing request from URI: {}", uri);
            HttpGet request = new HttpGet(uri);
            request.addHeader("User-Agent", USER_AGENT_HEADER);
            return this.httpClient.execute(request, this.responseHandler);
        } catch (IOException exc) {
            logger.error("Error occured while executing HTTP request", exc);
            return null;
        }
    }

    static public void main(String[] args) {
        Poller p = new Poller();
        logger.info("{}", p.searchRedditPost("shingeki no kyojin"));
    }
}
