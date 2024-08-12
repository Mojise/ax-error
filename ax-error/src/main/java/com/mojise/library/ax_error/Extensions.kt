package com.mojise.library.ax_error

import android.content.Context
import android.content.Intent
import android.text.Spanned
import androidx.core.app.TaskStackBuilder
import androidx.core.text.HtmlCompat
import java.io.PrintWriter
import java.io.StringWriter

internal fun List<Intent>.toTaskStackBuilder(context: Context): TaskStackBuilder {
    return TaskStackBuilder.create(context).apply {
        this@toTaskStackBuilder.forEach(::addNextIntent)
    }
}

internal fun Int.toStringFromHtml(context: Context): Spanned {
    return HtmlCompat.fromHtml(context.getString(this), HtmlCompat.FROM_HTML_MODE_LEGACY)
}

internal fun String.toStringFromHtml(): Spanned {
    return HtmlCompat.fromHtml(this, HtmlCompat.FROM_HTML_MODE_LEGACY)
}

internal fun Throwable?.toErrorLogText(): String {
    if (this == null) return ""
    val stringWriter = StringWriter()
    this.printStackTrace(PrintWriter(stringWriter))
    return stringWriter.toString()
}