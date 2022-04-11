package com.example.locationtracker2.utils

import kotlinx.coroutines.*


class StopWatch(private val timerChangeListener: TimerChangeListener) {

   private var time_passed=0L
   private var last_time=0L
   private var running=false


    fun startTimer(){
        running=true
        val current = System.currentTimeMillis()
        CoroutineScope(Dispatchers.Default).launch {
            while(running){
                    time_passed = System.currentTimeMillis() - current
                    withContext(Dispatchers.Main){
                        timerChangeListener.onTimerChange(time_passed+last_time)
                    }
                    delay(Constant.TIMER_DELAY_INTERVAL)
            }
        }
    }

    fun pauseTimer(){
      running=false
        last_time=time_passed
    }

    fun resetTimer(){
       running=false
        last_time=0L
        time_passed=0L
    }

}