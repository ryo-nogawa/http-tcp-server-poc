package com.example.http.tcp.server.poc.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

/**
 * HTTP統合テスト。
 *
 * <p>組み込みTomcatを起動し、エンドポイントへのHTTPリクエストの正常系・異常系を検証する。
 */
@TestInstance(Lifecycle.PER_CLASS)
class HttpIntegrationTest {

    private Tomcat tomcat;

    private int port;

    private HttpClient httpClient;

    /**
     * 組み込みTomcatを起動する。
     *
     * <p>テストクラス単位で1回だけ起動され、全テストメソッドで使い回される。
     * ポート番号は0を指定して空きポートを自動割当する。
     * テンポラリディレクトリはtarget配下に作成される。
     *
     * @throws Exception Tomcat起動に失敗した場合
     */
    @BeforeAll
    void setupServer() throws Exception {
        tomcat = new Tomcat();
        // ポート番号0は実行環境に依存せず空きポートを自動割当するため外部化対象外
        tomcat.setPort(0);

        // ベースディレクトリをtarget配下に設定
        // プロジェクトの固定構成である「target」ディレクトリのため、実行環境による値の変化がなく config.md の外部化対象外
        Path targetDir = Paths.get("target");
        File baseDir = targetDir.toFile();
        baseDir.mkdirs();
        String tempDirPath = targetDir.resolve("tomcat-" + System.nanoTime()).toString();
        tomcat.setBaseDir(tempDirPath);

        // docBaseに src/main/webapp を指定すると target/classes が
        // /WEB-INF/classes に載らないため、StandardRoot と DirResourceSet を
        // 使ってクラスパスを明示的に追加する必要があります
        // プロジェクトの固定構成である「src/main/webapp」パスのため、実行環境による値の変化がなく config.md の外部化対象外
        String docBase = Paths.get("src/main/webapp").toAbsolutePath().toString();
        Context context = tomcat.addWebapp("", docBase);

        // プロジェクトの固定構成である「target/classes」パスのため、実行環境による値の変化がなく config.md の外部化対象外
        String classesDir = Paths.get("target/classes").toAbsolutePath().toString();

        StandardRoot standardRoot = new StandardRoot(context);
        DirResourceSet dirResourceSet =
                new DirResourceSet(standardRoot, "/WEB-INF/classes", classesDir, "/");
        standardRoot.addPreResources(dirResourceSet);
        context.setResources(standardRoot);

        tomcat.start();
        port = tomcat.getConnector().getLocalPort();
    }

    /**
     * 組み込みTomcatを停止する。
     *
     * <p>テストクラスの全テストメソッド実行後に1回だけ呼ばれる。
     *
     * @throws Exception Tomcat停止に失敗した場合
     */
    @AfterAll
    void tearDownServer() throws Exception {
        if (tomcat == null) {
            return;
        }

        tomcat.stop();
        tomcat.destroy();
    }

    /**
     * 各テストメソッド前に初期化する。
     *
     * <p>テストメソッドが実行されるたびに、新しい{@link HttpClient}を生成する。
     */
    @BeforeEach
    void setUp() {
        httpClient = HttpClient.newHttpClient();
    }

    @Nested
    class 正常系 {

        @Test
        @DisplayName("Given: 有効なGETリクエストが/get-httpに送信されたとき, "
                + "When: リクエストを実行すると, "
                + "Then: ステータス200が返されレスポンスHTMLにエンドポイント・GET・受信時刻が含まれる")
        void returns200AndResponseContainsRequestInfoForGetHttpEndpoint()
                throws IOException, InterruptedException {
            String url = "http://localhost:" + port + "/get-http";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, BodyHandlers.ofString());

            assertEquals(200, response.statusCode());
            String responseBody = response.body();
            assertTrue(responseBody.contains("/get-http"),
                    "Response should contain endpoint /get-http");
            assertTrue(responseBody.contains("GET"),
                    "Response should contain HTTP method GET");
            // 受信時刻は yyyy-MM-dd HH:mm:ss.SSS フォーマット
            Pattern timestampPattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}");
            assertTrue(timestampPattern.matcher(responseBody).find(),
                    "Response should contain formatted timestamp");
        }
    }

    @Nested
    class 異常系 {

        @Test
        @DisplayName("Given: 存在しないパスへのGETリクエストが送信されたとき, "
                + "When: リクエストを実行すると, "
                + "Then: ステータス404が返される")
        void returns404ForNonExistentPath()
                throws IOException, InterruptedException {
            String url = "http://localhost:" + port + "/nonexistent";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, BodyHandlers.ofString());

            assertEquals(404, response.statusCode());
        }

        @Test
        @DisplayName("Given: /get-httpへのPOSTリクエストが送信されたとき, "
                + "When: リクエストを実行すると, "
                + "Then: CSRF保護により403が返される")
        void returns403ForPostMethodOnGetHttpEndpointDueToCsrfProtection()
                throws IOException, InterruptedException {
            String url = "http://localhost:" + port + "/get-http";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, BodyHandlers.ofString());

            assertEquals(403, response.statusCode());
        }
    }

}
