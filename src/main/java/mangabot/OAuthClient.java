package mangabot;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

class OAuthClient extends OAuthParameters {
    private HttpClient httpClient;

    private String requestState;

    public OAuthClient() {
        this.httpClient = HttpClients.createDefault();
        System.out.println("OAuth client created");
    }

    public void getAuthorization() throws URISyntaxException {
        // Create HTTP request
        List<NameValuePair> uriParameters = new ArrayList<>(6);
        this.requestState = UUID.randomUUID().toString();
        uriParameters.add(new BasicNameValuePair("client_id", CLIENT_ID));
        uriParameters.add(new BasicNameValuePair("response_type", "code"));
        uriParameters.add(new BasicNameValuePair("state", this.requestState));
        uriParameters.add(new BasicNameValuePair("redirect_uri", REDIRECT_URI));
        uriParameters.add(new BasicNameValuePair("duration", "permanent"));
        uriParameters.add(new BasicNameValuePair("scope", SCOPE));

        URI uri = new URIBuilder().setScheme("https").setHost("www.reddit.com").setPath("/api/v1/authorize")
                .setParameters(uriParameters).build();
        HttpGet request = new HttpGet(uri);
        request.addHeader("User-Agent", APP_USER_AGENT);

        // Response
        try {
            HttpResponse response = this.httpClient.execute(request);
            handleBadRequest(response);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void handleBadRequest(final HttpResponse response) {
        if (response.getStatusLine() == null) {
            throw new OAuthError("HTTP request return null status line.");
        }

        StatusLine statusLine = response.getStatusLine();
        if (statusLine.getStatusCode() >= 300) {
            String errorMessage = String.format("HTTP request return code %d (%s)", statusLine.getStatusCode(),
                    statusLine.toString());
            throw new OAuthError("HTTP request returns " + errorMessage);
        }
    }
}