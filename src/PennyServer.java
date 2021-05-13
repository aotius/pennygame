import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class PennyServer {
    private static final int MAX_PLAYERS = 4;
    private static final int BATCH_SIZE = 8;
    private static final int TOTAL_PENNIES = 16;
    private static final int BATCHES = TOTAL_PENNIES / BATCH_SIZE;

    public static void main(String[] args) throws Exception {
        System.out.println("Server started");

        final ServerSocket socket = new ServerSocket(1234);

        final List<ServerReadFromClientThread> serverReadFromClientThreads = new ArrayList<>();

        while (true) {
            final Socket connection = socket.accept();
            if (connection == null) {
                continue;
            }

            System.out.println("Initializing new player");

            final ServerReadFromClientThread serverReadFromClientThread = new ServerReadFromClientThread(connection, BATCH_SIZE);
            if (!serverReadFromClientThreads.isEmpty()) {
                serverReadFromClientThreads.get(serverReadFromClientThreads.size() - 1).setNext(serverReadFromClientThread);
            }
            serverReadFromClientThreads.add(serverReadFromClientThread);
            serverReadFromClientThread.start();

            // players.size() == 4
            if (serverReadFromClientThreads.size() == 2) {
                System.out.println("starting game");
                final ServerReadFromClientThread firstServerReadFromClientThread = serverReadFromClientThreads.get(0);
                firstServerReadFromClientThread.setBatches(BATCHES);
            }
        }

    }

}
