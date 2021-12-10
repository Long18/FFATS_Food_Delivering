package shipper.william.ffats.Model;

public class Shipper {

    private String name,phone,password,image,sumOrders;

    public Shipper(String name, String phone, String password, String image, String sumOrders) {
        this.name = name;
        this.phone = phone;
        this.password = password;
        this.image = image;
        this.sumOrders = sumOrders;
    }

    public Shipper() {

    }

    public String getSumOrders() {
        if (sumOrders == null)
        {
            sumOrders = "0";
        }
        return sumOrders;
    }

    public void setSumOrders(String sumOrders) {
        this.sumOrders = sumOrders;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
