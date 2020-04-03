package io.librehealth.firebasevideo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UploadVideo extends AppCompatActivity {

    private EditText imageNameET;
    private VideoView imageToUploadIV;

    private Button uploadBtn;
    private Uri objectUri;

    private StorageReference objectStorageReference;
    private FirebaseFirestore objectFirebaseFirestore;

    private Dialog objectDialog;
    private boolean isImageSelected=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_video);
    }

    private void connectXML()
    {
        try
        {
            imageNameET=findViewById(R.id.imageNameET);
            imageToUploadIV=findViewById(R.id.videoView2);

            uploadBtn=findViewById(R.id.button);
            imageToUploadIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectImageFromGallery();
                }
            });

            uploadBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    uploadImageToFirebaseStorage();
                }
            });
        }
        catch (Exception e)
        {
            Toast.makeText(this, "connectXML:"+
                    e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void selectImageFromGallery()
    {
        try
        {
            Intent objectIntent=new Intent();
            objectIntent.setType("image/*");

            objectIntent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(objectIntent,123);
        }
        catch (Exception e)
        {
            Toast.makeText(this, "selectImageFromGallery:"+
                    e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==123 && resultCode==RESULT_OK && data!=null)
        {
            objectUri=data.getData();
            objectUri=data.getData();
            imageToUploadIV.setVideoURI(objectUri);

            isImageSelected=true;
        }
    }

    private void uploadImageToFirebaseStorage()
    {
        try
        {
            if(isImageSelected) {

                if (!imageNameET.getText().toString().isEmpty()) {
                    objectDialog.show();
                    //Image.jpeg
                    String imageName = imageNameET.getText().toString() + "." + getExtension(objectUri);
                    final StorageReference finalImageRef = objectStorageReference.child(imageName);

                    //FirebaseStorage -> MyImages/yourName.jpeg
                    UploadTask objectUploadTask = finalImageRef.putFile(objectUri);
                    objectUploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                objectDialog.dismiss();
                                throw task.getException();

                            }

                            return finalImageRef.getDownloadUrl();
                        }
                    })
                            .addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()) {
                                        Map<String, Object> objectMap = new HashMap<>();
                                        objectMap.put("url", task.getResult().toString());

                                        objectFirebaseFirestore.collection("BSCSLinks")
                                                .document(imageNameET.getText().toString())
                                                .set(objectMap)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        objectDialog.dismiss();
                                                        Toast.makeText(UploadVideo.this, "Image Uploaded" +
                                                                " successfully", Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        objectDialog.dismiss();
                                                        Toast.makeText(UploadVideo.this, "Fails to upload image", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    } else {
                                        objectDialog.dismiss();
                                        Toast.makeText(UploadVideo.this, "Msg from Firebase:" +
                                                task.getException(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    objectDialog.dismiss();
                                    Toast.makeText(UploadVideo.this, "Firebase Storage Response:"
                                            + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                } else {
                    Toast.makeText(this, "Please enter a valid name.", Toast.LENGTH_SHORT).show();
                }
            }
            else
            {
                Toast.makeText(this, "Please choose image before uploading", Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception e)
        {
            objectDialog.dismiss();
            Toast.makeText(this, "uploadImageToFirebaseStorage:"+
                    e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String getExtension(Uri objectUri)
    {
        try
        {
            ContentResolver objectContentResolver=getContentResolver();
            MimeTypeMap objectMimeTypeMap=MimeTypeMap.getSingleton();

            String extensionOfImage=objectMimeTypeMap.getExtensionFromMimeType(objectContentResolver
                    .getType(objectUri));

            return extensionOfImage;
        }
        catch (Exception e)
        {
            Toast.makeText(this, "getExtension:"+
                    e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        return null;

    }

}
