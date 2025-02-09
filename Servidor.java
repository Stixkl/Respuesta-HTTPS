import java.io.IOException;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class Servidor {
    public static void main(String argv[]) throws Exception {
        int port = 6789;
        ServerSocket socketdeEscucha = new ServerSocket(port);
        int poolSize = Runtime.getRuntime().availableProcessors();

        @SuppressWarnings("resource")
        ExecutorService pool = new ThreadPoolExecutor(
            poolSize,
            100,
            60L, TimeUnit.SECONDS,
            new SynchronousQueue<>(),
            new ThreadPoolExecutor.CallerRunsPolicy()
        );

        System.out.println("The server is active in port " + port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                System.out.println("\nApagando servidor");
                socketdeEscucha.close();
                pool.shutdown();
            } catch (IOException e) {
                System.err.println("Error en apagado: " + e.getMessage());
            }
        }));

        try {
            while (true) {
                Socket socketdeConexion = socketdeEscucha.accept();
                SolicitudHttp solicitud = new SolicitudHttp(socketdeConexion);
                pool.execute(solicitud);
            }
        } catch (SocketException e) {
            System.out.println("Socket cerrado. Servidor detenido.");
        } finally {
            if (!pool.isShutdown()) {
                pool.shutdown();
            }
        }
    }
}
