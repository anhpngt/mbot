package mangabot;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Database {
    static final Logger logger = LoggerFactory.getLogger(Database.class);

    private String databaseUrl;

    /**
     * Class constructor.
     * 
     * @param databaseUrl the database url that will be passed to JDBC
     */
    public Database(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    /**
     * Initializes all tables in the database when the application is first run.
     * 
     * @throws SQLException
     */
    public void createDatabase() throws SQLException {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS bookmarks(\n"
                    + "manga_id             INTEGER PRIMARY KEY NOT NULL,\n"
                    + "manga_name           TEXT UNIQUE NOT NULL,\n"
                    + "manga_alternate_name TEXT,\n"
                    + "resp_image_uri       TEXT,\n"
                    + "added_on             INTEGER NOT NULL DEFAULT (strftime('%s', 'now'))\n"
                    + ")");
            logger.debug("Database table bookmarks created.");

            stmt.execute("CREATE TABLE IF NOT EXISTS trackings(\n"
                    + "manga_id         INTEGER NOT NULL,\n"
                    + "chapter_number   TEXT NOT NULL,\n"
                    + "source_url       TEXT NOT NULL,\n"
                    + "timestamp        INTEGER NOT NULL,\n"
                    + "FOREIGN KEY(manga_id) REFERENCES bookmarks(manga_id)\n"
                    + ")");
            logger.debug("Database table trackings created.");
            logger.info("Successfully initialize database.");
        }
    }

    /**
     * Adds a new tracking manga entry to the database.
     * 
     * @param mangaName     name of the manga which is used in tracking
     * @param alternateName alternative name of the manga
     * @param respImageUri  URI (a path-like) to the cover image of the manga
     * @throws SQLException due to database connection or SQL execution error
     */
    public void addManga(String mangaName, String alternateName, String respImageUri) throws SQLException {
        logger.info("Adding new entry to <bookmarks> table ({}).", mangaName);
        logger.debug("Data: {} | {} | {}", mangaName, alternateName, respImageUri);
        try (Connection conn = this.getConnection()) {
            // Using prepared statement to prevent injections
            String sql = "INSERT INTO bookmarks(manga_name, manga_alternate_name, resp_image_uri) VALUES(?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, mangaName.toLowerCase());
                stmt.setString(2, alternateName.toLowerCase());
                stmt.setString(3, respImageUri);
                stmt.executeUpdate();
                logger.info("Insert operation is successful.");
            }
        }
    }

    /**
     * Adds a new tracking manga entry to the database.
     * 
     * @param mangaName     name of the manga which is used in tracking
     * @param alternateName alternative name of the manga
     * @throws SQLException due to database connection or SQL execution error
     */
    public void addManga(String mangaName, String alternateName) throws SQLException {
        addManga(mangaName, alternateName, "");
    }

    /**
     * Adds a new tracking manga entry to the database.
     * 
     * @param mangaName name of the manga which is used in tracking
     * @throws SQLException due to database connection or SQL execution error
     */
    public void addManga(String mangaName) throws SQLException {
        addManga(mangaName, "", "");
    }

    /**
     * Adds a new entry to the tracking table. An entry indicates that there is a
     * post on Reddit for a new chapter of the manga.
     * 
     * @param mangaId       id of the manga in database
     * @param chapterNumber chapter number of the manga
     * @param sourceUrl     URL to the Reddit post or the source manga page
     * @param unixTimestamp timestamp of the Reddit post or the upload of the manga
     *                      chapter
     * @throws SQLException occurs when there is an error in database connection or
     *                      SQL execution
     */
    public void addTracking(int mangaId, String chapterNumber, String sourceUrl, int unixTimestamp)
            throws SQLException {
        logger.info("Adding new entry to <tracking> table.");
        logger.debug("Data: {} | {} | {} | {}", mangaId, chapterNumber, sourceUrl, unixTimestamp);
        try (Connection conn = this.getConnection()) {
            String sql = "INSERT INTO tracking(manga_id, chapter_number, source_url, timestamp) VALUES(?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, mangaId);
                stmt.setString(2, chapterNumber);
                stmt.setString(3, sourceUrl);
                stmt.setInt(4, unixTimestamp);
                stmt.executeUpdate();
                logger.info("Insert operation is successful.");
            }
        }
    }

    /**
     * Fetch all manga names in bookmarks table
     * 
     * @throws SQLException
     */
    public ArrayList<String> getAllTrackedManga() throws SQLException {
        ArrayList<String> allMangaNames = new ArrayList<>();
        String sql = "SELECT manga_name FROM bookmarks";
        try (Connection conn = this.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String mangaName = rs.getString("manga_name");
                allMangaNames.add(mangaName);
            }

            return allMangaNames;
        }
    }

    /**
     * Manually adds manga that we want to track.
     */
    public void addSampleManga() throws SQLException {
        logger.info("Adding sample manga to database.");
        addManga("SPY x FAMILY");
        addManga("Shingeki no Kyojin", "Attack on Titan");
        addManga("I Am The Sorcerer King");
    }

    /**
     * @return the database connection
     * @throws SQLException occurs when unable to connect to database
     */
    private Connection getConnection() throws SQLException {
        logger.debug("Attempting to get database connection.");
        return DriverManager.getConnection(this.databaseUrl);
    }

    public static void main(String[] args) {
        Database db = new Database("jdbc:sqlite:/home/echo/Desktop/mangabot/database.db");
        try {
            db.createDatabase();
            db.addSampleManga();

            ArrayList<String> allMangaNames = db.getAllTrackedManga();
            for (String name : allMangaNames) {
                logger.info(name);
            }
        } catch (SQLException sqle) {
            logger.error("Process failed at some point.", sqle);
        }
    }

}