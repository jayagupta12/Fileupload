package com.example.fileupload;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class PdfUpload extends AppCompatActivity {

    private static final int FILE_SELECT_CODE = 0;
    // private static final int READ_REQUEST_CODE = 42;
    private static final String TAG = "Mainactivity";
    Button click,upload;
    TextView text;
    PDFView pdfView;
    String encoded="";
    ImageView image;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pdfupload);

        click=(Button)findViewById(R.id.click);
        text=(TextView)findViewById(R.id.text) ;
        pdfView=(PDFView)findViewById(R.id.pdf);
        image=(ImageView)findViewById(R.id.image);
       upload=(Button)findViewById(R.id.upload);

        click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileChooser();
            }
        });


    }



    /**
     * Fires an intent to spin up the "file chooser" UI and select an image.
     */

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }






    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    try {
                        Uri uri = data.getData();
                        Log.d(TAG, "File Uri: " + uri.toString());
                        // Get the path
                        String path = FileUtils.getPath(PdfUpload.this, uri);
                        Log.d(TAG, "File Path: " + path);
                        text.setText(path);


                        int pageNumber=0;
                        pdfView.fromUri(uri)
                                .defaultPage(pageNumber)
                                .enableSwipe(true)
                                .swipeHorizontal(false)
                                //   .onPageChange(this)
                                .enableAnnotationRendering(true)
                                // .onLoad(this)
                                .scrollHandle(new DefaultScrollHandle(this))
                                .load();
                        File file =new File(path);
                        byte[] bytes = new byte[0];
                        try {
                            bytes = loadFile(file);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        encoded = Base64.encodeToString(bytes,Base64.DEFAULT);
                        upload.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String url="http://shikshan.indiansmarthub.co.in/api/android.php?";
                                Toast.makeText(PdfUpload.this, encoded, Toast.LENGTH_SHORT).show();



                                RequestQueue requestQueue= Volley.newRequestQueue(PdfUpload.this);
                                StringRequest stringRequest=new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        Log.e( "onResponse: ",response );
                                        Toast.makeText(PdfUpload.this, response, Toast.LENGTH_SHORT).show();
                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        // Toast.makeText(PdfUpload.this,error , Toast.LENGTH_SHORT).show();
                                    }
                                }) {
                                    @Override
                                    protected Map<String, String> getParams() throws AuthFailureError {

                                        Map<String,String> map = new HashMap<String, String>();
                                        map.put("image",encoded );



                                        return map;
                                    }};
                                Log.e("url", "uploadtoserver: "+url );
                                requestQueue.add(stringRequest);





                                //
                            }
                        });

                        Log.e(TAG, "onActivityResult: "+encoded );
                        // Get the file instance
                        // File file = new File(path);

                        // Initiate the upload
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private byte[] loadFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        long length = file.length();
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }
        byte[] bytes = new byte[(int)length];

        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
                && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }

        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }

        is.close();
        return bytes;
    }







}
