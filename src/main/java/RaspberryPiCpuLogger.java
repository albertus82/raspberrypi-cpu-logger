import java.io.BufferedReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class RaspberryPiCpuLogger {

	private static final String[] PATHS = { "/sys/class/thermal/thermal_zone0/temp", "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq" };
	private static final String URL = "https://api.thingspeak.com/update";
	private static final int INTERVAL_SECS = 15;
	private static final int MAX_ERRORS = 3600 / INTERVAL_SECS;
	private static final int HTTP_TIMEOUT = 15;

	public static void main(final String... args) throws IOException, InterruptedException, URISyntaxException {
		final String apiKey = args[0];
		final URI uri = new URI(URL);
		final HttpClient httpClient = HttpClient.newBuilder().build();
		int errors = 0;
		while (errors < MAX_ERRORS) {
			final StringBuilder body = new StringBuilder("api_key=").append(apiKey);
			for (int i = 0; i < PATHS.length; i++) {
				body.append("&field").append(i + 1).append('=');
				try (final BufferedReader reader = Files.newBufferedReader(Paths.get(PATHS[i]), StandardCharsets.US_ASCII)) {
					body.append(Integer.parseInt(reader.readLine().trim()) / 1000d);
				}
			}
			final HttpRequest request = HttpRequest.newBuilder(uri).POST(BodyPublishers.ofString(body.toString())).timeout(Duration.ofSeconds(HTTP_TIMEOUT)).build();
			try {
				final HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
				final int statusCode = response.statusCode();
				if (statusCode >= HttpURLConnection.HTTP_OK && statusCode < HttpURLConnection.HTTP_MULT_CHOICE) {
					errors = 0;
				}
				else {
					errors++;
					System.err.println(response); // NOSONAR
				}
			}
			catch (final IOException e) {
				errors++;
				e.printStackTrace(); // NOSONAR
				System.out.println("Error count: " + errors + '/' + MAX_ERRORS); // NOSONAR
			}
			TimeUnit.SECONDS.sleep(INTERVAL_SECS);
		}
	}

}
