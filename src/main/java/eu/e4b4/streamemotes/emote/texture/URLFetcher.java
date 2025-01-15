package eu.e4b4.streamemotes.emote.texture;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class URLFetcher {

    public static byte[] getBytesFromURL(String urlString) throws IOException {
        URL url = URI.create(urlString).toURL();
        int redirectCount = 0;
        int maxRedirects = 5;

        while (redirectCount < maxRedirects) {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setInstanceFollowRedirects(false); // Handle redirects manually
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Successful response
                try (InputStream inputStream = connection.getInputStream();
                     ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    return outputStream.toByteArray();
                }
            } else if (responseCode == HttpURLConnection.HTTP_MOVED_PERM || 
                       responseCode == HttpURLConnection.HTTP_MOVED_TEMP || 
                       responseCode == 308) { // Handle 308 redirects (permanent)
                // Get the "Location" header to resolve the new URL
                String location = connection.getHeaderField("Location");
                if (location == null) {
                    throw new IOException("Redirect response without Location header");
                }

                // Resolve relative redirects
                try {
                    url = url.toURI().resolve(location).toURL();
                } catch (URISyntaxException e) {
                    throw new IOException(e);
                }
                redirectCount++;
            } else {
                throw new IOException("Failed to download: HTTP response code " + responseCode);
            }
        }

        throw new IOException("Too many redirects");
    }
}
