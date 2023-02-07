package com.example.TimeLess;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.Manifest;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddProductforSale extends AppCompatActivity {

    public static final Integer RecordAudioRequestCode = 1;

    private EditText name, price, long_description;
    private AutoCompleteTextView brandtext;
    private ImageView photo_taken, micButton;
    private Button take_picture, RL, RR;
    private FirebaseDatabase database;

    private String username = "";
    private String file_in_string;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int GALLERY = 0;
    private SpeechRecognizer speechRecognizer;
    //private SpeechRecognizer speechRecognizer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_productfor_sale);
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            checkPermission();
        }
        /*Bundle extras = getIntent().getExtras();
        if(extras!=null){
            this.username = extras.getString("username");
        }*/
        

        SharedPreferences prefs = getSharedPreferences("TimeLess", MODE_PRIVATE);
        username = prefs.getString("username", "UNKNOWN");
        //for testing
        Toast.makeText(this, "username:" + username, Toast.LENGTH_LONG).show();

        name = (EditText) findViewById(R.id.name_of_product_entry);
        price = (EditText) findViewById(R.id.price_entry);
        brandtext = (AutoCompleteTextView) findViewById(R.id.brandinput);
        long_description = (EditText) findViewById(R.id.longdes_entry);

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {
                long_description.setText("");
                long_description.setHint("Listening...");
            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {
                micButton.setImageResource(R.drawable.ic_mic);
                ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                long_description.setText(data.get(0));
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });

        micButton = (ImageView) findViewById(R.id.STTbutton);
        micButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                    speechRecognizer.stopListening();
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    micButton.setImageResource(R.drawable.ic_mic);
                    speechRecognizer.startListening(speechRecognizerIntent);
                }
                return false;
            }
        });

        RL = (Button) findViewById(R.id.RotateL);
        RL.setVisibility(View.INVISIBLE);
        RR = (Button) findViewById(R.id.RotateR);
        RR.setVisibility(View.INVISIBLE);
        photo_taken = (ImageView) findViewById(R.id.taken_pic);
        take_picture = (Button) findViewById(R.id.TakePicture);
        take_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dispatchTakePictureIntent();
                RL.setVisibility(View.VISIBLE);
                RR.setVisibility(View.VISIBLE);
            }
        });


        String[] brands = getResources().getStringArray(R.array.watchbrands);
        ArrayAdapter brandAdapter = new ArrayAdapter(getApplicationContext(), R.layout.dropdown_item, brands);
        brandtext.setAdapter(brandAdapter);
        brandtext.setThreshold(0);
        database = FirebaseDatabase.getInstance();
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO},RecordAudioRequestCode);
        }
    }


    private void dispatchTakePictureIntent() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        if (galleryIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(galleryIntent, GALLERY);
        }
        /*AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle("Select Action");
        String[] pictureDialogItems = {
                "Capture photo from camera",
                "Select photo from gallery"
                 };
        pictureDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which==0){
                            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                            }
                        }
                        else if(which ==1){
                            Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                            if(galleryIntent.resolveActivity(getPackageManager())!=null){
                                startActivityForResult(galleryIntent, GALLERY);
                            }


                        }
                    }
                });
        pictureDialog.show();*/
    }

    public void takepicture(View view) {
        //Take or choose picture from here
        dispatchTakePictureIntent();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            this.file_in_string = BitMapToString(imageBitmap);
            photo_taken.setImageBitmap(imageBitmap);
            photo_taken.setScaleX((float) 1.5);
        } else if (requestCode == GALLERY && resultCode == RESULT_OK) {
            Uri contentURI = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);
                try {
                    bitmap = rotateImageIfRequired(bitmap, data.getData());
                    Toast.makeText(AddProductforSale.this, "Image rotated and saved!", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(AddProductforSale.this, "Image Saved, no rotation needed!", Toast.LENGTH_SHORT).show();
                }
                this.file_in_string = BitMapToString(bitmap);
                //get the path of the image
                photo_taken.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(AddProductforSale.this, "Cannot save image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public String BitMapToString(Bitmap bit) {
        //Reduce the size to 200x200
        Bitmap bitmap = Bitmap.createScaledBitmap(bit, 200, 200, false);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //Compress the bitmap
        bitmap.compress(Bitmap.CompressFormat.PNG, 50, baos);
        byte[] b = baos.toByteArray();
        String temp = Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    public void clearclick(View view) {
        //Reset everything
        name.setText("");
        price.setText("");
        brandtext.setText("");
        long_description.setText("");
        photo_taken.setImageDrawable(null);
    }

    public void returnclick(View view) {
        finish();
    }

    public void rotlclick(View view) {
        Bitmap img = StringToBitMap(file_in_string);
        img = rotateImage(img, 270);
        photo_taken.setImageBitmap(img);
        file_in_string = BitMapToString(img);
        Toast.makeText(AddProductforSale.this, "Image has been rotated", Toast.LENGTH_SHORT).show();
    }

    public void rotrclick(View view) {
        Bitmap img = StringToBitMap(file_in_string);
        img = rotateImage(img, 90);
        photo_taken.setImageBitmap(img);
        file_in_string = BitMapToString(img);
        Toast.makeText(AddProductforSale.this, "Image has been rotated", Toast.LENGTH_SHORT).show();
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

    public void submitclick(View view) {
        String title_of_item = name.getText().toString();
        String price_of_item = price.getText().toString();
        String short_description_of_iem = brandtext.getText().toString();
        String long_description_of_iem = long_description.getText().toString();

        Product product = new Product(this.username, title_of_item, price_of_item, short_description_of_iem, long_description_of_iem, this.file_in_string);
        DatabaseReference product_database = database.getReference("products");

        //Creating a key by the database should use persons key instead
        String userid = product_database.push().getKey();
        product.setUser_id(userid);

        //Set one copy of item in products database
        Map<String, Object> database_entry = new HashMap<String, Object>();
        database_entry.put("username", username);
        database_entry.put("id", userid);
        database_entry.put("title", title_of_item);
        database_entry.put("price", price_of_item);
        database_entry.put("short", short_description_of_iem);
        database_entry.put("long", long_description_of_iem);
        database_entry.put("image", this.file_in_string);
        product_database.child(userid).setValue(database_entry);

        //Set the id of product in user's database too
        FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
        String user_id = current_user.getUid();

        DatabaseReference user_database = database.getReference("users").child(user_id).child("myproducts");
        user_database.child(userid).setValue(database_entry);

        //product_database.child(userid).setValue(product);
        Intent intent = new Intent(view.getContext(), Slidermenu.class);
        view.getContext().startActivity(intent);
    }

    private Bitmap rotateImageIfRequired(Bitmap img, Uri selectedImage) throws IOException {

        ExifInterface ei = new ExifInterface(selectedImage.getPath());
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    private Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }


}
