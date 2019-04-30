package com.example.ryan.electronicstore;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.example.ryan.electronicstore.Common.Common;
import com.example.ryan.electronicstore.Interface.ItemClickListener;
import com.example.ryan.electronicstore.Model.Product;
import com.example.ryan.electronicstore.ViewHolder.ProductViewHolder;
import com.mancj.materialsearchbar.MaterialSearchBar;

public class ProductList extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FloatingActionButton fab;
    FloatingActionButton fabScan;

    RelativeLayout rootLayout;

    FirebaseDatabase database;
    DatabaseReference productList;
    FirebaseStorage storage;
    StorageReference storageReference;

    String categoryId="";

    FirebaseRecyclerAdapter<Product,ProductViewHolder> adapter;

    EditText edtName,edtManufacturer,edtPrice,edtBarcode,edtQty;
    Button btnSelect,btnUpload;

    Uri saveUri;
    Product newProduct;

    String auth = Common.currentUser.getIsStaff();

    //search funct
    FirebaseRecyclerAdapter<Product,ProductViewHolder> searchAdapter;
    List<String> suggestList = new ArrayList<>();
    MaterialSearchBar materialSearchBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        //Firebase
        database = FirebaseDatabase.getInstance();
        productList = database.getReference("Products");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        recyclerView = (RecyclerView)findViewById(R.id.recycler_product);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        rootLayout = (RelativeLayout)findViewById(R.id.rootLayout);


        fab = (FloatingActionButton) findViewById(R.id.fab3);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //later

                if(auth.equals("true")) {
                    showAddProductDialog();
                }
                else if (auth.equals("false"))
                {
                    Toast.makeText(ProductList.this, "Only assesable to warehouse staff", Toast.LENGTH_SHORT).show();
                }
            }
        });

        fabScan = (FloatingActionButton) findViewById(R.id.fab4);
        fabScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentIntegrator integrator = new IntentIntegrator(ProductList.this);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
                integrator.setPrompt("Scan");
                integrator.setCameraId(0);
                integrator.setBeepEnabled(false);
                integrator.setBarcodeImageEnabled(false);
                integrator.initiateScan();
            }
        });



        //Intent here
        if(getIntent() !=null)
            categoryId = getIntent().getStringExtra("CategoryId");
        if(!categoryId.isEmpty() && categoryId != null)
        {
            if(Common.isConnectedToInternet(getBaseContext()))
                loadListProduct(categoryId);
            else
            {
                Toast.makeText(ProductList.this, "Please check your Internet Connection!", Toast.LENGTH_SHORT).show();
                return;
            }

        }
        //search
        materialSearchBar = (MaterialSearchBar)findViewById(R.id.searchBar);
        materialSearchBar.setHint("Enter Product");
        //materialSearchBar.setSpeechMode(false);
        loadSuggest();
        materialSearchBar.setLastSuggestions(suggestList);
        materialSearchBar.setCardViewElevation(10);
        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //change suggestion as user types

                List<String>suggest = new ArrayList<String>();
                for(String search:suggestList)//
                {
                    if(search.toLowerCase().contains(materialSearchBar.getText().toLowerCase()))
                        suggest.add(search);
                }
                materialSearchBar.setLastSuggestions(suggest);

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                //when bar is closed restore original adapter
                if(!enabled)
                    recyclerView.setAdapter(adapter);
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                //when search is finished show the results
                //startSearch(text);
                startSearchByBarcode(text);

            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });

    }

    private void showAddProductDialog() {


        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ProductList.this);
        alertDialog.setTitle("Add new Product");
        alertDialog.setMessage("Please fill details");

        LayoutInflater inflater = this.getLayoutInflater();
        View add_menu_layout = inflater.inflate(R.layout.add_new_product_layout,null);

        edtName = add_menu_layout.findViewById(R.id.edtName);
        edtManufacturer = add_menu_layout.findViewById(R.id.edtManufacturer);
        edtPrice = add_menu_layout.findViewById(R.id.edtPrice);
        edtBarcode = add_menu_layout.findViewById(R.id.edtBarcode);
        edtQty = add_menu_layout.findViewById(R.id.edtQty);


        btnSelect = add_menu_layout.findViewById(R.id.btnSelect);
        btnUpload = add_menu_layout.findViewById(R.id.btnUpload);

        //create event for button
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage(); //
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
            }
        });

        alertDialog.setView(add_menu_layout);
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();

                // here create new category
                if(newProduct != null)
                {
                    productList.push().setValue(newProduct);
                    Snackbar.make(rootLayout,"New Category "+newProduct.getName()+"was added",Snackbar.LENGTH_SHORT).show();
                }

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

    private void startSearch(CharSequence text) {

        searchAdapter = new FirebaseRecyclerAdapter<Product, ProductViewHolder>(
                Product.class,
                R.layout.product_item,
                ProductViewHolder.class,
                productList.orderByChild("name").equalTo(text.toString())//compare name
        ) {
            @Override
            protected void populateViewHolder(ProductViewHolder viewHolder, Product model, int position) {
                viewHolder.product_name.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage())
                        .into(viewHolder.product_image);

                final Product local = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                        Intent productDetail = new Intent(ProductList.this, ProductDetail.class);
                        productDetail.putExtra("productId", searchAdapter.getRef(position).getKey());
                        startActivity(productDetail);

                    }
                });
            }
        };
        recyclerView.setAdapter(searchAdapter);
    }

    private void startSearchByBarcode(CharSequence text) {

        searchAdapter = new FirebaseRecyclerAdapter<Product, ProductViewHolder>(
                Product.class,
                R.layout.product_item,
                ProductViewHolder.class,
                productList.orderByChild("barcode").equalTo(text.toString())//compare name
                //productList.orderByChild("name").equalTo(text.toString())//compare name
        ) {
            @Override
            protected void populateViewHolder(ProductViewHolder viewHolder, Product model, int position) {
                //viewHolder.product_name.setText(model.getName());
                viewHolder.product_name.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage())
                        .into(viewHolder.product_image);

                final Product local = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                        Intent productDetail = new Intent(ProductList.this, ProductDetail.class);
                        productDetail.putExtra("productId", searchAdapter.getRef(position).getKey());
                        startActivity(productDetail);

                    }
                });
            }
        };
        recyclerView.setAdapter(searchAdapter);
    }

    private void uploadImage() {
        if(saveUri != null)
        {
            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploading");
            mDialog.show();

            String imageName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("images/"+imageName);
            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mDialog.dismiss();
                            Toast.makeText(ProductList.this, "Uploaded", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //set values for new category
                                    newProduct =new Product();
                                    newProduct.setName(edtName.getText().toString());
                                    newProduct.setAuthor(edtManufacturer.getText().toString());
                                    newProduct.setPrice(edtPrice.getText().toString());
                                    newProduct.setBarcode(edtBarcode.getText().toString());
                                    newProduct.setWarehouseQty(edtQty.getText().toString());
                                    newProduct.setMenuId(categoryId);
                                    newProduct.setImage(uri.toString());


                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mDialog.dismiss();
                            Toast.makeText(ProductList.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()/ taskSnapshot.getTotalByteCount());
                            mDialog.setMessage("uploaded" + progress+"%");
                        }
                    });


        }

    }

    private void chooseImage() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"), Common.PICK_IMAGE_REQUEST);
    }


    private void loadSuggest() {
        productList.orderByChild("menuId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot postSnapshot:dataSnapshot.getChildren())
                        {
                            Product item = postSnapshot.getValue(Product.class);
                            suggestList.add(item.getName());

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void loadListProduct(String categoryId) {

        adapter = new FirebaseRecyclerAdapter<Product, ProductViewHolder>(Product.class,R.layout.product_item,
                ProductViewHolder.class,productList.orderByChild("menuId").equalTo(categoryId)) //like select * from products where menu id =
        {
            @Override
            protected void populateViewHolder(ProductViewHolder viewHolder, Product model, int position) {

                viewHolder.product_name.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage())
                        .into(viewHolder.product_image);

                final Product local = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                        Intent productDetail = new Intent(ProductList.this,ProductDetail.class);
                        productDetail.putExtra("productId",adapter.getRef(position).getKey());
                        startActivity(productDetail);

                    }
                });


            }
        };

        //setting adapter
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

    }

    /*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
       super.onActivityResult(requestCode, resultCode, data);
       if (requestCode == Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null)  {

            saveUri = data.getData();
            btnSelect.setText("Image Selected");
       }
    }
    */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result !=null){
            if(result.getContents()==null){
                Toast.makeText(this, "Scanning Canncelled", Toast.LENGTH_SHORT).show();
            }
            else{
                //Toast.makeText(this, result.getContents(), Toast.LENGTH_LONG).show();
                materialSearchBar.setText(result.getContents());
            }

        }
        if (requestCode == Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null)  {

            saveUri = data.getData();
            btnSelect.setText("Image Selected");
        }
        else {


            super.onActivityResult(requestCode, resultCode, data);
        }
    }




    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (auth.equals("true") && item.getTitle().equals(Common.UPDATE))
        {
            showUpdateProductDialog(adapter.getRef(item.getOrder()).getKey(),adapter.getItem(item.getOrder()));

        }
        else if (auth.equals("true") && item.getTitle().equals(Common.DELETE))
        {
            deleteProduct(adapter.getRef(item.getOrder()).getKey());

        }
        else
        {
            Toast.makeText(ProductList.this, "Only assesable to warehouse staff", Toast.LENGTH_SHORT).show();
        }
        return super.onContextItemSelected(item);
    }

    private void deleteProduct(String key) {

        productList.child(key).removeValue();    }


    private void showUpdateProductDialog(final String key, final Product item) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ProductList.this);
        alertDialog.setTitle("Edit Product");
        alertDialog.setMessage("Please fill details");

        LayoutInflater inflater = this.getLayoutInflater();
        View add_menu_layout = inflater.inflate(R.layout.add_new_product_layout,null);

        edtName = add_menu_layout.findViewById(R.id.edtName);
        edtManufacturer = add_menu_layout.findViewById(R.id.edtManufacturer);
        edtPrice = add_menu_layout.findViewById(R.id.edtPrice);
        edtBarcode = add_menu_layout.findViewById(R.id.edtBarcode);
        edtQty = add_menu_layout.findViewById(R.id.edtQty);

        //set default value for view
        edtName.setText(item.getName());
        edtManufacturer.setText(item.getAuthor());
        edtPrice.setText(item.getPrice());
        edtBarcode.setText(item.getBarcode());
        edtQty.setText(item.getWarehouseQty());

        btnSelect = add_menu_layout.findViewById(R.id.btnSelect);
        btnUpload = add_menu_layout.findViewById(R.id.btnUpload);

        //create event for button
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage(); //
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeImage(item);
            }
        });

        alertDialog.setView(add_menu_layout);
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();


                item.setName(edtName.getText().toString());
                item.setAuthor(edtManufacturer.getText().toString());
                item.setPrice(edtPrice.getText().toString());
                item.setBarcode(edtBarcode.getText().toString());
                item.setWarehouseQty(edtQty.getText().toString());


                productList.child(key).setValue(item);

                Snackbar.make(rootLayout, "Product" + item.getName() + "was edited", Snackbar.LENGTH_SHORT).show();
                //Toast.makeText(DateStatus.this, "Product Updated ", Toast.LENGTH_SHORT).show();



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

    private void changeImage(final Product item) {
        if(saveUri != null)
        {
            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploading");
            mDialog.show();

            String imageName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("images/"+imageName);
            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mDialog.dismiss();
                            Toast.makeText(ProductList.this, "Uploaded", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //set values for new category
                                    item.setImage(uri.toString());

                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mDialog.dismiss();
                            Toast.makeText(ProductList.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()/ taskSnapshot.getTotalByteCount());
                            mDialog.setMessage("uploaded" + progress+"%");
                        }
                    });


        }

    }

}
