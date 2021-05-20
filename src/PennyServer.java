import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class PennyServer extends Thread {
    public static final int MAX_PLAYERS = 4;
    public static final int TOTAL_PENNIES = 20;
    // TODO these probably shouldnt be static
    public static int BATCH_SIZE;
    public static int BATCHES;
    public static long startTime = -1;
    private static long endTime = -1;

    @Override
    public void run() {
        try {
            ServerSocket socket = new ServerSocket(1234);

            final List<ServerReadFromClientThread> serverReadFromClientThreads = new ArrayList<>();

            while (true) {
                System.out.println("server");
                Socket connection = socket.accept();
                if (connection == null) {
                    continue;
                }

                Logger.info("Accepting new connection...");

                final ServerReadFromClientThread serverReadFromClientThread = new ServerReadFromClientThread(connection, serverReadFromClientThreads);
                if (!serverReadFromClientThreads.isEmpty()) {
                    serverReadFromClientThreads.get(serverReadFromClientThreads.size() - 1).setNext(serverReadFromClientThread);
                }
                serverReadFromClientThreads.add(serverReadFromClientThread);
                serverReadFromClientThread.setPlayerNumber(serverReadFromClientThreads.size());


                // Handshake - Batch size
                final int clientBatchSize = serverReadFromClientThread.getInputStream().readInt();
                Logger.info("server received handshake from client" + " " + clientBatchSize);
                // Batch size of -1 indicates that this player is joining the game rather then hosting (i.e. - creating new game)
                if (clientBatchSize != -1) {
                    BATCH_SIZE = clientBatchSize;
                }
                serverReadFromClientThread.getOutputStream().writeInt(BATCH_SIZE);

                Logger.info("Handshake complete - server");

                if (serverReadFromClientThreads.size() == MAX_PLAYERS) {
                    Logger.info("Starting game");
                    startTime = System.currentTimeMillis();
                    final ServerReadFromClientThread firstServerReadFromClientThread = serverReadFromClientThreads.get(0);
                    BATCHES = TOTAL_PENNIES / BATCH_SIZE;
                    firstServerReadFromClientThread.setBatches(BATCHES);
                }

                serverReadFromClientThread.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
