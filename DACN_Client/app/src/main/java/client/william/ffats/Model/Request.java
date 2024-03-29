package client.william.ffats.Model;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class Request {
    private String phone,name,address,total,status,comment,latLng,paymentMethod;
    private List<Order> foods;

    public Request() {
    }

    public Request(String phone, String name, String address, String total, String status, String comment, String latLng, String paymentMethod, List<Order> foods) {
        this.phone = phone;
        this.name = name;
        this.address = address;
        this.total = total;
        this.status = status;
        //If status is 0:Placed, 1:Shipping, 2:Shipped.
        this.comment = comment;
        this.latLng = latLng;
        this.paymentMethod = paymentMethod;
        this.foods = foods;
    }

    /*public Request(String phone, String name, String address, String total, String status, String comment, String latLng, String paymentMethod, List<Order> foods) {
        this.phone = phone;
        this.name = name;
        this.address = address;
        this.total = total;
        this.status = status;
        //If status is 0:Placed, 1:Shipping, 2:Shipped.
        this.comment = comment;
        this.latLng = latLng;
        this.paymentMethod = paymentMethod;
        this.foods = foods;
    }*/


    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
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
