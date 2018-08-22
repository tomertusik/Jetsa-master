package com.example.gvy.jesta;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.webkit.URLUtil;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Tomer on 26/05/2018.
 */

public class Model {

    static Model instance = new Model();
    private JestasRoomDataBase localDB;
    private LiveData<List<Jesta>> jestastListData;
    private LiveData<List<Jesta>> myJestastListData;

    public interface JestasDeleteListener {
        void onJestaDeleted();
    }

    public interface JestasDoneListener {
        void onJestasLoaded(List<Jesta> jestas);
    }

    private Model() {
        localDB = JestasRoomDataBase.getDatabase();
        jestastListData = new JestaListData();
        myJestastListData = new MyJestaListData();
    }

    class JestaListData extends MutableLiveData<List<Jesta>> {

        private ValueEventListener eventListener;

        @Override
        protected void onActive() {
            super.onActive();
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    List<Jesta> s = localDB.jestaDao().getAllJestas();
                    postValue(s);
                    FirebaseDatabase instance = FirebaseDatabase.getInstance();
                    final DatabaseReference ref = instance.getReference().child("Jestas");
                    eventListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            final ArrayList<Jesta> jestas = new ArrayList<>();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Jesta jesta = snapshot.getValue(Jesta.class);
                                jestas.add(jesta);
                            }
                            setValue(jestas);
                            AsyncTask.execute(new Runnable() {
                                @Override
                                public void run() {
                                    localDB.jestaDao().deleteAll();
                                    localDB.jestaDao().insertAll(jestas);
                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Loading.stopLoading();

                        }
                    };
                    ref.addListenerForSingleValueEvent(eventListener);
                }
            });
        }

        @Override
        protected void onInactive() {
            super.onInactive();
            DatabaseReference stRef = FirebaseDatabase.getInstance().getReference().child("Jestas");
            stRef.removeEventListener(eventListener);
        }

        public JestaListData() {
            super();
            setValue(new LinkedList<Jesta>());
        }
    }

    class MyJestaListData extends MutableLiveData<List<Jesta>> {

        private ValueEventListener eventListener;

        @Override
        protected void onActive() {
            super.onActive();
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    List<Jesta> s = localDB.jestaDao().getMyJestas(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    postValue(s);
                    FirebaseDatabase instance = FirebaseDatabase.getInstance();
                    final DatabaseReference ref = instance.getReference().child("Jestas");
                    eventListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            final ArrayList<Jesta> jestas = new ArrayList<>();
                            final ArrayList<Jesta> myJestas = new ArrayList<>();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Jesta jesta = snapshot.getValue(Jesta.class);
                                jestas.add(jesta);
                                if (jesta.getOwnerUserID().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                    myJestas.add(jesta);
                                }
                            }
                            setValue(myJestas);
                            AsyncTask.execute(new Runnable() {
                                @Override
                                public void run() {
                                    localDB.jestaDao().deleteAll();
                                    localDB.jestaDao().insertAll(jestas);
                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Loading.stopLoading();

                        }
                    };
                    ref.addListenerForSingleValueEvent(eventListener);
                }
            });
        }

        @Override
        protected void onInactive() {
            super.onInactive();
            DatabaseReference stRef = FirebaseDatabase.getInstance().getReference().child("Jestas");
            stRef.removeEventListener(eventListener);
        }

        public MyJestaListData() {
            super();
            setValue(new LinkedList<Jesta>());
        }
    }

    public void deleteAllJestas() {
        new RoomAsync().execute();
    }

    public void deleteJesta(Jesta jesta, JestasDeleteListener listener) {
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference().child("Jestas");
        mRef.child(jesta.getOwnerUserID() + jesta.getId()).removeValue(new RemoveListener(jesta, listener));
        deleteImage(jesta);
    }

    private void deleteImage(Jesta jesta) {
        File dir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        String localName = getLocalImageFileName(jesta.getImageURL());
        File imageFile = new File(dir, localName);
        if (imageFile.exists()) {
            imageFile.delete();
        }
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("images");
        StorageReference ref = storageRef.child(jesta.getOwnerUserID() + jesta.getId());
        ref.delete();
    }

    public void AddNewJesta(Jesta jesta, Uri selectedImage) {
        saveImage(jesta, selectedImage, FirebaseAuth.getInstance().getCurrentUser().getUid() + jesta.getId());
    }

    private long getLastUpdate() {
        SharedPreferences sharedRef = JestaApp.getContext().getSharedPreferences("jestasUpdate", JestaApp.getContext().MODE_PRIVATE);
        long lastUpdate = sharedRef.getLong("lastUpdate", 0);
        return lastUpdate;
    }

    public LiveData<List<Jesta>> getAllJestas() {
        return jestastListData;
    }

    public LiveData<List<Jesta>> getMyJestas() {
        return myJestastListData;
    }

    public void getMyJestas(JestasDoneListener listener) {
        getJestas(new MyJestasListener(getLastUpdate(), listener));
    }

    private void getJestas(ValueEventListener listener) {
        FirebaseDatabase instance = FirebaseDatabase.getInstance();
        final DatabaseReference ref = instance.getReference().child("Jestas");
        ref.addListenerForSingleValueEvent(listener);
    }

    private class MyJestasListener implements ValueEventListener {

        private final JestasDoneListener listener;
        private long lastUpdate;

        public MyJestasListener(long lastUpdate, JestasDoneListener listener) {
            this.lastUpdate = lastUpdate;
            this.listener = listener;
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            ArrayList<Jesta> jestas = new ArrayList<>();
            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                Jesta jesta = snapshot.getValue(Jesta.class);
                if (jesta.getLastUpdate() > lastUpdate) {
                    lastUpdate = jesta.getLastUpdate();
                    SharedPreferences sharedRef = JestaApp.getContext().getSharedPreferences("jestasUpdate", JestaApp.getContext().MODE_PRIVATE);
                    SharedPreferences.Editor ed = sharedRef.edit();
                    ed.putLong("lastUpdate", lastUpdate);
                    ed.commit();
                    jestas.add(jesta);
                }
            }
            new MyJestasRoomAsync(jestas, listener).execute();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Loading.stopLoading();
        }
    }

    private void saveImage(final Jesta jesta, final Uri image, String key) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("images");
        final StorageReference ref = storageRef.child(key);
        ref.putFile(image).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return ref.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUrl = task.getResult();
                    jesta.setImageURL(downloadUrl.toString());
                    String localName = getLocalImageFileName(downloadUrl.toString());
                    Bitmap imageBitmap = null;
                    try {
                        imageBitmap = MediaStore.Images.Media.getBitmap(JestaApp.getContext().getContentResolver(), image);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    saveImageToFile(imageBitmap, localName);
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            DatabaseReference mRef = FirebaseDatabase.getInstance().getReference().child("Jestas");
                            mRef.child(jesta.getOwnerUserID() + jesta.getId()).setValue(jesta, new DoneListener());
                        }
                    });
                } else {
                    // Handle failures
                    // ...
                }
            }
        });







