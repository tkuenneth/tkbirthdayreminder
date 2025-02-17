package com.thomaskuenneth.android.birthday

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class PeriodicWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        TKBirthdayReminder.updateWidgets(applicationContext)
        return Result.success()
    }
}
