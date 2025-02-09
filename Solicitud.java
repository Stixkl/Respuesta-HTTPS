import java.io.* ;
import java.net.* ;
import java.util.* ;

final class Solicitud implements Runnable {
    final static String CRLF = "\r\n";
    Socket socket;

    public Solicitud(Socket socket) throws Exception {
        this.socket = socket;
    }

    public void run() {
        try {
            proceseSolicitud();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private static void enviarBytes(FileInputStream fis, OutputStream os) throws Exception {
        byte[] buffer = new byte[1024];
        int bytes;

        while ((bytes = fis.read(buffer)) != -1) {
            os.write(buffer, 0, bytes);
        }
    }

    private static String contentType(String nombreArchivo) {
        if (nombreArchivo.endsWith(".htm") || nombreArchivo.endsWith(".html")) {
            return "text/html";
        }
        if (nombreArchivo.endsWith(".gif")) {
            return "image/gif";
        }
        if (nombreArchivo.endsWith(".jpg") || nombreArchivo.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        return "application/octet-stream";
    }

    private void proceseSolicitud() throws Exception {
        try (DataOutputStream os = new DataOutputStream(socket.getOutputStream());
             BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String lineaDeSolicitud = br.readLine();
            System.out.println("\n" + lineaDeSolicitud + "\n");

            String lineaDelHeader;
            while ((lineaDelHeader = br.readLine()).length() != 0) {
                System.out.println(lineaDelHeader);
            }

            for (int i = 0; i < 3; i++) {
                System.out.println("\n");
            }

            StringTokenizer partesLinea = new StringTokenizer(lineaDeSolicitud);
            partesLinea.nextToken();
            String nombreArchivo = partesLinea.nextToken();

            if (nombreArchivo.equals("/")) {
                nombreArchivo = "./index.html";
            } else {
                nombreArchivo = "." + nombreArchivo;
            }

            FileInputStream fis = null;
            boolean existeArchivo = true;
            try {
                fis = new FileInputStream(nombreArchivo);
            } catch (FileNotFoundException e) {
                existeArchivo = false;
            }

            String lineaDeEstado;
            String lineaDeTipoContenido;
            String cuerpoMensaje = null;

            if (existeArchivo) {
                lineaDeEstado = "HTTP/1.0 200 OK" + CRLF;
                lineaDeTipoContenido = "Content-type: " + contentType(nombreArchivo) + CRLF;
            } else {
                lineaDeEstado = "HTTP/1.0 404 Not Found" + CRLF;
                lineaDeTipoContenido = "Content-Type: text/html" + CRLF;
                cuerpoMensaje = "<HTML><HEAD><TITLE>404 Not Found</TITLE></HEAD><BODY><b>404</b> Not Found</BODY></HTML>";
            }

            os.writeBytes(lineaDeEstado);
            os.writeBytes(lineaDeTipoContenido);
            os.writeBytes(CRLF);

            if (existeArchivo) {
                enviarBytes(fis, os);
                fis.close();
            } else {
                os.writeBytes(cuerpoMensaje);
            }

        } catch (Exception e) {
            System.err.println("Error procesando solicitud: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Error cerrando socket: " + e.getMessage());
            }
        }
    }
}
