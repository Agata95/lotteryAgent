import com.google.gson.Gson;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@WebServlet(urlPatterns = "/play")
public class LotteryAgentEntryServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(LotteryAgentEntryServlet.class);

    private List<Integer> createRandomNumbers() {
        return new Random()
                .ints(1, 50)
                .distinct()
                .limit(6)
                .boxed()    // to samo co: mapToObj(Integer::valueOf)
                .collect(Collectors.toList());
    }

    /**
     * Wypisanie odpowiedzi za pomocą getWriter()
     *
     * @param resp
     * @param randomNumbers
     * @param responseFromBoss
     */
    private void writeResponseToClient(HttpServletResponse resp, List<Integer> randomNumbers, String responseFromBoss) throws IOException {
        String numbersAsString = randomNumbers.stream()
                .map(String::valueOf)
                .collect(Collectors
                        .joining(", "));

        PrintWriter writer = resp.getWriter();
        writer.println("Twoje liczby to: " + numbersAsString);
        writer.println(responseFromBoss);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Integer> randomNumbers = createRandomNumbers();
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        HttpPost httpPost = new HttpPost("http://localhost:8082/lotteryBoss/api/results");
        httpPost.setEntity(EntityBuilder
                .create()
                .setText(new Gson()
                        .toJson(randomNumbers))
                .setContentType(ContentType.APPLICATION_JSON)
                .build());

        CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
        BasicResponseHandler basicResponseHandler = new BasicResponseHandler();

        String responseFromBoss = basicResponseHandler.handleResponse(httpResponse);

        writeResponseToClient(resp, randomNumbers, responseFromBoss);
    }
}
