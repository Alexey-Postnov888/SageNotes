package ru.sagenotes.authservice.data.model.exception

sealed class CustomExceptions(message : String) : Exception(message) {
    sealed class LoginException(message: String) : CustomExceptions(message) {
        class LoginUsernameException(message : String) : LoginException(message )
        class LoginPasswordException(message : String) : LoginException(message )
    }

    class RefreshTokenException(message: String) : CustomExceptions(message)
}