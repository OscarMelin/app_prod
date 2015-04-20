package com.grupp3.projekt_it;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Daniel on 2015-04-15.
 */
public class DownloadData extends AsyncTask<String, Void, String> {
    String TAG = "com.grupp3.projekt_it";
    String fileName;
    Context context;

    public DownloadData(String fileName, Context context){
        this.fileName = fileName;
        this.context = context;
    }
    @Override
    protected String doInBackground(String... urls){
        String rawData = "";
        try {
            // get url
            URL url = new URL(urls[0]);
            // setup http connectuin
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            //add api key
            connection.addRequestProperty("x-api-key","92a437c886b5e874102c9d28c6c10359");
            // get input
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            // build string from input stream
            StringBuffer json = new StringBuffer(1024);
            String tmp="";
            while((tmp=reader.readLine())!=null)
                json.append(tmp).append("\n");
            reader.close();
            //check input satus code
            JSONObject data = new JSONObject(json.toString());
            rawData = json.toString();

            if(data.getInt("cod") != 200){
                Log.i(TAG, "cod not 200");
                return null;
            }
            // return json string
            return rawData;
        }catch(Exception e){
            Log.i(TAG, "Exception in  weather client");
            return null;
        }
    }
    protected void onPostExecute(String result){
        //open file
        String value = "";
        FileInputStream fileInputStream;
        try{
            fileInputStream = context.openFileInput(fileName);
            byte[] input = new byte[fileInputStream.available()];
            while(fileInputStream.read(input) != -1){
                value += new String(input);
            }
            fileInputStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //convert from json to java object for both weather and garden
        Gson gson = new Gson();
        //convert forecast json to java object
        Forecast forecast = gson.fromJson(result, Forecast.class);
        //convert json file to java object;
        Garden garden = gson.fromJson(value, Garden.class);
        //add forecast to garden object
        garden.setForecast(forecast);
        //convert garden back to json
        String json = gson.toJson(garden);
        //save json in same file
        try {
            FileOutputStream fileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            fileOutputStream.write(json.getBytes());
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
