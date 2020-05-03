import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Random;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.MediaType;

import lombok.SneakyThrows;

@ExtendWith(MockServerExtension.class)
class RaspberryPiCpuLoggerTest {

	private static final String[] PATHS = RaspberryPiCpuLogger.PATHS;

	private static final String PATH = "/update";

	private static RaspberryPiCpuLogger instance;

	@BeforeAll
	static void beforeAll() {
		instance = new RaspberryPiCpuLogger();
	}

	@AfterAll
	@Timeout(100)
	static void afterAll(final MockServerClient client) throws IOException, InterruptedException, URISyntaxException {
		// Test "Connection refused"
		final InetSocketAddress remoteAddress = client.remoteAddress();
		client.stop();
		final URI uri = new URI("http", null, remoteAddress.getHostString(), remoteAddress.getPort(), PATH, null, null);
		final boolean error = instance.post(HttpClient.newBuilder().build(), uri, "1234567890ABCDEF", Arrays.stream(PATHS).map(RaspberryPiCpuLoggerTest::getResourcePath).toArray(Path[]::new));
		Assertions.assertTrue(error);
	}

	@BeforeEach
	void beforeEach(final MockServerClient client) {
		client.reset();
	}

	@Test
	void testMain() {
		Assertions.assertThrows(RuntimeException.class, RaspberryPiCpuLogger::main);
	}

	@Test
	@Timeout(100)
	void testRun(final MockServerClient client) throws IOException, InterruptedException, URISyntaxException {
		final InetSocketAddress remoteAddress = client.remoteAddress();
		client.when(new HttpRequest().withMethod("POST")).respond(new HttpResponse().withStatusCode(404));
		final URI uri = new URI("http", null, remoteAddress.getHostString(), remoteAddress.getPort(), PATH, null, null);
		int errors = instance.run(3, uri, 1, "1234567890ABCDEF", Arrays.stream(PATHS).map(RaspberryPiCpuLoggerTest::getResourcePath).toArray(Path[]::new));
		Assertions.assertEquals(3, errors);
	}

	@Test
	@Timeout(100)
	void testPost(final MockServerClient client) throws IOException, InterruptedException, URISyntaxException {
		final InetSocketAddress remoteAddress = client.remoteAddress();
		final URI uri = new URI("http", null, remoteAddress.getHostString(), remoteAddress.getPort(), PATH, null, null);

		// Test HTTP 2xx
		final HttpRequest requestExpectation = new HttpRequest().withMethod("POST").withPath(PATH).withBody("api_key=1234567890ABCDEF&field1=" + 12345 / 1000d + "&field2=" + 654321 / 1000d);
		final HttpResponse responseExpectation = new HttpResponse().withStatusCode(200).withBody(Integer.toString(new Random().nextInt()), MediaType.PLAIN_TEXT_UTF_8);
		client.when(requestExpectation).respond(responseExpectation);
		boolean error = instance.post(HttpClient.newBuilder().build(), uri, "1234567890ABCDEF", Arrays.stream(PATHS).map(RaspberryPiCpuLoggerTest::getResourcePath).toArray(Path[]::new));
		Assertions.assertFalse(error);

		// Test HTTP >2xx
		client.reset();
		client.when(new HttpRequest().withMethod("POST")).respond(new HttpResponse().withStatusCode(300));
		error = instance.post(HttpClient.newBuilder().build(), uri, "1234567890ABCDEF", Arrays.stream(PATHS).map(RaspberryPiCpuLoggerTest::getResourcePath).toArray(Path[]::new));
		Assertions.assertTrue(error);
		client.reset();
		client.when(new HttpRequest().withMethod("POST")).respond(new HttpResponse().withStatusCode(500));
		error = instance.post(HttpClient.newBuilder().build(), uri, "1234567890ABCDEF", Arrays.stream(PATHS).map(RaspberryPiCpuLoggerTest::getResourcePath).toArray(Path[]::new));
		Assertions.assertTrue(error);

		// Test HTTP <2xx
		client.reset();
		client.when(new HttpRequest().withMethod("POST")).respond(new HttpResponse().withStatusCode(101));
		error = instance.post(HttpClient.newBuilder().build(), uri, "1234567890ABCDEF", Arrays.stream(PATHS).map(RaspberryPiCpuLoggerTest::getResourcePath).toArray(Path[]::new));
		Assertions.assertTrue(error);
	}

	@SneakyThrows(URISyntaxException.class)
	private static Path getResourcePath(final String resourceName) {
		return Paths.get(RaspberryPiCpuLoggerTest.class.getResource(resourceName).toURI());
	}

}
