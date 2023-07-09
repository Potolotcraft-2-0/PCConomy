package economy.pcconomy.backend.npc.objects;

public class LoanerObject implements INpcObject {
    public LoanerObject(double pull) {
        Pull = pull;
    }

    public double Pull;
    public String HomeTown;

    @Override
    public INpcObject getBaseClass() {
        return this;
    }
}