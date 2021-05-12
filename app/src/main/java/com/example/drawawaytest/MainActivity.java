package com.example.drawawaytest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.Toast;

import com.example.drawawaytest.Cards.ArrayAdapter;
import com.example.drawawaytest.Cards.Cards;
import com.example.drawawaytest.Matches.MatchesActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {
    private Cards cards_data[];
    private ArrayAdapter arrayAdapter;
    private int i;

    private FirebaseAuth mAuth;

    private String currentUID;

    private DatabaseReference usersDb;

    ListView listView;
    List<Cards> rowItems;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        usersDb = FirebaseDatabase.getInstance().getReference().child("Users");

        mAuth = FirebaseAuth.getInstance();
        currentUID = mAuth.getCurrentUser().getUid();

        checkUsers();


        rowItems = new ArrayList<Cards>();

        arrayAdapter = new ArrayAdapter(this, R.layout.item, rowItems);

        SwipeFlingAdapterView flingContainer = (SwipeFlingAdapterView) findViewById(R.id.frame);

        flingContainer.setAdapter(arrayAdapter);
        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                // this is the simplest way to delete an object from the Adapter (/AdapterView)
                Log.d("LIST", "removed object!");
                rowItems.remove(0);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onLeftCardExit(Object dataObject) {
                Cards obj = (Cards) dataObject;
                String userId = obj.getUserId();
                      usersDb.child(userId).child("connections").child("Nej tack").child(currentUID).setValue(true);
                      Toast.makeText(MainActivity.this, "Left", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRightCardExit(Object dataObject) {
                Cards obj = (Cards) dataObject;
                String userId = obj.getUserId();
                usersDb.child(userId).child("connections").child("Hell Yeah").child(currentUID).setValue(true);
                isConnectionMatch(userId);
                Toast.makeText(MainActivity.this, "Right", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                // Ask for more data here
            }

            @Override
            public void onScroll(float scrollProgressPercent) {

            }
        });


        // Optionally add an OnItemClickListener
        flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
            @Override
            public void onItemClicked(int itemPosition, Object dataObject) {
                Toast.makeText(MainActivity.this, "Click", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void isConnectionMatch(String userId) {
        DatabaseReference currentUserConnectionsDb = usersDb.child(currentUID).child("connections").child("Hell Yeah").child(userId);
        currentUserConnectionsDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Toast.makeText(MainActivity.this, "Ny matchning", Toast.LENGTH_LONG).show();

                    String key = FirebaseDatabase.getInstance().getReference().child("Chat").push().getKey();

                    usersDb.child(dataSnapshot.getKey()).child("connections").child("Matchningar").child(currentUID).child("chatId").setValue(key);
                    usersDb.child(currentUID).child("connections").child("Matchningar").child(dataSnapshot.getKey()).child("chatId").setValue(key);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
/*Få bort usersex och så man inte matchar med sig själv (lägg till && !=getCurrentUser)*/
    private String userSex;
    private String oppositSex;
    public void checkUsers(){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference userDb = usersDb.child(user.getUid());
        userDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange( DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()){
                        if(dataSnapshot.getValue() !=null){
                            getUsers();
                        }
                    }
                }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void getUsers(){
        usersDb.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
               if(dataSnapshot.exists() && !dataSnapshot.child("connections").child("Nej tack").hasChild(currentUID) && !dataSnapshot.child("connections").child("Hell Yeah").hasChild(currentUID)){

                   String profilBildUrl = "default";
                   String category = dataSnapshot.child("kategori").getValue().toString();
                   String name = dataSnapshot.child("namn").getValue().toString();
                   String description = dataSnapshot.child("beskrivning").getValue().toString();

                   if (!dataSnapshot.child("kategori").getValue().equals("HEJ")){
                       category = dataSnapshot.child("kategori").getValue().toString();
                   }
                   if (!dataSnapshot.child("profilBildUrl").getValue().equals("default")){
                       profilBildUrl = dataSnapshot.child("profilBildUrl").getValue().toString();
                   }
                   Cards item = new Cards(dataSnapshot.getKey(), name, profilBildUrl, category, description);

                   rowItems.add(item);
                   arrayAdapter.notifyDataSetChanged();
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
        });
    }

    public void logoutUser(View view){
        mAuth.signOut();
        Intent intent = new Intent(MainActivity.this, ChooseLoginRegistrationActivity.class);
        startActivity(intent);
        finish();
        return;
    }

    public void goToSettings(View view) {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
        return;
    }

    public void goToMatches(View view) {
        Intent intent = new Intent(MainActivity.this, MatchesActivity.class);
        startActivity(intent);
        return;
    }
}