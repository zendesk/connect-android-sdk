package com.zendesk.connect

import org.mockito.Mockito

/**
 * Shorthand to infer the type of a mocked class.
 *
 * val foo = mock<Foo>()
 *
 * @return a mock of [TypeT]
 */
inline fun <reified TypeT> mock(): TypeT = Mockito.mock(TypeT::class.java)
