package client.william.ffats.Account;

public class Validate {
    static final String ONE_DIGIT = "^(?=.*[0-9]).{6,}$";
    static final String ONE_LOWER_CASE = "^(?=.*[a-z]).{6,}$";
    static final String ONE_UPPER_CASE = "^(?=.*[A-Z]).{6,}$";
    static final String ONE_SPECIAL_CHAR = "^(?=.*[@!#$%*^&+=]).{6,}$";
    static final String NO_SPACE = "^(?=\\S+$).{6,}$";
    static final String MIN_CHAR = "^[a-zA-Z0-9._-].{5,}$";
    static final String MIN_STRING = "^[a-zA-Z0-9._-].{1,}$";
    static final String MIN_PHONENUMBER = "^(?=.*[0-9]).{9,}$";

    public static String validatePassword(String input) {

        if (input.isEmpty()) {
            return "Mật khẩu không được để trống!";
        } else if (!input.matches(MIN_CHAR)) {
            return "Mật khẩu phải có ít nhất 6 kí tự!";
        } else if (!input.matches(ONE_DIGIT)) {
            return "Mật khẩu phải có ít nhất 1 chữ số!";
        } else if (!input.matches(ONE_LOWER_CASE)) {
            return"Mật khẩu phải có ít nhất 1 chữ thường!";
        } else if (!input.matches(ONE_UPPER_CASE)) {
            return "Mật khẩu phải có ít nhất 1 chữ viết hoa!";
        } else if (!input.matches(ONE_SPECIAL_CHAR)) {
            return "Mật khẩu phải có ít nhất 1 kí tự đặc biệt!";
        } else if (!input.matches(NO_SPACE)) {
            return "Mật khẩu không được để khoảng cách!";
        } else {
            return null;
        }
    }

    public static String validatePhoneNumber(String input) {

        if (input.isEmpty()) {
            return "Số điện thoại không được để trống!";
        }else if (!input.matches(MIN_PHONENUMBER))
            return "Số điện thoại không được ít hơn 9 số!";
        else {
            return null;
        }
    }

    public static String validateInput(String input){
        if (input.isEmpty()) {
            return "Tên không được để trống!";
        }else if (!input.matches(MIN_STRING))
            return "Phải ít nhất có một kí tự!";
        else {
            return null;
        }
    }

}
