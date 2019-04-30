package com.example.ryan.electronicstore.ViewHolder;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.example.ryan.electronicstore.Common.Common;
import com.example.ryan.electronicstore.Interface.ItemClickListener;
import com.example.ryan.electronicstore.Model.Order;
import com.example.ryan.electronicstore.Model.Product;
import com.example.ryan.electronicstore.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;



class CartViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
        ,View.OnCreateContextMenuListener{

    public TextView txt_cart_name,txt_price,txt_qty;
    public ImageView img_cart_count;




    private ItemClickListener itemClickListener;

    public void setTxt_cart_name(TextView txt_cart_name) {
        this.txt_cart_name = txt_cart_name;
    }

    public CartViewHolder(View itemView) {
        super(itemView);
        txt_cart_name = (TextView)itemView.findViewById(R.id.cart_item_name);
        txt_price = (TextView)itemView.findViewById(R.id.cart_item_price);
        txt_qty = (TextView)itemView.findViewById(R.id.cart_item_qtyInWarehouse);
        img_cart_count = (ImageView)itemView.findViewById(R.id.cart_item_count);

        itemView.setOnCreateContextMenuListener(this);
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {

        contextMenu.setHeaderTitle("Select Action");
        contextMenu.add(0,0,getAdapterPosition(), Common.DELETE);

    }
}

public class CartAdapter extends RecyclerView.Adapter<CartViewHolder>{

    FirebaseRecyclerAdapter<Product,CartViewHolder> adapter;


    FirebaseDatabase database;
    DatabaseReference productList;

    private List<Order>listData = new ArrayList<>();
    private Context context;
    public CartAdapter(List<Order>listData,Context context){


        this.listData = listData;
        this.context = context;
    }


    @Override
    public CartViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.cart_layout,parent,false);
        return new CartViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CartViewHolder holder, int position) {


        TextDrawable drawable = TextDrawable.builder()
                .buildRound(""+listData.get(position).getQuantity(), Color.RED);
        holder.img_cart_count.setImageDrawable(drawable);

        Locale locale = new Locale("en","IE");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
        int price = (Integer.parseInt(listData.get(position).getPrice()))*(Integer.parseInt(listData.get(position).getQuantity()));
        int sum = (Integer.parseInt(listData.get(position).getQtyInWarehouse()))-(Integer.parseInt(listData.get(position).getQuantity()));
        String newWarehouseQty = Integer.toString(sum);

        holder.txt_price.setText(fmt.format(price));
        holder.txt_cart_name.setText(listData.get(position).getProductName());
        holder.txt_qty.setText(newWarehouseQty);

        //listData.get(position).setQtyInWarehouse(newWarehouseQty);

        Map<String,Object> qtyUpdate = new HashMap<>();
        qtyUpdate.put("warehouseQty",newWarehouseQty.toString());

        //make update
        DatabaseReference user =FirebaseDatabase.getInstance().getReference("Products");
        user.child(listData.get(position).getProductId())
                .updateChildren(qtyUpdate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {


                    }
                });







        //aProduct.setWarehouseQty("1000000");

        //String test = currentProduct.getWarehouseQty();
        //currentProduct.setWarehouseQty(test);
        //aProduct = new Product();
        //listData.get(position).getQuantity();
        //aProduct.getWarehouseQty();

        //int sum = (Integer.parseInt(listData.get(position).getQuantity()));
        //String sum2 = (aProduct.getWarehouseQty());


        //int sum = (Integer.parseInt(listData.get(position).getQuantity()))- (Integer.parseInt(aProduct.getWarehouseQty()));
        //String newWarehouseQty = Integer.toString(sum);
        //aProduct.setWarehouseQty(newWarehouseQty);

        //int stock = (Integer.parseInt(listedData.get(position).getWarehouseQty()))-(Integer.parseInt(listData.get(position).getQuantity()));
        //String newWarehouseQty = Integer.toString(stock);

    }

    @Override
    public int getItemCount() {
        return listData.size();
    }
}
