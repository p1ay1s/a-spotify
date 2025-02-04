package com.niki.spotify.remote

const val LOAD_BATCH_SIZE = 20

const val SPOTIFY_REMOTE_TAG = "Spotify Remote Sdk"

const val GET_CHILDREN_TIMEOUT = 20_000L

const val CONNECTION_WATCH_DELAY = 50L

const val CLIENT_ID = "729ad520a3964dc3b020c0db30bfccb7"
const val CLIENT_SECRET = "31a0f20ea9bd42418b973a83b83a2c7f"
const val REDIRECT_URI = "https://open.spotify.com/"

const val MAX_CALL_COUNT = 8

val SPOTIFY_ERROR_MESSAGES = listOf(
    "Result was not delivered on time.",
    "Timeout running com.spotify.get_children_of_item"
)