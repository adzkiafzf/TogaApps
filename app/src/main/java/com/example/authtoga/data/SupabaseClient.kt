package com.example.authtoga.data

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseClient {
    private const val SUPABASE_URL = "https://mrchzfuknwoynxtrlitm.supabase.co"
    private const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im1yY2h6ZnVrbndveW54dHJsaXRtIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzkwMTAyMTcsImV4cCI6MjA5NDU4NjIxN30.6q6Dk1_2T6nI5wnbo58vVb4hrr4J8aU7CPsh5A4t6z8"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)
    }
}
