package shipper.william.ffats.Maps;

import android.util.Log;

import androidx.annotation.NonNull;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class GraphConstructor {


    //region count function

    /**
     * count Node that contains more than 2 way inside,then add it into vertices
     * @param nodes     node list
     * @param vertices  vertex list
     * @return  Qty of vertices
     */
    public static int VertexCount(ArrayList<Node> nodes, ArrayList<Node> vertices) {
        int count = 0;
        for (Node node :
                nodes) {
            if (node.getWay().stream().count() > 1) {
                vertices.add(node);
                count++;
            }
        }
        return count;

    }

    /**
     * count way and print out
     * @param ways
     */
    public static void wayCount(ArrayList<Way> ways) {
        Log.e("Way", "count: " + ways.stream().count());
    }

    /**
     * count node and print out
     * @param nodes
     */
    public static void nodeCount(ArrayList<Node> nodes) {
        Log.e("Node", "count: " + nodes.stream().count());
    }

    //endregion

    //region graph constructor
    /**
     * remove node which don't contains any "way"
     * @param nodes
     */
    public static void removeBlankNode(ArrayList<Node> nodes){
        nodes.removeIf(n -> (n.getWay().stream().count() < 1));
    }

    /**
     * construcst graph(Double) for Shortest path algorithm
     * @param mapValue
     */
    public static void graphConstructor(MapValue mapValue) {
        mapValue.setMAX_length(VertexCount(mapValue.getNodes(), mapValue.getVertices()));
        removeBlankNode(mapValue.getNodes());

        Log.e("debug", "onMapReady: " + "vertex count: " + mapValue.getMAX_length());
        mapValue.setGraph(new Double[mapValue.getMAX_length()][mapValue.getMAX_length()]);

        for (int i = 0; i < mapValue.getMAX_length(); i++) {
            for (int j = 0; j < mapValue.getMAX_length(); j++) {
                calculateDistanceBetweenVertex(mapValue,i,j);
            }
        }
    }
    /**
     * calculate distance between vertex
     * @param mapValue
     */
    public static void calculateDistanceBetweenVertex(@NonNull MapValue mapValue, int i, int j){
        Node startVertex = mapValue.getVertices().get(i);
        Node endVertex = mapValue.getVertices().get(j);

        if (i == j) {
            mapValue.getGraph()[i][j] = 0.0d;
            return;
        }

        //find common way in node
        Way way = null;
        findWayLoop:
        for (Way wayI :
                startVertex.getWay()) {
            for (Way wayJ :
                    endVertex.getWay()) {
                if (wayI == wayJ) {
                    way = wayJ;
                    break findWayLoop;
                }
            }
        }

        // if don't have any common way, then set to MAX distance
        if (way != null) {

            Double totalDistance = 0d;

            Node previousNode = new Node();
            boolean firstTimeHuh = true;

            boolean isStartNode = false;
            boolean isFinishNode = false;

            // calculate distance from previous node to current node
            // then set current node to previous node
            // only start calculate from node after first node found
            for (Node currentNode :
                    way.getNodes()) {

                if (!firstTimeHuh && (isFinishNode || isStartNode)) {

                    totalDistance += distance(currentNode.getLatitude(), previousNode.getLatitude(), currentNode.getLongitude(), previousNode.getLongitude());

                    previousNode.setID(currentNode.getID());
                    previousNode.setLatitude(currentNode.getLatitude());
                    previousNode.setLongitude(currentNode.getLongitude());
                }
                // if this node is start node or finish node
                // set checkpoint and set current node to previous node
                //---------------------------------------------
                if (way.isOneWay()){
                    if (currentNode.getID().equals(startVertex.getID())) {
                        previousNode.setID(currentNode.getID());
                        previousNode.setLatitude(currentNode.getLatitude());
                        previousNode.setLongitude(currentNode.getLongitude());
                        firstTimeHuh = false;
                        isStartNode = true;
                    }

                    if (currentNode.getID().equals(endVertex.getID())) {
                        if (isStartNode == true){
                            previousNode.setID(currentNode.getID());
                            previousNode.setLatitude(currentNode.getLatitude());
                            previousNode.setLongitude(currentNode.getLongitude());
                            firstTimeHuh = false;
                            isFinishNode = true;
                        }else {
                            mapValue.getGraph()[i][j] = Double.MAX_VALUE;
                            return;
                        }
                    }
                }else{
                    if (currentNode.getID().equals(startVertex.getID())) {
                        previousNode.setID(currentNode.getID());
                        previousNode.setLatitude(currentNode.getLatitude());
                        previousNode.setLongitude(currentNode.getLongitude());
                        firstTimeHuh = false;
                        isStartNode = true;
                    }

                    if (currentNode.getID().equals(endVertex.getID())) {
                        previousNode.setID(currentNode.getID());
                        previousNode.setLatitude(currentNode.getLatitude());
                        previousNode.setLongitude(currentNode.getLongitude());
                        firstTimeHuh = false;
                        isFinishNode = true;
                    }
                }
                //-----------------------------------------------
            }
            mapValue.getGraph()[i][j] = totalDistance;

        } else {
            mapValue.getGraph()[i][j] = Double.MAX_VALUE;
        }
    }

    /**
     * get distance between 2 node with height
     * @param lat1  latitude of node 1
     * @param lat2  latitude of node 2
     * @param lon1  longitude of node 1
     * @param lon2  longitude of node 2
     * @param el1   height of node 1
     * @param el2   height of node 2
     * @return distance by meters
     */
    public static double distance(double lat1, double lat2, double lon1,
                                  double lon2, double el1, double el2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);// Pythagorean theorem
        return Math.sqrt(distance);// Pythagorean theorem
    }

    /**
     * get distance between 2 node with no height
     * @param lat1  latitude of node 1
     * @param lat2  latitude of node 2
     * @param lon1  longitude of node 1
     * @param lon2  longitude of node 2
     * @return distance by meters
     */
    public static double distance(double lat1, double lat2, double lon1,
                                  double lon2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distance = R * c * 1000; // convert to meters


        return distance;
    }
    //endregion

    /**
     * print time now
     * @return
     */
    public static String getTimeToString() {
        Date date = new Date(System.currentTimeMillis());
        Format format = new SimpleDateFormat("HH:mm:ss:SSS");
        return format.format(date);
    }

    /**
     * find closest node by distance
     * @param nodes         all node list
     * @param latitude      input node latitude
     * @param longitude     input node longitude
     * @return node that closest with input value
     */
    public static Node findClosestNode(@NonNull ArrayList<Node> nodes, Double latitude, Double longitude){
        Node closestNode = null;

        Double minDistance = 99999d;
        for (Node node :
                nodes) {
            Double currentDistance = distance(node.getLatitude(),latitude,node.getLongitude(),longitude);
            if(currentDistance < minDistance){
                closestNode = node;
                minDistance = currentDistance;
            }
        }
        return closestNode;
    }
}
