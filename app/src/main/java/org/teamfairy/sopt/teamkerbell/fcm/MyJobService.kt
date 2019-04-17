package org.teamfairy.sopt.teamkerbell.fcm

import android.annotation.SuppressLint
import android.app.job.JobParameters
import android.app.job.JobService
import android.util.Log

@SuppressLint("NewApi")
/**
 * Created by lumiere on 2018-01-13.
 */
class MyJobService : JobService() {

    override fun onStartJob(jobParameters: JobParameters): Boolean {
        Log.d(TAG, "Performing long running task in scheduled job")

        return false
    }

    override fun onStopJob(jobParameters: JobParameters): Boolean {
        return false
    }

    companion object {
        private val TAG = "MyJobService"
    }
}
