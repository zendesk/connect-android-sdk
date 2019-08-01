package com.zendesk.connect

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.verify
import java.util.concurrent.TimeUnit

class ConnectSchedulerTests {

    private val mockWorkManager = mock<WorkManager>()

    private val connectScheduler = ConnectScheduler(mockWorkManager)

    private val expectedConstrains = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    private val mockWorkName = "some-work-name"

    @Test
    fun `scheduleRecurringConfigRequests should enqueueUniquePeriodicWork`() {
        val captor = ArgumentCaptor.forClass(PeriodicWorkRequest::class.java)

        connectScheduler.scheduleRecurringConfigRequests()

        verify(mockWorkManager).enqueueUniquePeriodicWork(
            eq(ConfigWorker.CONFIG_RECURRING_WORK_TAG),
            eq(ExistingPeriodicWorkPolicy.REPLACE),
            captor.capture()
        )

        assertThat(captor.value.workSpec.intervalDuration).isEqualTo(TimeUnit.MINUTES.toMillis(60))
        assertThat(captor.value.workSpec.constraints).isEqualTo(expectedConstrains)
    }

    @Test
    fun `scheduleQueuedNetworkRequests should enqueueUniqueWork`() {
        val captor = ArgumentCaptor.forClass(OneTimeWorkRequest::class.java)

        connectScheduler.scheduleQueuedNetworkRequests()

        verify(mockWorkManager).enqueueUniqueWork(
            eq(QueuedRequestsWorker.QUEUED_REQUESTS_WORKER_TAG),
            eq(ExistingWorkPolicy.REPLACE),
            captor.capture()
        )

        assertThat(captor.value.workSpec.constraints).isEqualTo(expectedConstrains)
    }

    @Test
    fun `scheduleIpmTimeToLive should enqueueUniqueWork`() {
        val mockTimeToLive = 42L
        val captor = ArgumentCaptor.forClass(OneTimeWorkRequest::class.java)

        connectScheduler.scheduleIpmTimeToLive(mockWorkName, mockTimeToLive)

        verify(mockWorkManager).enqueueUniqueWork(
            eq(mockWorkName),
            eq(ExistingWorkPolicy.REPLACE),
            captor.capture()
        )

        val workDelayInSeconds = captor.value.workSpec.initialDelay / 1000
        assertThat(workDelayInSeconds).isEqualTo(mockTimeToLive)
    }

    @Test
    fun `scheduleIpmTimeToLive should enqueueUniqueWork with 0 delay if received delay is less than 0`() {
        val captor = ArgumentCaptor.forClass(OneTimeWorkRequest::class.java)

        connectScheduler.scheduleIpmTimeToLive(mockWorkName, -1)

        verify(mockWorkManager).enqueueUniqueWork(
            eq(mockWorkName),
            eq(ExistingWorkPolicy.REPLACE),
            captor.capture()
        )

        assertThat(captor.value.workSpec.initialDelay).isEqualTo(0)
    }

    @Test
    fun `cancelIpmTimeToLive should cancelUniqueWork`() {
        connectScheduler.cancelIpmTimeToLive(mockWorkName)

        verify(mockWorkManager).cancelUniqueWork(mockWorkName)
    }
}
