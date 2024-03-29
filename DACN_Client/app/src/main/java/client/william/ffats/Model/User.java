package client.william.ffats.Model;

public class User {
    private String Name;
    private String Password;
    private String Phone;
    private String Image;
    private String Address;
    private String Email;
    private String IsStaff;

    public User() {
    }

    public User(String name, String password, String phone) {
        Name = name;
        Password = password;
        Phone = phone;
        IsStaff = "false";
    }

    public User(String name, String password, String phone, String image, String address, String email, String isStaff) {
        Name = name;
        Password = password;
        Phone = phone;
        Image = image;
        Address = address;
        Email = email;
        IsStaff = "false";
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }

    public String getIsStaff() {
        return IsStaff;
    }

    public void setIsStaff(String isStaff) {
        IsStaff = isStaff;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }
}
