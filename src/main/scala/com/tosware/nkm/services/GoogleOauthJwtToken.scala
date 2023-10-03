package com.tosware.nkm.services

case class GoogleOauthJwtToken(
    iss: String,
    nbf: String,
    aud: String,
    sub: String,
    email: String,
    email_verified: String,
    azp: String,
    name: String,
    picture: String,
    given_name: String,
    family_name: String,
    iat: String,
    exp: String,
    jti: String,
    alg: String,
    kid: String,
    typ: String,
)
