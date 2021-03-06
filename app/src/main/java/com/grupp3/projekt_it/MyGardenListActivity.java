package com.grupp3.projekt_it;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.lang.*;
import java.util.ArrayList;
import java.util.Arrays;

public class MyGardenListActivity extends BaseActivity {
    String TAG = "com.grupp3.projekt_it";
    Menu menu;

    public Boolean onModify;
    ArrayList<Garden> allGardens;
    ArrayList<String> selectedGardens;
    LinearLayout linearLayoutLeft;
    LinearLayout linearLayoutRight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_my_garden_list);
        /**
         * Adding our layout to parent class frame layout.
         */
        getLayoutInflater().inflate(R.layout.activity_my_garden_list2, frameLayout);

        /**
         * Setting title and itemChecked
         */
        mDrawerList.setItemChecked(position, true);
        //setTitle(listArray[position]);
        //((ImageView)findViewById(R.id.image_view)).setBackgroundResource(R.drawable.image1);

        onModify = false;
        // Build the list of created gardens (if none created, empty)
        selectedGardens = new ArrayList<>();

        linearLayoutLeft = (LinearLayout) findViewById(R.id.leftLinear);
        linearLayoutRight = (LinearLayout) findViewById(R.id.rightLinear);
        buildListView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my_garden_list, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
         int id = item.getItemId();

        //Change name setting in overflow menu is pressed
        if (id == R.id.action_settings) {
            onModify = true;
            Toast.makeText(MyGardenListActivity.this, "Tryck för att döpa om trädgård", Toast.LENGTH_LONG).show();
            changeNameView();
            return true;
        }
        //Delete garden in overflow menu is pressed
        if(id == R.id.remove_garden){
            onModify = true;
            Toast.makeText(MyGardenListActivity.this, "Tryck för att välja trädgård", Toast.LENGTH_LONG).show();
            // Change view to be able to delete a garden
            deleteGardenView();
            return true;
        }
        //Add a new garden option in action menu is pressed
        if (id == R.id.action_new) {
            Intent intent = new Intent(this, NewGardenActivity.class);
            startActivityForResult(intent, 1);
            return true;
        }
        if(id == R.id.action_discard){
            if(selectedGardens == null){
                return true;
            }
            GardenUtil gardenUtil = new GardenUtil();
            for(String gardenName : selectedGardens){
                gardenUtil.deleteGarden(gardenName, getApplicationContext());
            }
            selectedGardens.clear();
            MenuItem discardItem = menu.findItem(R.id.action_discard);
            discardItem.setVisible(false);
            buildListView();
            onModify = false;
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == NewGardenActivity.RESULT_OK) {
            if (requestCode == 1) {
                String[] result = data.getStringArrayExtra("Result");
                Context context = getApplicationContext();
                GardenUtil gardenUtil = new GardenUtil();
                //counters
                int tableNumber = gardenUtil.getTableNumber(context);
                int picNumber = gardenUtil.getPicNumber(context);
                gardenUtil.setTableNumber(context, tableNumber + 1);
                gardenUtil.setPicNumber(context, picNumber + 1);

                Log.i(TAG, Integer.toString(tableNumber));
                String tableName = "t" + Integer.toString(tableNumber);
                Log.i(TAG, tableName);
                //set zone = 1, quickfix
                Garden garden = new Garden(result[0], result[1], tableName, 1, picNumber);
                String[] files = getApplicationContext().fileList();

                ArrayList<String> files2 = new ArrayList<String>(Arrays.asList(files));
                if (files2.contains(result[0] + ".grdn")) {
                    Toast toast = Toast.makeText(context, "Trädgård finns redan", Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }

                SQLPlantHelper sqlPlantHelper = new SQLPlantHelper(getApplicationContext());
                sqlPlantHelper.createNewTable(tableName);

                gardenUtil.saveGarden(garden, context);
                OnBootReceiver.setForecastAlarms(getApplicationContext());
                buildListView();
            }
        }
    }

    public void buildListView() {
        getSupportActionBar().setTitle("Min Trädgård");
        GardenUtil gardenUtil = new GardenUtil();
        allGardens = gardenUtil.loadAllGardens(getApplicationContext());
        ArrayList<Garden> toLeftList = new ArrayList<>();
        ArrayList<Garden> toRightList = new ArrayList<>();
        boolean left = false;
        for(Garden garden : allGardens){
            if(left){
                toLeftList.add(garden);
                left = false;
            }else{
                toRightList.add(garden);
                left = true;
            }
        }
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(5, 0, 5, 48);

        ArrayAdapter <Garden> adapterLeft = new GardenListAdapter(toLeftList);
        ArrayAdapter <Garden> adapterRight = new GardenListAdapter(toRightList);

        final int adapterCountLeft = adapterLeft.getCount();
        linearLayoutLeft.removeAllViews();
        View view = getLayoutInflater().inflate(R.layout.garden_list_item_new, null);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNewItemClick(v);
            }
        });

        linearLayoutLeft.addView(view, layoutParams);

        for (int i = 0; i < adapterCountLeft; i++) {
            View item = adapterLeft.getView(i, null, null);
            linearLayoutLeft.addView(item, layoutParams);
        }
        final int adapterCountRight = adapterRight.getCount();
        linearLayoutRight.removeAllViews();
        for (int i = 0; i < adapterCountRight; i++) {
            View item = adapterRight.getView(i, null, null);
            linearLayoutRight.addView(item, layoutParams);
        }
    }

    // When the delete garden settings option is chosen this view is shown and clicking on items
    // in the list will remove them
    public void deleteGardenView() {
        getSupportActionBar().setTitle("Redigeringsläge");
        MenuItem discardItem = menu.findItem(R.id.action_discard);
        discardItem.setVisible(true);

        GardenUtil gardenUtil = new GardenUtil();
        allGardens = gardenUtil.loadAllGardens(getApplicationContext());
        ArrayList<Garden> toLeftList = new ArrayList<>();
        ArrayList<Garden> toRightList = new ArrayList<>();
        boolean left = false;
        for(Garden garden : allGardens){
            if(left){
                toLeftList.add(garden);
                left = false;
            }else{
                toRightList.add(garden);
                left = true;
            }
        }
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(5, 0, 5, 48);

        ArrayAdapter <Garden> adapterLeft = new DeleteGardenListAdapter(toLeftList);
        ArrayAdapter <Garden> adapterRight = new DeleteGardenListAdapter(toRightList);

        final int adapterCountLeft = adapterLeft.getCount();
        linearLayoutLeft.removeAllViews();
        View view = getLayoutInflater().inflate(R.layout.garden_list_item_new, null);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNewItemClick(v);
            }
        });
        linearLayoutLeft.addView(view, layoutParams);
        for (int i = 0; i < adapterCountLeft; i++) {
            View item = adapterLeft.getView(i, null, null);
            linearLayoutLeft.addView(item, layoutParams);
        }

        final int adapterCountRight = adapterRight.getCount();
        linearLayoutRight.removeAllViews();
        for (int i = 0; i < adapterCountRight; i++) {
            View item = adapterRight.getView(i, null, null);
            linearLayoutRight.addView(item, layoutParams);
        }
    }
    // When the change name settings option is chosen this view is shown and clicking on an item in
    // the list will allow the user to change the name of the garden
    public void changeNameView() {
        getSupportActionBar().setTitle("Redigeringsläge");

        GardenUtil gardenUtil = new GardenUtil();
        allGardens = gardenUtil.loadAllGardens(getApplicationContext());
        ArrayList<Garden> toLeftList = new ArrayList<>();
        ArrayList<Garden> toRightList = new ArrayList<>();
        boolean left = false;
        for(Garden garden : allGardens){
            if(left){
                toLeftList.add(garden);
                left = false;
            }else{
                toRightList.add(garden);
                left = true;
            }
        }
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(5, 0, 5, 48);

        ArrayAdapter <Garden> adapterLeft = new ChangeGardenListAdapter(toLeftList);
        ArrayAdapter <Garden> adapterRight = new ChangeGardenListAdapter(toRightList);

        final int adapterCountLeft = adapterLeft.getCount();
        linearLayoutLeft.removeAllViews();
        View view = getLayoutInflater().inflate(R.layout.garden_list_item_new, null);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNewItemClick(v);
            }
        });
        linearLayoutLeft.addView(view, layoutParams);

        for (int i = 0; i < adapterCountLeft; i++) {
            View item = adapterLeft.getView(i, null, null);
            linearLayoutLeft.addView(item, layoutParams);
        }
        final int adapterCountRight = adapterRight.getCount();
        linearLayoutRight.removeAllViews();
        for (int i = 0; i < adapterCountRight; i++) {
            View item = adapterRight.getView(i, null, null);
            linearLayoutRight.addView(item, layoutParams);
        }
    }
    // Override the back button to change the view back to the previous if it is set as one of the two
    // settings views, if not go back to previous activity
    @Override
    public void onBackPressed() {
        if(onModify == true){
            onModify = false;
            MenuItem discardItem = menu.findItem(R.id.action_discard);
            discardItem.setVisible(false);
            buildListView();
            return;
        }else {
            super.onBackPressed();
        }
    }
    public void onNewItemClick(View view){
        Intent intent = new Intent(this, NewGardenActivity.class);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void openActivity(int position) {
        /**
         * All activities with navigation drawer must contain this override function in order
         * to not be able to launch another instance of itself from navigation bar.
         * Remove correct startActivity(..) call to do so.
         */

//		mDrawerList.setItemChecked(position, true);
//		setTitle(listArray[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
        BaseActivity.position = position; //Setting currently selected position in this field so that it will be available in our child activities.

        switch (position) {
            case 0:
                startActivity(new Intent(this, MainActivity.class));
                break;
            case 1:
                break;
            case 2:
                startActivity(new Intent(this, NotificationManager.class));
                break;
            case 3:
                startActivity(new Intent(this, PlantSearchActivity.class));
                break;
            case 4:
                startActivity(new Intent(this, HelpActivity.class));
                break;
            case 5:
                startActivity(new Intent(this, Login.class));
                break;
            case 6:
                startActivity(new Intent(this, Preferences.class));
                break;
            default:
                break;
        }
    }
    private class DeleteGardenListAdapter extends ArrayAdapter <Garden> {
        ArrayList <Garden> gardenList;
        public DeleteGardenListAdapter(ArrayList<Garden> gardenList) {
            super(MyGardenListActivity.this, R.layout.garden_list_item_delete2, gardenList);
            this.gardenList = gardenList;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            Context context = getApplicationContext();
            View gardenItemView = convertView;
            //check if given view is null, if so inflate a new one
            if (gardenItemView == null) {
                gardenItemView = getLayoutInflater().inflate(R.layout.garden_list_item_delete2, parent, false);
            }
            //find garden to work with
            final Garden garden = gardenList.get(position);

            SQLPlantHelper sqlPlantHelper = new SQLPlantHelper(getApplicationContext());
            ArrayList <Plant_DB> allPlants = sqlPlantHelper.getAllPlants(garden.getTableName());
            ImageView imageView1 = (ImageView) gardenItemView.findViewById(R.id.imageView1);
            int picNumber = garden.getPicNumber();
            if(picNumber == 1){
                imageView1.setImageResource(R.drawable.ic_flower_to_garden1);
            }
            if(picNumber == 2){
                imageView1.setImageResource(R.drawable.ic_flower_to_garden2);
            }
            if(picNumber == 3){
                imageView1.setImageResource(R.drawable.ic_flower_to_garden3);
            }
            if(picNumber == 4){
                imageView1.setImageResource(R.drawable.ic_flower_to_garden4);
            }

            TextView textView1 = (TextView) gardenItemView.findViewById(R.id.textView1);
            textView1.setText(garden.getName());

            String gardenCity = garden.getLocation().substring(0, garden.getLocation().length() - 4);
            TextView textView2 = (TextView) gardenItemView.findViewById(R.id.textView2);
            textView2.setText(gardenCity);

            final CheckBox checkBox1 = (CheckBox)gardenItemView.findViewById(R.id.checkBox1);
            checkBox1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        boolean inList = false;
                        for(String gardenName: selectedGardens){
                            if(gardenName.equals(garden.getName())) {
                                inList = true;
                                break;
                            }
                        }
                        if(!inList){
                            selectedGardens.add(garden.getName());
                        }
                    }else if(!isChecked){
                        boolean inList = false;
                        for(String gardenName: selectedGardens){
                            if(gardenName.equals(garden.getName())) {
                                inList = true;
                                break;
                            }
                        }
                        if(inList){
                            selectedGardens.remove(garden.getName());
                        }
                    }
                }
            });
            return gardenItemView;
        }
    }
    private class GardenListAdapter extends ArrayAdapter <Garden> {
        ArrayList <Garden> gardenList;
        public GardenListAdapter(ArrayList<Garden> gardenList){
            super(MyGardenListActivity.this, R.layout.garden_list_item2, gardenList);
            this.gardenList = gardenList;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Context context = getApplicationContext();
            View gardenItemView = convertView;
            //check if given view is null, if so inflate a new one
            if(gardenItemView == null){
                gardenItemView = getLayoutInflater().inflate(R.layout.garden_list_item2, parent, false);
            }
            //find garden to work with
            Garden garden = gardenList.get(position);


            SQLPlantHelper sqlPlantHelper = new SQLPlantHelper(getApplicationContext());
            ArrayList <Plant_DB> allPlants = sqlPlantHelper.getAllPlants(garden.getTableName());
            ImageView imageView1 = (ImageView) gardenItemView.findViewById(R.id.imageView1);

            int picNumber = garden.getPicNumber();
            if(picNumber == 1){
                imageView1.setImageResource(R.drawable.ic_flower_to_garden1);
            }
            if(picNumber == 2){
                imageView1.setImageResource(R.drawable.ic_flower_to_garden2);
            }
            if(picNumber == 3){
                imageView1.setImageResource(R.drawable.ic_flower_to_garden3);
            }
            if(picNumber == 4){
                imageView1.setImageResource(R.drawable.ic_flower_to_garden4);
            }

            TextView textView1 = (TextView) gardenItemView.findViewById(R.id.textView1);
            textView1.setText(garden.getName());

            String gardenCity = garden.getLocation().substring(0, garden.getLocation().length()-4);
            TextView textView2 = (TextView) gardenItemView.findViewById(R.id.textView2);
            textView2.setText(gardenCity);

            final int finalPosition = position;
            ImageButton showGarden = (ImageButton)gardenItemView.findViewById(R.id.show_garden);
            showGarden.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Garden garden = gardenList.get(finalPosition);
                    String gardenName = garden.getName();
                    Intent intent = new Intent(getApplicationContext(), MyGardenActivity.class);
                    intent.putExtra("gardenName", gardenName);
                    startActivity(intent);
                }
            });

            gardenItemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    Garden garden = gardenList.get(finalPosition);
                    String gardenName = garden.getName();
                    Intent intent = new Intent(getApplicationContext(), MyGardenActivity.class);
                    intent.putExtra("gardenName", gardenName);
                    startActivity(intent);
                }
            });
            gardenItemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Garden garden = gardenList.get(finalPosition);
                    String gardenName = garden.getName();

                    FragmentManager fragmentManager = getFragmentManager();
                    GardenListFragment quickOptions = new GardenListFragment();

                    Bundle bundle = new Bundle();
                    bundle.putString("gardenName", gardenName);

                    quickOptions.setArguments(bundle);
                    quickOptions.show(fragmentManager, "disIsTag");
                    return true;
                }
            });


            return gardenItemView;
        }
    }
    private class ChangeGardenListAdapter extends ArrayAdapter <Garden> {
        ArrayList<Garden> gardenList;

        public ChangeGardenListAdapter(ArrayList<Garden> gardenList) {
            super(MyGardenListActivity.this, R.layout.garden_list_item2, gardenList);
            this.gardenList = gardenList;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Context context = getApplicationContext();
            View gardenItemView = convertView;
            //check if given view is null, if so inflate a new one
            if (gardenItemView == null) {
                gardenItemView = getLayoutInflater().inflate(R.layout.garden_list_item2, parent, false);
            }
            //find garden to work with
            Garden garden = gardenList.get(position);

            //set image
            SQLPlantHelper sqlPlantHelper = new SQLPlantHelper(getApplicationContext());
            ArrayList <Plant_DB> allPlants = sqlPlantHelper.getAllPlants(garden.getTableName());
            ImageView imageView1 = (ImageView) gardenItemView.findViewById(R.id.imageView1);
            int picNumber = garden.getPicNumber();
            if(picNumber == 1){
                imageView1.setImageResource(R.drawable.ic_flower_to_garden1);
            }
            if(picNumber == 2){
                imageView1.setImageResource(R.drawable.ic_flower_to_garden2);
            }
            if(picNumber == 3){
                imageView1.setImageResource(R.drawable.ic_flower_to_garden3);
            }
            if(picNumber == 4){
                imageView1.setImageResource(R.drawable.ic_flower_to_garden4);
            }

            TextView textView1 = (TextView) gardenItemView.findViewById(R.id.textView1);
            textView1.setText(garden.getName());

            String gardenCity = garden.getLocation().substring(0, garden.getLocation().length() - 4);
            TextView textView2 = (TextView) gardenItemView.findViewById(R.id.textView2);
            textView2.setText(gardenCity);

            final int finalPosition = position;
            ImageButton showGarden = (ImageButton) gardenItemView.findViewById(R.id.show_garden);
            showGarden.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Garden garden = gardenList.get(finalPosition);
                    String gardenName = garden.getName();
                    // Change the name of the chosen garden
                    FragmentManager fragmentManager = getFragmentManager();
                    ChangeGardenNameFragment changName = new ChangeGardenNameFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("gardenName", gardenName);
                    changName.setArguments(bundle);
                    changName.show(fragmentManager, "disIsTag2");
                    onModify = false;
                    // Change back to the default view
                    MenuItem discardItem = menu.findItem(R.id.action_discard);
                    discardItem.setVisible(false);
                    buildListView();
                }
            });
            gardenItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Garden garden = gardenList.get(finalPosition);
                    String gardenName = garden.getName();
                    // Change the name of the chosen garden
                    FragmentManager fragmentManager = getFragmentManager();
                    ChangeGardenNameFragment changName = new ChangeGardenNameFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("gardenName", gardenName);
                    changName.setArguments(bundle);
                    changName.show(fragmentManager, "disIsTag2");
                    onModify = false;
                    // Change back to the default view
                    MenuItem discardItem = menu.findItem(R.id.action_discard);
                    discardItem.setVisible(false);
                    buildListView();
                }
            });
            return gardenItemView;
        }
    }
}
