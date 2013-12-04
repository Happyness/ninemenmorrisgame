package KTH.joel.ninemenmorris;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.content.Intent;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

/**
 * @description Main activity, handling convert currencies
 * @author Joel Denke
 *
 */

public class NineMorrisGame extends Activity
{
    private static final int RESULT_SETTINGS = 1;

    private GameBoard[] gameBoards = new GameBoard[5];
    private GameData[] gameData;
    private TextView textView;
    private LinearLayout surface;
    private int currentBoard = 0;
    private GameLoader loader = new GameLoader(this, "games");
    private boolean started = false;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
	    super.onCreate(savedInstanceState);
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        currentBoard = Integer.parseInt(sharedPrefs.getString("prefGameBoard", "1")) - 1;
        initUI();
        started = true;
    }

    private GameBoard importGameData(int width, int height)
    {
        //viewMessage(String.format("Got %d number of datainstances and current board is %d", gameData.length, currentBoard), true);

        if (gameBoards[currentBoard] == null) {
            GameData data = gameData[currentBoard];
            gameBoards[currentBoard] = new GameBoard(this, data, width, height);
        }

        return gameBoards[currentBoard];
    }

    public void viewMessage(int resId, boolean flash)
    {
        viewMessage(getString(resId), flash);
    }

    /**
     * @description view message on the screen
     * @author Joel Denke
     *
     */
    public void viewMessage(String message, boolean flash)
    {
        textView.setText(message);
        if (flash)
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * @description Initiaties GUI components
     * @author Joel Denke
     *
     */
    private void initUI()
    {
        // init the GUI
        setTitle(R.string.mainTitle);
        setContentView(R.layout.main);
        LinearLayout context = (LinearLayout)findViewById(R.id.context);
        context.setBackgroundColor(Color.BLACK);

        surface = (LinearLayout)findViewById(R.id.surface);
        textView = (TextView) findViewById(R.id.textUserSettings);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(30);
        textView.setTextColor(Color.WHITE);

        initGameBoard();
    }

    private void initGameBoard()
    {
        ViewGroup.LayoutParams params = surface.getLayoutParams();
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        params.width = size.x - 50;
        params.height = size.y - 300;

        gameData = loader.loadGames();

        GameBoard gameBoard = importGameData(size.x-50, size.y - 300);

        surface.removeAllViews();
        surface.addView(gameBoard);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_SETTINGS:
                SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
                currentBoard = Integer.parseInt(sharedPrefs.getString("prefGameBoard", "1")) - 1;
                initGameBoard();
                break;

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //board.setGameStarted(false);

        switch (item.getItemId()) {
            case R.id.menu_settings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivityForResult(i, RESULT_SETTINGS);
                break;
            case R.id.restartGame:
                viewMessage("Will now restart the game", true);

                if (gameBoards[currentBoard] != null) {
                    gameBoards[currentBoard].initGame();
                }
                break;

        }

        return true;
    }

    /**
     * @description Overrides the start method, when app is resuming.
     *              Will update to latest currency if file data is older than update frequency
     *              and if not use offline data last stored.
     *
     * @author Joel Denke
     *
     */
    @Override
    public void onStart()
    {
        Log.i("SaveActivity", "onStart called");
    	super.onStart();

        if (!started) {
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
            currentBoard = Integer.parseInt(sharedPrefs.getString("prefGameBoard", "1")) - 1;
            initGameBoard();
        }
    }

    /**
     * @description Listen for configuration changes and reload config as well as the gui
     * @author Joel Denke
     *
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        //viewMessage(getString(R.string.changedLang) + ": " + newConfig.locale.getLanguage(), true);

        getBaseContext().getResources().updateConfiguration(newConfig, getBaseContext().getResources().getDisplayMetrics());
        initUI();
    }

    private GameData[] getData()
    {
        int i;
        for (i = 0; i < gameData.length; i++) {
            if (gameBoards[i] != null) {
                gameBoards[i].stopAnimations();
                gameData[i] = gameBoards[i].getGameData();
            }
        }

        return gameData;
    }

    @Override
    protected void onPause()
    {
        loader.writeGames(getData());
        super.onPause();
        Log.i("SaveActivity", "onPause called");
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        //loader.writeGames(gameData);
        Log.i("SaveActivity", "onStop called");
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        loader.writeGames(getData());
        Log.i("SaveActivity", "onDestroy called");
    }
}