//                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                        // Get a URL to the uploaded content
//                        Uri downloadUrl = taskSnapshot.getUploadSessionUri();
//                        jesta.setImageURL(downloadUrl.toString());
//                        String localName = getLocalImageFileName(downloadUrl.toString());
//                        Bitmap imageBitmap = null;
//                        try {
//                            imageBitmap = MediaStore.Images.Media.getBitmap(JestaApp.getContext().getContentResolver(), image);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                        saveImageToFile(imageBitmap, localName);
//                        AsyncTask.execute(new Runnable() {
//                            @Override
//                            public void run() {
//                                DatabaseReference mRef = FirebaseDatabase.getInstance().getReference().child("Jestas");
//                                mRef.child(jesta.getOwnerUserID() + jesta.getId()).setValue(jesta, new DoneListener());
//                            }
//                        });
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception exception) {
//                        // Handle unsuccessful uploads
//                        // ...
//                    }
//                });




    }

    public String getLocalImageFileName(String url) {
        String name = URLUtil.guessFileName(url, null, null);
        return name;
    }

    public void saveImageToFile(Bitmap imageBitmap, String imageFileName) {
        try {
            File dir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES);
            if (!dir.exists()) {
                dir.mkdir();
            }
            File imageFile = new File(dir, imageFileName);
            imageFile.createNewFile();
            OutputStream out = new FileOutputStream(imageFile);
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();
            addPicureToGallery(imageFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addPicureToGallery(File imageFile) {
        //add the picture to the gallery so we dont need to manage the cache size
        Intent mediaScanIntent = new
                Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(imageFile);
        mediaScanIntent.setData(contentUri);
        JestaApp.getContext().sendBroadcast(mediaScanIntent);
    }

    public Bitmap loadImageFromFile(String imageFileName) {
        Bitmap bitmap = null;
        try {
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File imageFile = new File(dir, imageFileName);
            InputStream inputStream = new FileInputStream(imageFile);
            bitmap = BitmapFactory.decodeStream(inputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private class DoneListener implements DatabaseReference.CompletionListener {
        @Override
        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
            EventBus.getDefault().post(new AddJestaEvent());
        }
    }

    private class RoomAsync extends AsyncTask {

        public RoomAsync() {
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            localDB.jestaDao().deleteAll();
            return null;
        }
    }

    private class MyJestasRoomAsync extends AsyncTask {

        private final List<Jesta> jestas;
        private final JestasDoneListener listener;

        public MyJestasRoomAsync(List<Jesta> jestas, JestasDoneListener listener) {
            this.jestas = jestas;
            this.listener = listener;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            if (jestas.size() > 0) {
                localDB.jestaDao().insertAll(jestas);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    List<Jesta> jestas = localDB.jestaDao().getMyJestas(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    listener.onJestasLoaded(jestas);
                }
            });
        }
    }

    private class RemoveListener implements DatabaseReference.CompletionListener {

        private final JestasDeleteListener listener;
        private final Jesta jesta;

        public RemoveListener(Jesta jesta, JestasDeleteListener listener) {
            this.jesta = jesta;
            this.listener = listener;
        }

        @Override
        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
            new DeleteJestaRoomAsync(jesta, listener).execute();
        }
    }

    private class DeleteJestaRoomAsync extends AsyncTask {

        private final Jesta jesta;
        private final JestasDeleteListener listener;

        public DeleteJestaRoomAsync(Jesta jesta, JestasDeleteListener listener) {
            this.jesta = jesta;
            this.listener = listener;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            localDB.jestaDao().delete(jesta);
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    listener.onJestaDeleted();
                }
            });
        }
    }
}
