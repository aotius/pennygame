import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class PennyServer {
    public static final int MAX_PLAYERS = 4;
    public static final int BATCH_SIZE = 20;
    public static final int TOTAL_PENNIES = 20;
    public static final int BATCHES = TOTAL_PENNIES / BATCH_SIZE;
    private static long time = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("Server started");

        final ServerSocket socket = new ServerSocket(1234);

        final List<ServerReadFromClientThread> serverReadFromClientThreads = new ArrayList<>();

        while (true) {
            final Socket connection = socket.accept();
            if (connection == null) {
                if (serverReadFromClientThreads.get(0).getBatches() == 0 && serverReadFromClientThreads.get(1).getBatches() == 0
                        && serverReadFromClientThreads.get(2).getBatches() == 0 && serverReadFromClientThreads.get(3).getBatches() == 0) {
                    time = System.currentTimeMillis() - time;
                    time /= 1000;
                }
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
                System.out.println("starting game");
                time = System.currentTimeMillis();
                final ServerReadFromClientThread firstServerReadFromClientThread = serverReadFromClientThreads.get(0);
                firstServerReadFromClientThread.setBatches(BATCHES);
            }

        }

    }

}
