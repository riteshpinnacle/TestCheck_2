package com.ssccgl.pinnacle.testcheck_2

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun HtmlText(html: String) {
    var webView: WebView? by remember { mutableStateOf<WebView?>(null) }
    val currentHtml by rememberUpdatedState(html)

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()
                loadData(currentHtml, "text/html", "UTF-8")
                webView = this
            }
        },
        update = {
            webView?.loadData(currentHtml, "text/html", "UTF-8")
        }
    )
}