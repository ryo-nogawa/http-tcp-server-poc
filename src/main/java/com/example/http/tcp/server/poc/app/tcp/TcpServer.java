package com.example.http.tcp.server.poc.app.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * TCPサーバー本体.
 *
 * <p>受信ソケットをバインドし、接続ごとにワーカースレッドで処理を行う。
 * {@link InitializingBean} インタフェース実装により、Spring がコンストラクタインジェクション完了直後に
 * {@link #afterPropertiesSet()} を自動呼び出しするため、インジェクション完了後に初期化処理が確実に実行される。
 */
@Component
public class TcpServer implements InitializingBean, DisposableBean {

    private final int port;
    private final int threadPoolSize;
    private final int socketTimeoutMillis;
    private final TcpConnectionHandler connectionHandler;

    private ServerSocket serverSocket;
    private ExecutorService acceptExecutor;
    private ExecutorService workerExecutor;
    // 複数の accept スレッドと worker スレッドから読み取られ、afterPropertiesSet() と destroy() で
    // 書き込まれるため、メモリ可視性を volatile で保証。複数フィールドにまたがる整合性は不要
    // （単一フラグの状態管理のみ）のため AtomicBoolean ではなく volatile で十分
    private volatile boolean running = false;

    /**
     * コンストラクタ.
     *
     * @param port バインドするポート番号
     * @param threadPoolSize ワーカースレッドプールのサイズ
     * @param socketTimeoutMillis ソケットのタイムアウト時間（ミリ秒）
     * @param connectionHandler コネクション処理ハンドラ
     */
    public TcpServer(
            @Value("${tcp.server.port}") int port,
            @Value("${tcp.server.threadPoolSize}") int threadPoolSize,
            @Value("${tcp.server.socketTimeoutMillis}") int socketTimeoutMillis,
            TcpConnectionHandler connectionHandler) {
        this.port = port;
        this.threadPoolSize = threadPoolSize;
        this.socketTimeoutMillis = socketTimeoutMillis;
        this.connectionHandler = connectionHandler;
    }

    /**
     * TCPサーバーを起動する.
     *
     * <p>{@code ServerSocket}を生成し、accept用スレッドで受け入れループを開始する。
     * Spring の {@link InitializingBean} インタフェース実装により自動呼び出される。
     */
    @Override
    public void afterPropertiesSet() {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException(
                    "Failed to create ServerSocket on port " + port, e);
        }
        running = true;
        acceptExecutor = Executors.newSingleThreadExecutor();
        workerExecutor = Executors.newFixedThreadPool(threadPoolSize);

        // accept()がブロッキングするため専用スレッドに分離し、
        // 接続処理は固定プールで並行実行することで受け入れが滞らないようにする
        acceptExecutor.submit(() -> {
            while (running) {
                try {
                    var socket = serverSocket.accept();
                    socket.setSoTimeout(socketTimeoutMillis);
                    workerExecutor.submit(() -> connectionHandler.handle(socket));
                } catch (IOException e) {
                    // 停止時のServerSocket#close()によりaccept()がIOExceptionで抜ける
                    // のは正常な停止処理の一部であるため、稼働フラグで判断して
                    // スタックトレースは停止中の例外のみ抑止
                    if (running) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * TCPサーバーを停止する.
     *
     * <p>{@code ServerSocket}をクローズし、スレッドプールをシャットダウンする。
     * Spring の {@link DisposableBean} インタフェース実装により自動呼び出される。
     */
    @Override
    public void destroy() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (acceptExecutor != null) {
            acceptExecutor.shutdown();
        }
        if (workerExecutor != null) {
            workerExecutor.shutdown();
        }
    }

    /**
     * サーバーがバインドしたポート番号を返す.
     *
     * <p>設定でポート{@code 0}を指定すると空きポートが自動割当されるため、
     * テストから実際のバインドポートを取得できるようにこのメソッドを提供する。
     *
     * @return バインドされたポート番号
     */
    public int getPort() {
        if (serverSocket != null && !serverSocket.isClosed()) {
            return serverSocket.getLocalPort();
        }
        return port;
    }

}
