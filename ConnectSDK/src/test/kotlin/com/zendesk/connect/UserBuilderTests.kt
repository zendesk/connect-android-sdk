package com.zendesk.connect

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.*

class UserBuilderTests {

    private val userId = "bird"
    private val aliasId = "${userId}_alias"
    private val userBuilder = UserBuilder(userId)

    @Test
    fun `user builder should attach a user id to the user`() {
        val user = UserBuilder(userId).build()

        assertThat(user.userId).isEqualTo(userId)
    }

    @Test
    fun `user builder should attach any given previous id to the user`() {
        val previousId = "The Aluminium Monster"

        val user = userBuilder.setPreviousId(previousId).build()

        assertThat(user.previousId).isEqualTo(previousId)
    }

    @Test
    fun `user builder should attach any given first name to the user`() {
        val firstName = "Dee"

        val user = userBuilder.setFirstName(firstName).build()

        assertThat(user.firstName).isEqualTo(firstName)
    }

    @Test
    fun `user builder should attach any given last name to the user`() {
        val lastName = "Reynolds"

        val user = userBuilder.setLastName(lastName).build()

        assertThat(user.lastName).isEqualTo(lastName)
    }

    @Test
    fun `user builder should attach any given email to the user`() {
        val email = "sweetdee@example.com"

        val user = userBuilder.setEmail(email).build()

        assertThat(user.email).isEqualTo(email)
    }

    @Test
    fun `user builder should attach any given phone number to the user`() {
        val phoneNumber = "01234567"

        val user = userBuilder.setPhoneNumber(phoneNumber).build()

        assertThat(user.phoneNumber).isEqualTo(phoneNumber)
    }

    @Test
    fun `user builder should attach any given user attributes to the user`() {
        val userAttributes = mapOf(Pair("Funny", false))

        val user = userBuilder.setUserAttributes(userAttributes).build()

        assertThat(user.attributes).isEqualTo(userAttributes)
    }

    @Test
    fun `user builder should attach any given group id to the user`() {
        val groupId = "The Gang"

        val user = userBuilder.setGroupId(groupId).build()

        assertThat(user.groupId).isEqualTo(groupId)
    }

    @Test
    fun `user builder should attach any given group attributes to the user`() {
        val groupAttributes = mapOf(Pair("Organised", false), Pair("Good people", false))

        val user = userBuilder.setGroupAttributes(groupAttributes).build()

        assertThat(user.groupAttributes).isEqualTo(groupAttributes)
    }

    @Test
    fun `user builder should attach any given timezone to the user`() {
        val timezone = "Moon/Moon"

        val user = userBuilder.setTimezone(timezone).build()

        assertThat(user.timezone).isEqualTo(timezone)
    }

    @Test
    fun `user builder should attach a default timezone to the user if none is given`() {
        val user = userBuilder.build()

        val defaultTimezone = TimeZone.getDefault().id

        assertThat(user.timezone).isEqualTo(defaultTimezone)
    }

    @Test
    fun `user builder should attach any given fcm token to the user`() {
        val token = "pg43"

        val user = userBuilder.setFcmToken(token).build()

        assertThat(user.fcm).contains(token)
    }

    @Test
    fun `anonymous user should build a user with a random unique id`() {
        val user1 = UserBuilder.anonymousUser()
        val user2 = UserBuilder.anonymousUser()

        assertThat(user1.userId).isNotEqualTo(user2.userId)
    }

    @Test
    fun `anonymous user should have an associated timezone`() {
        val user = UserBuilder.anonymousUser()
        val defaultTimezone = TimeZone.getDefault().id

        assertThat(user.timezone).isEqualTo(defaultTimezone)
    }

    @Test
    fun `anonymous builder should return a builder used to construct an anonymous user`() {
        val dummyToken = "Meseeks"
        val anonBuilder = UserBuilder.anonymousUserBuilder()
        anonBuilder.setFcmToken(dummyToken)

        val user = anonBuilder.build()
        assertThat(user.userId).isNotNull()
        assertThat(user.fcm).contains(dummyToken)
    }

    @Test
    fun `new builder should populate a builder with values from a user object`() {
        val existingUser = userBuilder
                .setFirstName("Dee")
                .build()

        val newUser = UserBuilder
                .newBuilder(existingUser)
                .build()

        assertThat(newUser.userId).isEqualTo(existingUser.userId)
        assertThat(newUser.firstName).isEqualTo(existingUser.firstName)
    }

    @Test
    fun `new builder should be usable to overwrite existing properties for a user`() {
        val existingUser = userBuilder
                .setFirstName("Dee")
                .build()

        val newUser = UserBuilder
                .newBuilder(existingUser)
                .setFirstName("Big")
                .build()

        assertThat(newUser.firstName).isNotEqualTo(existingUser.firstName)
    }

    @Test
    fun `new builder should be usable to add new properties to a user`() {
        val existingUser = userBuilder
                .build()

        val newUser = UserBuilder
                .newBuilder(existingUser)
                .setLastName("Reynolds")
                .build()

        assertThat(newUser.lastName).isNotNull()
    }

    @Test
    fun `aliased should create a new user with the alias id as the new user id`() {
        val existingUser = userBuilder.build()
        val aliasedUser = UserBuilder.aliased(existingUser, aliasId)

        assertThat(aliasedUser.userId).isEqualTo(aliasId)
    }

    @Test
    fun `aliased should create a new user with the existing user id as the new previous id`() {
        val existingUser = userBuilder.build()
        val aliasedUser = UserBuilder.aliased(existingUser, aliasId)

        assertThat(aliasedUser.previousId).isEqualTo(existingUser.userId)
    }

}