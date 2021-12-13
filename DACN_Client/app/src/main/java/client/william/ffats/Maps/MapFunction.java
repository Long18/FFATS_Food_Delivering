package client.william.ffats.Maps;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class MapFunction {
    //region DrawVertex

    /**
     * draw line between vertices
     * @param mMap          map
     * @param findList      list vertices found from start to finish
     * @param startNode     start node
     * @param finishNode    finish node
     * @return  List of Polyline contains all way to vertex
     */
    public static ArrayList<Polyline> DrawVertex(GoogleMap mMap, ArrayList<Node> findList, Node startNode, Node finishNode){
        ArrayList<Polyline> lines = new ArrayList();

        int findListCount = (int) findList.stream().count();
        boolean isFirstTimeHuh = true;

        for (int i = findListCount - 1; i >= 0; i--) {

            //print way
            PolylineOptions polylineOptions = new PolylineOptions();
            if (isFirstTimeHuh) {
                //draw start vertex to next vertex
                polylineOptions.clickable(false).add(new LatLng(startNode.getLatitude(), startNode.getLongitude()));
                isFirstTimeHuh = false;
            } else {
                polylineOptions.clickable(false).add(new LatLng(findList.get(i + 1).getLatitude(), findList.get(i + 1).getLongitude()));
            }
            polylineOptions.clickable(false).add(new LatLng(findList.get(i).getLatitude(), findList.get(i).getLongitude()));
            lines.add(mMap.addPolyline(polylineOptions));
        }

        PolylineOptions polylineOptions = new PolylineOptions();
        //draw next vertex to finish vertex
        polylineOptions.clickable(false).add(new LatLng(findList.get(0).getLatitude(), findList.get(0).getLongitude()));
        polylineOptions.clickable(false).add(new LatLng(finishNode.getLatitude(), finishNode.getLongitude()));
        lines.add(mMap.addPolyline(polylineOptions));
        return lines;
    }

    //endregion

    //region DrawVertexAndWay

    /**
     * draw line between vertices and node in way
     * @param mMap          map
     * @param findList      list vertices found from start to finish
     * @param startNode     start node
     * @param finishNode    finish node
     * @return  List of Polyline contains all way to vertex
     */
    public static ArrayList<Polyline> DrawVertexAndWay(GoogleMap mMap, ArrayList<Node> findList, Node startNode, Node finishNode){
        try{
            ArrayList<Polyline> lines = new ArrayList();


            int findListCount = (int) findList.stream().count();
            if (findListCount > 0){
                boolean isFirstTimeHuh = true;

                for (int i = findListCount - 1; i >= 0; i--) {
                    ArrayList<Polyline> temp;
                    //print way
                    if (isFirstTimeHuh) {
                        //draw start vertex to next vertex

                        temp = DrawWay(mMap,startNode,findList.get(i));
                        isFirstTimeHuh = false;
                    } else {
                        temp = DrawWay(mMap,findList.get(i + 1),findList.get(i));
                    }
                    lines.addAll(temp);
                }

                ArrayList<Polyline> temp = DrawWay(mMap,findList.get(0),finishNode);
                lines.addAll(temp);
            }else{
                ArrayList<Polyline> temp = DrawWay(mMap,startNode,finishNode);
                lines.addAll(temp);
            }

            return lines;
        }catch( Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     *  draw line between node inside a way
     * @param mMap          map
     * @param startNode     start node
     * @param finishNode    finish node
     * @return
     */
    public static ArrayList<Polyline> DrawWay(GoogleMap mMap, Node startNode, Node finishNode){
        ArrayList<Polyline> lines = new ArrayList();

        // find common way between startNode and FinishNode
        Way way = null;
        for (Way itemStart :
                startNode.getWay()){
            for (Way itemFinish :
                    finishNode.getWay()) {
                if (itemStart == itemFinish){
                    way = itemStart;
                }
            }
        }

        // checkpoint
        boolean isFirstTimeHuh = true;
        boolean isStartNode = false;
        boolean isFinishNode = false;

        // draw line
        // only start draw from node after first node found(start or finish)
        long nodeCount = way.getNodes().stream().count();
        for (int i = 0; i < nodeCount ; i++) {

            PolylineOptions polylineOptions = new PolylineOptions();

            if (!isFirstTimeHuh && (isStartNode || isFinishNode)){
                polylineOptions.clickable(false).add(new LatLng(way.getNodes().get(i - 1).getLatitude(),way.getNodes().get(i - 1).getLongitude()));
                polylineOptions.clickable(false).add(new LatLng(way.getNodes().get(i).getLatitude(),way.getNodes().get(i).getLongitude()));
                lines.add(mMap.addPolyline(polylineOptions));
            }
            // if this node is start node or finish node
            // set checkpoint
            if (way.getNodes().get(i) == startNode){
                isStartNode = true;
                isFirstTimeHuh = false;
            }

            if (way.getNodes().get(i) == finishNode){
                isFinishNode = true;
                isFirstTimeHuh = false;
            }
            // end
            if (isStartNode && isFinishNode){
                break;
            }
        }
        return lines;
    }

    //endregion
}
