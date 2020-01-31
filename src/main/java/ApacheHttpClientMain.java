import com.google.api.client.util.Base64;
import com.google.api.client.util.StringUtils;
import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class ApacheHttpClientMain {

  private static class RandoContent extends AbstractHttpEntity {
    private int megaBytes;
    private RandoContent(int megaBytes) {
      this.megaBytes = megaBytes;
      setContentType(ContentType.APPLICATION_OCTET_STREAM.toString());
    }
    @Override public boolean isRepeatable() { return false; }
    @Override public boolean isStreaming() { return false; }
    @Override public long getContentLength() { return megaBytes * 1024 * 1024; }
    @Override public void writeTo(OutputStream out) throws IOException {
      byte[] rando = new byte[1024];
      for (int i = 0; i < megaBytes * 1024; i++) out.write(rando);
      out.flush();
    }
    @Override public InputStream getContent() throws IOException, UnsupportedOperationException {
      throw new UnsupportedOperationException();
    }
  }

  public static void main(String[] args) throws IOException {
    String username = "nithin4325";
    String password = YOUR PASSWORD
    String dockerHubRepo = "nithin4325/demo";

    String authToken = getAuthToken(dockerHubRepo, username, password), // don't show this in public
           uploadUrl = getUploadUrl(dockerHubRepo, authToken);

    System.out.println("start timing uploading 40MB to Docker Hub...");
    long started = System.nanoTime();
    HttpPatch req = new HttpPatch(uploadUrl);
    req.setHeader("Authorization", "Bearer " + authToken);
    req.setEntity(new RandoContent(40));
    try (CloseableHttpClient client = HttpClients.createDefault();
         CloseableHttpResponse res = client.execute(req)) { }
    System.out.println("elapsed (s): " + (System.nanoTime() - started) / 100000000L / 10.);
  }

  private static String getAuthToken(String repository, String username, String password) throws IOException {
    System.out.println("requesting bearer auth token from Docker Hub...");
    String authUrl = "https://auth.docker.io/token?service=registry.docker.io&scope=repository:" + repository + ":pull,push";
    HttpGet req = new HttpGet(authUrl);
    req.setHeader("Authorization", "Basic " + Base64.encodeBase64String(StringUtils.getBytesUtf8(username + ':' + password)));
    try (CloseableHttpClient client = HttpClients.createDefault();
         CloseableHttpResponse res = client.execute(req)) {
      String json = new String(ByteStreams.toByteArray(res.getEntity().getContent()), StandardCharsets.UTF_8);
      Matcher m = Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
      m.find();
      return m.group(1); // don't show this in public
    }
  }

  private static String getUploadUrl(String repository, String authToken) throws IOException {
    System.out.println("get upload URL...");
    String uploadInitUrl = "https://registry-1.docker.io/v2/" + repository + "/blobs/uploads/";
    HttpPost req = new HttpPost(uploadInitUrl);
    req.setHeader("Authorization", "Bearer " + authToken);
    try (CloseableHttpClient client = HttpClients.createDefault();
        CloseableHttpResponse res = client.execute(req)) {
      return res.getFirstHeader("Location").getValue();
    }
  }
}
