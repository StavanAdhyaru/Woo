package com.example.woo.Chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.woo.Matches.MatchesActivity;
import com.example.woo.Matches.MatchesAdapter;
import com.example.woo.Matches.MatchesObject;
import com.example.woo.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class ChatActivity extends AppCompatActivity {


    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mChatAdapter;
    private RecyclerView.LayoutManager mChatLayoutManager;
    private TextView mChatUserName;
    private EditText mSendEditText;
    private Button mSendButton;
    private ImageView mChatUserPicture;
    private String currentUserId, matchId,chatId;
    private String password = "AeIoU184xabcyz34";
    private String AES = "AES";

    DatabaseReference mDatabaseUser,mDatabaseChat,mChatUserDetails;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        matchId = getIntent().getExtras().getString("matchId");

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId).child("connections").child("matches").child(matchId).child("ChatId");
        mDatabaseChat = FirebaseDatabase.getInstance().getReference().child("Chat");
        mChatUserDetails = FirebaseDatabase.getInstance().getReference().child("Users").child(matchId);
        getChatId();
        mChatUserName = (TextView)findViewById(R.id.chatUserId);
     //   mChatUserPicture = (ImageView)findViewById(R.id.chatUserPicture);
        mRecyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setHasFixedSize(false);
        mChatLayoutManager = new LinearLayoutManager(ChatActivity.this);
        mRecyclerView.setLayoutManager(mChatLayoutManager);
        mChatAdapter = new ChatAdapter(getDataSetChat(),ChatActivity.this);
        mRecyclerView.setAdapter(mChatAdapter);
        mSendEditText = (EditText) findViewById(R.id.message);
        mSendButton = (Button)findViewById(R.id.send);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
    }
    private void sendMessage() {
        String sendMessageText = null;
        try {
            sendMessageText = encryption(mSendEditText.getText().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(!sendMessageText.isEmpty()){
            DatabaseReference newMessageDb = mDatabaseChat.push();

            Map newMessage = new HashMap();
            newMessage.put("createByUser",currentUserId);
            newMessage.put("text",sendMessageText);
            newMessageDb.setValue(newMessage);
        }
        mSendEditText.setText(null);
    }

    private String encryption(String text) throws Exception{
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = password.getBytes("UTF-8");
        digest.update(bytes,0,bytes.length);
        byte[] key = digest.digest();
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");

        Cipher c = Cipher.getInstance(AES);
        c.init(Cipher.ENCRYPT_MODE,secretKeySpec);
        byte[] encVal = c.doFinal(text.getBytes());
        String encryptedValue = Base64.encodeToString(encVal,Base64.DEFAULT);
        return encryptedValue;
    }
    private String decryption(String text) throws Exception {
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = password.getBytes("UTF-8");
        digest.update(bytes,0,bytes.length);
        byte[] key = digest.digest();
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");

        Cipher c = Cipher.getInstance(AES);
        c.init(Cipher.DECRYPT_MODE,secretKeySpec);
        byte[] decodedValue = Base64.decode(text,Base64.DEFAULT);
        byte[] decValue = c.doFinal(decodedValue);
        String decryptedValue = new String(decValue);
        return decryptedValue;
    }
    private void getChatId(){
        mChatUserDetails.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.child("name").getValue()!= null){
                    mChatUserName.setText(dataSnapshot.child("name").getValue().toString());
                }

               // if(dataSnapshot.child("profileImageUrl").getValue()!= null){
                 //   mChatUserPicture.setImageURI(Uri.parse(dataSnapshot.child("profileImageUrl").getValue().toString()));}

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        mDatabaseUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    chatId = dataSnapshot.getValue().toString();
                    mDatabaseChat = mDatabaseChat.child(chatId);
                    getChatMessages();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getChatMessages() {

        mDatabaseChat.addChildEventListener((new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                if(dataSnapshot.exists()){
                    String message = null;
                    String createdByUser  =null;
                    if(dataSnapshot.child("text").getValue()!=null){
                        try {
                            message = decryption(dataSnapshot.child("text").getValue().toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if(dataSnapshot.child("createByUser").getValue()!=null){
                        createdByUser = dataSnapshot.child("createByUser").getValue().toString();
                    }
                    if(message!=null && createdByUser!=null){
                            Boolean currentUserBoolean=false;
                            if(createdByUser.equals(currentUserId)){
                            currentUserBoolean=true;
                        }
                        ChatObject newMessage = new ChatObject(message,currentUserBoolean);
                        resultsChat.add(newMessage);
                        mChatAdapter.notifyDataSetChanged();
                    }
                }
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            }
        @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        }));
    }

    private ArrayList<ChatObject> resultsChat = new ArrayList <ChatObject>();
    private List<ChatObject> getDataSetChat() {
        return resultsChat;
    }
}