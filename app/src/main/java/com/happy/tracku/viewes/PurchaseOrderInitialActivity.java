package com.happy.tracku.viewes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.happy.tracku.R;
import com.happy.tracku.databinding.ActivityPurchaseOrderInitialBinding;
import com.happy.tracku.databinding.ActivityStockOutInitialBinding;

public class PurchaseOrderInitialActivity extends AppCompatActivity
{

    private ActivityPurchaseOrderInitialBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        binding = ActivityPurchaseOrderInitialBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        EdgeToEdge.enable(this);

        View rootView = findViewById(android.R.id.content); // Get root view

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            WindowInsetsCompat insetsCompat = WindowInsetsCompat.toWindowInsetsCompat(insets.toWindowInsets());
            int systemBarsInsetsTop = insetsCompat.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            int systemBarsInsetsBottom = insetsCompat.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;

            // Apply padding to your main content view
            v.setPadding(v.getPaddingLeft(), systemBarsInsetsTop, v.getPaddingRight(), systemBarsInsetsBottom);

            return WindowInsetsCompat.CONSUMED;
        });


        getSupportActionBar().setTitle("StockIn Initial Screen");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void listeners(View view)
    {
        switch (view.getId())
        {
            case R.id.ecopackbutton:
            {
                Intent firstIntent = new Intent(this, StockOutOrderListActivity.class);
                firstIntent.putExtra("company", 1);
                this.startActivity(firstIntent);
                // startActivity(new Intent(this, StockOutOrderListActivity.class));

            }
            break;
            case R.id.ecofiedbutton:
            {

                Intent secondIntent = new Intent(this, StockOutOrderListActivity.class);
                secondIntent.putExtra("company", 2);
                this.startActivity(secondIntent);
                //startActivity(new Intent(this, StockOutOrderListActivity.class));

            }
            break;
        }
    }
}