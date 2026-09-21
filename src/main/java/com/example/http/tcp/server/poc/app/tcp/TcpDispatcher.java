package com.example.http.tcp.server.poc.app.tcp;

import com.example.http.tcp.server.poc.domain.model.tcp.MessageType;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * TCP電文種別から対応するコントローラを解決するディスパッチャ.
 *
 * <p>複数の接続スレッドから同時に参照されるため、
 * スレッドセーフな{@link ConcurrentHashMap}を使用する。
 */
@Component
public class TcpDispatcher {

    private final ConcurrentHashMap<MessageType, TcpController>
            controllerMap;

    /**
     * コンストラクタ.
     *
     * <p>提供されたコントローラのリストから電文種別をキーとする
     * マップを構築する。
     *
     * @param controllers 利用可能なTCPコントローラのリスト
     */
    public TcpDispatcher(List<TcpController> controllers) {
        this.controllerMap = new ConcurrentHashMap<>();
        for (TcpController controller : controllers) {
            controllerMap.put(controller.getMessageType(), controller);
        }
    }

    /**
     * 電文種別から対応するコントローラを返す.
     *
     * @param messageType 解決対象の電文種別
     * @return 対応するコントローラ。登録されていない場合は{@code null}を返す
     */
    public TcpController resolve(MessageType messageType) {
        return controllerMap.get(messageType);
    }

}
