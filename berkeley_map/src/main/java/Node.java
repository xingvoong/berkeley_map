import java.util.ArrayList;

public class Node {
    long id;
    String name;
    double lat;
    double lon;
    double x;
    double y;
    ArrayList<Long> wayIDs;

    public Node(String id, String lon, String lat) {
        this.id = Long.parseLong(id);
        this.name = null;
        this.lat = Double.parseDouble(lat);
        this.lon = Double.parseDouble(lon);
        this.wayIDs = new ArrayList<>();
        this.x = GraphDB.projectToX(this.lon, this.lat);
        this.y = GraphDB.projectToY(this.lon, this.lat);
    }

    public void addName(String n) {
        this.name = n;
    }

    public void addWay(Way w) {
        wayIDs.add(w.id);
    }

    @Override
    public String toString() {
        return "(id: " + id + ")";
    }
}
