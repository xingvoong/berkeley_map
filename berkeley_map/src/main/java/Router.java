import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class provides a <code>shortestPath</code> method and <code>routeDirections</code> for
 * finding routes between two points on the map.
 */
public class Router {
    /**
     * Return a <code>List</code> of vertex IDs corresponding to the shortest path from a given
     * starting coordinate and destination coordinate.
     * @param g <code>GraphDB</code> data source.
     * @param stlon The longitude of the starting coordinate.
     * @param stlat The latitude of the starting coordinate.
     * @param destlon The longitude of the destination coordinate.
     * @param destlat The latitude of the destination coordinate.
     * @return The <code>List</code> of vertex IDs corresponding to the shortest path.
     */
    public static List<Long> shortestPath(GraphDB g,
                                          double stlon, double stlat,
                                          double destlon, double destlat) {
        // Find closest vertex to start and end lat/lon
        long startID = g.closest(stlon, stlat);
        long endID = g.closest(destlon, destlat);
        //initializes best
        HashMap<Key, Double> best = new HashMap<>();
        HashSet<Long> visitedIDs = new HashSet<>();
        Key firstKey = new Key(startID, startID);
        best.put(firstKey, 0.0);
        //initialize fringe
        PriorityQueue<Vertex> fringe = new PriorityQueue<>();
        Vertex initial = new Vertex(startID, distance(startID, endID, g));
        fringe.add(initial);
        while (!fringe.isEmpty()) {
            Vertex vertexV = fringe.poll();
            long v = vertexV.id;
            if (visitedIDs.contains(v)) {
                continue;
            }
            if (v == endID) {
                return getPath(best, startID, endID);
            } else {
                visitedIDs.add(v);
                for (long w: g.adjacent(v)) {
                    Key keyv = new Key(startID, v);
                    double dsv = best.get(keyv);
                    double edvw = distance(v, w, g);
                    Key keyw = new Key(startID, v, w);
                    if (!best.containsKey(keyw)) {
                        double dsw = dsv + edvw;
                        best.put(keyw, dsw);
                        double h = distance(w, endID, g);
                        Vertex toAdd = new Vertex(w, dsw + h);
                        fringe.add(toAdd);
                    } else {
                        double dsw = best.get(keyw);
                        if (dsv + edvw < dsw) {
                            best.remove(keyw);
                            best.put(keyw, dsv + edvw);
                            dsw = best.get(keyw);
                            double h = distance(w, endID, g);
                            Vertex toAdd = new Vertex(w, dsw + h);
                            fringe.add(toAdd);
                        }
                    }
                }
            }
        }
        return Collections.emptyList();
    }

    private static ArrayList<Long> getPath(HashMap<Key, Double> best, long startId, long endID) {
        ArrayList<Long> toReturn = new ArrayList<>();
        toReturn.add(endID);
        long currTo = endID;
        while (currTo != startId) {
            for (Key k: best.keySet()) {
                if (k.to == currTo) {
                    currTo = k.from;
                    toReturn.add(currTo);
                    break;
                }
            }
        }
        Collections.reverse(toReturn);
        return toReturn;
    }

    private static class Vertex implements Comparable<Vertex> {
        long id;
        double distance;

        Vertex(long id, double distance) {
            this.id = id;
            this.distance = distance;
        }

        public String toString() {
            return "id: " + id + " distance: " + distance;
        }

        @Override
        public int compareTo(Vertex other) {
            double cmp = distance - other.distance;
            int toReturn = 0;
            if (cmp == 0) {
                toReturn = 0;
            } else if (cmp > 0) {
                toReturn = 1;
            } else {
                toReturn = -1;
            }
            return toReturn;
        }
    }

    private static class Key {
        long source;
        long from;
        long to;

        Key(long source, long to) {
            this.source = source;
            this.to = to;
        }

        Key(long source, long from, long to) {
            this.source = source;
            this.from = from;
            this.to = to;
        }

        public String toString() {
            return "from: " + source + " to: " + to;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Key key = (Key) o;
            return source == key.source
                    && to == key.to;
        }

