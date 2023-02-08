package com.example.TimeLess;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class AccountSettings extends Slidermenu {
    private String password;
    private TextView accountid;

    private static final String PREFS_NAME = "prefs";
    private static final String PREF_DARK_THEME = "dark_theme";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean useDarkTheme = preferences.getBoolean(PREF_DARK_THEME, false);

        if(useDarkTheme) {
            setTheme(R.style.AppTheme_Dark_NoActionBar);

        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        TextView usernameshow = (TextView) findViewById(R.id.UsernameShow);
        TextView emailshow = (TextView) findViewById(R.id.EmailShow);
        TextView listingscountshow = (TextView) findViewById(R.id.ListingCountShow);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String user_id = user.getUid();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference products = database.getReference("users").child(user_id).child("myproducts");
        products.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long count = dataSnapshot.getChildrenCount();
                Log.d("Count", "Number of entries: " + count);
                listingscountshow.setText(String.valueOf(count));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
                Toast.makeText(AccountSettings.this, "database error", Toast.LENGTH_LONG).show();
            }
        });
        SharedPreferences prefs = getSharedPreferences("TimeLess", MODE_PRIVATE);
        if(user!=null){
                emailshow.setText(user.getEmail());
                usernameshow.setText(prefs.getString("username", "UNKNOWN"));
                password = user.getUid();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.slidermenu, menu);
        return true;
    }
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.Cart) {
            //Load Cart page
            Intent intent = new Intent(getApplicationContext(), Cart.class);
            startActivity(intent);

        } else if (id == R.id.AccountSettings) {
            Intent intent = new Intent(getApplicationContext(), AccountSettings.class);
            startActivity(intent);


        } else if (id == R.id.Marketplace) {
            startActivity(new Intent(getApplicationContext(), Slidermenu.class));

        } else if (id == R.id.MySale) {
            //Connect with server and get all items currently on sale sold by me
            //recyclerView.swapAdapter(new ProductAdapter(this, Communicate_with_server.get_my_items_on_sale()),true);

            //Set the id of product in user's database too
            FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
            String user_id = current_user.getUid();

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference products = database.getReference("users").child(user_id).child("myproducts");

            products.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Map<String, Object> data = (Map<String, Object>) dataSnapshot.getValue();
                    if (data != null) {
                        update_my_items(data);
                    } else {
                        Toast toast = Toast.makeText(getApplicationContext(), "No Current Items on Sale by You", Toast.LENGTH_LONG);
                        toast.show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        } else if (id == R.id.SellItems) {
            Intent intent = new Intent(getApplicationContext(), AddProductforSale.class);
            startActivity(intent);

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
