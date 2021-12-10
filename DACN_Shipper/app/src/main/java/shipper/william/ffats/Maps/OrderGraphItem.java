package shipper.william.ffats.Maps;

import java.util.ArrayList;

public class OrderGraphItem {
    private ArrayList<Node> wayList;

    private Double distance;

    public OrderGraphItem() {
        wayList = new ArrayList<>();
    }

    public OrderGraphItem(ArrayList<Node> wayList, Double distance) {
        wayList = wayList;
        this.distance = distance;
    }

    public ArrayList<Node> getWayList() {
        return wayList;
    }

    public void setWayList(ArrayList<Node> wayList) {
        wayList = wayList;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }
}
