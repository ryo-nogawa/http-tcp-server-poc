package com.example.http.tcp.server.poc.app.tcpvisualizer;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * TCP電文処理フロー可視化ページを提供するコントローラ.
 *
 * <p>GET /tcp-visualizerエンドポイントで静的HTMLページをフォワードする。
 */
@Controller
public class TcpVisualizerController {

    /**
     * フォワード先パス.
     *
     * <p>WARファイル内の固定構成のため環境依存値ではなく定数として保持する。
     */
    private static final String FORWARD_PATH = "forward:/resources/app/tcp-visualizer/index.html";

    /**
     * TCP電文処理フロー可視化ページを表示する.
     *
     * <p>GET /tcp-visualizerエンドポイントへのリクエストをリソースディレクトリ配下の
     * {@code index.html}にフォワードする。
     *
     * @return フォワード先パス {@code "forward:/resources/app/tcp-visualizer/index.html"}
     */
    @GetMapping("/tcp-visualizer")
    public String show() {
        return FORWARD_PATH;
    }

}
