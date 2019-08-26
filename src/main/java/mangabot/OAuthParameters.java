package mangabot;

class OAuthParameters {
    protected OAuthParameters() {
    }

    /**
     * Reddit OAuth2 authorization request parameters
     */

    protected static final String AUTHORIZATION_URL = "https://www.reddit.com/api/v1/access_token";
    protected static final String CLIENT_ID = "M__BVyYV4vPzWw";
    protected static final String RESPONSE_TYPE = "code";
    protected static final String REDIRECT_URI = "http://www.example.com/unused/redirect/uri";
    protected static final String DURATION = "permanent";
    protected static final String SCOPE = "identity,read";
    protected static final String APP_USER_AGENT = "phamngtuananh";
}