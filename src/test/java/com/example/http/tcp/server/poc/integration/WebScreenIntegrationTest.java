package com.example.http.tcp.server.poc.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

/**
 * Selenium結合テスト。
 *
 * <p>組み込みTomcatとヘッドレスChromeを起動し、
 * 画面表示の正常系を検証する。
 */
@TestInstance(Lifecycle.PER_CLASS)
class WebScreenIntegrationTest {

    private Tomcat tomcat;

    private int port;

    private ChromeDriver driver;

    /**
     * 組み込みTomcatとヘッドレスChromeを起動する。
     *
     * <p>テストクラス単位で1回だけ起動され、
     * 全テストメソッドで使い回される。
     * Tomcatのポート番号は0を指定して空きポートを自動割当する。
     * ヘッドレスChromeは起動コストが高いため、
     * 全テストメソッドで1つのドライバを使い回す。
     *
     * @throws Exception Tomcat起動に失敗した場合
     */
    @BeforeAll
    void setupServerAndBrowser() throws Exception {
        // Tomcat起動
        tomcat = new Tomcat();
        // ポート番号0は実行環境に依存せず空きポートを自動割当する
        // ため外部化対象外
        tomcat.setPort(0);

        // ベースディレクトリをtarget配下に設定
        // プロジェクトの固定構成である「target」ディレクトリのため、
        // 実行環境による値の変化がなく config.md の外部化対象外
        Path targetDir = Paths.get("target");
        File baseDir = targetDir.toFile();
        baseDir.mkdirs();
        String tempDirPath = targetDir.resolve("tomcat-" + System.nanoTime()).toString();
        tomcat.setBaseDir(tempDirPath);

        // docBaseに src/main/webapp を指定すると target/classes が
        // /WEB-INF/classes に載らないため、StandardRoot と DirResourceSet を
        // 使ってクラスパスを明示的に追加する必要があります
        // プロジェクトの固定構成である「src/main/webapp」パスのため、
        // 実行環境による値の変化がなく config.md の外部化対象外
        String docBase = Paths.get("src/main/webapp").toAbsolutePath().toString();
        Context context = tomcat.addWebapp("", docBase);

        // プロジェクトの固定構成である「target/classes」パスのため、
        // 実行環境による値の変化がなく config.md の外部化対象外
        String classesDir = Paths.get("target/classes").toAbsolutePath().toString();

        StandardRoot standardRoot = new StandardRoot(context);
        DirResourceSet dirResourceSet =
                new DirResourceSet(standardRoot, "/WEB-INF/classes", classesDir, "/");
        standardRoot.addPreResources(dirResourceSet);
        context.setResources(standardRoot);

        tomcat.start();
        port = tomcat.getConnector().getLocalPort();

        // ヘッドレスChrome起動
        // ヘッドレス起動オプションはテスト実行専用の固定値であり、
        // 実行環境で変化しないため config.md の外部化対象外
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        driver = new ChromeDriver(options);
    }

    /**
     * 組み込みTomcatとヘッドレスChromeを停止する。
     *
     * <p>テストクラスの全テストメソッド実行後に1回だけ呼ばれる。
     *
     * @throws Exception Tomcat停止に失敗した場合
     */
    @AfterAll
    void tearDownServerAndBrowser() throws Exception {
        if (driver != null) {
            driver.quit();
        }

        if (tomcat == null) {
            return;
        }

        tomcat.stop();
        tomcat.destroy();
    }

    /**
     * 各テストメソッド前に初期化する。
     *
     * <p>前のテストのブラウザ状態を引き継がないよう、
     * 全Cookieを削除する。
     * ブラウザ本体は起動コストが高いため使い回す。
     */
    @BeforeEach
    void setUp() {
        driver.manage().deleteAllCookies();
    }

    @Nested
    class 正常系 {

        @Test
        @DisplayName("Given: トップ画面へのアクセスが行われたとき, "
                + "When: ページを表示すると, "
                + "Then: タイトルが'Home'、h1のテキストが'Hello world!'、"
                + "p要素に'The time on the server is'が含まれる")
        void displaysHomePageCorrectly() {
            String url = "http://localhost:" + port + "/";
            driver.get(url);

            assertEquals("Home", driver.getTitle(),
                    "Page title should be 'Home'");
            String titleText = driver.findElement(By.id("title")).getText();
            assertEquals("Hello world!", titleText,
                    "Title element should display 'Hello world!'");
            String paragraphText = driver.findElement(By.tagName("p")).getText();
            assertTrue(paragraphText.contains("The time on the server is"),
                    "Paragraph should contain 'The time on the server is'");
        }

        @Test
        @DisplayName("Given: /get-httpページへのアクセスが行われたとき, "
                + "When: ページを表示すると, "
                + "Then: タイトルが'Get HTTP'、"
                + "h1のテキストが'GET HTTP Request Information'、"
                + "テーブルに'/get-http'と'GET'が表示される")
        void displaysGetHttpPageCorrectly() {
            String url = "http://localhost:" + port + "/get-http";
            driver.get(url);

            assertEquals("Get HTTP", driver.getTitle(),
                    "Page title should be 'Get HTTP'");
            String titleText = driver.findElement(By.id("title")).getText();
            assertEquals("GET HTTP Request Information", titleText,
                    "Title element should display 'GET HTTP Request Information'");

            String pageSource = driver.getPageSource();
            assertTrue(pageSource.contains("<td>/get-http</td>"),
                    "Page should contain endpoint /get-http in table cell");
            assertTrue(pageSource.contains("<td>GET</td>"),
                    "Page should contain HTTP method GET in table cell");
        }
    }
}
