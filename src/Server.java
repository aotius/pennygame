import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {
    private static final int MAX_PLAYERS = 4;
    private static final int BATCH_SIZE = 20;
    private static final int TOTAL_PENNIES = 20;
    private static final int BATCHES = TOTAL_PENNIES / BATCH_SIZE;

    public static void main(String[] args) throws Exception {
        final ServerSocket socket = new ServerSocket(1234);

        final List<Player> players = new ArrayList<>();

        while (true) {
            final Socket connection = socket.accept();
            if (connection == null) {
                continue;
            }

            final Player player = new Player(connection, 0);

            if (!players.isEmpty()) {
                players.get(players.size() - 1).setNext(player);
            }

            players.add(player);
            final Player clientWriteThread = new Player(connection, BATCH_SIZE);
            clientWriteThread.start();

            if (players.size() == 4) {
                final Player firstPlayer = players.get(0);
                firstPlayer.setBatchesRemaining(BATCHES);
            }
        }

    }

}
