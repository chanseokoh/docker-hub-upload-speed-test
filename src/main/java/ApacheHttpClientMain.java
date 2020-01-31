import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.apache.v2.ApacheHttpTransport;
import com.google.api.client.util.Base64;
import com.google.api.client.util.StringUtils;
import com.google.common.io.CharStreams;
import com.google.common.net.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;

public class ApacheHttpClientMain {

  static class RandoContent implements HttpEntity {
    int megaBytes;
    RandoContent(int megaBytes) { this.megaBytes = megaBytes; }
    @Override public boolean isRepeatable() { return false; }
    @Override public boolean isChunked() { return false; }
    @Override public long getContentLength() { return megaBytes * 1024 * 1024; }
    @Override public Header getContentType() { return new BasicHeader("Content-Type", MediaType.OCTET_STREAM.toString()); }
    @Override public Header getContentEncoding() { return null; }
    @Override public InputStream getContent() throws IOException, UnsupportedOperationException {
      throw new UnsupportedOperationException();
    }
    @Override public void writeTo(OutputStream out) throws IOException {
      byte[] rando = new byte[1024];
      for (int i = 0; i < megaBytes * 1024; i++) out.write(rando);
      out.flush();
    }
    @Override public boolean isStreaming() { return false; }
    @Override public void consumeContent() throws IOException {}
  }

  static HttpRequestFactory reqFactory = new ApacheHttpTransport().createRequestFactory();

  public static void main(String[] args) throws IOException {
    String username = "nithin4325";
    String password = YOUR PASSWORD
    String dockerHubRepo = "nithin4325/demo";

    String authToken = getAuthToken(dockerHubRepo, username, password), // don't show this in public
           uploadUrl = getUploadUrl(dockerHubRepo, authToken);

    System.out.println("start timing uploading 40MB to Docker Hub...");
    long started = System.nanoTime();
    try (CloseableHttpClient client = HttpClients.createDefault()) {
      HttpPatch req = new HttpPatch(uploadUrl);
      req.setHeader("Authorization", "Bearer " + authToken);
      req.setEntity(new RandoContent(40));
      try (CloseableHttpResponse res = client.execute(req)) { }
    }
    System.out.println("elapsed (s): " + (System.nanoTime() - started) / 100000000L / 10.);
  }

  private static String getAuthToken(String repository, String username, String password) throws IOException {
    System.out.println("requesting bearer auth token from Docker Hub...");
    String authUrl = "https://auth.docker.io/token?service=registry.docker.io&scope=repository:" + repository + ":pull,push";
    try (CloseableHttpClient client = HttpClients.createDefault()) {
      HttpGet req = new HttpGet(authUrl);
      req.setHeader("Authorization", "Basic " + Base64.encodeBase64String(StringUtils.getBytesUtf8(username + ':' + password)));
      try (CloseableHttpResponse res = client.execute(req)) {
        String json = CharStreams.toString(new InputStreamReader(res.getEntity().getContent(), StandardCharsets.UTF_8));
        Matcher m = Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
        m.find();
        return m.group(1); // don't show this in public
      }
    }
  }

  private static String getUploadUrl(String repository, String authToken) throws IOException {
    System.out.println("get upload URL...");
    String uploadInitUrl = "https://registry-1.docker.io/v2/" + repository + "/blobs/uploads/";
    try (CloseableHttpClient client = HttpClients.createDefault()) {
      HttpPost req = new HttpPost(uploadInitUrl);
      req.setHeader("Authorization", "Bearer " + authToken);
      try (CloseableHttpResponse res = client.execute(req)) {
        return res.getFirstHeader("Location").getValue();
      }
    }
  }
}