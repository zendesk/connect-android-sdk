package com.zendesk.connect

import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.common.truth.Truth.assertThat
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import com.zendesk.logger.Logger
import org.junit.After
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.Silent::class)
class ConnectInstanceIdTests {

    companion object {
        private const val NULL_LISTENERS = "Success listener and failure listener must be non null"
    }

    private val testToken = "test_token"
    private val testException = Exception("test_exception")
    private val logAppender = TestLogAppender().apply {
        Logger.setLoggable(true)
        Logger.addLogAppender(this)
    }

    private lateinit var connectInstanceId: ConnectInstanceId
    private lateinit var onSuccessListener: OnSuccessListener<InstanceIdResult>
    private lateinit var onFailureListener: OnFailureListener

    @Mock private lateinit var mockFirebaseInstanceId: FirebaseInstanceId
    @Mock private lateinit var mockTask: Task<InstanceIdResult>
    @Mock private lateinit var mockInstanceIdResult: InstanceIdResult

    @Before
    fun setUp() {
        connectInstanceId = ConnectInstanceId(mockFirebaseInstanceId)

        `when`(mockInstanceIdResult.token).thenReturn(testToken)

        `when`(mockTask.addOnSuccessListener(any<OnSuccessListener<InstanceIdResult>>())).thenReturn(mockTask)
        `when`(mockTask.addOnFailureListener(any<OnFailureListener>())).thenReturn(mockTask)

        onSuccessListener = OnSuccessListener { fail() }
        onFailureListener = OnFailureListener { fail() }
    }

    @After
    fun tearDown() {
        logAppender.reset()
    }

    @Test
    fun `calling get token with null success listener should log a warning`() {
        connectInstanceId.getToken(null, OnFailureListener {  })

        assertThat(logAppender.lastLog()).isEqualTo(NULL_LISTENERS)
    }

    @Test
    fun `calling get token with null failure listener should log a warning`() {
        connectInstanceId.getToken(OnSuccessListener<InstanceIdResult> {  }, null)

        assertThat(logAppender.lastLog()).isEqualTo(NULL_LISTENERS)
    }

    @Test
    fun `get token should call the success listener if the task is successful`() {
        onSuccessListener = OnSuccessListener {
            assertThat(it.token).isEqualTo(testToken)
        }

        `when`(mockFirebaseInstanceId.instanceId).then {
            onSuccessListener.onSuccess(mockInstanceIdResult)
            mockTask
        }

        connectInstanceId.getToken(onSuccessListener, onFailureListener)
    }

    @Test
    fun `get token should call the failure listener if the task is not successful`() {
        onFailureListener = OnFailureListener {
            assertThat(it).isEqualTo(testException)
        }

        `when`(mockFirebaseInstanceId.instanceId).then {
            onFailureListener.onFailure(testException)
            mockTask
        }

        connectInstanceId.getToken(onSuccessListener, onFailureListener)
    }

}