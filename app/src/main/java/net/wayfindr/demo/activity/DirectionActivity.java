package net.wayfindr.demo.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import net.wayfindr.demo.R;
import net.wayfindr.demo.controller.DirectionsController;
import net.wayfindr.demo.controller.NearbyMessagesController;
import net.wayfindr.demo.controller.TextToSpeechController;
import net.wayfindr.demo.model.Message;

import java.util.HashSet;
import java.util.Set;

public class DirectionActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_NEARBY_MESSAGES_RESOLUTION = 0;
    private TextToSpeechController textToSpeechController;
    private NearbyMessagesController nearbyMessagesController;
    private DirectionsController directionsController;
    private TextView messageTextView;
    private View restartButton;
    private View loadingPanel;
    private View directionsPanel;

    public static Intent createIntent(Context context) {
        return new Intent(context, DirectionActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direction);
        setTitle(getString(R.string.direction_title));

        textToSpeechController = new TextToSpeechController(this, new TextToSpeechController.Callback() {
            @Override
            public void onUtteranceStart(String text) {
                messageTextView.setText(text);
            }

            @Override
            public void onUtteranceDone(String text) {
                directionsController.onTextToSpeechDone();
            }
        });
        directionsController = new DirectionsController(textToSpeechController, savedInstanceState, new DirectionsController.Callback() {
            @Override
            public void onWaitingForIdChanged(String id) {
                updateUiVisibility();
            }

            @Override
            public void onJourneyFinished() {
                updateUiVisibility();
            }
        });

        nearbyMessagesController = new NearbyMessagesController(this, REQUEST_CODE_NEARBY_MESSAGES_RESOLUTION, savedInstanceState, new NearbyMessagesController.Callback() {
            @Override
            public void onNearbyMessagesReset() {
                updateCurrentMessages(new HashSet<Message>());
            }

            @Override
            public void onNearbyMessageFound(Message message, Set<Message> currentMessages) {
                updateCurrentMessages(currentMessages);
            }

            @Override
            public void onNearbyMessageLost(Message message, Set<Message> currentMessages) {
                updateCurrentMessages(currentMessages);
            }
        });

        directionsPanel = findViewById(R.id.directionsPanel);
        loadingPanel = findViewById(R.id.loadingPanel);
        messageTextView = (TextView) findViewById(R.id.message);
        restartButton = findViewById(R.id.restart);
        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageTextView.setText("");
                directionsController.restart();
                updateUiVisibility();
            }
        });

        updateUiVisibility();
    }

    private void updateUiVisibility() {
        restartButton.setVisibility(directionsController.isFinished() ? View.VISIBLE : View.GONE);
        boolean loading = directionsController.isLookingForStartMessage();
        loadingPanel.setVisibility(loading ? View.VISIBLE : View.GONE);
        directionsPanel.setVisibility(loading ? View.GONE : View.VISIBLE);
    }

    private void updateCurrentMessages(Set<Message> currentMessages) {
        directionsController.setCurrentMessages(currentMessages);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        textToSpeechController.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        nearbyMessagesController.onStart();
    }

    @Override
    protected void onStop() {
        nearbyMessagesController.onStop();
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        nearbyMessagesController.onSaveInstanceState(state);
        directionsController.onSaveInstanceState(state);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_NEARBY_MESSAGES_RESOLUTION) {
            nearbyMessagesController.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_direction, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.switch_to_debug:
                startActivity(DebugActivity.createIntent(this));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
