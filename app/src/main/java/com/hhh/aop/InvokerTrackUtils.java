package com.hhh.aop;

import android.util.Log;

import com.hhh.annotation.InvokerTrack;
import com.hhh.annotation.InvokerTrackMark;

@InvokerTrackMark
public class InvokerTrackUtils {

  @InvokerTrack(className = "android.view.LayoutInflater", methodName = "inflate")
  public static void track_LayoutInflater_inflate() {
    Log.d(Constants.TAG, "aop", new RuntimeException("track_LayoutInflater_inflate"));
  }

  @InvokerTrack(className = "java.lang.Thread", methodName = "sleep")
  public static void track_Thread_sleep() {
    Log.d(Constants.TAG, "aop", new RuntimeException("track_Thread_sleep"));
  }
}
