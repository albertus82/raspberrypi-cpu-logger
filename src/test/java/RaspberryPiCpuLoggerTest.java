import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Random;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.MediaType;

import lombok.SneakyThrows;

@ExtendWith(MockServerExtension.class)
class RaspberryPiCpuLoggerTest {

	private static final String[] PATHS = { "/sys/class/thermal/thermal_zone0/temp", "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq" };

	private static RaspberryPiCpuLogger instance;

	@BeforeAll
	static void beforeAll() {
		instance = new RaspberryPiCpuLogger();
	}

	@Test
	void test(final MockServerClient client) throws IOException, InterruptedException, URISyntaxException {
		final String path = "/update";
		final HttpRequest requestExpectation = new HttpRequest().withMethod("POST").withPath(path).withBody("api_key=1234567890ABCDEF&field1=" + 12345 / 1000d + "&field2=" + 654321 / 1000d);
		final InetSocketAddress remoteAddress = client.remoteAddress();

		// Test HTTP 200
		client.when(requestExpectation).respond(new HttpResponse().withStatusCode(200).withBody(Integer.toString(new Random().nextInt()), MediaType.PLAIN_TEXT_UTF_8));
		final URI uri = new URI("http", null, remoteAddress.getHostString(), remoteAddress.getPort(), path, null, null);
		boolean error = instance.post(HttpClient.newBuilder().build(), uri, "1234567890ABCDEF", Arrays.stream(PATHS).map(this::fromResource).toArray(Path[]::new));
		Assertions.assertFalse(error);

		// Test HTTP >=300
		client.clear(requestExpectation);
		client.when(new HttpRequest().withMethod("POST")).respond(new HttpResponse().withStatusCode(300));
		error = instance.post(HttpClient.newBuilder().build(), uri, "1234567890ABCDEF", Arrays.stream(PATHS).map(this::fromResource).toArray(Path[]::new));
		Assertions.assertTrue(error);

		// Test HTTP <200
		client.clear(requestExpectation);
		client.when(new HttpRequest().withMethod("POST")).respond(new HttpResponse().withStatusCode(199));
		error = instance.post(HttpClient.newBuilder().build(), uri, "1234567890ABCDEF", Arrays.stream(PATHS).map(this::fromResource).toArray(Path[]::new));
		Assertions.assertTrue(error);

		// Test "Connection refused"
		client.stop();
		error = instance.post(HttpClient.newBuilder().build(), uri, "1234567890ABCDEF", Arrays.stream(PATHS).map(this::fromResource).toArray(Path[]::new));
		Assertions.assertTrue(error);
	}

	@SneakyThrows(URISyntaxException.class)
	private Path fromResource(final String path) {
		return Paths.get(getClass().getResource(path).toURI());
	}

}
