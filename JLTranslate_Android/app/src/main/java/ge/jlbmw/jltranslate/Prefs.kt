package ge.jlbmw.jltranslate
import android.content.Context
object Prefs {
    private fun p(c:Context) = c.getSharedPreferences("jltranslate.settings",Context.MODE_PRIVATE)
    fun incoming(c:Context) = p(c).getBoolean("incoming",false)
    fun outgoing(c:Context) = p(c).getBoolean("outgoing",false)
    fun setIncoming(c:Context,b:Boolean) = p(c).edit().putBoolean("incoming",b).apply()
    fun setOutgoing(c:Context,b:Boolean) = p(c).edit().putBoolean("outgoing",b).apply()
}
