import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class PennyServer extends Thread {
    public static final int MAX_PLAYERS = 4;
    public static int BATCH_SIZE = 20;
    public static final int TOTAL_PENNIES = 20;
    public static final int BATCHES = TOTAL_PENNIES / BATCH_SIZE;
    public static long startTime;

    public PennyServer(int batchSize) {
        BATCH_SIZE = batchSize;
    }

    @Override
    public void run() {
        try {
            ServerSocket socket = new ServerSocket(1234);

            final List<ServerReadFromClientThread> serverReadFromClientThreads = new ArrayList<>();

            while (true) {
                Socket connection = socket.accept();
                if (connection == null) {
                    continue;
                }

                System.out.println("Initializing new player");

                final ServerReadFromClientThread serverReadFromClientThread = new ServerReadFromClientThread(connection, BATCH_SIZE, serverReadFromClientThreads);
                if (!serverReadFromClientThreads.isEmpty()) {
                    serverReadFromClientThreads.get(serverReadFromClientThreads.size() - 1).setNext(serverReadFromClientThread);
                }
                serverReadFromClientThreads.add(serverReadFromClientThread);
                serverReadFromClientThread.start();

                if (serverReadFromClientThreads.size() == MAX_PLAYERS) {
                    System.out.println("Starting game");
                    startTime = System.currentTimeMillis();
                    final ServerReadFromClientThread firstServerReadFromClientThread = serverReadFromClientThreads.get(0);
                    firstServerReadFromClientThread.setBatches(BATCHES);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
