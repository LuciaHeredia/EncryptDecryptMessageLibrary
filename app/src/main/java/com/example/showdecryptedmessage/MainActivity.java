package com.example.showdecryptedmessage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class MainActivity extends AppCompatActivity {

    private TextView message_show;
    private EditText dec_secret_key_text;
    private Button dec_btn;
    private TextView message_decrypted;

    // add your FirebaseDatabase RealTime URL
    private static final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://encryptmessageapp-default-rtdb.europe-west1.firebasedatabase.app/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        listeners();

    }

    private void init() {
        message_show = findViewById(R.id.message_show);
        dec_secret_key_text = findViewById(R.id.dec_secret_key_text);
        dec_btn = findViewById(R.id.dec_main_btn);
        message_decrypted = findViewById(R.id.dec_message_show);
    }

    private void listeners() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                message_show.setText(dataSnapshot.child("message").getValue(String.class));
                Log.d("FIREBASE","Data Loaded");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

        dec_btn.setOnClickListener(v -> {
            String message_received;
            String secretKey = dec_secret_key_text.getText().toString();

            if(secretKey.isEmpty()) {
                dec_secret_key_text.setError("Must be filled.");
            }
            else {

                try {

                    message_received = EncryptUtils.showDecryptedMessage((String) message_show.getText(), secretKey);
                    message_decrypted.setText(message_received);

                } catch (NoSuchPaddingException | InvalidKeyException |
                        NoSuchAlgorithmException | IllegalBlockSizeException |
                        BadPaddingException | InvalidAlgorithmParameterException e) {
                    e.printStackTrace();
                }


            }
        });


    }

}