        @Override
        public int hashCode() {
            return Objects.hash(source, to);
        }
    }

    private static double distance(long v, long w, GraphDB g) {
        return g.distance(v, w);
    }
    /**
     * Given a <code>route</code> of vertex IDs, return a <code>List</code> of
     * <code>NavigationDirection</code> objects representing the travel directions in order.
     * @param g <code>GraphDB</code> data source.
     * @param route The shortest-path route of vertex IDs.
     * @return A new <code>List</code> of <code>NavigationDirection</code> objects.
     */
    public static List<NavigationDirection> routeDirections(GraphDB g, List<Long> route) {
        return Collections.emptyList();
    }

    /**
     * Class to represent a navigation direction, which consists of 3 attributes:
     * a direction to go, a way, and the distance to travel for.
     */
    public static class NavigationDirection {

        /** Integer constants representing directions. */
        public static final int START = 0, STRAIGHT = 1, SLIGHT_LEFT = 2, SLIGHT_RIGHT = 3,
                RIGHT = 4, LEFT = 5, SHARP_LEFT = 6, SHARP_RIGHT = 7;

        /** Number of directions supported. */
        public static final int NUM_DIRECTIONS = 8;

        /** A mapping of integer values to directions.*/
        public static final String[] DIRECTIONS = new String[NUM_DIRECTIONS];

        static {
            DIRECTIONS[START] = "Start";
            DIRECTIONS[STRAIGHT] = "Go straight";
            DIRECTIONS[SLIGHT_LEFT] = "Slight left";
            DIRECTIONS[SLIGHT_RIGHT] = "Slight right";
            DIRECTIONS[RIGHT] = "Turn right";
            DIRECTIONS[LEFT] = "Turn left";
            DIRECTIONS[SHARP_LEFT] = "Sharp left";
            DIRECTIONS[SHARP_RIGHT] = "Sharp right";
        }

        /** The direction represented.*/
        int direction;
        /** The name of this way. */
        String way;
        /** The distance along this way. */
        double distance = 0.0;

        public String toString() {
            return String.format("%s on %s and continue for %.3f miles.",
                    DIRECTIONS[direction], way, distance);
        }

        /**
         * Returns a new <code>NavigationDirection</code> from a string representation.
         * @param dirAsString <code>String</code> instructions for a navigation direction.
         * @return A new <code>NavigationDirection</code> based on the string, or <code>null</code>
         * if unable to parse.
         */
        public static NavigationDirection fromString(String dirAsString) {
            String regex = "([a-zA-Z\\s]+) on ([\\w\\s]*) and continue for ([0-9\\.]+) miles\\.";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(dirAsString);
            NavigationDirection nd = new NavigationDirection();
            if (m.matches()) {
                String direction = m.group(1);
                if (direction.equals("Start")) {
                    nd.direction = NavigationDirection.START;
                } else if (direction.equals("Go straight")) {
                    nd.direction = NavigationDirection.STRAIGHT;
                } else if (direction.equals("Slight left")) {
                    nd.direction = NavigationDirection.SLIGHT_LEFT;
                } else if (direction.equals("Slight right")) {
                    nd.direction = NavigationDirection.SLIGHT_RIGHT;
                } else if (direction.equals("Turn right")) {
                    nd.direction = NavigationDirection.RIGHT;
                } else if (direction.equals("Turn left")) {
                    nd.direction = NavigationDirection.LEFT;
                } else if (direction.equals("Sharp left")) {
                    nd.direction = NavigationDirection.SHARP_LEFT;
                } else if (direction.equals("Sharp right")) {
                    nd.direction = NavigationDirection.SHARP_RIGHT;
                } else {
                    return null;
                }

                nd.way = m.group(2);
                try {
                    nd.distance = Double.parseDouble(m.group(3));
                } catch (NumberFormatException e) {
                    return null;
                }
                return nd;
            } else {
                // Not a valid nd
                return null;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof NavigationDirection) {
                return direction == ((NavigationDirection) o).direction
                        && way.equals(((NavigationDirection) o).way)
                        && distance == ((NavigationDirection) o).distance;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(direction, way, distance);
        }
    }
}