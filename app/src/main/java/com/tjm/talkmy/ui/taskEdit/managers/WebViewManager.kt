package com.tjm.talkmy.ui.taskEdit.managers

import android.content.Context
import android.webkit.WebView
import androidx.core.content.ContextCompat
import com.orhanobut.logger.Logger
import com.tjm.talkmy.R
import com.tjm.talkmy.core.extensions.separateSentences
import com.tjm.talkmy.core.extensions.translateInnerTextToPlain
import kotlin.properties.Delegates

class WebViewManager(private val myWebView: WebView, context: Context) {
    val color = ContextCompat.getColor(context, R.color.background)
    val editText = "document.querySelector('.contenidoArchivo')"
    val parrafoEnmarcado = "document.querySelector(\".parrafoEnfocadoRemarcado\")"
    fun loadHTML(src:String) = myWebView.apply {
            setBackgroundColor(color)
            settings.javaScriptEnabled = true
            loadUrl(src)
        }

    fun setFontColor() = myWebView.evaluateJavascript("""(function(){if($color == -14671840) document.body.classList.add("oscureMode");})();""".trimIndent(), null)

    fun modifiedVerify(then: (String) -> Unit = {}) = myWebView.evaluateJavascript(
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


    fun setText(text: String = "") {
        myWebView.evaluateJavascript(
            """
                (function(){$editText.innerHTML = `${text.replace("`","\\`")}`;})();
            """.trimIndent(), null
        )
    }

    fun innerHTML(function: (String) -> Unit) = myWebView.evaluateJavascript(
        """(function() { return $editText.innerHTML; })();""".trimIndent()
    ) {
        function(it)
    }

    fun text(function: (String) -> Unit) = myWebView.evaluateJavascript(
        """(function() { 
          const elementos = Array.from(document.querySelectorAll('.contenidoArchivo > *'));
          return elementos.map(elemento => {
            if (elemento.textContent.trim() !== "") {
              if(elemento.tagName === 'DIV'){
                return elemento.textContent + '\n';
              }
              else{
                return elemento.textContent;                    
              }
            } else {
              return '\n';
            }
          }).join('');
    })();""".trimIndent()
        ) {
        function(it.translateInnerTextToPlain().removeSurrounding("\"","\""))
    }


    fun reloadText(then: ((List<String>, Int) -> Unit)? = null) = myWebView.evaluateJavascript(
        """
            (function() { 
                    let text = $editText.innerHTML;
                    if (!text.match(/^<[^>]+>/)) {
                        $editText.innerHTML = text.replace(/^[^<]+/, "<div>${'$'}&</div>");
                    }
                    text = Array.from(document.querySelectorAll(".contenidoArchivo > *")).filter(p => p.textContent.split('. ').length > 1);
                    text.map(p => {
                        const oraciones = p.innerText.split(/(?<=\.)(?=\s)/g);
                        oraciones.reverse().forEach(oracion => {
                            const nuevoParrafo = document.createElement('p');
                            nuevoParrafo.textContent = oracion;
                            p.parentNode.insertBefore(nuevoParrafo, p.nextSibling);
                        });
                        p.parentNode.removeChild(p);
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


    var fontSize: Float by Delegates.observable(1f) { _, oldValue, newValue ->
        myWebView.evaluateJavascript(
            """
                (function() { $editText.style.fontSize = '${fontSize}px'; })();
            """.trimIndent(), null
        )
    }
    var editable:Boolean by Delegates.observable(true) { _, oldValue, newValue ->
        myWebView.evaluateJavascript(
            """
                (function() { $editText.contentEditable = $newValue;})();
            """.trimIndent(), null
        )
    }
    fun getSentences(function: (List<String>, Int) -> Unit) {
        myWebView.evaluateJavascript(
            """(function() { return $editText.innerText; })();""".trimIndent()
        ) { allText ->
            myWebView.evaluateJavascript(
                """
                    (function() { 
                        const pHighlited = $parrafoEnmarcado;
                        let value = -1;
                        if (pHighlited) {
                            const arrayParrafos = Array.from(document.querySelectorAll(".contenidoArchivo > *")).filter(p => p.innerText.trim() !== "");
                            const indice = arrayParrafos.indexOf(pHighlited);
                            value = indice;
                        }
                        return value !== null ? value : -1;
                    })();
                """.trimIndent()
            ) { indice ->
                val sentences = allText.removeSurrounding("\"","\"").separateSentences()
                function(sentences, indice.toInt())
            }
        }
    }


    fun setSelection(selected: Int) {
        myWebView.evaluateJavascript(
            """
                (function() {
                    $parrafoEnmarcado?.classList.remove("parrafoEnfocadoRemarcado");
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
                    $editText.addEventListener('click', function(item) {
                        $parrafoEnmarcado?.classList.remove("parrafoEnfocadoRemarcado");
                        if(item.target != document.querySelector(".contenidoArchivo") && item.target.innerText.trim()!=""){
                            item.target.classList.add("parrafoEnfocadoRemarcado");
                        }
                    });
                })();
            """.trimIndent(), null
        )
    }
    fun initExtraListeners(){
        myWebView.evaluateJavascript(
            """
                (function() {
                    document.querySelector(".contenidoArchivo").addEventListener("input", function() {
                      modified = true;
                      $parrafoEnmarcado?.classList.remove("parrafoEnfocadoRemarcado");
                    });
                })();
            """.trimIndent(), null
        )
    }
}