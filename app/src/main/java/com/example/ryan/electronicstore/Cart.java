package com.example.ryan.electronicstore;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ryan.electronicstore.Common.Common;
import com.example.ryan.electronicstore.Database.Database;
import com.example.ryan.electronicstore.Model.Order;
import com.example.ryan.electronicstore.Model.Product;
import com.example.ryan.electronicstore.Model.Request;
import com.example.ryan.electronicstore.ViewHolder.CartAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class Cart extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference requests;

    TextView txtTotalPrice;
    Button btnPlace;

    List<Order> cart = new ArrayList<>();

    CartAdapter adapter;

    DatePickerDialog datePickerDialog;
    private EditText edtDelivery;
    private EditText edtComment;

    Product lstProducts;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);


        //Firebase
        database = FirebaseDatabase.getInstance();
        requests= database.getReference("Requests");

        //init
        recyclerView = (RecyclerView)findViewById(R.id.listCart);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);


        txtTotalPrice = (TextView)findViewById(R.id.total);
        btnPlace = (Button)findViewById(R.id.btnPlaceOrder);

        btnPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //create new request
                if(cart.size() > 0)
                    showAlertDialog();
                else
                    Toast.makeText(Cart.this ,"Your Cart Is Empty!",Toast.LENGTH_SHORT).show();

            }


        });

        loadListProduct();

    }



    private void showAlertDialog() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Cart.this);
        alertDialog.setTitle("Last Step");
        alertDialog.setTitle("Enter when you want delivered");

        LayoutInflater inflater = this.getLayoutInflater();
        View order_delivery_comment = inflater.inflate(R.layout.order_delivery_comment,null);

        edtDelivery = (EditText)order_delivery_comment.findViewById(R.id.edtDelivery);
        edtComment = (EditText)order_delivery_comment.findViewById(R.id.edtComment);

        alertDialog.setView(order_delivery_comment);
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        edtDelivery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // calender class's instance and get current date , month and year from calender
                final Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR); // current year
                int mMonth = c.get(Calendar.MONTH); // current month
                int mDay = c.get(Calendar.DAY_OF_MONTH); // current day
                // date picker dialog
                datePickerDialog = new DatePickerDialog(Cart.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // set day of month , month and year value in the edit text
                                edtDelivery.setText(dayOfMonth + "/"
                                        + (monthOfYear + 1) + "/" + year);

                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });


        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //new request



                Request request = new Request(
                        Common.currentUser.getUserName(),
                        edtDelivery.getText().toString(),
                        txtTotalPrice.getText().toString(),
                        "0",
                        edtComment.getText().toString(),
                        cart
                );

                //submit to firebase
                //Using Syste.currentMilli
                requests.child(String.valueOf(System.currentTimeMillis()))
                        .setValue(request);
                //delete cart
                new Database(getBaseContext()).cleanCart();
                Toast.makeText(Cart.this, "Thank you Order Placed", Toast.LENGTH_SHORT).show();
                createPdf();
                finish();;



            }
        });

        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface,int i){

            }
        });
        alertDialog.show();

    }

    private void createPdf(){
        // create a new document
        PdfDocument document = new PdfDocument();

        // crate a page description
        PdfDocument.PageInfo pageInfo =
                new PdfDocument.PageInfo.Builder(1000, 1000, 1).create();

        // start a page
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        String strl;

        for(Order order:cart)
            strl =(order.getProductName());



        String test = Common.currentUser.getUserName();

        Paint paint = new Paint();
        Paint text = new Paint();
        Paint textSecond = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPaint(paint);

        paint.setColor(Color.BLACK);
        text.setColor(Color.RED);
        textSecond.setColor(Color.CYAN);
        paint.setTextSize(20);
        text.setTextSize(40);
        textSecond.setTextSize(20);
        canvas.drawText("INVOICE", 320, 30, text);
        canvas.drawText("THANK YOU FOR YOUR ORDER BELOW IS YOUR INVOICE", 10, 50, paint);
        canvas.drawText("ORDER PLACED FOR DELIVERY TO :", 10, 75, paint);
        canvas.drawText(Common.currentUser.getUserName(), 340, 75, textSecond);
        canvas.drawText("DELIVERY DATE SELECTED :", 10, 100, paint);
        canvas.drawText(edtDelivery.getText().toString(), 280, 100, textSecond);
        canvas.drawText("COMMENTS ON DELIVERY TO STORE :", 10, 125, paint);
        canvas.drawText(edtComment.getText().toString(), 365, 125, textSecond);
        canvas.drawText("PURCHASE ORDER NUMBER :", 10, 150, paint);
        canvas.drawText(String.valueOf(System.currentTimeMillis()), 280, 150, textSecond);
        canvas.drawText("TOTAL RETAIL VALUE OF GOODS BEING DELIVERED :", 10, 175, paint);
        canvas.drawText(txtTotalPrice.getText().toString(), 490, 175, textSecond);
        int total = 0;
        for(Order order:cart)
            total+=(Integer.parseInt(order.getQuantity()));
        canvas.drawText("TOTAL NUMBER OF PIECES BEING DELIVERED :", 10, 200, paint);
        canvas.drawText(String.valueOf(total), 450, 200, textSecond);
        canvas.drawText("Product Code", 10, 275, paint);
        canvas.drawText("Product Description", 175, 275, paint);
        canvas.drawText("Quantity", 400, 275, paint);
        canvas.drawText("Price", 550, 275, paint);
        canvas.drawText("Total", 700, 275, paint);
        canvas.drawText("-----", 700, 575, paint);
        canvas.drawText(txtTotalPrice.getText().toString(), 700, 600, paint);
        for(Order order:cart) {
            // Object fieldValue = foo.fieldName;
        }

        String[] requestNos = new String[cart.size()];
//canvas doesnt allow multi line had to do i if statements
        for (int i = 0; i < cart.size(); i++) {
            requestNos[i] = cart.get(0).getProductName();
            if (0 == cart.size())
            {
                //canvas.drawText(cart.get(0).getProductName(), 10, 325, paint);

            }
            else if (1 == cart.size())
            {
                canvas.drawText(cart.get(0).getProductId(), 10, 325, paint);
                canvas.drawText(cart.get(0).getProductName(), 175, 325, paint);
                canvas.drawText(cart.get(0).getQuantity(), 425, 325, paint);
                canvas.drawText(cart.get(0).getPrice(), 550, 325, paint);
                int priceTotal = 0;
                priceTotal+=(Integer.parseInt(cart.get(0).getPrice()))*(Integer.parseInt(cart.get(0).getQuantity()));
                canvas.drawText(String.valueOf(priceTotal), 700, 325, paint);

            }
            else if (2 == cart.size())
            {
                canvas.drawText(cart.get(0).getProductId(), 10, 325, paint);
                canvas.drawText(cart.get(0).getProductName(), 175, 325, paint);
                canvas.drawText(cart.get(0).getQuantity(), 425, 325, paint);
                canvas.drawText(cart.get(0).getPrice(), 550, 325, paint);
                int priceTotal = 0;
                priceTotal+=(Integer.parseInt(cart.get(0).getPrice()))*(Integer.parseInt(cart.get(0).getQuantity()));
                canvas.drawText(String.valueOf(priceTotal), 700, 325, paint);
                canvas.drawText(cart.get(1).getProductId(), 10, 350, paint);
                canvas.drawText(cart.get(1).getProductName(), 175, 350, paint);
                canvas.drawText(cart.get(1).getQuantity(), 425, 350, paint);
                canvas.drawText(cart.get(1).getPrice(), 550, 350, paint);
                //int priceTotal = 0;
                priceTotal=(Integer.parseInt(cart.get(1).getPrice()))*(Integer.parseInt(cart.get(1).getQuantity()));
                canvas.drawText(String.valueOf(priceTotal), 700, 350, paint);


            }
            else if (3 == cart.size())
            {
                canvas.drawText(cart.get(0).getProductId(), 10, 325, paint);
                canvas.drawText(cart.get(0).getProductName(), 175, 325, paint);
                canvas.drawText(cart.get(0).getQuantity(), 425, 325, paint);
                canvas.drawText(cart.get(0).getPrice(), 550, 325, paint);
                int priceTotal = 0;
                priceTotal+=(Integer.parseInt(cart.get(0).getPrice()))*(Integer.parseInt(cart.get(0).getQuantity()));
                canvas.drawText(String.valueOf(priceTotal), 700, 325, paint);
                canvas.drawText(cart.get(1).getProductId(), 10, 350, paint);
                canvas.drawText(cart.get(1).getProductName(), 175, 350, paint);
                canvas.drawText(cart.get(1).getQuantity(), 425, 350, paint);
                canvas.drawText(cart.get(1).getPrice(), 550, 350, paint);
                priceTotal=(Integer.parseInt(cart.get(1).getPrice()))*(Integer.parseInt(cart.get(1).getQuantity()));
                canvas.drawText(String.valueOf(priceTotal), 700, 350, paint);
                canvas.drawText(cart.get(2).getProductId(), 10, 375, paint);
                canvas.drawText(cart.get(2).getProductName(), 175, 375, paint);
                canvas.drawText(cart.get(2).getQuantity(), 425, 375, paint);
                canvas.drawText(cart.get(2).getPrice(), 550, 375, paint);
                priceTotal=(Integer.parseInt(cart.get(2).getPrice()))*(Integer.parseInt(cart.get(2).getQuantity()));
                canvas.drawText(String.valueOf(priceTotal), 700, 375, paint);


            }
            else if (4 == cart.size())
            {
                canvas.drawText(cart.get(0).getProductId(), 10, 325, paint);
                canvas.drawText(cart.get(0).getProductName(), 175, 325, paint);
                canvas.drawText(cart.get(0).getQuantity(), 425, 325, paint);
                canvas.drawText(cart.get(0).getPrice(), 550, 325, paint);
                int priceTotal = 0;
                priceTotal+=(Integer.parseInt(cart.get(0).getPrice()))*(Integer.parseInt(cart.get(0).getQuantity()));
                canvas.drawText(String.valueOf(priceTotal), 700, 325, paint);
                canvas.drawText(cart.get(1).getProductId(), 10, 350, paint);
                canvas.drawText(cart.get(1).getProductName(), 175, 350, paint);
                canvas.drawText(cart.get(1).getQuantity(), 425, 350, paint);
                canvas.drawText(cart.get(1).getPrice(), 550, 350, paint);
                priceTotal=(Integer.parseInt(cart.get(1).getPrice()))*(Integer.parseInt(cart.get(1).getQuantity()));
                canvas.drawText(String.valueOf(priceTotal), 700, 350, paint);
                canvas.drawText(cart.get(2).getProductId(), 10, 375, paint);
                canvas.drawText(cart.get(2).getProductName(), 175, 375, paint);
                canvas.drawText(cart.get(2).getQuantity(), 425, 375, paint);
                canvas.drawText(cart.get(2).getPrice(), 550, 375, paint);
                priceTotal=(Integer.parseInt(cart.get(2).getPrice()))*(Integer.parseInt(cart.get(2).getQuantity()));
                canvas.drawText(String.valueOf(priceTotal), 700, 375, paint);
                canvas.drawText(cart.get(3).getProductId(), 10, 400, paint);
                canvas.drawText(cart.get(3).getProductName(), 175, 400, paint);
                canvas.drawText(cart.get(3).getQuantity(), 425, 400, paint);
                canvas.drawText(cart.get(3).getPrice(), 550, 400, paint);
                priceTotal=(Integer.parseInt(cart.get(3).getPrice()))*(Integer.parseInt(cart.get(3).getQuantity()));
                canvas.drawText(String.valueOf(priceTotal), 700, 400, paint);
            }
            else if (5 == cart.size())
            {
                canvas.drawText(cart.get(0).getProductId(), 10, 325, paint);
                canvas.drawText(cart.get(0).getProductName(), 175, 325, paint);
                canvas.drawText(cart.get(0).getQuantity(), 425, 325, paint);
                canvas.drawText(cart.get(0).getPrice(), 550, 325, paint);
                int priceTotal = 0;
                priceTotal+=(Integer.parseInt(cart.get(0).getPrice()))*(Integer.parseInt(cart.get(0).getQuantity()));
                canvas.drawText(String.valueOf(priceTotal), 700, 325, paint);
                canvas.drawText(cart.get(1).getProductId(), 10, 350, paint);
                canvas.drawText(cart.get(1).getProductName(), 175, 350, paint);
                canvas.drawText(cart.get(1).getQuantity(), 425, 350, paint);
                canvas.drawText(cart.get(1).getPrice(), 550, 350, paint);
                priceTotal=(Integer.parseInt(cart.get(1).getPrice()))*(Integer.parseInt(cart.get(1).getQuantity()));
                canvas.drawText(String.valueOf(priceTotal), 700, 350, paint);
                canvas.drawText(cart.get(2).getProductId(), 10, 375, paint);
                canvas.drawText(cart.get(2).getProductName(), 175, 375, paint);
                canvas.drawText(cart.get(2).getQuantity(), 425, 375, paint);
                canvas.drawText(cart.get(2).getPrice(), 550, 375, paint);
                priceTotal=(Integer.parseInt(cart.get(2).getPrice()))*(Integer.parseInt(cart.get(2).getQuantity()));
                canvas.drawText(String.valueOf(priceTotal), 700, 375, paint);
                canvas.drawText(cart.get(3).getProductId(), 10, 400, paint);
                canvas.drawText(cart.get(3).getProductName(), 175, 400, paint);
                canvas.drawText(cart.get(3).getQuantity(), 425, 400, paint);
                canvas.drawText(cart.get(3).getPrice(), 550, 400, paint);
                priceTotal=(Integer.parseInt(cart.get(3).getPrice()))*(Integer.parseInt(cart.get(3).getQuantity()));
                canvas.drawText(String.valueOf(priceTotal), 700, 400, paint);
                canvas.drawText(cart.get(4).getProductId(), 10, 425, paint);
                canvas.drawText(cart.get(4).getProductName(), 175, 425, paint);
                canvas.drawText(cart.get(4).getQuantity(), 425, 425, paint);
                canvas.drawText(cart.get(4).getPrice(), 550, 425, paint);
                priceTotal=(Integer.parseInt(cart.get(4).getPrice()))*(Integer.parseInt(cart.get(4).getQuantity()));
                canvas.drawText(String.valueOf(priceTotal), 700, 425, paint);
            }
            else if (6 == cart.size())
            {
                canvas.drawText(cart.get(0).getProductId(), 10, 325, paint);
                canvas.drawText(cart.get(0).getProductName(), 175, 325, paint);
                canvas.drawText(cart.get(0).getQuantity(), 425, 325, paint);
                canvas.drawText(cart.get(0).getPrice(), 550, 325, paint);
                int priceTotal = 0;
                priceTotal+=(Integer.parseInt(cart.get(0).getPrice()))*(Integer.parseInt(cart.get(0).getQuantity()));
                canvas.drawText(String.valueOf(priceTotal), 700, 325, paint);
                canvas.drawText(cart.get(1).getProductId(), 10, 350, paint);
                canvas.drawText(cart.get(1).getProductName(), 175, 350, paint);
                canvas.drawText(cart.get(1).getQuantity(), 425, 350, paint);
                canvas.drawText(cart.get(1).getPrice(), 550, 350, paint);
                priceTotal=(Integer.parseInt(cart.get(1).getPrice()))*(Integer.parseInt(cart.get(1).getQuantity()));
                canvas.drawText(String.valueOf(priceTotal), 700, 350, paint);
                canvas.drawText(cart.get(2).getProductId(), 10, 375, paint);
                canvas.drawText(cart.get(2).getProductName(), 175, 375, paint);
                canvas.drawText(cart.get(2).getQuantity(), 425, 375, paint);
                canvas.drawText(cart.get(2).getPrice(), 550, 375, paint);
                priceTotal=(Integer.parseInt(cart.get(2).getPrice()))*(Integer.parseInt(cart.get(2).getQuantity()));
                canvas.drawText(String.valueOf(priceTotal), 700, 375, paint);
                canvas.drawText(cart.get(3).getProductId(), 10, 400, paint);
                canvas.drawText(cart.get(3).getProductName(), 175, 400, paint);
                canvas.drawText(cart.get(3).getQuantity(), 425, 400, paint);
                canvas.drawText(cart.get(3).getPrice(), 550, 400, paint);
                priceTotal=(Integer.parseInt(cart.get(3).getPrice()))*(Integer.parseInt(cart.get(3).getQuantity()));
                canvas.drawText(String.valueOf(priceTotal), 700, 400, paint);
                canvas.drawText(cart.get(4).getProductId(), 10, 425, paint);
                canvas.drawText(cart.get(4).getProductName(), 175, 425, paint);
                canvas.drawText(cart.get(4).getQuantity(), 425, 425, paint);
                canvas.drawText(cart.get(4).getPrice(), 550, 425, paint);
                priceTotal=(Integer.parseInt(cart.get(4).getPrice()))*(Integer.parseInt(cart.get(4).getQuantity()));
                canvas.drawText(String.valueOf(priceTotal), 700, 425, paint);
                canvas.drawText(cart.get(5).getProductId(), 10, 450, paint);
                canvas.drawText(cart.get(5).getProductName(), 175, 450, paint);
                canvas.drawText(cart.get(5).getQuantity(), 425, 450, paint);
                canvas.drawText(cart.get(5).getPrice(), 550, 450, paint);
                priceTotal=(Integer.parseInt(cart.get(5).getPrice()))*(Integer.parseInt(cart.get(5).getQuantity()));
                canvas.drawText(String.valueOf(priceTotal), 700, 450, paint);
            }
            else if (7 == cart.size())
            {
                canvas.drawText(cart.get(0).getProductId(), 10, 325, paint);
                canvas.drawText(cart.get(0).getProductName(), 175, 325, paint);
                canvas.drawText(cart.get(0).getQuantity(), 425, 325, paint);
                canvas.drawText(cart.get(0).getPrice(), 550, 325, paint);
                int priceTotal = 0;
                priceTotal+=(Integer.parseInt(cart.get(0).getPrice()))*(Integer.parseInt(cart.get(0).getQuantity()));
                canvas.drawText(String.valueOf(priceTotal), 700, 325, paint);
                canvas.drawText(cart.get(1).getProductId(), 10, 350, paint);
                canvas.drawText(cart.get(1).getProductName(), 175, 350, paint);
                canvas.drawText(cart.get(1).getQuantity(), 425, 350, paint);
                canvas.drawText(cart.get(1).getPrice(), 550, 350, paint);
                priceTotal=(Integer.parseInt(cart.get(1).getPrice()))*(Integer.parseInt(cart.get(1).getQuantity()));
                canvas.drawText(String.valueOf(priceTotal), 700, 350, paint);
                canvas.drawText(cart.get(2).getProductId(), 10, 375, paint);
                canvas.drawText(cart.get(2).getProductName(), 175, 375, paint);
                canvas.drawText(cart.get(2).getQuantity(), 425, 375, paint);
                canvas.drawText(cart.get(2).getPrice(), 550, 375, paint);
                priceTotal=(Integer.parseInt(cart.get(2).getPrice()))*(Integer.parseInt(cart.get(2).getQuantity()));
                canvas.drawText(String.valueOf(priceTotal), 700, 375, paint);
                canvas.drawText(cart.get(3).getProductId(), 10, 400, paint);
                canvas.drawText(cart.get(3).getProductName(), 175, 400, paint);
                canvas.drawText(cart.get(3).getQuantity(), 425, 400, paint);
                canvas.drawText(cart.get(3).getPrice(), 550, 400, paint);
                priceTotal=(Integer.parseInt(cart.get(3).getPrice()))*(Integer.parseInt(cart.get(3).getQuantity()));
                canvas.drawText(String.valueOf(priceTotal), 700, 400, paint);
                canvas.drawText(cart.get(4).getProductId(), 10, 425, paint);
                canvas.drawText(cart.get(4).getProductName(), 175, 425, paint);
                canvas.drawText(cart.get(4).getQuantity(), 425, 425, paint);
                canvas.drawText(cart.get(4).getPrice(), 550, 425, paint);
                priceTotal=(Integer.parseInt(cart.get(4).getPrice()))*(Integer.parseInt(cart.get(4).getQuantity()));
                canvas.drawText(String.valueOf(priceTotal), 700, 425, paint);
                canvas.drawText(cart.get(5).getProductId(), 10, 450, paint);
                canvas.drawText(cart.get(5).getProductName(), 175, 450, paint);
                canvas.drawText(cart.get(5).getQuantity(), 425, 450, paint);
                canvas.drawText(cart.get(5).getPrice(), 550, 450, paint);
                priceTotal=(Integer.parseInt(cart.get(5).getPrice()))*(Integer.parseInt(cart.get(5).getQuantity()));
                canvas.drawText(String.valueOf(priceTotal), 700, 450, paint);
                canvas.drawText(cart.get(6).getProductId(), 10, 475, paint);
                canvas.drawText(cart.get(6).getProductName(), 175, 475, paint);
                canvas.drawText(cart.get(6).getQuantity(), 425, 475, paint);
                canvas.drawText(cart.get(6).getPrice(), 550, 475, paint);
                priceTotal=(Integer.parseInt(cart.get(6).getPrice()))*(Integer.parseInt(cart.get(6).getQuantity()));
                canvas.drawText(String.valueOf(priceTotal), 700, 475, paint);
            }
            else if (8 == cart.size())
            {
                canvas.drawText(cart.get(0).getProductId(), 10, 325, paint);
                canvas.drawText(cart.get(0).getProductName(), 175, 325, paint);
                canvas.drawText(cart.get(0).getQuantity(), 425, 325, paint);
                canvas.drawText(cart.get(0).getPrice(), 550, 325, paint);
                int priceTotal = 0;
                priceTotal+=(Integer.parseInt(cart.get(0).getPrice()))*(Integer.parseInt(cart.get(0).getQuantity()));
                canvas.drawText(String.valueOf(priceTotal), 700, 325, paint);
                canvas.drawText(cart.get(1).getProductId(), 10, 350, paint);
                canvas.drawText(cart.get(1).getProductName(), 175, 350, paint);
                canvas.drawText(cart.get(1).getQuantity(), 425, 350, paint);
                canvas.drawText(cart.get(1).getPrice(), 550, 350, paint);
                priceTotal=(Integer.parseInt(cart.get(1).getPrice()))*(Integer.parseInt(cart.get(1).getQuantity()));
                canvas.drawText(String.valueOf(priceTotal), 700, 350, paint);
                canvas.drawText(cart.get(2).getProductId(), 10, 375, paint);
                canvas.drawText(cart.get(2).getProductName(), 175, 375, paint);
                canvas.drawText(cart.get(2).getQuantity(), 425, 375, paint);
                canvas.drawText(cart.get(2).getPrice(), 550, 375, paint);
                priceTotal=(Integer.parseInt(cart.get(2).getPrice()))*(Integer.parseInt(cart.get(2).getQuantity()));
                canvas.drawText(String.valueOf(priceTotal), 700, 375, paint);
                canvas.drawText(cart.get(3).getProductId(), 10, 400, paint);
                canvas.drawText(cart.get(3).getProductName(), 175, 400, paint);
                canvas.drawText(cart.get(3).getQuantity(), 425, 400, paint);
                canvas.drawText(cart.get(3).getPrice(), 550, 400, paint);
                priceTotal=(Integer.parseInt(cart.get(3).getPrice()))*(Integer.parseInt(cart.get(3).getQuantity()));
                canvas.drawText(String.valueOf(priceTotal), 700, 400, paint);
                canvas.drawText(cart.get(4).getProductId(), 10, 425, paint);
                canvas.drawText(cart.get(4).getProductName(), 175, 425, paint);
                canvas.drawText(cart.get(4).getQuantity(), 425, 425, paint);
                canvas.drawText(cart.get(4).getPrice(), 550, 425, paint);
                priceTotal=(Integer.parseInt(cart.get(4).getPrice()))*(Integer.parseInt(cart.get(4).getQuantity()));
                canvas.drawText(String.valueOf(priceTotal), 700, 425, paint);
                canvas.drawText(cart.get(5).getProductId(), 10, 450, paint);
                canvas.drawText(cart.get(5).getProductName(), 175, 450, paint);
                canvas.drawText(cart.get(5).getQuantity(), 425, 450, paint);
                canvas.drawText(cart.get(5).getPrice(), 550, 450, paint);
                priceTotal=(Integer.parseInt(cart.get(5).getPrice()))*(Integer.parseInt(cart.get(5).getQuantity()));
                canvas.drawText(String.valueOf(priceTotal), 700, 450, paint);
                canvas.drawText(cart.get(6).getProductId(), 10, 475, paint);
                canvas.drawText(cart.get(6).getProductName(), 175, 475, paint);
                canvas.drawText(cart.get(6).getQuantity(), 425, 475, paint);
                canvas.drawText(cart.get(6).getPrice(), 550, 475, paint);
                priceTotal=(Integer.parseInt(cart.get(6).getPrice()))*(Integer.parseInt(cart.get(6).getQuantity()));
                canvas.drawText(String.valueOf(priceTotal), 700, 475, paint);
                canvas.drawText(cart.get(7).getProductId(), 10, 500, paint);
                canvas.drawText(cart.get(7).getProductName(), 175, 500, paint);
                canvas.drawText(cart.get(7).getQuantity(), 425, 500, paint);
                canvas.drawText(cart.get(7).getPrice(), 550, 500, paint);
                priceTotal=(Integer.parseInt(cart.get(7).getPrice()))*(Integer.parseInt(cart.get(7).getQuantity()));
                canvas.drawText(String.valueOf(priceTotal), 700, 500, paint);
            }
            else if (9 == cart.size())
            {
                canvas.drawText(cart.get(0).getProductId(), 10, 325, paint);
                canvas.drawText(cart.get(0).getProductName(), 175, 325, paint);
                canvas.drawText(cart.get(0).getQuantity(), 425, 325, paint);
                canvas.drawText(cart.get(0).getPrice(), 550, 325, paint);
                int priceTotal = 0;
                priceTotal+=(Integer.parseInt(cart.get(0).getPrice()))*(Integer.parseInt(cart.get(0).getQuantity()));
                canvas.drawText(String.valueOf(priceTotal), 700, 325, paint);
                canvas.drawText(cart.get(1).getProductId(), 10, 350, paint);
                canvas.drawText(cart.get(1).getProductName(), 175, 350, paint);
                canvas.drawText(cart.get(1).getQuantity(), 425, 350, paint);
                canvas.drawText(cart.get(1).getPrice(), 550, 350, paint);
                priceTotal=(Integer.parseInt(cart.get(1).getPrice()))*(Integer.parseInt(cart.get(1).getQuantity()));
                canvas.drawText(String.valueOf(priceTotal), 700, 350, paint);
                canvas.drawText(cart.get(2).getProductId(), 10, 375, paint);
                canvas.drawText(cart.get(2).getProductName(), 175, 375, paint);
                canvas.drawText(cart.get(2).getQuantity(), 425, 375, paint);
                canvas.drawText(cart.get(2).getPrice(), 550, 375, paint);
                priceTotal=(Integer.parseInt(cart.get(2).getPrice()))*(Integer.parseInt(cart.get(2).getQuantity()));
                canvas.drawText(String.valueOf(priceTotal), 700, 375, paint);
                canvas.drawText(cart.get(3).getProductId(), 10, 400, paint);
                canvas.drawText(cart.get(3).getProductName(), 175, 400, paint);
                canvas.drawText(cart.get(3).getQuantity(), 425, 400, paint);
                canvas.drawText(cart.get(3).getPrice(), 550, 400, paint);
                priceTotal=(Integer.parseInt(cart.get(3).getPrice()))*(Integer.parseInt(cart.get(3).getQuantity()));
                canvas.drawText(String.valueOf(priceTotal), 700, 400, paint);
                canvas.drawText(cart.get(4).getProductId(), 10, 425, paint);
                canvas.drawText(cart.get(4).getProductName(), 175, 425, paint);
                canvas.drawText(cart.get(4).getQuantity(), 425, 425, paint);
                canvas.drawText(cart.get(4).getPrice(), 550, 425, paint);
                priceTotal=(Integer.parseInt(cart.get(4).getPrice()))*(Integer.parseInt(cart.get(4).getQuantity()));
                canvas.drawText(String.valueOf(priceTotal), 700, 425, paint);
                canvas.drawText(cart.get(5).getProductId(), 10, 450, paint);
                canvas.drawText(cart.get(5).getProductName(), 175, 450, paint);
                canvas.drawText(cart.get(5).getQuantity(), 425, 450, paint);
                canvas.drawText(cart.get(5).getPrice(), 550, 450, paint);
                priceTotal=(Integer.parseInt(cart.get(5).getPrice()))*(Integer.parseInt(cart.get(5).getQuantity()));
                canvas.drawText(String.valueOf(priceTotal), 700, 450, paint);
                canvas.drawText(cart.get(6).getProductId(), 10, 475, paint);
                canvas.drawText(cart.get(6).getProductName(), 175, 475, paint);
                canvas.drawText(cart.get(6).getQuantity(), 425, 475, paint);
                canvas.drawText(cart.get(6).getPrice(), 550, 475, paint);
                priceTotal=(Integer.parseInt(cart.get(6).getPrice()))*(Integer.parseInt(cart.get(6).getQuantity()));
                canvas.drawText(String.valueOf(priceTotal), 700, 475, paint);
                canvas.drawText(cart.get(7).getProductId(), 10, 500, paint);
                canvas.drawText(cart.get(7).getProductName(), 175, 500, paint);
                canvas.drawText(cart.get(7).getQuantity(), 425, 500, paint);
                canvas.drawText(cart.get(7).getPrice(), 550, 500, paint);
                priceTotal=(Integer.parseInt(cart.get(7).getPrice()))*(Integer.parseInt(cart.get(7).getQuantity()));
                canvas.drawText(String.valueOf(priceTotal), 700, 500, paint);
                canvas.drawText(cart.get(8).getProductId(), 10, 525, paint);
                canvas.drawText(cart.get(8).getProductName(), 175, 525, paint);
                canvas.drawText(cart.get(8).getQuantity(), 425, 525, paint);
                canvas.drawText(cart.get(8).getPrice(), 550, 525, paint);
                priceTotal=(Integer.parseInt(cart.get(8).getPrice()))*(Integer.parseInt(cart.get(8).getQuantity()));
                canvas.drawText(String.valueOf(priceTotal), 700, 525, paint);
            }
            else if (10 == cart.size())
            {
                canvas.drawText(cart.get(0).getProductId(), 10, 325, paint);
                canvas.drawText(cart.get(0).getProductName(), 175, 325, paint);
                canvas.drawText(cart.get(0).getQuantity(), 425, 325, paint);
                canvas.drawText(cart.get(0).getPrice(), 550, 325, paint);
                int priceTotal = 0;
                priceTotal+=(Integer.parseInt(cart.get(0).getPrice()))*(Integer.parseInt(cart.get(0).getQuantity()));
                canvas.drawText(String.valueOf(priceTotal), 700, 325, paint);
                canvas.drawText(cart.get(1).getProductId(), 10, 350, paint);
                canvas.drawText(cart.get(1).getProductName(), 175, 350, paint);
                canvas.drawText(cart.get(1).getQuantity(), 425, 350, paint);
                canvas.drawText(cart.get(1).getPrice(), 550, 350, paint);
                priceTotal=(Integer.parseInt(cart.get(1).getPrice()))*(Integer.parseInt(cart.get(1).getQuantity()));
                canvas.drawText(String.valueOf(priceTotal), 700, 350, paint);
                canvas.drawText(cart.get(2).getProductId(), 10, 375, paint);
                canvas.drawText(cart.get(2).getProductName(), 175, 375, paint);
                canvas.drawText(cart.get(2).getQuantity(), 425, 375, paint);
                canvas.drawText(cart.get(2).getPrice(), 550, 375, paint);
                priceTotal=(Integer.parseInt(cart.get(2).getPrice()))*(Integer.parseInt(cart.get(2).getQuantity()));
                canvas.drawText(String.valueOf(priceTotal), 700, 375, paint);
                canvas.drawText(cart.get(3).getProductId(), 10, 400, paint);
                canvas.drawText(cart.get(3).getProductName(), 175, 400, paint);
                canvas.drawText(cart.get(3).getQuantity(), 425, 400, paint);
                canvas.drawText(cart.get(3).getPrice(), 550, 400, paint);
                priceTotal=(Integer.parseInt(cart.get(3).getPrice()))*(Integer.parseInt(cart.get(3).getQuantity()));
                canvas.drawText(String.valueOf(priceTotal), 700, 400, paint);
                canvas.drawText(cart.get(4).getProductId(), 10, 425, paint);
                canvas.drawText(cart.get(4).getProductName(), 175, 425, paint);
                canvas.drawText(cart.get(4).getQuantity(), 425, 425, paint);
                canvas.drawText(cart.get(4).getPrice(), 550, 425, paint);
                priceTotal=(Integer.parseInt(cart.get(4).getPrice()))*(Integer.parseInt(cart.get(4).getQuantity()));
                canvas.drawText(String.valueOf(priceTotal), 700, 425, paint);
                canvas.drawText(cart.get(5).getProductId(), 10, 450, paint);
                canvas.drawText(cart.get(5).getProductName(), 175, 450, paint);
                canvas.drawText(cart.get(5).getQuantity(), 425, 450, paint);
                canvas.drawText(cart.get(5).getPrice(), 550, 450, paint);
                priceTotal=(Integer.parseInt(cart.get(5).getPrice()))*(Integer.parseInt(cart.get(5).getQuantity()));
                canvas.drawText(String.valueOf(priceTotal), 700, 450, paint);
                canvas.drawText(cart.get(6).getProductId(), 10, 475, paint);
                canvas.drawText(cart.get(6).getProductName(), 175, 475, paint);
                canvas.drawText(cart.get(6).getQuantity(), 425, 475, paint);
                canvas.drawText(cart.get(6).getPrice(), 550, 475, paint);
                priceTotal=(Integer.parseInt(cart.get(6).getPrice()))*(Integer.parseInt(cart.get(6).getQuantity()));
                canvas.drawText(String.valueOf(priceTotal), 700, 475, paint);
                canvas.drawText(cart.get(7).getProductId(), 10, 500, paint);
                canvas.drawText(cart.get(7).getProductName(), 175, 500, paint);
                canvas.drawText(cart.get(7).getQuantity(), 425, 500, paint);
                canvas.drawText(cart.get(7).getPrice(), 550, 500, paint);
                priceTotal=(Integer.parseInt(cart.get(7).getPrice()))*(Integer.parseInt(cart.get(7).getQuantity()));
                canvas.drawText(String.valueOf(priceTotal), 700, 500, paint);
                canvas.drawText(cart.get(8).getProductId(), 10, 525, paint);
                canvas.drawText(cart.get(8).getProductName(), 175, 525, paint);
                canvas.drawText(cart.get(8).getQuantity(), 425, 525, paint);
                canvas.drawText(cart.get(8).getPrice(), 550, 525, paint);
                priceTotal=(Integer.parseInt(cart.get(8).getPrice()))*(Integer.parseInt(cart.get(8).getQuantity()));
                canvas.drawText(String.valueOf(priceTotal), 700, 525, paint);
                canvas.drawText(cart.get(9).getProductId(), 10, 550, paint);
                canvas.drawText(cart.get(9).getProductName(), 175, 550, paint);
                canvas.drawText(cart.get(9).getQuantity(), 425, 550, paint);
                canvas.drawText(cart.get(9).getPrice(), 550, 550, paint);
                priceTotal=(Integer.parseInt(cart.get(9).getPrice()))*(Integer.parseInt(cart.get(9).getQuantity()));
                canvas.drawText(String.valueOf(priceTotal), 700, 550, paint);

            }


        }

        // finish the page
        document.finishPage(page);


        // write the document content
        String targetPdf = "/sdcard/cart.pdf";
        // String targetPdf ="/sdcard/"+(String.valueOf(System.currentTimeMillis()))+".pdf";

        File filePath = new File(targetPdf);
        try {
            document.writeTo(new FileOutputStream(filePath));
            Toast.makeText(this, "Done", Toast.LENGTH_LONG).show();
            sendMessageAttachement();
            //sendMessage();
            //displaypdf();
            //sendInvoice();
            //sendEmail();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Something wrong: " + e.toString(),
                    Toast.LENGTH_LONG).show();
        }

        // close the document
        document.close();
    }


    public void displaypdf() {

        File file = null;
        file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/cart.pdf");
        Toast.makeText(getApplicationContext(), file.toString() , Toast.LENGTH_LONG).show();
        if(file.exists()) {
            Intent target = new Intent(Intent.ACTION_VIEW);
            target.setDataAndType(Uri.fromFile(file), "application/pdf");
            target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

            Intent intent = Intent.createChooser(target, "Open File");
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                // Instruct the user to install a PDF reader here, or something
            }
        }
        else
            Toast.makeText(getApplicationContext(), "File path is incorrect." , Toast.LENGTH_LONG).show();

    }


    private void sendMessageAttachement() {
        final ProgressDialog dialog = new ProgressDialog(Cart.this);
        //dialog.setTitle("Sending Email");
        //dialog.setMessage("Please wait");
        // dialog.show();

        Thread sender = new Thread(new Runnable() {
            File file = null;
            @Override
            public void run() {
                try {
                    GMailSender sender = new GMailSender("trc42testing@gmail.com", "Liverpool1@");
                    sender.sendMailAttachement("TRC INVOICE MAILING ",
                            "BELOW IS YOUR INVOICE PLACED TODAY BY :" + " "+ Common.currentUser.getUserName() + " " +"For delivery on :" + edtDelivery.getText().toString(),
                            "trc42testing@gmail.com",
                            Common.currentUser.getEmail(),
                            file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/cart.pdf"));
                    dialog.dismiss();
                } catch (Exception e) {
                    Log.e("mylog", "Error: " + e.getMessage());
                }
            }
        });
        sender.start();
    }








    private void loadListProduct() {

        cart = new Database(this).getCarts();
        adapter = new CartAdapter(cart,this);
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);


        int total = 0;
        for(Order order:cart)
            total+=(Integer.parseInt(order.getPrice()))*(Integer.parseInt(order.getQuantity()));



        Locale locale = new Locale("en","IE");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);

        txtTotalPrice.setText(fmt.format(total));


    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getTitle().equals(Common.DELETE))
            deleteCart(item.getOrder());
        return true;
    }

    private void deleteCart(int position) {

        //remove item at list<order>by position
        cart.remove(position);
        //delete old sqlite
        new Database(this).cleanCart();
        //update sqlite
        for(Order item:cart)
            new Database(this).addToCart(item);
        //refresh
        loadListProduct();

    }


}
