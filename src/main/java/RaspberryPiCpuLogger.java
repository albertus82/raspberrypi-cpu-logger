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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

class RaspberryPiCpuLogger {

	static final String[] PATHS = { "/sys/class/thermal/thermal_zone0/temp", "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq" };

	private static final String URL = "https://api.thingspeak.com/update";
	private static final int INTERVAL_SECS = 15;
	private static final int MAX_ERRORS = 3600 / INTERVAL_SECS;
	private static final int HTTP_TIMEOUT = 15;

	public static void main(final String... args) throws IOException, InterruptedException, URISyntaxException {
		final String apiKey = args[0];
		new RaspberryPiCpuLogger().run(MAX_ERRORS, new URI(URL), INTERVAL_SECS, apiKey, Arrays.stream(PATHS).map(Paths::get).toArray(Path[]::new));
	}

	int run(final int maxErrors, final URI uri, final int intervalSecs, final String apiKey, final Path... paths) throws IOException, InterruptedException {
		final HttpClient httpClient = HttpClient.newBuilder().build();
		int errors = 0;
		while (errors < maxErrors) {
			if (post(httpClient, uri, apiKey, paths)) {
				errors++;
				System.out.println("Error count: " + errors + '/' + maxErrors); // NOSONAR
			}
			else {
				errors = 0;
			}
			TimeUnit.SECONDS.sleep(intervalSecs);
		}
		return errors;
	}

	boolean post(final HttpClient httpClient, final URI uri, final String apiKey, final Path... paths) throws IOException, InterruptedException {
		boolean error = false;
		final StringBuilder body = new StringBuilder("api_key=").append(apiKey);
		for (int i = 0; i < paths.length; i++) {
			body.append("&field").append(i + 1).append('=');
			try (final BufferedReader reader = Files.newBufferedReader(paths[i], StandardCharsets.US_ASCII)) {
				body.append(Integer.parseInt(reader.readLine().trim()) / 1000d);
			}
		}
		final HttpRequest request = HttpRequest.newBuilder(uri).POST(BodyPublishers.ofString(body.toString())).timeout(Duration.ofSeconds(HTTP_TIMEOUT)).build();
		try {
			final HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
			final int statusCode = response.statusCode();
			if (statusCode < HttpURLConnection.HTTP_OK || statusCode >= HttpURLConnection.HTTP_MULT_CHOICE) {
				error = true;
				System.err.println(response); // NOSONAR
			}
		}
		catch (final IOException e) {
			error = true;
			e.printStackTrace(); // NOSONAR
		}
		return error;
	}

}
