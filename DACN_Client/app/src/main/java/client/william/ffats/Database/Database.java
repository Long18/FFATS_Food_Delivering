package client.william.ffats.Database;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.util.ArrayList;
import java.util.List;

import client.william.ffats.Model.Favorites;
import client.william.ffats.Model.Order;

public class Database extends SQLiteAssetHelper {

    private static final String DB_NAME = "FFATS_Cart_DB.db";
    private static final int DB_VER = 1;

    public Database(Context context) {
        super(context, DB_NAME, null, DB_VER);
    }

    @SuppressLint("Range")
    public List<Order> getCart(String userPhone){
        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        String[] sqlSelect = {"UserPhone","ProductName","ProductId","Quantity","Price","Discount","Image"};
        String sqlTable = "OrderDetail";

        qb.setTables(sqlTable);
        Cursor cursor = qb.query(db,sqlSelect,"UserPhone=?",
                new String[]{userPhone},null,null,null);

        final List<Order> result = new ArrayList<>();
        if (cursor.moveToFirst())
        {
            do {
                result.add(new Order(
                        cursor.getString(cursor.getColumnIndex("UserPhone")),
                        cursor.getString(cursor.getColumnIndex("ProductId")),
                        cursor.getString(cursor.getColumnIndex("ProductName")),
                        cursor.getString(cursor.getColumnIndex("Quantity")),
                        cursor.getString(cursor.getColumnIndex("Price")),
                        cursor.getString(cursor.getColumnIndex("Discount")),
                        cursor.getString(cursor.getColumnIndex("Image"))));
            }while (cursor.moveToNext());
        }
            return result;
    }

    public boolean checkFoodExists(String foodId,String userPhone){
        boolean flag = false;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        String SQLQuery = String.format("SELECT * From OrderDetail WHERE UserPhone = '%s' AND ProductId = '%s'",userPhone,foodId);
        cursor = db.rawQuery(SQLQuery,null);
        if (cursor.getCount()>0){
            flag = true;
        }
        else {
            flag = false;
        }

        cursor.close();
        return flag;
    }

    public void addToCart(Order order) {
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("INSERT OR REPLACE INTO OrderDetail(UserPhone,ProductId,ProductName,Quantity,Price,Discount,Image) VALUES('%s','%s','%s','%s','%s','%s','%s');",
                order.getUserPhone(),
                order.getProductId(),
                order.getProductName(),
                order.getQuantity(),
                order.getPrice(),
                order.getDiscount(),
                order.getImage());
        db.execSQL(query);
    }

    public void cleanCart(String userPhone){
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("DELETE FROM OrderDetail WHERE UserPhone = '%s'",userPhone);
        db.execSQL(query);
    }

    public int getCountCart(String userPhone) {
        int count=0;

        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("SELECT COUNT(*) FROM OrderDetail WHERE UserPhone ='%s'", userPhone);
        Cursor cursor = db.rawQuery(query,null);
        if(cursor.moveToFirst())
        {
            do{
                count = cursor.getInt(0);
            }while (cursor.moveToNext());
        }
        return count;
    }

    public void updateCart(Order order) {
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("UPDATE OrderDetail SET Quantity= '%s' WHERE UserPhone = '%s' AND ProductId ='%s'",order.getQuantity(),order.getUserPhone(),order.getProductId());
        db.execSQL(query);
    }

    public void removeCart(String productId, String phone) {
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("DELETE FROM OrderDetail WHERE UserPhone = '%s' AND ProductId = '%s'",phone,productId);
        db.execSQL(query);
    }

    public void increaseCart(String userPhone, String foodId) {
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("UPDATE OrderDetail SET Quantity= Quantity +1 WHERE UserPhone = '%s' AND ProductId ='%s'",userPhone,foodId);
        db.execSQL(query);
    }

    @SuppressLint("Range")
    public List<Favorites> getFavorites(String userPhone){
        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        String[] sqlSelect = {"UserPhone","FoodId","FoodName","FoodPrice","FoodMenuId","FoodImage","FoodDiscount","FoodDescription"};
        String sqlTable = "Favorites";

        qb.setTables(sqlTable);
        Cursor cursor = qb.query(db,sqlSelect,"UserPhone=?",
                new String[]{userPhone},null,null,null);

        final List<Favorites> result = new ArrayList<>();
        if (cursor.moveToFirst())
        {
            do {
                result.add(new Favorites(

                        cursor.getString(cursor.getColumnIndex("FoodId")),
                        cursor.getString(cursor.getColumnIndex("FoodName")),
                        cursor.getString(cursor.getColumnIndex("FoodPrice")),
                        cursor.getString(cursor.getColumnIndex("FoodMenuId")),
                        cursor.getString(cursor.getColumnIndex("FoodImage")),
                        cursor.getString(cursor.getColumnIndex("FoodDiscount")),
                        cursor.getString(cursor.getColumnIndex("FoodDescription")),
                        cursor.getString(cursor.getColumnIndex("UserPhone"))));
            }while (cursor.moveToNext());
        }
        return result;
    }

    public void addToFavorites(Favorites food){
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("INSERT INTO Favorites(FoodId,FoodName,FoodPrice,FoodMenuId,FoodImage,FoodDiscount,FoodDescription,UserPhone) VALUES('%s','%s','%s','%s','%s','%s','%s','%s');"
                ,food.getFoodId(),food.getFoodName(),food.getFoodPrice(),food.getFoodMenuId(),food.getFoodImage(),food.getFoodDiscount(),food.getFoodDescription(),food.getUserPhone());
        db.execSQL(query);
    }

    public void removeFavorites(String foodId,String userPhone){
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("DELETE FROM Favorites WHERE FoodId='%s' and UserPhone = '%s';",foodId,userPhone);
        db.execSQL(query);
    }

    public boolean isFavorites(String foodId,String userPhone){
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("SELECT * FROM Favorites WHERE FoodId='%s' and UserPhone = '%s';",foodId,userPhone);
        Cursor cursor = db.rawQuery(query,null);
        if (cursor.getCount() <= 0){
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

}
