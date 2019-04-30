package com.example.ryan.electronicstore;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.ryan.electronicstore.Common.Common;
import com.example.ryan.electronicstore.Interface.ItemClickListener;
import com.example.ryan.electronicstore.Model.Request;
import com.example.ryan.electronicstore.ViewHolder.OrderViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jaredrummler.materialspinner.MaterialSpinner;

public class OrderStatus extends AppCompatActivity {

    public RecyclerView recyclerView;
    public RecyclerView.LayoutManager layoutManager;

    FirebaseRecyclerAdapter<Request,OrderViewHolder> adapter;

    FirebaseDatabase database;
    DatabaseReference requests;

    MaterialSpinner spinner;
    DrawerLayout drawer;

    String auth = Common.currentUser.getIsStaff();
    String admin = Common.currentUser.getIsAdmin();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
        toolbar.setTitle("Order Selection");
        setSupportActionBar(toolbar);

        //Firebase

        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

        recyclerView = (RecyclerView)findViewById(R.id.listOrders);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);


        //loadOrders(Common.currentUser.getStoreName());

        if(auth.equals("true") )
        {
            loadOrders(Common.currentUser.getUserName());

        }
        else if(auth.equals("false"))
        {
            loadStoresOwnOrders(Common.currentUser.getUserName());
        }
        else
        {
            Toast.makeText(OrderStatus.this, "Problem accesing orders try again", Toast.LENGTH_SHORT).show();
        }




        //if(getIntent() == null)
        //    loadOrders(Common.currentUser.getStoreName());
        //else
        //    loadOrders(getIntent().getStringExtra("storeName"));






    }

    private void loadStoresOwnOrders(String userName) {
        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(
                Request.class,
                R.layout.order_layout,
                OrderViewHolder.class,
                requests.orderByChild("userName")
                        .equalTo(userName)
        ) {




            @Override
            protected void populateViewHolder(OrderViewHolder viewHolder, final Request model, final int position) {

                viewHolder.txtOrderId.setText(adapter.getRef(position).getKey());
                viewHolder.txtOrderStatus.setText(Common.convertCodeToStatus(model.getStatus()));
                viewHolder.txtOrderExpectedDelivery.setText(model.getRequestedDeliveryDate());
                viewHolder.txtOrderUserName.setText(model.getUserName());

                viewHolder.editbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(auth.equals("true"))
                        {
                            showUpdateDialog(adapter.getRef(position).getKey(),adapter.getItem(position));

                        }
                        else if(auth.equals("false"))
                        {
                            Toast.makeText(OrderStatus.this, "Only assesable to warehouse staff", Toast.LENGTH_SHORT).show();
                        }


                    }
                });

                viewHolder.btndelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(auth.equals("true"))
                        {
                            deleteOrder(adapter.getRef(position).getKey());

                        }
                        else if(auth.equals("false"))
                        {
                            Toast.makeText(OrderStatus.this, "", Toast.LENGTH_SHORT).show();
                        }


                    }
                });

                viewHolder.editdetail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent orderDetail = new Intent(OrderStatus.this,OrderDetail.class);
                        Common.currentRequest = model;
                        orderDetail.putExtra("OrderId",adapter.getRef(position).getKey());
                        startActivity(orderDetail);

                    }
                });

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int posistion, boolean isLongClick) {
                        Intent orderDetail = new Intent(OrderStatus.this,OrderDetail.class);
                        Common.currentRequest = model;
                        orderDetail.putExtra("OrderId",adapter.getRef(position).getKey());
                        startActivity(orderDetail);
                    }
                });

            }
        };
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

    }

    private void loadOrders(String userName) {


        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(
                Request.class,
                R.layout.order_layout,
                OrderViewHolder.class,
                requests
                //requests.orderByChild("storeName")
                //        .equalTo(storeName)
        ) {




            @Override
            protected void populateViewHolder(OrderViewHolder viewHolder, final Request model, final int position) {

                viewHolder.txtOrderId.setText(adapter.getRef(position).getKey());
                viewHolder.txtOrderStatus.setText(Common.convertCodeToStatus(model.getStatus()));
                viewHolder.txtOrderExpectedDelivery.setText(model.getRequestedDeliveryDate());
                viewHolder.txtOrderUserName.setText(model.getUserName());

                viewHolder.editbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(auth.equals("true"))
                        {
                            showUpdateDialog(adapter.getRef(position).getKey(),adapter.getItem(position));

                        }
                        else if(auth.equals("false"))
                        {
                            Toast.makeText(OrderStatus.this, "Only assesable to warehouse staff", Toast.LENGTH_SHORT).show();
                        }


                    }
                });

                viewHolder.btndelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(auth.equals("true"))
                        {
                            deleteOrder(adapter.getRef(position).getKey());

                        }
                        else if(auth.equals("false"))
                        {
                            Toast.makeText(OrderStatus.this, "Only assesable to warehouse staff", Toast.LENGTH_SHORT).show();
                        }


                    }
                });

                viewHolder.editdetail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent orderDetail = new Intent(OrderStatus.this,OrderDetail.class);
                        Common.currentRequest = model;
                        orderDetail.putExtra("OrderId",adapter.getRef(position).getKey());
                        startActivity(orderDetail);

                    }
                });

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int posistion, boolean isLongClick) {
                        Intent orderDetail = new Intent(OrderStatus.this,OrderDetail.class);
                        Common.currentRequest = model;
                        orderDetail.putExtra("OrderId",adapter.getRef(position).getKey());
                        startActivity(orderDetail);
                    }
                });
            }
        };
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }

    private void loadOrdersByStatusPlaced() {


        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(
                Request.class,
                R.layout.order_layout,
                OrderViewHolder.class,
                requests.orderByChild("status")
                        .equalTo("0")

        ) {




            @Override
            protected void populateViewHolder(OrderViewHolder viewHolder, final Request model, final int position) {

                viewHolder.txtOrderId.setText(adapter.getRef(position).getKey());
                viewHolder.txtOrderStatus.setText(Common.convertCodeToStatus(model.getStatus()));
                viewHolder.txtOrderExpectedDelivery.setText(model.getRequestedDeliveryDate());
                viewHolder.txtOrderUserName.setText(model.getUserName());

                viewHolder.editbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(auth.equals("true"))
                        {
                            showUpdateDialog(adapter.getRef(position).getKey(),adapter.getItem(position));

                        }
                        else if(auth.equals("false"))
                        {
                            Toast.makeText(OrderStatus.this, "11", Toast.LENGTH_SHORT).show();
                        }


                    }
                });

                viewHolder.btndelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(auth.equals("true"))
                        {
                            deleteOrder(adapter.getRef(position).getKey());

                        }
                        else if(auth.equals("false"))
                        {
                            Toast.makeText(OrderStatus.this, "11", Toast.LENGTH_SHORT).show();
                        }


                    }
                });

                viewHolder.editdetail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent orderDetail = new Intent(OrderStatus.this,OrderDetail.class);
                        Common.currentRequest = model;
                        orderDetail.putExtra("OrderId",adapter.getRef(position).getKey());
                        startActivity(orderDetail);

                    }
                });

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int posistion, boolean isLongClick) {
                        Intent orderDetail = new Intent(OrderStatus.this,OrderDetail.class);
                        Common.currentRequest = model;
                        orderDetail.putExtra("OrderId",adapter.getRef(position).getKey());
                        startActivity(orderDetail);
                    }
                });
            }
        };
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }

    private void loadOrdersByStatusOn() {


        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(
                Request.class,
                R.layout.order_layout,
                OrderViewHolder.class,
                requests.orderByChild("status")
                        .equalTo("1")

        ) {




            @Override
            protected void populateViewHolder(OrderViewHolder viewHolder, final Request model, final int position) {

                viewHolder.txtOrderId.setText(adapter.getRef(position).getKey());
                viewHolder.txtOrderStatus.setText(Common.convertCodeToStatus(model.getStatus()));
                viewHolder.txtOrderExpectedDelivery.setText(model.getRequestedDeliveryDate());
                viewHolder.txtOrderUserName.setText(model.getUserName());

                viewHolder.editbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(auth.equals("true"))
                        {
                            showUpdateDialog(adapter.getRef(position).getKey(),adapter.getItem(position));

                        }
                        else if(auth.equals("false"))
                        {
                            Toast.makeText(OrderStatus.this, "Only assesable to warehouse staff", Toast.LENGTH_SHORT).show();
                        }


                    }
                });

                viewHolder.btndelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(auth.equals("true"))
                        {
                            deleteOrder(adapter.getRef(position).getKey());

                        }
                        else if(auth.equals("false"))
                        {
                            Toast.makeText(OrderStatus.this, "Only assesable to warehouse staff", Toast.LENGTH_SHORT).show();
                        }


                    }
                });

                viewHolder.editdetail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent orderDetail = new Intent(OrderStatus.this,OrderDetail.class);
                        Common.currentRequest = model;
                        orderDetail.putExtra("OrderId",adapter.getRef(position).getKey());
                        startActivity(orderDetail);

                    }
                });

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int posistion, boolean isLongClick) {
                        Intent orderDetail = new Intent(OrderStatus.this,OrderDetail.class);
                        Common.currentRequest = model;
                        orderDetail.putExtra("OrderId",adapter.getRef(position).getKey());
                        startActivity(orderDetail);
                    }
                });
            }
        };
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }

    private void loadOrdersByStatusDelivered() {


        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(
                Request.class,
                R.layout.order_layout,
                OrderViewHolder.class,
                requests.orderByChild("status")
                        .equalTo("2")

        ) {




            @Override
            protected void populateViewHolder(OrderViewHolder viewHolder, final Request model, final int position) {

                viewHolder.txtOrderId.setText(adapter.getRef(position).getKey());
                viewHolder.txtOrderStatus.setText(Common.convertCodeToStatus(model.getStatus()));
                viewHolder.txtOrderExpectedDelivery.setText(model.getRequestedDeliveryDate());
                viewHolder.txtOrderUserName.setText(model.getUserName());

                viewHolder.editbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(auth.equals("true"))
                        {
                            showUpdateDialog(adapter.getRef(position).getKey(),adapter.getItem(position));

                        }
                        else if(auth.equals("false"))
                        {
                            Toast.makeText(OrderStatus.this, "Only assesable to warehouse staff", Toast.LENGTH_SHORT).show();
                        }


                    }
                });

                viewHolder.btndelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(auth.equals("true"))
                        {
                            deleteOrder(adapter.getRef(position).getKey());

                        }
                        else if(auth.equals("false"))
                        {
                            Toast.makeText(OrderStatus.this, "Only assesable to warehouse staff", Toast.LENGTH_SHORT).show();
                        }


                    }
                });

                viewHolder.editdetail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent orderDetail = new Intent(OrderStatus.this,OrderDetail.class);
                        Common.currentRequest = model;
                        orderDetail.putExtra("OrderId",adapter.getRef(position).getKey());
                        startActivity(orderDetail);

                    }
                });

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int posistion, boolean isLongClick) {
                        Intent orderDetail = new Intent(OrderStatus.this,OrderDetail.class);
                        Common.currentRequest = model;
                        orderDetail.putExtra("OrderId",adapter.getRef(position).getKey());
                        startActivity(orderDetail);
                    }
                });
            }
        };
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }

    private String convertCodeToStatus(String status) {

        if(status.equals("0"))
            return "Placed";
        else if(status.equals("1"))
            return "On Route";
        else
            return "Shipped";

    }

    //need to make on accessable to warehouse

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if(auth.equals("true") && item.getTitle().equals(Common.UPDATE))
        {
            showUpdateDialog(adapter.getRef(item.getOrder()).getKey(),adapter.getItem(item.getOrder()));
        }
        else if(auth.equals("true") && item.getTitle().equals(Common.DELETE))
        {
            deleteOrder(adapter.getRef(item.getOrder()).getKey());
        }
        else
        {
            Toast.makeText(OrderStatus.this, "11", Toast.LENGTH_SHORT).show();
        }

        return super.onContextItemSelected(item);
    }

    private void deleteOrder(String key) {

        requests.child(key).removeValue();
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(auth.equals("true")){
            getMenuInflater().inflate(R.menu.sort_orders, menu);
            return true;
        }
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.placed)
            loadOrdersByStatusPlaced();
        else if(item.getItemId() == R.id.onroute){

            loadOrdersByStatusOn();
        }
        else if(item.getItemId() == R.id.delivered){
            loadOrdersByStatusDelivered();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showUpdateDialog(String key, final Request item) {

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(OrderStatus.this);
        alertDialog.setTitle("Update Order");
        alertDialog.setMessage("Please choose status");

        LayoutInflater inflater = this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.update_order_layout,null);

        spinner = (MaterialSpinner)view.findViewById(R.id.statusSpinner);
        spinner.setItems("Placed","On Route","");

        alertDialog.setView(view);

        final String localKey = key;
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                item.setStatus(String.valueOf(spinner.getSelectedIndex()));

                requests.child(localKey).setValue(item);
                adapter.notifyDataSetChanged();

            }
        });

        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        alertDialog.show();

    }

}
