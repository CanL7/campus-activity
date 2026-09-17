import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 报名接口并发压测打桩（不依赖任何第三方库，JDK 自带 HttpClient）。
 *
 * 用法：
 *   javac -d loadtest/out loadtest/EnrollLoadTest.java
 *   java -cp loadtest/out EnrollLoadTest <activityId> [线程数]
 *
 * 例：java -cp loadtest/out EnrollLoadTest 3 50
 *     → 50 个线程同时抢活动 3 的名额
 *
 * 原理：所有线程先 countDown 到 ready，再用 start 这道闸门对齐，
 *       确保它们尽最大可能同时发出请求（否则线程池会串行执行，压不出并发）。
 */
public class EnrollLoadTest {

    static final String URL = "http://localhost:8080/enrollments";
    static final long START_USER_ID = 10000;   // 起始用户 id，避免和已有记录撞唯一索引

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("用法: java -cp loadtest/out EnrollLoadTest <activityId> [线程数]");
            return;
        }
        long activityId = Long.parseLong(args[0]);
        int threads = args.length > 1 ? Integer.parseInt(args[1]) : 50;

        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        AtomicInteger success = new AtomicInteger();
        AtomicInteger failed = new AtomicInteger();
        ConcurrentLinkedQueue<String> failSamples = new ConcurrentLinkedQueue<>();

        ExecutorService pool = Executors.newFixedThreadPool(threads);

        for (int i = 0; i < threads; i++) {
            final long userId = START_USER_ID + i;
            pool.submit(() -> {
                try {
                    ready.countDown();
                    start.await();

                    String body = "{\"activityId\":" + activityId + ",\"userId\":" + userId + "}";
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(URL))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(body))
                            .build();

                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    String respBody = response.body();

                    if (respBody.contains("\"code\":200")) {
                        success.incrementAndGet();
                    } else {
                        failed.incrementAndGet();
                        if (failSamples.size() < 5) {
                            failSamples.add(respBody);
                        }
                    }
                } catch (Exception e) {
                    failed.incrementAndGet();
                    if (failSamples.size() < 5) {
                        failSamples.add(e.getClass().getSimpleName() + ": " + e.getMessage());
                    }
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await();
        long begin = System.currentTimeMillis();
        start.countDown();
        done.await();
        long cost = System.currentTimeMillis() - begin;
        pool.shutdown();

        System.out.println("=================================================");
        System.out.println("  活动 id      : " + activityId);
        System.out.println("  并发线程数   : " + threads);
        System.out.println("  总耗时       : " + cost + " ms");
        System.out.println("  报名成功     : " + success.get());
        System.out.println("  报名失败     : " + failed.get());
        System.out.println("=================================================");
        if (!failSamples.isEmpty()) {
            System.out.println("  失败响应样例：");
            failSamples.forEach(s -> System.out.println("    " + s));
        }
    }
}
