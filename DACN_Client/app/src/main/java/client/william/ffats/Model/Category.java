package client.william.ffats.Model;

public class Category {
    private String Name;
    private String Image;
    private String Address;

    public Category() {
    }

    public Category(String name, String image, String address) {
        Name = name;
        Image = image;
        Address = address;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }
}
