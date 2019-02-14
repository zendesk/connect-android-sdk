package com.zendesk.connect

import com.firebase.jobdispatcher.Constraint
import com.firebase.jobdispatcher.FirebaseJobDispatcher
import com.firebase.jobdispatcher.Job
import com.firebase.jobdispatcher.JobService
import com.firebase.jobdispatcher.JobTrigger
import com.firebase.jobdispatcher.Lifetime
import com.firebase.jobdispatcher.Trigger
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import java.util.concurrent.TimeUnit

/**
 * These tests look very verbose and chunky but they allow us to flag when the rules for a
 * scheduled job changes so we don't accidentally break the rules of a job such as the
 * [Constraint]s or [Trigger]s.
 */
@RunWith(MockitoJUnitRunner.Silent::class)
class ConnectSchedulerTests {

    private lateinit var connectScheduler: ConnectScheduler

    private val windowStart = TimeUnit.SECONDS.convert(55, TimeUnit.MINUTES).toInt()
    private val windowEnd = TimeUnit.SECONDS.convert(65, TimeUnit.MINUTES).toInt()

    @Mock
    private lateinit var mockDispatcher: FirebaseJobDispatcher

    @Mock
    private lateinit var mockJob: Job

    @Mock
    private lateinit var mockJobBuilder: Job.Builder

    @Before
    fun setUp() {
        connectScheduler = ConnectScheduler(mockDispatcher)

        `when`(mockDispatcher.newJobBuilder()).thenReturn(mockJobBuilder)

        `when`(mockJobBuilder.setService(any<Class<JobService>>())).thenReturn(mockJobBuilder)
        `when`(mockJobBuilder.setTag(anyString())).thenReturn(mockJobBuilder)
        `when`(mockJobBuilder.setConstraints(anyInt())).thenReturn(mockJobBuilder)
        `when`(mockJobBuilder.setReplaceCurrent(anyBoolean())).thenReturn(mockJobBuilder)
        `when`(mockJobBuilder.setLifetime(anyInt())).thenReturn(mockJobBuilder)
        `when`(mockJobBuilder.setRecurring(anyBoolean())).thenReturn(mockJobBuilder)
        `when`(mockJobBuilder.setTrigger(any<JobTrigger>())).thenReturn(mockJobBuilder)

        `when`(mockJobBuilder.build()).thenReturn(mockJob)
    }

    @Test
    fun `schedule single config request should schedule a single run config job`() {
        connectScheduler.scheduleSingleConfigRequest()

        verify(mockJobBuilder).setService(ConfigJobService::class.java)
        verify(mockJobBuilder).tag = ConfigJobService.CONFIG_SINGLE_JOB_TAG
        verify(mockJobBuilder).setConstraints(Constraint.ON_ANY_NETWORK)

        verify(mockDispatcher).mustSchedule(mockJob)
    }

    @Test
    fun `schedule recurring config job should schedule a recurring config job`() {
        connectScheduler.scheduleRecurringConfigRequests()

        val captor = ArgumentCaptor.forClass(JobTrigger.ExecutionWindowTrigger::class.java)

        verify(mockJobBuilder).setService(ConfigJobService::class.java)
        verify(mockJobBuilder).tag = ConfigJobService.CONFIG_RECURRING_JOB_TAG
        verify(mockJobBuilder).setConstraints(Constraint.ON_ANY_NETWORK)
        verify(mockJobBuilder).lifetime = Lifetime.FOREVER
        verify(mockJobBuilder).isRecurring = true
        verify(mockJobBuilder).trigger = captor.capture()

        assertThat(captor.value.windowStart).isEqualTo(windowStart)
        assertThat(captor.value.windowEnd).isEqualTo(windowEnd)

        verify(mockDispatcher).mustSchedule(mockJob)
    }

    @Test
    fun `schedule queued network requests should schedule a job`() {
        connectScheduler.scheduleQueuedNetworkRequests()

        verify(mockJobBuilder).setService(QueuedRequestsJobService::class.java)
        verify(mockJobBuilder).tag = QueuedRequestsJobService.QUEUED_REQUESTS_JOB_TAG
        verify(mockJobBuilder).setConstraints(Constraint.ON_ANY_NETWORK)

        verify(mockDispatcher).mustSchedule(mockJob)
    }

    @After
    fun tearDown() {

    }
}