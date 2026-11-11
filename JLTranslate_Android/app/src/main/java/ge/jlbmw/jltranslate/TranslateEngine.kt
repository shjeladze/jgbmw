package ge.jlbmw.jltranslate
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions

object TranslateEngine {
    private val toEn: Translator by lazy { Translation.getClient(
        TranslatorOptions.Builder().setSourceLanguage(TranslateLanguage.GEORGIAN).setTargetLanguage(TranslateLanguage.ENGLISH).build()) }
    private val toKa: Translator by lazy { Translation.getClient(
        TranslatorOptions.Builder().setSourceLanguage(TranslateLanguage.ENGLISH).setTargetLanguage(TranslateLanguage.GEORGIAN).build()) }
    fun isGeorgian(s: String) = s.any { it in '\u10A0'..'\u10FF' }
    fun download(done: (Boolean)->Unit) {
        val wifi = DownloadConditions.Builder().requireWifi().build()
        toEn.downloadModelIfNeeded(wifi).addOnSuccessListener {
            toKa.downloadModelIfNeeded(wifi).addOnSuccessListener { done(true) }.addOnFailureListener { done(false) }
        }.addOnFailureListener { done(false) }
    }
    fun translate(s: String, intoEnglish: Boolean, done: (Result<String>)->Unit) {
        if (s.isBlank()) { done(Result.failure(IllegalArgumentException("empty"))); return }
        val tr = if (intoEnglish) toEn else toKa
        tr.downloadModelIfNeeded(DownloadConditions.Builder().requireWifi().build()).addOnSuccessListener {
            tr.translate(s).addOnSuccessListener { done(Result.success(it)) }.addOnFailureListener { done(Result.failure(it)) }
        }.addOnFailureListener { done(Result.failure(it)) }
    }
}
