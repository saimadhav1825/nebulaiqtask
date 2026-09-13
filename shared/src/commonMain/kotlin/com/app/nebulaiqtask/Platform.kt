package com.app.nebulaiqtask

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform