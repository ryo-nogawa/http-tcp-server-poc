package com.example.http.tcp.server.poc.app.gethttp;

import com.example.http.tcp.server.poc.domain.service.gethttp.RequestInfoService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * GET /get-httpエンドポイントを処理するコントローラ.
 *
 * <p>リクエスト情報を取得し、サービスで整形した上でビューに渡す。
 */
@Controller
public class GetHttpController {

    private final RequestInfoService requestInfoService;

    /**
     * コンストラクタ.
     *
     * @param requestInfoService リクエスト情報を生成するサービス
     */
    public GetHttpController(RequestInfoService requestInfoService) {
        this.requestInfoService = requestInfoService;
    }

    /**
     * GET /get-httpのリクエストを処理する.
     *
     * <p>リクエストのエンドポイントとHTTPメソッドを取得し、
     * {@link RequestInfoService}で整形してビューに渡す。
     *
     * @param request HTTPリクエスト
     * @param model ビューに渡すモデル
     * @return ビュー名 {@code "gethttp/getHttp"}
     */
    @GetMapping("/get-http")
    public String getHttp(HttpServletRequest request, Model model) {
        String endpoint = request.getRequestURI();
        String httpMethod = request.getMethod();

        model.addAttribute("requestInfo",
                requestInfoService.create(endpoint, httpMethod));

        return "gethttp/getHttp";
    }

}
