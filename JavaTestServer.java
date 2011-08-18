import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.commons.codec.binary.Base64;

public class JavaTestServer {
    private ServerSocket serverSocket;
    private Thread serverThread;
    BlockingQueue<ClientProcessor> clientQueue = new LinkedBlockingQueue<ClientProcessor>();

    public JavaTestServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    private Socket getNewConnection() {
        Socket cs = null;
        try {
            cs = serverSocket.accept();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return cs;
    }
    public void run() {
        serverThread = Thread.currentThread();

        while (true) {
            Socket s = getNewConnection();
            if (serverThread.isInterrupted()) {
                break;
            } else if (s != null) {
                try {
                    final ClientProcessor processor = new ClientProcessor(s);
                    final Thread thread = new Thread(processor);
                    thread.setDaemon(true);
                    thread.start();
                    clientQueue.offer(processor);
                } catch (IOException ignored) {}
            }
        }
    }
    public void sendToAll(String[] msgs) {
        for (String msg : msgs) {
            for (ClientProcessor sp : clientQueue) {
                sp.addMessage(msg);
            }
        }
    }
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Server starts at port: " + 8015);
        new JavaTestServer(8015).run();
    }

    private class ClientProcessor implements Runnable {
        private LinkedBlockingQueue<String> messageQueue = new LinkedBlockingQueue<String>();

        Socket clientSocket;
        BufferedReader br;
        BufferedWriter bw;
        OutputStreamWriter osw;

        ClientProcessor(Socket socket) throws IOException {
            clientSocket = socket;

            br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
            bw = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));
        }

        @Override
        protected void finalize() throws Throwable {
            super.finalize();
            close();
        }

        @Override
        public void run() {
            String line;
            char[] cbuf = new char[1000];
            byte[] _cbuf = new byte[1000];
            int n = 0;

            // protocol variables
            String[] pieces, pieces2;
            String out;

            // Handshake
            if (!clientSocket.isClosed()) {
                line = "";

                System.out.println("Wait for handshake.");
                try {
                    if ( (n = br.read(cbuf, 0, 1000)) == -1) {
                        close();
                        return;
                    }
                    cbuf[n] = '\0';
                    line = new String(cbuf);
                } catch (IOException e) {
                    e.printStackTrace();
                    close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.exit(1);
                }

                if (line == null) {
                    close();
                    return;
                }

                if (line.length() > 0) {
                    System.out.println(line);
                }

                line = line.replaceAll("\r", "");
                pieces = line.split("\n");
                /*pieces2 = line.split("\n\n");

                char[] chars = pieces2[1].toCharArray();
                for (int i = 0; i < chars.length; i++) {
                    System.out.format("0x%H ", chars[i]);
                }
                System.out.println();*/

                try {
                    bw.write("HTTP/1.1 500\n\n", 0, "HTTP/1.1 500\n\n".length());
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(1);
                }

                for (int i = 0; i < pieces.length; i++) {
                    if (pieces[i].startsWith("Sec-WebSocket-Key")) {
                        String[] pair = pieces[i].split(": ");
                        String wskey = pair[1];
                        try {
                            out =  "HTTP/1.1 101 Switching Protocols\r\n";
                            out += "Sec-WebSocket-Accept: " + getHandshakeKey(wskey) + "\r\n";
                            out += "Sec-WebSocket-Origin: null\r\n";
                            out += "Upgrade: websocket\r\n";
                            out += "Connection: Upgrade\r\n";
                            out += "Sec-WebSocket-Version: 7\r\n\r\n";
                            System.out.println("Send handshake.");
                            clientSocket.getOutputStream().write(out.getBytes());
                            System.out.println(out);
                            break;
                        } catch (IOException e) {
                            e.printStackTrace();
                            System.exit(1);
                        }
                    }
                }
            }

            while (!clientSocket.isClosed()) {
                line = "";

                System.out.println("Wait for new messages.");
                try {
                    if ( (n = clientSocket.getInputStream().read(_cbuf, 0, 1000)) == -1) {
                        close();
                        return;
                    }
                    cbuf[n] = '\0';
                    line = new String(cbuf);
                } catch (IOException e) {
                    e.printStackTrace();
                    close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.exit(1);
                }

                if (line == null) {
                    close();
                    return;
                }

                if (line.length() > 0) {
                    System.out.println(clientSocket.getInetAddress() + " --> " + line);
                }
            }

            System.out.println("Socket closed.");
        }
        public String getHandshakeKey(String key) {
            String result = key;
            result += "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
            try {
                MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
                byte[] key_sha1 = sha1.digest(result.getBytes("UTF-8"));
                result = new String(Base64.encodeBase64(key_sha1), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                System.exit(1);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                System.exit(1);
            }

            return result;
        }
        public synchronized void close() {
            clientQueue.remove(this);
            if (!clientSocket.isClosed()) {
                try {
                    clientSocket.close();
                } catch (IOException ignored) {}
            } else {
                return;
            }
        }
        public void addMessage(String s) {
            try {
                messageQueue.put(s);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        public void sendMessage(String msg) throws IOException {
            bw.write(msg, 0, msg.length());
        }
    }
}
