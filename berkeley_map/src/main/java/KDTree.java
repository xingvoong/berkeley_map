import java.util.ArrayList;
import java.util.PriorityQueue;

class KDTree {
    //Commit this please
    private static final int R = 3963;
    Node root;
    KDTree left;
    KDTree right;

    KDTree(Node root, KDTree left, KDTree right) {
        this.root = root;
        this.left = left;
        this.right = right;
    }

    Node nearestNeighborSearch(double x, double y) {
        return nearestNeighborSearchHelper(x, y, 0, this);
    }

    Node nearestNeighborSearchHelper(double x, double y, int depth, KDTree curr) {
        if (curr == null) {
            return new Node("1200", "1000000", "1000000");
        } else if (isLeaf(curr)) {
            return curr.root;
        } else {
            Node currNode = curr.root;
            double currDistance = distance(currNode, x, y);
            Node bestNode;
            double bestDistance;
            if (traverseLeft(x, y, curr, depth)) {
                bestNode = nearestNeighborSearchHelper(x, y, depth + 1, curr.left);
                bestDistance = distance(bestNode, x, y);
                if (currDistance < bestDistance) {
                    bestDistance = currDistance;
                    bestNode = currNode;
                }
                if (circleOverlaps(bestDistance, depth, currNode, x, y)) {
                    Node bestNoderight = nearestNeighborSearchHelper(x, y, depth + 1, curr.right);
                    double bestDistanceRight = distance(bestNoderight, x, y);
                    if (bestDistanceRight < bestDistance) {
                        bestNode = bestNoderight;
                    }
                }
                return bestNode;
            } else {
                bestNode = nearestNeighborSearchHelper(x, y, depth + 1, curr.right);
                bestDistance = distance(bestNode, x, y);
                if (currDistance < bestDistance) {
                    bestDistance = currDistance;
                    bestNode = currNode;
                }
                if (circleOverlaps(bestDistance, depth, currNode, x, y)) {
                    Node bestNodeLeft = nearestNeighborSearchHelper(x, y, depth + 1, curr.left);
                    double bestDistanceLeft = distance(bestNodeLeft, x, y);
                    if (bestDistanceLeft < bestDistance) {
                        bestNode = bestNodeLeft;
                    }
                }
                return bestNode;
            }
        }
    }

    boolean isLeaf(KDTree tree) {
        return tree.left == null && tree.right == null;
    }

    boolean traverseLeft(double x, double y, KDTree tree, int depth) {
        Node n = tree.root;
        if (depth % 2 == 0) {
            return n.x > x;
        } else {
            return n.y > y;
        }
    }

    boolean circleOverlaps(double bestDistance, int depth, Node parent, double x, double y) {
        if (depth % 2 == 1) {
            //compute latitude distance
            double diff = Math.abs(parent.y - y);
            return !(bestDistance <= diff);
        } else {
            //compute longitude distance
            double diff = Math.abs(parent.x - x);
            return !(bestDistance <= diff);
        }
    }

    public static double distance(Node n, double x, double y) {
        double xDiff = n.x - x;
        double yDiff = n.y - y;
        return Math.sqrt(xDiff * xDiff + yDiff * yDiff);
    }

    public static KDTree kdTreeConstructor(ArrayList<Node> list, int depth) {
        if (list.size() == 0) {
            return null;
        }
        int axis = depth % 2;
        PriorityQueue sorted = sort(list, axis);
        ArrayList<Node> left = new ArrayList<>();
        int medianIndex = sorted.size() / 2;
        Node median;
        ArrayList<Node> right = new ArrayList<>();
        for (int i = 0; i < medianIndex; i++) {
            Node toAdd = (Node) sorted.poll();
            left.add(toAdd);
        }
        median = (Node) sorted.poll();

        while (!sorted.isEmpty()) {
            Node toAdd = (Node) sorted.poll();
            right.add(toAdd);
        }
        KDTree leftTree = kdTreeConstructor(left, depth + 1);
        KDTree rightTree = kdTreeConstructor(right, depth + 1);
        return new KDTree(median, leftTree, rightTree);
    }

    private static PriorityQueue<Node> sort(ArrayList<Node> list, int axis) {
        if (axis == 0) {
            PriorityQueue<Node> pq = new PriorityQueue<>(1, (o1, o2) -> {
                double diff = o1.x - o2.x;
                if (diff > 0) {
                    return 1;
                } else if (diff < 0) {
                    return -1;
                } else {
                    return 0;
                }
            });
            for (Node n: list) {
                pq.add(n);
            }
            return pq;
        } else {
            PriorityQueue<Node> pq = new PriorityQueue<>(1, (o1, o2) -> {
                double diff = o1.y - o2.y;
                if (diff > 0) {
                    return 1;
                } else if (diff < 0) {
                    return -1;
                } else {
                    return 0;
                }
            });
            for (Node n: list) {
                pq.add(n);
            }
            return pq;
        }
    }

    @Override
    public String toString() {
        if (left == null && right == null) {
            return "Root: " + root;
        } else if (left == null) {
            return "Root: " + root + "rightside: " + right.toString();
        } else if (right == null) {
            return "Root: " + root + "leftside: " + left.toString();
        } else {
            return "(leftside: " + left.toString()
                    + System.lineSeparator() + "Root: " + root
                    + "rightside: " + right.toString() + ")";
        }
    }
}