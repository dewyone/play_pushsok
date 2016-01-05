package com.dewy.engine.platform;

import android.os.Bundle;

/**
 * Created by dewyone on 2015-10-17.
 */
public interface ActivityLifeCycle {

    /* called when app process doesn't exist or app process is killed*/
    void onCreate(Bundle savedInstanceState);

    /* called After onStop() and before onStart() when user navigates to the activity*/
    void onRestart();

    void onStart();

    /* This method is called after onStart()
        when the activity is being re-initialized from a previously saved state
    */
    void onRestoreInstanceState(Bundle savedInstanceState);

    void onResume();

    void onPause();

    /* If called, this method will occur before onStop().
        There are no guarantees about whether it will occur before or after onPause()
    */
    void onSaveInstanceState(Bundle outState);

    void onStop();

    void onDestroy();
}
