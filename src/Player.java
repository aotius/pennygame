import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class Player extends Thread {
    private Player next;
    private int batches;
    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    public Player(Socket socket, int batches) {
        this.next = null;
        try {
            this.socket = socket;
            this.inputStream = new DataInputStream(socket.getInputStream());
            this.outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.writeInt(batches);
        } catch (Exception e) {
            this.socket = null;
            this.inputStream = null;
            this.outputStream = null;
            e.printStackTrace();
        }
    }

    public void setNext(Player next) {
        this.next = next;
    }

    public void setBatches(int batches) {
        this.batches = batches;
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (batches != 0) {
                    System.out.println("sending batches to client");
                    outputStream.writeInt(batches);
                }

                final int batchComplete = inputStream.readInt();
                // TODO send 2 next player if they exist
//                final UUID nextPlayer = player.getNextPlayer();
//                if (nextPlayer == null) {
//                    return;
//                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
