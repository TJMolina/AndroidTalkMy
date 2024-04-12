package com.tjm.talkmy.ui.taskEdit.managers

import android.webkit.WebView
import com.orhanobut.logger.Logger
import com.tjm.talkmy.ui.core.extensions.separateSentences
import com.tjm.talkmy.ui.core.extensions.separateSentencesInsertPTag
import com.tjm.talkmy.ui.core.extensions.translateInnerTextToPlain
import kotlin.properties.Delegates

class WebViewManager(private val myWebView: WebView) {
    fun loadHTML() {
        myWebView.loadUrl("file:///android_asset/edittext.html")
        myWebView.settings.javaScriptEnabled = true
    }

    fun setText(text: String = "") {
        var txt = text.separateSentencesInsertPTag()
        myWebView.evaluateJavascript(
            """
                (function() { document.querySelector('.contenidoArchivo').innerHTML = `$txt`; })();
            """.trimIndent(), null
        )
    }

    fun text(function: (String) -> Unit) {
        myWebView.evaluateJavascript(
            """
                (function() { return document.querySelector('.contenidoArchivo').innerText; })();
            """.trimIndent()
        ) {
            val text = it.translateInnerTextToPlain()
            function(text)
        }
    }


    var fontSize: Float by Delegates.observable(1f) { _, oldValue, newValue ->
        myWebView.evaluateJavascript(
            """
                (function() { document.querySelector('.contenidoArchivo').style.fontSize = '${fontSize}px'; })();
            """.trimIndent(), null
        )
    }

    fun getSentences(function: (List<String>, Int) -> Unit) {
        myWebView.evaluateJavascript(
            """ (function() { return document.querySelector('.contenidoArchivo').innerText;})();""".trimIndent()
        ) { allText ->
            myWebView.evaluateJavascript(
                """
                    (function() { 
                        const pHighlited = document.querySelector(".parrafoEnfocadoRemarcado");
                        const pArray = Array.from(document.querySelectorAll("p")); 
                        let value = -1;
                        if (pHighlited) {
                            const arrayParrafos = Array.from(document.querySelectorAll("p"));
                            const parrafoEspecifico = document.querySelector(".parrafoEnfocadoRemarcado");
                            const indice = arrayParrafos.indexOf(parrafoEspecifico);
                            value = indice;
                        }
                        return value;
                    })()
            """.trimIndent()
            ) { indice ->
                val sentences = allText.translateInnerTextToPlain().separateSentences()
                function(sentences, indice.toInt())

            }
        }
    }


    fun setSelection(selected: Int) {
        myWebView.evaluateJavascript(
            """
                (function() {
                    document.querySelector(".parrafoEnfocadoRemarcado")?.classList.remove("parrafoEnfocadoRemarcado");
                    let pTags = Array.from(document.querySelectorAll("p"))
                    pTags[$selected].classList.add("parrafoEnfocadoRemarcado");
                    pTags[$selected].scrollIntoView({ behavior: "smooth", block: "center" });
                 })();
            """.trimIndent(), null
        )
    }

    fun setParagraphClickedListener() {
        myWebView.evaluateJavascript(
            """
            (function() {
                document.querySelector(".contenidoArchivo").addEventListener('click', function(item) {
                    document.querySelector(".parrafoEnfocadoRemarcado")?.classList.remove("parrafoEnfocadoRemarcado");
                    item.target.classList.add("parrafoEnfocadoRemarcado");
                    item.target.scrollIntoView({ behavior: "smooth", block: "center" });   
                });
            })();
        """.trimIndent(), null
        )
    }
}