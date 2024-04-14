package com.tjm.talkmy.ui.taskEdit.managers

import android.content.Context
import android.webkit.WebView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.tjm.talkmy.R
import com.tjm.talkmy.ui.core.extensions.separateSentences
import com.tjm.talkmy.ui.core.extensions.separateSentencesInsertPTag
import com.tjm.talkmy.ui.core.extensions.translateInnerTextToPlain
import kotlin.properties.Delegates

class WebViewManager(private val myWebView: WebView) {
    fun loadHTML(context: Context) {
        myWebView.setBackgroundColor(ContextCompat.getColor(context, R.color.background))
        myWebView.loadUrl("file:///android_asset/edittext.html")
        myWebView.settings.javaScriptEnabled = true

    }

    fun modifiedVerify(then: (String) -> Unit = {}) {
        myWebView.evaluateJavascript(
            """
                (function() { 
                  if(modified){
                    modified = false;
                    return true;
                  }
                  return false;
                })();
            """.trimIndent()
        ) { then(it) }
    }

    fun setText(text: String = "") {
        var txt = text.translateInnerTextToPlain().separateSentencesInsertPTag()
        myWebView.evaluateJavascript(
            """
                        (function() { 
                          document.querySelector('.contenidoArchivo').innerHTML = `$txt`;
                        })();
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

    fun reloadText(then: ((List<String>, Int) -> Unit)? = null) {
        myWebView.evaluateJavascript(
            """
        (function() { 
            let text = document.querySelector('.contenidoArchivo');
            if (!text.innerHTML.match(/^<[^>]+>/)) {
                document.querySelector('.contenidoArchivo').innerHTML = text.innerHTML.replace(/^[^<]+/, "<div>${'$'}&</div>");
            }
            text = Array.from(document.querySelectorAll(".contenidoArchivo > *")).filter(p => p.textContent.split('. ').length > 1);
            text.map(p => {
                let tag = p.tagName.toLowerCase();
                p.innerHTML = p.innerHTML.split('. ').join("<"+tag+">"+"</"+tag+">");
            });
        })();
        """.trimIndent()
        ) {
            if (then != null) {
                getSentences { sentences, indice ->
                    then(sentences, indice)
                }
            }
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
            """(function() { return document.querySelector('.contenidoArchivo').innerText;})();""".trimIndent()
        ) { allText ->
            myWebView.evaluateJavascript(
                """
            (function() { 
                const pHighlited = document.querySelector(".parrafoEnfocadoRemarcado");
                let value = -1;
                if (pHighlited) {
                    const arrayParrafos = Array.from(document.querySelectorAll(".contenidoArchivo > *")).filter(p => p.innerText.trim() !== "");
                    const indice = arrayParrafos.indexOf(pHighlited);
                    value = indice;
                }
                return value !== null ? value : -1;
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
                let pTags = Array.from(document.querySelectorAll(".contenidoArchivo > *")).filter(p => p.innerText.trim() !== "");
                if(pTags[$selected].innerText.trim()!=""){
                    pTags[$selected].classList.add("parrafoEnfocadoRemarcado");
                    pTags[$selected].scrollIntoView({ behavior: "smooth", block: "center" });
                }
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
                    if(item.target != document.querySelector(".contenidoArchivo") && item.target.innerText.trim()!=""){
                        item.target.classList.add("parrafoEnfocadoRemarcado");
                        item.target.scrollIntoView({ behavior: "smooth", block: "center" });   
                    }
                });
                
                document.querySelector(".contenidoArchivo").addEventListener("input", function() {
                  modified = true;
                });
                
            })();
        """.trimIndent(), null
        )
    }
}