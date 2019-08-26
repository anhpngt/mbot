package mangabot;

class OAuthError extends Error {
    public OAuthError(String message) {
        super(message);
    }

    public OAuthError(String message, Throwable cause) {
        super(message, cause);
    }

    public OAuthError(Throwable cause) {
        super(cause);
    }

    private static final long serialVersionUID = 1L;
}