package com.example.TimeLess;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;


public class Buy_Screen extends AppCompatActivity implements View.OnClickListener {
    private TextView price_total, success_screen;
    double price_of_item, current_total;
    private Product product;
    private String product_id, title_of_product;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy__screen);

        //Get items by their id
        TextView test = (TextView) findViewById(R.id.title);
        TextView short_description = (TextView) findViewById(R.id.short_description_cart);
        price_total = (TextView) findViewById(R.id.price_update);


        success_screen = (TextView) findViewById(R.id.Success_banner);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            //Get key passed from previous intent


            //get the product Object from singleton class
            this.product = ProductsDatabase.Get_product_with_key(product_id);
            //this.product_id= product.getUser_id();
            this.product_id = product.getUsername();
            this.title_of_product = product.getTitle();
            //Modify the screen with items
            //test.setText(product.getTitle());

            test.setText(title_of_product);
            ProductsDatabase.setLast_name(title_of_product);
            ProductsDatabase.setLast_id(product.getUser_id());

            short_description.setText("Title: " + product.getShort_description());
            price_of_item = Double.valueOf(product.getPrice());
            price_total.setText((product.getPrice()));
            TextView long_des = (TextView) findViewById(R.id.long_description_cart);
            long_des.setText("Description: " + product.getLong_description());
            current_total = price_of_item;
            ImageView img = (ImageView) findViewById(R.id.activity_buy_image);
            img.setImageBitmap(StringToBitMap(product.getImage_uri()));
            Button add_to_cart = (Button) findViewById(R.id.add_to_cart_button);
            Button buy_click = (Button) findViewById(R.id.item_buy_button_cart);
            add_to_cart.setOnClickListener(this);
            buy_click.setOnClickListener(this);

        }
    }

    public Bitmap StringToBitMap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();

        if (id == R.id.add_to_cart_button) {
            Double quantity = current_total / price_of_item;

            //Get the current user id
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            String user_id = user.getUid();

            //open the record in the database of the user
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            if (product_id == null) {
                DatabaseReference my_cart = database.getReference("users").child(user_id).child("cart").child("123");

                //Create a new map for putting data
                HashMap<String, String> data = new HashMap<>();
                data.put("id", "exception");
                data.put("name", "Bazar");
                data.put("quantity", String.valueOf(quantity));

                //Add to the server
                my_cart.setValue(data);


                ProductsDatabase.add_to_cart(product, quantity);

            } else {
                DatabaseReference my_cart = database.getReference("users").child(user_id).child("cart").child(product_id);

                //Create a new map for putting data
                HashMap<String, String> data = new HashMap<>();
                data.put("id", product_id);
                data.put("name", title_of_product);
                data.put("quantity", String.valueOf(quantity));

                //Add to the server
                my_cart.setValue(data);


                ProductsDatabase.add_to_cart(product, quantity);

            }
            Toast toast = Toast.makeText(getApplicationContext(), "Item Added to Cart", Toast.LENGTH_SHORT);
            toast.show();
        } else if (id == R.id.item_buy_button_cart) {
            //Calculate number of items
            Double quantity = current_total / price_of_item;

            //Get the current user id
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            String user_id = user.getUid();

            //open the record in the database of the user
            FirebaseDatabase database = FirebaseDatabase.getInstance();

            DatabaseReference my_cart = database.getReference("users").child(user_id).child("cart").child(product_id);

            if (product_id != null) {
                //Create a new map for putting data
                HashMap<String, String> data = new HashMap<>();
                data.put("id", product_id);
                data.put("name", title_of_product);
                data.put("quantity", String.valueOf(quantity));

                //Add to the server
                my_cart.setValue(data);

                //ProductsDatabase.add_to_cart(product, quantity);
            }

            //Create a new intent and put ID of product in it and quantity
            Intent intent = new Intent(v.getContext(), Cart.class);
            intent.putExtra("id_number", product_id);
            intent.putExtra("quantity", quantity);

            ProductsDatabase.add_to_cart(product, quantity);
            ProductsDatabase.Add_product_with_key(product, product_id);

            v.getContext().startActivity(intent);

        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
