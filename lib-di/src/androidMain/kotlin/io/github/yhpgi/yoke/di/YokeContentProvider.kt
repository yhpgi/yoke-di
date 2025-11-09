package io.github.yhpgi.yoke.di

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri

/**
 * A `ContentProvider` used for automatic initialization of Yoke on Android.
 * By adding this to the `AndroidManifest.xml`, Yoke can capture the application context
 * at startup without requiring manual setup in the `Application` class.
 */
internal class YokeContentProvider : ContentProvider() {
  override fun onCreate(): Boolean {
    // This only captures the context. It does not initialize the DI graph.
    context?.let { AndroidYoke.autoInitialize(it) }
    return true
  }

  override fun query(
    uri: Uri,
    projection: Array<out String>?,
    selection: String?,
    selectionArgs: Array<out String>?,
    sortOrder: String?
  ): Cursor? = null

  override fun getType(uri: Uri): String? = null
  override fun insert(uri: Uri, values: ContentValues?): Uri? = null
  override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
  override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}
