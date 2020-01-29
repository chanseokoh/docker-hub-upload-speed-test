import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.apache.v2.ApacheHttpTransport;
import com.google.common.io.CharStreams;
import com.google.common.net.MediaType;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GoogleHttpClientMain {

  static class RandoContent implements HttpContent {
    int megaBytes;
    RandoContent(int megaBytes) { this.megaBytes = megaBytes; }
    @Override public long getLength() throws IOException { return megaBytes * 1024 * 1024; }
    @Override public String getType() { return MediaType.OCTET_STREAM.toString(); }
    @Override public boolean retrySupported() { return true; }
    @Override public void writeTo(OutputStream out) throws IOException {
      byte[] rando = new byte[1024];
      for (int i = 0; i < megaBytes * 1024; i++) out.write(rando);
      out.flush();
    }
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
    reqFactory.buildPatchRequest(new GenericUrl(uploadUrl), new RandoContent(40))
        .setHeaders(new HttpHeaders().setAuthorization("Bearer " + authToken))
        .execute();
    System.out.println("elapsed (s): " + (System.nanoTime() - started) / 100000000L / 10.);
  }

  private static String getAuthToken(String repository, String username, String password) throws IOException {
    System.out.println("requesting bearer auth token from Docker Hub...");
    String authUrl = "https://auth.docker.io/token?service=registry.docker.io&scope=repository:" + repository + ":pull,push";
    HttpResponse res = reqFactory.buildGetRequest(new GenericUrl(authUrl))
        .setHeaders(new HttpHeaders().setBasicAuthentication(username, password))
        .execute();

    String json = CharStreams.toString(new InputStreamReader(res.getContent(), StandardCharsets.UTF_8));
    Matcher m = Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
    m.find();
    return m.group(1); // don't show this in public
  }

  private static String getUploadUrl(String repository, String authToken) throws IOException {
    System.out.println("get upload URL...");
    String uploadInitUrl = "https://registry-1.docker.io/v2/" + repository + "/blobs/uploads/";
    HttpResponse res = reqFactory.buildPostRequest(new GenericUrl(uploadInitUrl), new RandoContent(0))
        .setHeaders(new HttpHeaders().setAuthorization("Bearer " + authToken))
        .execute();
    return res.getHeaders().getLocation();
  }
}
