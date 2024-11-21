package com.happy.tracku.viewes;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.happy.tracku.R;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class PhotoPunchActivity extends AppCompatActivity
{

    // Define the pic id
    private static final int pic_id = 123;
    // Define the button and imageview type variable
    Button camera_open_id;
    ImageView click_image_id;
    Bitmap photo, resizedBitmapBig;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_punch);

        // By ID we can get each component which id is assigned in XML file get Buttons and imageview.
        camera_open_id = findViewById(R.id.camera_button);
        click_image_id = findViewById(R.id.click_image);

        // Camera_open button is for open the camera and add the setOnClickListener in this button
//        camera_open_id.setOnClickListener(v -> {
//            // Create the camera_intent ACTION_IMAGE_CAPTURE it will open the camera for capture the image
//            Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//            // Start the activity with camera_intent, and request pic id
//            startActivityForResult(camera_intent, pic_id);
//        });

    }

    // This method will help to retrieve the image
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Match the request 'pic id with requestCode
        if (requestCode == pic_id) {
            // BitMap is data structure of image file which store the image in memory
            photo = (Bitmap) data.getExtras().get("data");
            // Set the image in imageview for display
            click_image_id.setImageBitmap(photo);
        }
    }

    public void listeners(View view)
    {
        switch (view.getId())
        {
            case R.id.camera_button:
            {
                Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Start the activity with camera_intent, and request pic id
                startActivityForResult(camera_intent, pic_id);
            }
            break;

            case R.id.submitButon:
            {
                /**
                 * Today's Date
                 */
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String currentDateAndTime = sdf.format(calendar.getTime());
                Log.e("Log", "currentDateAndTime" + currentDateAndTime);

                /**
                 * Bitmap to base64
                 */

                byte[] bytearray = photo;
                InputStream myInputStream = new ByteArrayInputStream(bytearray);
                Bitmap bitmap = BitmapFactory.decodeStream(myInputStream);
                //Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 300, 200, true);
                //Drawable image = new BitmapDrawable(getResources(), BitmapFactory.decodeByteArray(bytearray, 0, bytearray.length));


                //previewImageView.setImageDrawable(image);
                resizedBitmapBig = Bitmap.createScaledBitmap(bitmap, 480, 800, true);
                if(bytearray.length<=1024)
                {

                    resizedBitmapBig = bitmap;

                }

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                resizedBitmapBig.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream .toByteArray();

                String base64 = Base64.encodeToString(byteArray, Base64.DEFAULT);

            }
            break;
        }
    }
}