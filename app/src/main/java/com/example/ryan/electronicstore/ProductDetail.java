package com.example.ryan.electronicstore;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.ryan.electronicstore.Common.Common;
import com.example.ryan.electronicstore.Database.Database;
import com.example.ryan.electronicstore.Model.Order;
import com.example.ryan.electronicstore.Model.Product;
import com.example.ryan.electronicstore.Model.Rating;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class ProductDetail extends AppCompatActivity implements RatingDialogListener {

    TextView product_name,product_price,product_manufacturer,product_barcode,product_qty;
    ImageView product_image;
    CollapsingToolbarLayout collapsingToolbarLayout;
    FloatingActionButton btnCart,btnRating;
    ElegantNumberButton numberButton;
    RatingBar ratingBar;

    String productId="";

    FirebaseDatabase database;
    DatabaseReference products;
    DatabaseReference ratingTbl;

    Product currentProduct;
    Order currentOrder;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        //FirebaseDatabase
        database = FirebaseDatabase.getInstance();
        products = database.getReference("Products");
        ratingTbl = database.getReference("Rating");

        //View
        numberButton = (ElegantNumberButton)findViewById(R.id.number_button);
        btnCart = (FloatingActionButton)findViewById(R.id.btnCart);
        btnRating =(FloatingActionButton)findViewById(R.id.btn_rating);
        ratingBar = (RatingBar)findViewById(R.id.ratingBar);

        btnRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRatingDialog();
            }
        });

        //Function for add to cart
        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Database(getBaseContext()).addToCart(new Order(

                        productId,
                        currentProduct.getName(),
                        numberButton.getNumber(),
                        currentProduct.getPrice(),
                        currentProduct.getWarehouseQty()




                ));

                Toast.makeText(ProductDetail.this,"Added To Cart",Toast.LENGTH_SHORT).show();
            }
        });

        product_manufacturer = (TextView)findViewById(R.id.product_manufacturer);
        product_barcode = (TextView)findViewById(R.id.product_barcode);
        product_name = (TextView)findViewById(R.id.product_name);
        product_price = (TextView)findViewById(R.id.product_price);
        product_image = (ImageView) findViewById(R.id.img_product);
        product_qty = (TextView) findViewById(R.id.product_qty);

        collapsingToolbarLayout = (CollapsingToolbarLayout)findViewById(R.id.collapsing);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppBar);

        if(getIntent() != null)
            productId = getIntent().getStringExtra("productId");
        if(!productId.isEmpty())
        {
            if(Common.isConnectedToInternet(getBaseContext()))
            {
                getDetailProduct(productId);
                getRatingProduct(productId);
            }
            else
            {
                Toast.makeText(ProductDetail.this, "Please Check Your Connection", Toast.LENGTH_SHORT).show();
                return;
            }

        }


    }

    private void getRatingProduct(String productId) {

        Query foodRating = ratingTbl.orderByChild("productId").equalTo(productId);

        foodRating.addValueEventListener(new ValueEventListener() {
            int count=0, sum=0;
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot:dataSnapshot.getChildren())
                {
                    Rating item = postSnapshot.getValue(Rating.class);
                    sum+=Integer.parseInt(item.getRateValue());
                    count++;
                }
                if(count !=0)
                {
                    float average = sum/count;
                    ratingBar.setRating(average);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void showRatingDialog() {

        new AppRatingDialog.Builder()
                .setPositiveButtonText("Submit")
                .setNegativeButtonText("Cancel")
                .setNoteDescriptions(Arrays.asList("Terrible","Poor","Ok","Good","Great"))
                .setDefaultRating(1)
                .setTitle("Rate This Product")
                .setDescription("Please give your Feedback")
                .setTitleTextColor(R.color.colorPrimary)
                .setHint("Write your comments here..")
                .setHintTextColor(R.color.colorAccent)
                .setCommentTextColor(android.R.color.white)
                .setCommentBackgroundColor(R.color.colorPrimaryDark)
                .setWindowAnimation(R.style.RatingDialogFadeAnim)
                .create(ProductDetail.this)
                .show();
    }

    private void getDetailProduct(String productId) {
        products.child(productId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentProduct = dataSnapshot.getValue(Product.class);



                //bind image

                Picasso.with(getBaseContext()).load(currentProduct.getImage())
                        .into(product_image);

                collapsingToolbarLayout.setTitle(currentProduct.getName());


                product_price.setText(currentProduct.getPrice());
                product_name.setText(currentProduct.getName());
                product_manufacturer.setText(String.format("Product manufacturer : %s",currentProduct.getAuthor()));
                product_barcode.setText(String.format("Barcode : %s",currentProduct.getBarcode()));
                product_qty.setText(String.format("Qty In Warehouse : %s",currentProduct.getWarehouseQty()));

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onPositiveButtonClicked(int value, @NotNull String comments) {
        //get rating from firebase
        final Rating rating = new Rating(Common.currentUser.getUserName(),
                productId,
                String.valueOf(value),
                comments);
        ratingTbl.child(Common.currentUser.getUserName()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(Common.currentUser.getUserName()).exists())
                {
                    ratingTbl.child(Common.currentUser.getUserName()).removeValue();

                    ratingTbl.child(Common.currentUser.getUserName()).setValue(rating);
                }
                else
                {
                    ratingTbl.child(Common.currentUser.getUserName()).setValue(rating);
                }
                //Toast.makeText(ProductDetail.this, "Thanks for the feedback", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onNegativeButtonClicked() {

    }
}
