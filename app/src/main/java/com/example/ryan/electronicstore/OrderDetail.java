package com.example.ryan.electronicstore;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Button;
import android.widget.TextView;

import com.example.ryan.electronicstore.Common.Common;
import com.example.ryan.electronicstore.ViewHolder.OrderDetailAdapter;

public class OrderDetail extends AppCompatActivity {

    TextView order_id,order_userName,order_total,order_expectedDelivery,order_comment;
    Button update;
    String order_id_value="";
    RecyclerView lstProducts;
    RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);


        order_id = (TextView) findViewById(R.id.order_id);
        order_userName = (TextView) findViewById(R.id.order_userName);
        order_total = (TextView) findViewById(R.id.order_total);
        order_expectedDelivery = (TextView) findViewById(R.id.order_expectedDelivery);
        order_comment = (TextView) findViewById(R.id.order_comment);

        lstProducts = (RecyclerView) findViewById(R.id.lstProducts);
        lstProducts.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        lstProducts.setLayoutManager(layoutManager);

        if (getIntent() != null)
            order_id_value = getIntent().getStringExtra("OrderId");

        order_id.setText(order_id_value);
        order_userName.setText(Common.currentUser.getUserName());
        order_total.setText(Common.currentRequest.getTotal());
        order_expectedDelivery.setText(Common.currentRequest.getRequestedDeliveryDate());
        order_comment.setText(Common.currentRequest.getComment());


        final OrderDetailAdapter adapter = new OrderDetailAdapter(Common.currentRequest.getProducts());
        adapter.notifyDataSetChanged();
        lstProducts.setAdapter(adapter);

    }

}
