import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {
    private static final int MAX_PLAYERS = 4;
    private static final int BATCH_SIZE = 8;
    private static final int TOTAL_PENNIES = 16;
    private static final int BATCHES = TOTAL_PENNIES / BATCH_SIZE;

    public static void main(String[] args) throws Exception {
        final ServerSocket socket = new ServerSocket(1234);

        final List<Player> players = new ArrayList<>();

        while (true) {
            final Socket connection = socket.accept();
            if (connection == null) {
                continue;
            }

            System.out.println("Initializing new player");

            final Player player = new Player(connection, BATCH_SIZE);
            if (!players.isEmpty()) {
                players.get(players.size() - 1).setNext(player);
            }
            players.add(player);
            player.start();

            // players.size() == 4
            if (players.size() == 2) {
                System.out.println("starting game");
                final Player firstPlayer = players.get(0);
                firstPlayer.setBatches(BATCHES);
            }
        }

    }

}
