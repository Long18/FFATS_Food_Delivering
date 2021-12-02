package server.william.ffats.Model;

import java.util.ArrayList;
import java.util.List;

import server.william.ffats.Maps.Node;

public class Request {
    private String phone,name,address,total,status,comment,latLng;
    private List<Order> foods;

    private Double latitude,longitude;
    private Node closestVertex;
    private ArrayList<Node> paths;

    public Request() {
    }

    public Request(String phone, String name, String address, String total, String status, String comment,String latLng, List<Order> foods) {
        this.phone = phone;
        this.name = name;
        this.address = address;
        this.total = total;
        this.status = status;
        this.comment = comment;
        this.latLng = latLng;
        //If status is 0:Placed, 1:Shipping, 2:Shipped.
        this.foods = foods;
    }

    public ArrayList<Node> getPaths() {
        return paths;
    }

    public void setPaths(ArrayList<Node> paths) {
        this.paths = paths;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Node getClosestVertex() {
        return closestVertex;
    }

    public void setClosestVertex(Node closestVertex) {
        this.closestVertex = closestVertex;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getLatLng() {
        return latLng;
    }

    public void setLatLng(String latLng) {
        this.latLng = latLng;
    }

    public List<Order> getFoods() {
        return foods;
    }

    public void setFoods(List<Order> foods) {
        this.foods = foods;
    }
}
