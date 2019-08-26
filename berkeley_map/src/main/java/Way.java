import java.util.ArrayList;

public class Way {
    long id;
    String name;
    ArrayList<Long> nodeIDs;

    public Way() {
        nodeIDs = new ArrayList<>();
    }

    public Way(String id) {
        this.id = Long.parseLong(id);
        nodeIDs = new ArrayList<>();
    }

    public void addName(String n) {
        this.name = n;
    }

    public void addNodeID(long nID) {
        this.nodeIDs.add(nID);
    }

    public ArrayList<Long> getAdjacent(long nodeID) {
        ArrayList<Long> toReturn = new ArrayList<>();
        int index = nodeIDs.indexOf(nodeID);
        if (index == 0) {
            toReturn.add(nodeIDs.get(index + 1));
        } else if (index == nodeIDs.size() - 1) {
            toReturn.add(nodeIDs.get(index - 1));
        } else {
            toReturn.add(nodeIDs.get(index - 1));
            toReturn.add(nodeIDs.get(index + 1));
        }
        return toReturn;
    }
}
