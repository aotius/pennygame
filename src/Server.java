import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

public class Server {
    private static final int MAX_PLAYERS = 4;
    private static final int BATCH_SIZE = 20;
    private static final int TOTAL_PENNIES = 20;
    private static final int BATCHES = TOTAL_PENNIES / BATCH_SIZE;

    public static void main(String[] args) throws Exception {
        final ServerSocket socket = new ServerSocket(1234);

        // TODO
        final List<Player> players = new LinkedList<>();

        while (true) {
            final Socket connection = socket.accept();
            if (connection == null) {
                continue;
            }
            final Player player = new Player();
            players.add(player);
            // TODO init
        }

    }

}
