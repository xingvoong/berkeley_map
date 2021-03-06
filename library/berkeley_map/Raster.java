import java.util.HashMap;

/**
 * This class provides all code necessary to take a query box and produce
 * a query result. The getMapRaster method must return a Map containing all
 * seven of the required fields, otherwise the front end code will probably
 * not draw the output correctly.
 */
public class Rasterer {
    /** The max image depth level. */
    public static final int MAX_DEPTH = 7;
    public static final int SL = 288200;

    /**
     * Takes a user query and finds the grid of images that best matches the query. These images
     * will be combined into one big image (rastered) by the front end. The grid of images must obey
     * the following properties, where image in the grid is referred to as a "tile".
     * <ul>
     *     <li>The tiles collected must cover the most longitudinal distance per pixel (LonDPP)
     *     possible, while still covering less than or equal to the amount of longitudinal distance
     *     per pixel in the query box for the user viewport size.</li>
     *     <li>Contains all tiles that intersect the query bounding box that fulfill the above
     *     condition.</li>
     *     <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     * </ul>
     * @param params The RasterRequestParams containing coordinates of the query box and the browser
     *               viewport width and height.
     * @return A valid RasterResultParams containing the computed results.
     */
    public RasterResultParams getMapRaster(RasterRequestParams params) {
        /*
         * Hint: Define additional classes to make it easier to pass around multiple values, and
         * define additional methods to make it easier to test and reason about code. */
        double londDPP = lonDPP(params.lrlon, params.ullon, params.w);
        int depth = getDepth(londDPP);
        HashMap<String, Double> xVals = getxValues(depth, params.ullon, params.lrlon);
        HashMap<String, Double> yVals = getyValues(depth, params.ullat, params.lrlat);
        RasterResultParams.Builder toBuild = new RasterResultParams.Builder();
        toBuild.setDepth(depth);
        toBuild.setQuerySuccess(true);
        toBuild.setRasterUlLon(xVals.get("ULLon"));
        toBuild.setRasterUlLat(yVals.get("ULLat"));
        toBuild.setRasterLrLon(xVals.get("LRLon"));
        toBuild.setRasterLrLat(yVals.get("LRLat"));
        String[][] renderGrid = getRenderGrid(depth, xVals.get("ULxVal"), yVals.get("ULyVal"), xVals.get("LRxVal"), yVals.get("LRyVal"));
        toBuild.setRenderGrid(renderGrid);
        RasterResultParams result = toBuild.create();
        return result;
    }

    /**
     * Calculates the lonDPP of an image or query box
     * @param lrlon Lower right longitudinal value of the image or query box
     * @param ullon Upper left longitudinal value of the image or query box
     * @param width Width of the query box or image
     * @return lonDPP
     */
    private double lonDPP(double lrlon, double ullon, double width) {
        return (lrlon - ullon) / width;
    }

    private int getDepth(double lonDPP) {
        double ftPixel = lonDPP * SL;
        System.out.println(ftPixel);
        double d0 = 98.94561767578125;
        double d1 = 49.472808837890625;
        double d2 = 24.736404418945312;
        double d3 = 12.368202209472656;
        double d4 = 6.184101104736328;
        double d5 = 3.092050552368164;
        double d6 = 1.546025276184082;
        if (ftPixel >= d0) {
            return 0;
        } else if (ftPixel < d0 && ftPixel >= d1) {
            return 1;
        } else if (ftPixel < d1 && ftPixel >= d2) {
            return 2;
        } else if (ftPixel < d2 && ftPixel >= d3) {
            return 3;
        } else if (ftPixel < d3 && ftPixel >= d4) {
            return 4;
        } else if (ftPixel < d4 && ftPixel >= d5) {
            return 5;
        } else if (ftPixel < d5 && ftPixel >= d6) {
            return 6;
        } else {
            return 7;
        }
    }

    private HashMap<String, Double> getxValues(double depth, double ullon, double lrlon) {
        HashMap<String, Double> toReturn = new HashMap<>();
        double numTiles = Math.pow(2, depth);
        double xdist = (MapServer.ROOT_ULLON - MapServer.ROOT_LRLON) / numTiles;
        int UlxVal = 0;
        double currLon = MapServer.ROOT_ULLON - xdist;
        while (currLon < ullon) {
            UlxVal += 1;
            currLon -= xdist;
        }
        toReturn.put("ULLon", currLon + xdist);
        toReturn.put("ULxVal", (double) UlxVal);
        int LRxVal = UlxVal;
        while (currLon < lrlon) {
            if (LRxVal >= numTiles - 1) {
                break;
            }
            LRxVal += 1;
            currLon -= xdist;
        }
        toReturn.put("LRLon", currLon);
        toReturn.put("LRxVal", (double) LRxVal);
        return toReturn;
    }

    private HashMap<String, Double> getyValues(double depth, double ullat, double lrlat) {
        HashMap<String, Double> toReturn = new HashMap<>();
        double numTiles = Math.pow(2, depth);
        double ydist = (MapServer.ROOT_ULLAT - MapServer.ROOT_LRLAT) / numTiles;
        int ULyVal = 0;
        double currLat = MapServer.ROOT_ULLAT - ydist;
        while (currLat > ullat) {
            ULyVal += 1;
            currLat -= ydist;
        }
        toReturn.put("ULyVal", (double) ULyVal);
        toReturn.put("ULLat", currLat + ydist);
        int LRyVal = ULyVal;
        while (currLat > lrlat) {
            if (LRyVal >= numTiles - 1) {
                break;
            }
            LRyVal += 1;
            currLat -= ydist;
        }
        toReturn.put("LRyVal", (double) LRyVal);
        toReturn.put("LRLat", currLat);
        return toReturn;
    }

    private String[][] getRenderGrid(int depth, double ULxVal, double ULyVal, double LRxVal, double LRyVal) {
        int x0 = (int) Math.round(ULxVal);
        int y0 = (int) Math.round(ULyVal);
        int x1 = (int) Math.round(LRxVal);
        int y1 = (int) Math.round(LRyVal);
        int iterx = x1 - x0 + 1;
        int itery = y1 - y0 + 1;
        String[][] grid = new String[itery][iterx];
        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[0].length; x++) {
                grid[y][x] = "d" + String.valueOf(depth) + "_x" + String.valueOf(x0 + x) + "_y" + String.valueOf(y0 + y) + ".png";
            }
        }
        return grid;
    }
}