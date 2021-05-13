import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class Player extends Thread {
    private Player next;
    private int batchesRemaining;
    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    public Player(Socket socket, int batchesRemaining) {
        this.next = null;
        try {
            this.socket = socket;
            this.inputStream = new DataInputStream(socket.getInputStream());
            this.outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.writeInt(batchesRemaining);
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

    public void setBatchesRemaining(int batchesRemaining) {
        this.batchesRemaining = batchesRemaining;
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (batchesRemaining != 0) {

                }
                final int code = inputStream.readInt();

//                final UUID nextPlayer = player.getNextPlayer();
//                if (nextPlayer == null) {
//                    return;
//                }
                // TODO something
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
