import java.util.UUID;

public class Player {
    private final UUID uuid;
    private int batches;
    private int pennies;

    public Player() {
        this.uuid = UUID.randomUUID();
        this.batches = 0;
        this.pennies = 0;
    }

    public int getPennies() {
        return pennies;
    }

    public void setPennies(int pennies) {
        this.pennies = pennies;
    }

    public void addPenny() {
        this.pennies++;
    }

    public UUID getUuid() {
        return uuid;
    }

}
