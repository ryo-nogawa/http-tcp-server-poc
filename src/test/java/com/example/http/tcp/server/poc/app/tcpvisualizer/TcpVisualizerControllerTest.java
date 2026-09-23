package com.example.http.tcp.server.poc.app.tcpvisualizer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * {@link TcpVisualizerController}の単体テスト。
 */
class TcpVisualizerControllerTest {

    private TcpVisualizerController controller;

    @Nested
    class 正常系 {

        @BeforeEach
        void setUp() {
            controller = new TcpVisualizerController();
        }

        @Test
        @DisplayName("Given: TcpVisualizerControllerが生成されたとき, "
                + "When: showメソッドを実行すると, "
                + "Then: 戻り値がforward:/resources/app/tcp-visualizer/index.htmlである")
        void returnsForwardPathToTcpVisualizerIndexHtml() {
            String result = controller.show();

            assertEquals("forward:/resources/app/tcp-visualizer/index.html", result);
        }
    }

}
