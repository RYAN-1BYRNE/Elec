package com.example.ryan.electronicstore.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.ryan.electronicstore.Interface.ItemClickListener;
import com.example.ryan.electronicstore.R;


public class OrderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
        View.OnLongClickListener,
        View.OnCreateContextMenuListener {

    public TextView txtOrderId,txtOrderStatus,txtOrderUserName,txtOrderExpectedDelivery;

    public Button editbtn,btndelete,editdetail;

    private ItemClickListener itemClickListener;


    public OrderViewHolder(View itemView) {
        super(itemView);

        txtOrderExpectedDelivery = (TextView)itemView.findViewById(R.id.order_expectedDelivery);
        txtOrderId = (TextView)itemView.findViewById(R.id.order_id);
        txtOrderStatus = (TextView)itemView.findViewById(R.id.order_status);
        txtOrderUserName = (TextView)itemView.findViewById(R.id.order_userName);

        editbtn = (Button)itemView.findViewById(R.id.editbtn);
        btndelete = (Button)itemView.findViewById(R.id.btndelete);
        editdetail = (Button)itemView.findViewById(R.id.editdetail);

        itemView.setOnClickListener(this);
        //itemView.setOnLongClickListener(this);
        itemView.setOnCreateContextMenuListener(this);

    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View view) {

        //itemClickListener.onClick(view,getAdapterPosition(),false);
        itemClickListener.onClick(view,getAdapterPosition(),true);


    }

    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {

        contextMenu.setHeaderTitle("Select the Action");

        contextMenu.add(0,0,getAdapterPosition(),"Update");
        contextMenu.add(0,1,getAdapterPosition(),"Delete");
    }

    @Override
    public boolean onLongClick(View view) {
        itemClickListener.onClick(view,getAdapterPosition(),true);
        return true;
    }
}
