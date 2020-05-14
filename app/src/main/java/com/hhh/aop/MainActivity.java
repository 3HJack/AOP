package com.hhh.aop;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    findViewById(R.id.test).setOnClickListener(v -> testAOP());
  }

  /**
   * AOP之后代码如下：
   * Log.e("hhh", "testAOP start");
   * LayoutInflater var10000 = LayoutInflater.from(this);
   * InvokerTrackUtils.track_LayoutInflater_inflate();
   * var10000.inflate(R.layout.activity_main, (ViewGroup)null);
   * Log.e("hhh", "testAOP middle");
   * <p>
   * try {
   * InvokerTrackUtils.track_Thread_sleep();
   * Thread.sleep(100L);
   * } catch (InterruptedException var2) {
   * var2.printStackTrace();
   * }
   * <p>
   * Log.e("hhh", "testAOP end");
   */
  private void testAOP() {
    Log.e(Constants.TAG, "testAOP start");
    LayoutInflater.from(this).inflate(R.layout.activity_main, null);
    Log.e(Constants.TAG, "testAOP middle");
    try {
      Thread.sleep(100L);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    Log.e(Constants.TAG, "testAOP end");
  }
}
