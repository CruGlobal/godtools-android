package org.cru.godtools.account.provider

internal sealed class AuthenticationException : Exception() {
    data object NoActiveProvider : AuthenticationException()
    data object MissingCredentials : AuthenticationException()
    data object UnableToRefreshCredentials : AuthenticationException()
    data object UnknownError : AuthenticationException()

    data object UserAlreadyExists : AuthenticationException()
    data object UserNotFound : AuthenticationException()
}
