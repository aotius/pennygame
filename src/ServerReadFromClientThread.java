import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerReadFromClientThread extends Thread {
    private int playerNumber;
    private ServerReadFromClientThread next;
    private AtomicInteger batches;
    private int batchesCompleted;
    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private List<ServerReadFromClientThread> users;

    public ServerReadFromClientThread(Socket socket, List<ServerReadFromClientThread> users) {
        this.next = null;
        this.batches = new AtomicInteger(0);
        this.batchesCompleted = 0;
        this.users = users;
        try {
            this.socket = socket;
            this.inputStream = new DataInputStream(socket.getInputStream());
            this.outputStream = new DataOutputStream(socket.getOutputStream());

//            // Handshake - Send batch size to client
//            outputStream.writeInt(batchSize);
        } catch (Exception e) {
            this.socket = null;
            this.inputStream = null;
            this.outputStream = null;
            e.printStackTrace();
        }
    }

    public void setPlayerNumber(int playerNumber) {
        this.playerNumber = playerNumber;
    }

    public DataOutputStream getOutputStream() {
        return outputStream;
    }

    public DataInputStream getInputStream() {
        return inputStream;
    }

    public void setNext(ServerReadFromClientThread next) {
        this.next = next;
    }

    public int getBatches() {
        return batches.get();
    }

    public void setBatches(int batches) {
        this.batches.set(batches);
    }

    public void addBatch() {
        this.batches.set(this.batches.get() + 1);
    }

    public int getBatchesCompleted() {
        return batchesCompleted;
    }

    private void broadcast(String msg) {
        if (users.isEmpty()) {
            return;
        }
        this.users.forEach(user -> {
            try {
                user.getOutputStream().writeUTF(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private String getPlayerDisplayName(int id) {
        return id <= 4 ? String.valueOf(id) : "Client";
    }

    @Override
    public void run() {
        new Thread(() -> {
            while (true) {
                final int batches = this.batches.get();
                if (batches != 0) {
                    Logger.info(String.format("%s is  sending %,d batches to %s", getPlayerDisplayName(playerNumber), batches, getPlayerDisplayName(playerNumber + 1)));
                    try {
                        outputStream.writeUTF("0," + batches);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    this.batches.set(0);
                }
            }
        }).start();

        while (true) {
            try {
                Logger.info("Player - Blocking");

                final int batchComplete = inputStream.readInt();

                Logger.info("Player - received batch from client");

                broadcast(String.format("1,%d", playerNumber - 1));

                final long now = System.currentTimeMillis();

                // First batch
                if (next == null && batchesCompleted == 0) {
                    final String firstBatchTime = String.format("3,%d", now - PennyServer.startTime);
                    broadcast(firstBatchTime);
                }
                batchesCompleted++;

                // Last batch
                if (next == null) {
                    boolean gameover = true;
                    for (ServerReadFromClientThread user : users) {
                        Logger.info(user + " " + user.getBatchesCompleted() + " " + PennyServer.BATCHES);
                        if (user.getBatchesCompleted() != PennyServer.BATCHES) {
                            gameover = false;
                            //break;
                        }
                    }

                    if (gameover) {
                        final String gameTime = String.format("2,%d", now - PennyServer.startTime);
                        broadcast(gameTime);
                        Logger.info("GAME OVER");
                    } else {
                        Logger.info("GAME NOT OVER");
                    }
                } else {
                    Logger.info(String.format("%s sending batch to %s", getPlayerDisplayName(playerNumber), getPlayerDisplayName(playerNumber + 1)));
                    next.addBatch();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
