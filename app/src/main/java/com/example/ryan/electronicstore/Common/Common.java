package com.example.ryan.electronicstore.Common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.ryan.electronicstore.Model.Product;
import com.example.ryan.electronicstore.Model.Request;
import com.example.ryan.electronicstore.Model.User;


public class Common {

    public static User currentUser;
    public static Request currentRequest;
    public static Product currentProduct;

    public static final String UPDATE = "Update";
    public static final String DELETE = "Delete";

    public static final String Discount = "Discount";
    public static final String Waste = "Waste";

    public static final int PICK_IMAGE_REQUEST = 71;

    public static String convertCodeToStatus(String code)
    {
        if(code.equals("0"))
            return "Placed";
        else if(code.equals("1"))
            return "On my way";
        else
            return "shipped";

    }

    public static boolean isConnectedToInternet(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if(connectivityManager !=null)
        {
            NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
            if(info !=null)
            {
                for (int i=0;i<info.length;i++)
                {
                    if(info[i].getState() == NetworkInfo.State.CONNECTED)
                        return true;
                }
            }
        }
        return false;
    }

}
