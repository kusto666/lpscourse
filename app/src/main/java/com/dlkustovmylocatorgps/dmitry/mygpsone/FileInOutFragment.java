package com.dlkustovmylocatorgps.dmitry.mygpsone;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.UUID;


public class FileInOutFragment extends Fragment
{
    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    //Firebase - потом обобщим  здесь не оставим!!!
    FirebaseStorage storage;
    StorageReference storageReference;

    private Button btnChoose, btnUpload;
    private ImageView imageView;

    private Uri filePath;

    private final int PICK_IMAGE_REQUEST = 71;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View retView = inflater.inflate(R.layout.fragment_file_in_out, container, false);

        //Initialize Views
        btnChoose = (Button)retView.findViewById(R.id.btnChoose);
        btnUpload = (Button)retView.findViewById(R.id.btnUpload);
        imageView = (ImageView)retView.findViewById(R.id.imgView);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        btnChoose.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                System.out.println("chooseImage();");
                chooseImage();
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                System.out.println("uploadImage();");
                uploadImage();
            }
        });

        return retView;
    }

    private void chooseImage()
    {
        Intent intent = new Intent();
       /* intent.setType("image/*");*/
        intent.setType("file/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Выберите документ"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST /*&& resultCode == RESULT_OK */&& data != null && data.getData() != null )
        {
            filePath = data.getData();
            try
            {
                //Context applicationContext = MainActivity.getContextOfApplication();
                //applicationContext.getContentResolver();
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getApplicationContext().getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void uploadImage()
    {

        if(filePath != null)
        {

            final ProgressDialog progressDialog = new ProgressDialog(this.getContext());
            progressDialog.setTitle("Отправка...");
            progressDialog.show();

            // Здесь позже рандом названия будем брать из textfield!!!
            StorageReference ref = storageReference.child("images/"+ UUID.randomUUID().toString());
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(getActivity().getApplicationContext(), "Файл отправлен!", Toast.LENGTH_SHORT).show();
                            filePath = null;
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getActivity().getApplicationContext(), "Ошибка отправки файла "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
        }
        else
        {
            Toast.makeText(getActivity().getApplicationContext(), "Документ не выбран!", Toast.LENGTH_SHORT).show();
        }
    }
}