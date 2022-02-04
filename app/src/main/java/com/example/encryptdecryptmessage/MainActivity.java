package com.example.encryptdecryptmessage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.encdecmsglib.EncryptUtils;
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

    private EditText message_text;
    private String msg_input;
    private EditText secret_key_text;
    private String secret_key_input;
    private Button encrypt_btn;

    // add your FirebaseDatabase RealTime URL
    private static final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://encryptmessageapp-default-rtdb.europe-west1.firebasedatabase.app/");

    private TextView message_show; // OTHER APP
    private EditText dec_secret_key_text; // OTHER APP
    private Button dec_btn; // OTHER APP
    private TextView message_decrypted; // OTHER APP

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        listeners();
    }

    private void init() {
        message_text = findViewById(R.id.message_text);
        secret_key_text = findViewById(R.id.secret_key_text);
        encrypt_btn = findViewById(R.id.enc_main_btn);

        message_show = findViewById(R.id.message_show); // OTHER APP
        dec_secret_key_text = findViewById(R.id.dec_secret_key_text); // OTHER APP
        dec_btn = findViewById(R.id.dec_main_btn); // OTHER APP
        message_decrypted = findViewById(R.id.dec_message_show); // OTHER APP
    }

    private void listeners() {
        encrypt_btn.setOnClickListener(v -> {
            msg_input = message_text.getText().toString();
            secret_key_input = secret_key_text.getText().toString();

            if(msg_input.isEmpty()){
                message_text.setError("Must be filled.");
            }
            else if(secret_key_input.isEmpty()) {
                secret_key_text.setError("Must be filled.");
            }
            else{
                try {
                    String encryptedMessage = EncryptUtils.SendEncryptedMessage(msg_input, secret_key_input);
                    saveData(encryptedMessage, databaseReference);
                } catch (NoSuchPaddingException | InvalidKeyException |
                        NoSuchAlgorithmException | IllegalBlockSizeException |
                        BadPaddingException | InvalidAlgorithmParameterException e) {
                    e.printStackTrace();
                }
            }
        });


        // OTHER APP - THE RECEIVER

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

    public static void saveData(String encryptedMessage, DatabaseReference databaseReference){
        databaseReference.child("message").setValue(encryptedMessage).addOnCompleteListener(
                task -> Log.d("FIREBASE","Data saved"));
    }

}