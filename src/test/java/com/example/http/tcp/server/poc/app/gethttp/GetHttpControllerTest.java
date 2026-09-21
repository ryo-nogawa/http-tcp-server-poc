package com.example.http.tcp.server.poc.app.gethttp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.http.tcp.server.poc.domain.model.gethttp.RequestInfo;
import com.example.http.tcp.server.poc.domain.service.gethttp.RequestInfoService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.ui.Model;

/**
 * {@link GetHttpController}の単体テスト。
 */
class GetHttpControllerTest {

    private GetHttpController controller;

    @Nested
    class 正常系 {

        @BeforeEach
        void setUp() {
            RequestInfoService mockService = mock(RequestInfoService.class);
            controller = new GetHttpController(mockService);
        }

        @Test
        @DisplayName("Given: 有効なHttpServletRequestとModelが与えられたとき, "
                + "When: getHttpメソッドを実行すると, "
                + "Then: ビュー名がgethttp/getHttpで属性requestInfoが追加されている")
        void returnsGetHttpViewAndAddsRequestInfoToModel() {
            RequestInfoService mockService = mock(RequestInfoService.class);
            controller = new GetHttpController(mockService);

            HttpServletRequest mockRequest = mock(HttpServletRequest.class);
            Model mockModel = mock(Model.class);

            String endpoint = "/get-http";
            String httpMethod = "GET";

            when(mockRequest.getRequestURI()).thenReturn(endpoint);
            when(mockRequest.getMethod()).thenReturn(httpMethod);

            RequestInfo expectedInfo = new RequestInfo(endpoint, httpMethod,
                    "2024-01-01 12:00:00.000");
            when(mockService.create(endpoint, httpMethod)).thenReturn(expectedInfo);

            String viewName = controller.getHttp(mockRequest, mockModel);

            assertEquals("gethttp/getHttp", viewName);
            verify(mockModel).addAttribute(eq("requestInfo"), eq(expectedInfo));
            verify(mockService).create(endpoint, httpMethod);
        }

        @Test
        @DisplayName("Given: 異なるエンドポイントとHTTPメソッドが与えられたとき, "
                + "When: getHttpメソッドを実行すると, "
                + "Then: リクエスト情報がサービスに正しく渡されている")
        void passesCorrectParametersToService() {
            RequestInfoService mockService = mock(RequestInfoService.class);
            controller = new GetHttpController(mockService);

            HttpServletRequest mockRequest = mock(HttpServletRequest.class);
            Model mockModel = mock(Model.class);

            String endpoint = "/api/test";
            String httpMethod = "POST";

            when(mockRequest.getRequestURI()).thenReturn(endpoint);
            when(mockRequest.getMethod()).thenReturn(httpMethod);

            RequestInfo expectedInfo = new RequestInfo(endpoint, httpMethod,
                    "2024-01-01 13:00:00.000");
            when(mockService.create(endpoint, httpMethod)).thenReturn(expectedInfo);

            controller.getHttp(mockRequest, mockModel);

            verify(mockService).create(endpoint, httpMethod);
            verify(mockModel).addAttribute(eq("requestInfo"), eq(expectedInfo));
        }
    }

    @Nested
    class 異常系 {

        @BeforeEach
        void setUp() {
            RequestInfoService mockService = mock(RequestInfoService.class);
            controller = new GetHttpController(mockService);
        }

        @Test
        @DisplayName("Given: サービスが例外をスローするとき, "
                + "When: getHttpメソッドを実行すると, "
                + "Then: 例外がそのままスローされる")
        void propagatesExceptionWhenServiceThrowsException() {
            RequestInfoService mockService = mock(RequestInfoService.class);
            controller = new GetHttpController(mockService);

            HttpServletRequest mockRequest = mock(HttpServletRequest.class);
            Model mockModel = mock(Model.class);

            when(mockRequest.getRequestURI()).thenReturn("/get-http");
            when(mockRequest.getMethod()).thenReturn("GET");

            RuntimeException expectedEx = new RuntimeException("Service error");
            when(mockService.create(anyString(), anyString()))
                    .thenThrow(expectedEx);

            assertThrows(RuntimeException.class, () -> {
                controller.getHttp(mockRequest, mockModel);
            });
        }
    }

}
